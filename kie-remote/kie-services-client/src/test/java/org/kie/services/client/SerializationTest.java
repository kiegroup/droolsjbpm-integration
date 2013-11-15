package org.kie.services.client;

import static org.junit.Assert.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.drools.core.SessionConfiguration;
import org.drools.core.command.runtime.process.GetProcessInstanceByCorrelationKeyCommand;
import org.drools.core.command.runtime.process.StartProcessCommand;
import org.drools.core.command.runtime.rule.InsertObjectCommand;
import org.drools.core.impl.EnvironmentFactory;
import org.jbpm.bpmn2.objects.TestWorkItemHandler;
import org.jbpm.persistence.correlation.CorrelationKeyInfo;
import org.jbpm.persistence.correlation.CorrelationPropertyInfo;
import org.jbpm.process.audit.NodeInstanceLog;
import org.jbpm.process.audit.ProcessInstanceLog;
import org.jbpm.process.audit.VariableInstanceLog;
import org.jbpm.process.audit.command.FindProcessInstanceCommand;
import org.jbpm.process.instance.event.DefaultSignalManagerFactory;
import org.jbpm.process.instance.impl.DefaultProcessInstanceManagerFactory;
import org.jbpm.services.task.commands.GetTaskAssignedAsBusinessAdminCommand;
import org.jbpm.services.task.commands.GetTasksByProcessInstanceIdCommand;
import org.jbpm.services.task.commands.StartTaskCommand;
import org.jbpm.services.task.commands.TaskCommand;
import org.junit.Assume;
import org.junit.Ignore;
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
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandsRequest;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandsResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbOtherResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbVariablesResponse;
import org.kie.services.client.serialization.jaxb.impl.audit.JaxbHistoryLogList;
import org.kie.services.client.serialization.jaxb.impl.audit.JaxbNodeInstanceLog;
import org.kie.services.client.serialization.jaxb.impl.audit.JaxbProcessInstanceLog;
import org.kie.services.client.serialization.jaxb.impl.audit.JaxbVariableInstanceLog;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessInstanceListResponse;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessInstanceResponse;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessInstanceWithVariablesResponse;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbWorkItem;
import org.kie.services.client.serialization.jaxb.rest.JaxbGenericResponse;
import org.kie.services.client.serialization.jaxb.rest.JaxbRequestStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class SerializationTest {

    protected static final Logger logger = LoggerFactory.getLogger(SerializationTest.class);

    protected enum TestType {
        JAXB, JSON, YAML;
    }

    abstract public TestType getType();
    abstract public void addClassesToSerializationProvider(Class<?>... extraClass);

    private Object getField(String fieldName, Class<?> clazz, Object obj) throws Exception {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(obj);
    }

    public static KieSession createKnowledgeSession(String processFile) throws Exception {
        KieServices ks = KieServices.Factory.get();
        KieRepository kr = ks.getRepository();
        KieFileSystem kfs = ks.newKieFileSystem();
        if (processFile != null) {
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
    public void commandRequestTest() throws Exception {
        // Don't run with JSON: /execute is only JAXB
        Assume.assumeTrue(!getType().equals(TestType.JSON));

        String userId = "krisv";
        long taskId = 1;
        Command<?> cmd = new StartTaskCommand(taskId, "krisv");
        JaxbCommandsRequest req = new JaxbCommandsRequest("test", cmd);
        Command<?> newCmd = ((JaxbCommandsRequest) testRoundtrip(req)).getCommands().get(0);
        assertNotNull(newCmd);
        assertEquals("taskId is not equal", taskId, getField("taskId", TaskCommand.class, newCmd));
        assertEquals("userId is not equal", userId, getField("userId", TaskCommand.class, newCmd));

        req = new JaxbCommandsRequest();
        List<Command<?>> cmds = new ArrayList<Command<?>>();
        req.setCommands(cmds);
        req.setDeploymentId("depId");
        req.setProcessInstanceId(43l);
        req.setVersion(2);
        StartProcessCommand spCmd = new StartProcessCommand("test.proc.yaml");
        cmds.add(spCmd);
        spCmd.getParameters().put("one", "a");
        spCmd.getParameters().put("two", "B");

        JaxbCommandsRequest newReq = (JaxbCommandsRequest) testRoundtrip(req);
        assertEquals(((StartProcessCommand) newReq.getCommands().get(0)).getParameters().get("two"), "B");

        req = new JaxbCommandsRequest("deployment", new StartProcessCommand("org.jbpm.humantask"));
        newReq = (JaxbCommandsRequest) testRoundtrip(req);
    }

    @Test
    public void taskSummaryListTest() throws Exception {
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

    @Test
    public void commandsResponseTest() throws Exception {
        KieSession ksession = createKnowledgeSession("BPMN2-StringStructureRef.bpmn2");
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", workItemHandler);

        Map<String, Object> params = new HashMap<String, Object>();
        String val = "initial-val";
        params.put("test", val);
        StartProcessCommand cmd = new StartProcessCommand("StructureRef");
        cmd.setParameters(params);
        ProcessInstance processInstance = ksession.execute(cmd);
        assertTrue(processInstance.getState() == ProcessInstance.STATE_ACTIVE);

        JaxbCommandsResponse resp = new JaxbCommandsResponse();
        resp.setDeploymentId("deploy");
        resp.setProcessInstanceId(processInstance.getId());
        resp.addResult(processInstance, 0, cmd);

        testRoundtrip(resp);
    }

    @Test
    public void xmlAdapterTest() throws Exception {
        // Don't run with JSON: /execute is only JAXB
        Assume.assumeTrue(!getType().equals(TestType.JSON));

        CorrelationKeyInfo corrKey = new CorrelationKeyInfo();
        corrKey.addProperty(new CorrelationPropertyInfo("null", "val"));

        GetProcessInstanceByCorrelationKeyCommand cmd = new GetProcessInstanceByCorrelationKeyCommand(corrKey);
        JaxbCommandsRequest req = new JaxbCommandsRequest("test", cmd);
        testRoundtrip(req);
    }

    @Test
    public void factHandleTest() throws Exception {
        // Don't run with YAML?
        Assume.assumeTrue(!getType().equals(TestType.YAML));

        KieSession ksession = createKnowledgeSession(null);

        InsertObjectCommand cmd = new InsertObjectCommand("The Sky is Green");
        FactHandle factHandle = ksession.execute(cmd);
        JaxbOtherResponse jor = new JaxbOtherResponse(factHandle, 0, cmd);
        testRoundtrip(jor);
    }

    @Test
    public void processInstanceWithVariablesTest() throws Exception {
        KieSession ksession = createKnowledgeSession("BPMN2-StringStructureRef.bpmn2");
        TestWorkItemHandler workItemHandler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler("Human Task", workItemHandler);

        Map<String, Object> params = new HashMap<String, Object>();
        String val = "initial-val";
        params.put("test", val);
        ProcessInstance processInstance = ksession.startProcess("StructureRef");
        assertTrue(processInstance.getState() == ProcessInstance.STATE_ACTIVE);

        Map<String, Object> res = new HashMap<String, Object>();
        res.put("testHT", "test value");
        ksession.getWorkItemManager().completeWorkItem(workItemHandler.getWorkItem().getId(), res);

        Map<String, String> map = new HashMap<String, String>();
        map.put("test", "initial-val");

        JaxbProcessInstanceWithVariablesResponse jpiwvr = new JaxbProcessInstanceWithVariablesResponse(processInstance, map);
        testRoundtrip(jpiwvr);

        JaxbProcessInstanceListResponse jpilp = new JaxbProcessInstanceListResponse();
        List<ProcessInstance> procInstList = new ArrayList<ProcessInstance>();
        procInstList.add(new JaxbProcessInstanceResponse(processInstance));
        jpilp.setResult(procInstList);
        testRoundtrip(jpilp);
    }

    @Test
    public void workItemObjectTest() throws Exception {
        // Don't run with YAML?
        Assume.assumeTrue(!getType().equals(TestType.YAML));

        JaxbWorkItem workitemObject = new JaxbWorkItem();
        workitemObject.setId(35l);
        workitemObject.setName("Clau");
        workitemObject.setState(0);
        workitemObject.setProcessInstanceId(1l);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("test", "driving");
        workitemObject.setParameters(params);
        testRoundtrip(workitemObject);
    }

    @Test
    public void serializingPrimitiveArraysTest() throws Exception  {
        // Don't run with JSON: /execute is only JAXB
        Assume.assumeTrue(!getType().equals(TestType.JSON));

        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("url", "http://soaptest.parasoft.com/calculator.wsdl");
        parameters.put("namespace", "http://www.parasoft.com/wsdl/calculator/");
        parameters.put("interface", "Calculator");
        parameters.put("operation", "add");
        Object arrayParam = new Float[]{9.0f, 12.0f};
        parameters.put("parameters", arrayParam);
        
        addClassesToSerializationProvider(arrayParam.getClass());
        
        Command<?> cmd = new StartProcessCommand("proc.with.array.params", parameters);
        JaxbCommandsRequest req = new JaxbCommandsRequest("test", cmd);
        Command<?> newCmd = ((JaxbCommandsRequest) testRoundtrip(req)).getCommands().get(0);
        assertNotNull(newCmd);
    }

}
