package org.kie.server.integrationtests.jbpm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runners.Parameterized;
import org.kie.api.KieServices;
import org.kie.internal.runtime.conf.RuntimeStrategy;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieServerConfigItem;
import org.kie.server.api.model.Message;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.Severity;
import org.kie.server.api.model.admin.TimerInstance;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.integrationtests.shared.KieServerAssert;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.kie.server.integrationtests.shared.KieServerSynchronization;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ReproducerIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    private static final KieServerConfigItem PPI_RUNTIME_STRATEGY = new KieServerConfigItem(KieServerConstants.PCFG_RUNTIME_STRATEGY, RuntimeStrategy.PER_PROCESS_INSTANCE.name(), String.class.getName());

    private static final ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "definition-project",
            "1.0.0.Final");
    private static final ReleaseId releaseId101 = new ReleaseId("org.kie.server.testing", "definition-project",
            "1.0.1.Final");
    private static final ReleaseId releaseIdBroken = new ReleaseId("org.kie.server.testing", "definition-project",
            "1.0.2.Final");
    private static ReleaseId releaseIdTimer = new ReleaseId("org.kie.server.testing", "timer-project",
            "1.0.0.Final");

    @Parameterized.Parameters(name = "{index}: {0} {1}")
    public static Collection<Object[]> data() {
        KieServicesConfiguration restConfiguration = createKieServicesRestConfiguration();

        Collection<Object[]> parameterData = new ArrayList<Object[]>(Arrays.asList(new Object[][]
                        {
                                {MarshallingFormat.JAXB, restConfiguration}
                        }
        ));
        return parameterData;
    }

    @BeforeClass
    public static void buildAndDeployArtifacts() {

        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/definition-project");
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/definition-project-101");
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/broken-project");
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/timer-project");

        kieContainer = KieServices.Factory.get().newKieContainer(releaseId);
    }

    @Override
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses) throws Exception {
        extraClasses.put(PERSON_CLASS_NAME, Class.forName(PERSON_CLASS_NAME, true, kieContainer.getClassLoader()));
    }

    @Test
    public void reproducer() throws Exception {
        // Create a container, start timer process, update it, wait until it finish and dispose container
        startAndUpdateTimer();
        disposeAllContainers();
        
        // Create a container, update it to broken container (error in WorkItemHandler definition), keep the container active
        containerUpdateContainerToBroken();

        testTimerStartEvent();
    }

    public void startAndUpdateTimer() throws Exception {
        createContainer(CONTAINER_ID, releaseId, PPI_RUNTIME_STRATEGY);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("timer", "1h");
        Long processInstanceId = processClient.startProcess(CONTAINER_ID, PROCESS_ID_TIMER, parameters);
        assertNotNull(processInstanceId);
        assertTrue(processInstanceId.longValue() > 0);

        try {
            List<TimerInstance> timers = processAdminClient.getTimerInstances(CONTAINER_ID, processInstanceId);
            assertNotNull(timers);
            assertEquals(1, timers.size());

            TimerInstance timerInstance = timers.get(0);
            assertNotNull(timerInstance);
            assertEquals("timer", timerInstance.getTimerName());

            processAdminClient.updateTimer(CONTAINER_ID, processInstanceId, timerInstance.getTimerId(), 3, 0, 0);

            KieServerSynchronization.waitForProcessInstanceToFinish(processClient, CONTAINER_ID, processInstanceId);

            ProcessInstance pi = processClient.getProcessInstance(CONTAINER_ID, processInstanceId);
            assertNotNull(pi);
            assertEquals(org.kie.api.runtime.process.ProcessInstance.STATE_COMPLETED, pi.getState().intValue());

        } catch (Exception e){
            processClient.abortProcessInstance(CONTAINER_ID, processInstanceId);
            fail(e.getMessage());
        }
    }

    public void containerUpdateContainerToBroken() throws Exception {
        createContainer("container2", releaseId101);

        ServiceResponse<KieContainerResource> response = client.getContainerInfo("container2");
        KieServerAssert.assertSuccess(response);

        KieContainerResource resource = response.getResult();
        assertThat(resource.getMessages()).as("Shound have one message").hasSize(1);
        Message message = resource.getMessages().get(0);
        assertThat(message.getSeverity()).as("Message should be of type info").isEqualTo(Severity.INFO);

        ServiceResponse<ReleaseId> updateReleaseId = client.updateReleaseId("container2", releaseIdBroken);
        KieServerAssert.assertFailure(updateReleaseId);

        response = client.getContainerInfo("container2");
        KieServerAssert.assertSuccess(response);

        resource = response.getResult();
        assertThat(resource.getMessages()).as("Shound have two messages").hasSize(2);
        message = resource.getMessages().get(0);
        assertThat(message.getSeverity()).as("Message should be of type error").isEqualTo(Severity.ERROR);
        message = resource.getMessages().get(1);
        assertThat(message.getSeverity()).as("Message should be of type warn").isEqualTo(Severity.WARN);
        assertThat(message.getMessages()).hasSize(1);
        assertThat(message.getMessages().iterator().next()).contains("release id returned back");
    }

    public void testTimerStartEvent() throws Exception {
        String containerId = "timer-project-aaa";
        createContainer(containerId, releaseIdTimer);

        List<Integer> completedOnly = Arrays.asList(2);
        
        // Test will fail here, process isn't started by EJB timer
        KieServerSynchronization.waitForProcessInstanceStart(queryClient, containerId, 3, completedOnly);
    }
}
