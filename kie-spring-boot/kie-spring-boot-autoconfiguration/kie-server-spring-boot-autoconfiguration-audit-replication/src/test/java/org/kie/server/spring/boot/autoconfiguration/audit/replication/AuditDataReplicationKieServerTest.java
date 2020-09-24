package org.kie.server.spring.boot.autoconfiguration.audit.replication;

import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;

import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.apache.activemq.artemis.api.core.RoutingType;
import org.apache.activemq.artemis.core.config.Configuration;
import org.apache.activemq.artemis.core.config.CoreQueueConfiguration;
import org.apache.activemq.artemis.core.config.impl.ConfigurationImpl;
import org.apache.activemq.artemis.core.server.embedded.EmbeddedActiveMQ;
import org.assertj.core.api.Assertions;
import org.jbpm.process.audit.NodeInstanceLog;
import org.jbpm.process.audit.VariableInstanceLog;
import org.jbpm.services.api.ProcessService;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.UserTaskService;
import org.jbpm.services.task.audit.impl.model.AuditTaskImpl;
import org.jbpm.services.task.audit.impl.model.BAMTaskSummaryImpl;
import org.jbpm.services.task.audit.impl.model.TaskEventImpl;
import org.jbpm.services.task.audit.impl.model.TaskVariableImpl;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.runtime.manager.audit.ProcessInstanceLog;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieServerConfigItem;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.services.api.KieServer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK, classes = ApplicationSender.class)
@TestPropertySource(locations = "classpath:application-integrationtest.properties")
public class AuditDataReplicationKieServerTest {

    private static final Long TIMEOUT = 10000L;
    private static final String USER_GENERIC = "salaboy";
    private static final String USER_NOMINATED = "krisv";
    private static final String USER_ADMIN = "Administrator";

    @Autowired
    private EntityManagerFactory originalEntityManagerFactory;

    @Autowired
    @Qualifier("auditEntityManagerFactory")
    private EntityManagerFactory auditEntityManagerFactory;

    @Autowired
    private DataSource datasourceOriginal;

    @Autowired
    @Qualifier("datasource-replica")

    private DataSource datasourceReplica;

    @Autowired
    private KieServer kieServer;

    @Autowired
    private ProcessService processService;

    @Autowired
    private RuntimeDataService runtimeDataService;

    @Autowired
    private UserTaskService userTaskService;

    protected static final EmbeddedActiveMQ embedded = new EmbeddedActiveMQ();

    @Autowired
    @Qualifier("auditDataReplicationConsumer")
    private  AuditDataReplicationJMSQueueConsumer consumer;

    @BeforeClass
    public static void startUp() throws Exception {
        KieJarBuildHelper.createKieJar("src/test/resources/kjar/");
        Configuration config = new ConfigurationImpl();
        config.setSecurityEnabled(false);
        config.addAcceptorConfiguration("amqp-acceptor", "tcp://localhost:8888?protocols=AMQP");

        CoreQueueConfiguration auditQueue = new CoreQueueConfiguration();
        auditQueue.setAddress("audit-queue");
        auditQueue.setRoutingType(RoutingType.ANYCAST);
        auditQueue.setName("audit-queue");
        config.addQueueConfiguration(auditQueue); 

        embedded.setConfiguration(config);
        embedded.start();
        
    }

    @AfterClass
    public static void shutDown() throws Exception {
        embedded.stop();
    }

    @Before
    public void reset() throws Exception {
        clearDatasource(datasourceOriginal);
        clearDatasource(datasourceReplica);
        consumer.reset();

        KieContainerResource resource = new KieContainerResource();
        resource.setReleaseId(new ReleaseId("org.kie", "spring-boot-kjar-test", "1.0.0-SNAPSHOT"));
        resource.addConfigItem(new KieServerConfigItem());
        kieServer.createContainer("test", resource);

    }

    @Test
    public void testSimpleProcess() throws Exception {
        processService.startProcess("test", "kjar.simple-process");
        waitForEventProcessing(15);
        compareData();
    }

    @Test
    public void testSimpleHumanTaskProcess() throws Exception {
        Long processInstanceId = processService.startProcess("test", "kjar.simple-ht-process", singletonMap("my var",
                                                                                                            "my var value"));
        List<Long> tasks = runtimeDataService.getTasksByProcessInstanceId(processInstanceId);

        tasks.forEach(e -> userTaskService.release(e, USER_GENERIC));
        tasks.forEach(e -> userTaskService.claim(e, USER_GENERIC));
        tasks.forEach(e -> userTaskService.start(e, USER_GENERIC));
        tasks.forEach(e -> userTaskService.saveContentFromUser(e, USER_GENERIC, singletonMap("my key 1",
                                                                                             "my value 1")));
        tasks.forEach(e -> userTaskService.suspend(e, USER_GENERIC));
        tasks.forEach(e -> userTaskService.resume(e, USER_GENERIC));
        tasks.forEach(e -> userTaskService.complete(e, USER_GENERIC, emptyMap()));

        waitForEventProcessing(50);
        compareData();
    }

    @Test
    public void testSimpleHumanTaskSkipProcess() throws Exception {
        Long processInstanceId = processService.startProcess("test", "kjar.simple-ht-process", singletonMap("my var",
                                                                                                            "my var value"));
        List<Long> tasks = runtimeDataService.getTasksByProcessInstanceId(processInstanceId);

        tasks.forEach(e -> userTaskService.release(e, USER_GENERIC));
        tasks.forEach(e -> userTaskService.forward(e, USER_GENERIC, USER_NOMINATED));
        tasks.forEach(e -> userTaskService.skip(e, USER_NOMINATED));
        
        waitForEventProcessing(35);
        compareData();
    }

