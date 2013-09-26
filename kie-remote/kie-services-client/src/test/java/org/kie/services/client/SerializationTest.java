package org.kie.services.client;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.drools.core.SessionConfiguration;
import org.drools.core.command.runtime.process.StartProcessCommand;
import org.drools.core.command.runtime.rule.InsertObjectCommand;
import org.drools.core.impl.EnvironmentFactory;
import org.jbpm.bpmn2.objects.TestWorkItemHandler;
import org.jbpm.process.audit.NodeInstanceLog;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.jbpm.process.audit.VariableInstanceLog;
import org.jbpm.process.audit.command.FindProcessInstanceCommand;
import org.jbpm.process.audit.command.FindVariableInstancesCommand;
import org.jbpm.process.audit.xml.JaxbNodeInstanceLog;
import org.jbpm.process.audit.xml.JaxbProcessInstanceLog;
import org.jbpm.process.audit.xml.JaxbVariableInstanceLog;
import org.jbpm.process.instance.event.DefaultSignalManagerFactory;
import org.jbpm.process.instance.impl.DefaultProcessInstanceManagerFactory;
import org.jbpm.services.task.commands.GetTaskAssignedAsBusinessAdminCommand;
import org.jbpm.services.task.commands.GetTasksByProcessInstanceIdCommand;
import org.jbpm.services.task.commands.StartTaskCommand;
import org.jbpm.services.task.commands.TaskCommand;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieRepository;
import org.kie.api.builder.Message.Level;
import org.kie.api.command.Command;
import org.kie.api.io.Resource;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.runtime.StatefulKnowledgeSession;
import org.kie.services.client.serialization.jaxb.JaxbCommandsRequest;
import org.kie.services.client.serialization.jaxb.JaxbCommandsResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbHistoryLogList;
import org.kie.services.client.serialization.jaxb.impl.JaxbOtherResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbProcessInstanceWithVariablesResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbVariablesResponse;
import org.kie.services.client.serialization.jaxb.rest.JaxbGenericResponse;
import org.kie.services.client.serialization.jaxb.rest.JaxbRequestStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SerializationTest {

    protected static final Logger log = LoggerFactory.getLogger(SerializationTest.class);

    private Object getField(String fieldName, Class<?> clazz, Object obj) throws Exception {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(obj);
    }

    protected KieSession createKnowledgeSession(String processFile) throws Exception {
        KieServices ks = KieServices.Factory.get();
        KieRepository kr = ks.getRepository();
        KieFileSystem kfs = ks.newKieFileSystem();
        if( processFile != null ) { 
            Resource process = ResourceFactory.newClassPathResource(processFile);
            kfs.write(process);
        }
    
        KieBuilder kb = ks.newKieBuilder(kfs);
        kb.buildAll();
    
        if (kb.getResults().hasMessages(Level.ERROR)) {
            throw new RuntimeException("Build Errors:\n" + kb.getResults().toString());
        }
    
        KieContainer kContainer = ks.newKieContainer(kr.getDefaultReleaseId());
        KieBase kbase = kContainer.getKieBase();
    
        Environment env = EnvironmentFactory.newEnvironment();
    
        Properties defaultProps = new Properties();
        defaultProps.setProperty("drools.processSignalManagerFactory", DefaultSignalManagerFactory.class.getName());
        defaultProps.setProperty("drools.processInstanceManagerFactory", DefaultProcessInstanceManagerFactory.class.getName());
        SessionConfiguration conf = new SessionConfiguration(defaultProps);
    
        KieSession ksession = (StatefulKnowledgeSession) kbase.newKieSession(conf, env);
        return ksession;
    }

    public abstract Object testRoundtrip(Object in) throws Exception;

    /*
     * Tests
     */
    
    @Test
    public void testCommandSerialization() throws Exception {
        String userId = "krisv";
        long taskId = 1;
        Command<?> cmd = new StartTaskCommand(taskId, "krisv");
        JaxbCommandsRequest req = new JaxbCommandsRequest("test", cmd);
        Command<?> newCmd = ((JaxbCommandsRequest) testRoundtrip(req)).getCommands().get(0);
        assertNotNull(newCmd);
        assertEquals("taskId is not equal", taskId, getField("taskId", TaskCommand.class, newCmd));
        assertEquals("userId is not equal", userId, getField("userId", TaskCommand.class, newCmd));
    }

    @Test
    public void testTaskSummaryList() throws Exception {
        Command<?> cmd = new GetTaskAssignedAsBusinessAdminCommand();
        List<TaskSummary> result = new ArrayList<TaskSummary>();

        JaxbCommandsResponse resp = new JaxbCommandsResponse();
        resp.addResult(result, 0, cmd);

        cmd = new GetTasksByProcessInstanceIdCommand();
        List<Long> resultTwo = new ArrayList<Long>();
        resp.addResult(resultTwo, 1, cmd);

        Object newResp = testRoundtrip(resp);
    }

    @Test
    public void genericTest() throws Exception {
        JaxbGenericResponse resp = new JaxbGenericResponse();
        resp.setError("error");
        resp.setStackTrace("stack");
        resp.setStatus(JaxbRequestStatus.SUCCESS);
        resp.setUrl("http://here");

        testRoundtrip(resp);
    }

    @Test
    public void commandsRequestTest() throws Exception {
        JaxbCommandsRequest req = new JaxbCommandsRequest();
        List<Command<?>> cmds = new ArrayList<Command<?>>();
        req.setCommands(cmds);
        req.setDeploymentId("depId");
        req.setProcessInstanceId(43l);
        req.setVersion(2);
        cmds.add(new StartProcessCommand("test.proc.yaml"));

        JaxbCommandsRequest newReq = (JaxbCommandsRequest) testRoundtrip(req);
    }

    @Test
    public void variablesResponseTest() throws Exception {
        JaxbVariablesResponse resp = new JaxbVariablesResponse();

        testRoundtrip(resp);

        Map<String, String> vars = new HashMap<String, String>();
        vars.put("one", "two");
        resp.setVariables(vars);

        testRoundtrip(resp);
    }

    @Test
    public void historyLogListTest() throws Exception {
        JaxbHistoryLogList resp = new JaxbHistoryLogList();

        testRoundtrip(resp);

        // vLog
        VariableInstanceLog vLog = new VariableInstanceLog(23, "process", "varInst", "var", "two", "one");
        vLog.setExternalId("domain");
        Field dateField = VariableInstanceLog.class.getDeclaredField("date");
        dateField.setAccessible(true);
        dateField.set(vLog, new Date());
        Field idField = VariableInstanceLog.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(vLog, 32l);
        resp.getHistoryLogList().add(new JaxbVariableInstanceLog(vLog));
       
        // pLog
        ProcessInstanceLog pLog = new ProcessInstanceLog(23, "process");
        pLog.setDuration(2000l);
        pLog.setEnd(new Date());
        pLog.setExternalId("domain");
        pLog.setIdentity("id");
        pLog.setOutcome("error");
        pLog.setParentProcessInstanceId(42);
        pLog.setProcessName("name");
        pLog.setProcessVersion("1-SNAP");
        pLog.setStatus(2);
        idField = ProcessInstanceLog.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(pLog, 32l);
        resp.getHistoryLogList().add(new JaxbProcessInstanceLog(pLog));

        // nLog
        NodeInstanceLog nLog = new NodeInstanceLog(0, 23, "process", "nodeInst", "node", "wally");
        idField = NodeInstanceLog.class.getDeclaredField("id");
        idField.setAccessible(true);
        idField.set(nLog, 32l);
        dateField = NodeInstanceLog.class.getDeclaredField("date");
        dateField.setAccessible(true);
        dateField.set(nLog, new Date());
        nLog.setNodeType("type");
        nLog.setWorkItemId(88l);
        nLog.setConnection("connex");
        nLog.setExternalId("domain");
        resp.getHistoryLogList().add(new JaxbNodeInstanceLog(nLog));
        
        testRoundtrip(resp);
    }
    
    @Test
    public void auditCommandsTest() throws Exception {
       FindProcessInstanceCommand cmd = new FindProcessInstanceCommand(23);
       
       testRoundtrip(cmd);
    }
}