    @Test
    public void testSimpleHumanTaskExitProcess() throws Exception {
        Long processInstanceId = processService.startProcess("test", "kjar.simple-ht-process", singletonMap("my var",
                                                                                                            "my var value"));
        List<Long> tasks = runtimeDataService.getTasksByProcessInstanceId(processInstanceId);
        tasks.forEach(e -> userTaskService.exit(e, USER_ADMIN));

        waitForEventProcessing(19);
        compareData();
    }

    @Test
    public void testSimpleHumanTaskFailProcess() throws Exception {
        Long processInstanceId = processService.startProcess("test", "kjar.simple-ht-process", singletonMap("my var",
                                                                                                            "my var value"));
        List<Long> tasks = runtimeDataService.getTasksByProcessInstanceId(processInstanceId);
        tasks.forEach(e -> userTaskService.start(e, USER_GENERIC));
        tasks.forEach(e -> userTaskService.fail(e, USER_ADMIN, emptyMap()));

        waitForEventProcessing(32);
        compareData();
    }

    private boolean compareData() {
        EntityManager original = originalEntityManagerFactory.createEntityManager();
        EntityManager audit = auditEntityManagerFactory.createEntityManager();

        List<ProcessInstanceLog> pil = audit.createQuery("SELECT o FROM ProcessInstanceLog o ORDER BY o.id ASC",
                                                         ProcessInstanceLog.class).getResultList();
        List<ProcessInstanceLog> pil_a = original.createQuery("SELECT o FROM ProcessInstanceLog o ORDER BY o.id ASC",
                                                              ProcessInstanceLog.class).getResultList();
        Assertions.assertThat(pil).containsExactlyElementsOf(pil_a);

        List<NodeInstanceLog> nil = audit.createQuery("SELECT o FROM NodeInstanceLog o ORDER BY o.id ASC",
                                                      NodeInstanceLog.class).getResultList();
        List<NodeInstanceLog> nil_a = original.createQuery("SELECT o FROM NodeInstanceLog o ORDER BY o.id ASC",
                                                           NodeInstanceLog.class).getResultList();
        Assertions.assertThat(nil).containsExactlyElementsOf(nil_a);

        List<VariableInstanceLog> vil = audit.createQuery("SELECT o FROM VariableInstanceLog o ORDER BY o.id ASC",
                                                          VariableInstanceLog.class).getResultList();
        List<VariableInstanceLog> vil_a = original.createQuery("SELECT o FROM VariableInstanceLog o ORDER BY o.id ASC",
                                                               VariableInstanceLog.class).getResultList();
        Assertions.assertThat(vil).containsExactlyElementsOf(vil_a);

        List<BAMTaskSummaryImpl> btl = audit.createQuery("SELECT o FROM BAMTaskSummaryImpl o ORDER BY o.pk ASC",
                                                         BAMTaskSummaryImpl.class).getResultList();
        List<BAMTaskSummaryImpl> btl_a = original.createQuery("SELECT o FROM BAMTaskSummaryImpl o ORDER BY o.pk ASC",
                                                              BAMTaskSummaryImpl.class).getResultList();
        Assertions.assertThat(btl).containsExactlyElementsOf(btl_a);

        // here we have a problem as TaskVariableImpl are removed in the original db after process is gone
        // so audit table well have more entries than the original
        List<TaskVariableImpl> tvl = audit.createQuery("SELECT o FROM TaskVariableImpl o ORDER BY o.id ASC",
                                                       TaskVariableImpl.class).getResultList();
        List<TaskVariableImpl> tvl_a = original.createQuery("SELECT o FROM TaskVariableImpl o ORDER BY o.id ASC",
                                                            TaskVariableImpl.class).getResultList();
        Assertions.assertThat(tvl).containsAll(tvl_a);

        List<AuditTaskImpl> atl = audit.createQuery("SELECT o FROM AuditTaskImpl o ORDER BY o.id ASC",
                                                    AuditTaskImpl.class).getResultList();
        List<AuditTaskImpl> atl_a = original.createQuery("SELECT o FROM AuditTaskImpl o ORDER BY o.id ASC",
                                                         AuditTaskImpl.class).getResultList();
        Assertions.assertThat(atl).containsExactlyElementsOf(atl_a);

        List<TaskEventImpl> tel = audit.createQuery("SELECT o FROM TaskEventImpl o ORDER BY o.id ASC",
                                                    TaskEventImpl.class).getResultList();
        List<TaskEventImpl> tel_a = original.createQuery("SELECT o FROM TaskEventImpl o ORDER BY o.id ASC",
                                                         TaskEventImpl.class).getResultList();
        Assertions.assertThat(tel).containsExactlyElementsOf(tel_a);

        original.close();
        audit.close();
        return true;
    }

    private void waitForEventProcessing(long total) throws Exception {
        
        long count = -1;
        long start = System.currentTimeMillis();
        while(count != total && start + TIMEOUT > System.currentTimeMillis()) {
            if(total == consumer.get()) {
                break;
            }
            Thread.sleep(100L);
        }

    }

    private void clearDatasource(DataSource datasource) {
        try (Connection c = datasource.getConnection(); Statement s = c.createStatement();) {

            s.execute("TRUNCATE TABLE ProcessInstanceLog");
            s.execute("TRUNCATE TABLE NodeInstanceLog");
            s.execute("TRUNCATE TABLE VariableInstanceLog");
            s.execute("TRUNCATE TABLE BAMTaskSummary");
            s.execute("TRUNCATE TABLE TaskVariableImpl");
            s.execute("TRUNCATE TABLE AuditTaskImpl");
            s.execute("TRUNCATE TABLE TaskEvent");

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
