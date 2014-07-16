package org.kie.services.client;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.jbpm.kie.services.impl.KModuleDeploymentUnit;
import org.jbpm.kie.services.impl.model.ProcessAssetDesc;
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
import org.jbpm.services.task.impl.model.UserImpl;
import org.jbpm.services.task.jaxb.ComparePair;
import org.jbpm.services.task.query.TaskSummaryImpl;
import org.jbpm.test.JbpmJUnitBaseTestCase;
import org.junit.After;
import org.junit.Assume;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.KieRepository;
import org.kie.api.builder.Message.Level;
import org.kie.api.command.Command;
import org.kie.api.definition.KieDefinition.KnowledgeType;
import org.kie.api.io.Resource;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.api.task.TaskService;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.deployment.DeploymentUnit.RuntimeStrategy;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.runtime.StatefulKnowledgeSession;
import org.kie.internal.task.api.InternalTaskService;
import org.kie.internal.task.api.TaskQueryService;
import org.kie.remote.common.jaxb.JaxbRequestStatus;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandsRequest;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandsResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbOtherResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbVariablesResponse;
import org.kie.services.client.serialization.jaxb.impl.audit.JaxbHistoryLogList;
import org.kie.services.client.serialization.jaxb.impl.audit.JaxbNodeInstanceLog;
import org.kie.services.client.serialization.jaxb.impl.audit.JaxbProcessInstanceLog;
import org.kie.services.client.serialization.jaxb.impl.audit.JaxbVariableInstanceLog;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentJobResult;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentUnit;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentUnit.JaxbDeploymentStatus;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentUnitList;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessDefinition;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessInstanceListResponse;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessInstanceResponse;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessInstanceWithVariablesResponse;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbWorkItem;
import org.kie.services.client.serialization.jaxb.impl.task.JaxbTaskSummaryListResponse;
import org.kie.services.client.serialization.jaxb.rest.JaxbExceptionResponse;
import org.kie.services.client.serialization.jaxb.rest.JaxbGenericResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractServicesSerializationTest extends JbpmJUnitBaseTestCase {

    protected static final Logger logger = LoggerFactory.getLogger(AbstractServicesSerializationTest.class);

    protected enum TestType {
        JAXB, JSON, YAML;
    }

    abstract public TestType getType();
    abstract public void addClassesToSerializationProvider(Class<?>... extraClass);
    public abstract <T> T testRoundTrip(T in) throws Exception;
    
    private Object getField(String fieldName, Class<?> clazz, Object obj) throws Exception {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(obj);
    }
  
    // TESTS
    
    private static KieSession createKnowledgeSession(String processFile) throws Exception {
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
    
    @After
    public void after() throws Exception { 
        super.tearDown();
        this.setupDataSource = false;
        this.sessionPersistence = false;
    }
    
    // TESTS
    
    /*
     * Tests
     */

    @Test
    public void commandRequestTest() throws Exception {
        // Don't run with JSON: /execute is only JAXB
        Assume.assumeFalse(getType().equals(TestType.JSON));

        String userId = "krisv";
        long taskId = 1;
        Command<?> cmd = new StartTaskCommand(taskId, "krisv");
        JaxbCommandsRequest req = new JaxbCommandsRequest("test", cmd);
        Command<?> newCmd = testRoundTrip(req).getCommands().get(0);
        assertNotNull(newCmd);
        assertEquals("taskId is not equal", taskId, getField("taskId", TaskCommand.class, newCmd));
        assertEquals("userId is not equal", userId, getField("userId", TaskCommand.class, newCmd));

        req = new JaxbCommandsRequest();
        req.setUser("krampus");
        List<Command> cmds = new ArrayList<Command>();
        req.setCommands(cmds);
        req.setDeploymentId("depId");
        req.setProcessInstanceId(43l);
        req.setVersion("6.0.1.0");
        StartProcessCommand spCmd = new StartProcessCommand("test.proc.yaml");
        cmds.add(spCmd);
        spCmd.getParameters().put("one", "a");
        spCmd.getParameters().put("two", "B");
        Object weirdParam = new Integer[] { 59, 2195 };
        spCmd.getParameters().put("thr", weirdParam);
        
        addClassesToSerializationProvider(weirdParam.getClass());

        JaxbCommandsRequest newReq = testRoundTrip(req);
        assertEquals(((StartProcessCommand) newReq.getCommands().get(0)).getParameters().get("two"), "B");

        req = new JaxbCommandsRequest("deployment", new StartProcessCommand("org.jbpm.humantask"));
        newReq = testRoundTrip(req);

        CorrelationKeyInfo corrKey = new CorrelationKeyInfo();
        corrKey.addProperty(new CorrelationPropertyInfo("null", "val"));
    
        GetProcessInstanceByCorrelationKeyCommand gpibckCmd = new GetProcessInstanceByCorrelationKeyCommand(corrKey);
        req = new JaxbCommandsRequest("test", gpibckCmd);
        testRoundTrip(req);
    }
    
    @Test
    public void commandsResponseTest() throws Exception {
        this.setupDataSource = true;
        this.sessionPersistence = true;
        super.setUp();
        
        RuntimeEngine runtimeEngine = createRuntimeManager("BPMN2-StringStructureRef.bpmn2").getRuntimeEngine(null);
        KieSession ksession = runtimeEngine.getKieSession();
    
        Map<String, Object> params = new HashMap<String, Object>();
        String val = "initial-val";
        params.put("test", val);
        Command cmd = new StartProcessCommand("StructureRef");
        ((StartProcessCommand) cmd).setParameters(params);
        ProcessInstance processInstance = ksession.execute((StartProcessCommand) cmd);
        assertTrue(processInstance.getState() == ProcessInstance.STATE_ACTIVE);
    
        JaxbCommandsResponse resp = new JaxbCommandsResponse();
        resp.setDeploymentId("deploy");
        resp.setProcessInstanceId(processInstance.getId());
        resp.addResult(processInstance, 0, cmd);
    
        testRoundTrip(resp);
    
        cmd = new GetTaskAssignedAsBusinessAdminCommand();
        List<TaskSummary> result = new ArrayList<TaskSummary>();

        resp = new JaxbCommandsResponse();
        resp.addResult(result, 0, cmd);

        cmd = new GetTasksByProcessInstanceIdCommand();
        List<Long> resultTwo = new ArrayList<Long>();
        resp.addResult(resultTwo, 1, cmd);

        Object newResp = testRoundTrip(resp);
        assertNotNull(newResp);
        assertEquals( 2, ((JaxbCommandsResponse) newResp).getResponses().size());
    }

    @Test
    public void taskSummaryListTest() throws Exception {
        Assume.assumeFalse(getType().equals(TestType.YAML));
        this.setupDataSource = true;
        this.sessionPersistence = true;
        super.setUp();
        
        RuntimeManager runtimeManager = createRuntimeManager(Strategy.SINGLETON, "test", "BPMN2-HumanTaskWithTaskContent.bpmn2");
        RuntimeEngine runtimeEngine = runtimeManager.getRuntimeEngine(null);
        KieSession ksession = runtimeEngine.getKieSession();
        TaskService taskService = runtimeEngine.getTaskService();
        
        ProcessInstance procInst = ksession.startProcess("org.kie.remote.test.usertask.UserTask");
        long procInstId = procInst.getId();

        List<Long> statuses = new ArrayList<Long>();
        statuses.add(procInstId);
        
        Map<String, List<?>> fieldVals = new HashMap<String, List<?>>();
        fieldVals.put(TaskQueryService.PROCESS_INST_ID_LIST, statuses);
        List<TaskSummary> taskSumList = ((InternalTaskService)taskService).getTasksByVariousFields(fieldVals, true);
        assertEquals( "Task summary list size", 1, taskSumList.size());
        TaskSummaryImpl taskSumImpl = (TaskSummaryImpl) taskSumList.get(0);
        taskSumImpl.setActualOwner(new UserImpl("Minnie"));
        taskSumImpl.setCreatedBy(new UserImpl("Mickey"));
        
        JaxbTaskSummaryListResponse jaxbTaskSumListResp = new JaxbTaskSummaryListResponse(taskSumList);
        JaxbTaskSummaryListResponse jaxbTaskSumListRespCopy = testRoundTrip(jaxbTaskSumListResp);
        assertEquals( jaxbTaskSumListResp.getList().size(), jaxbTaskSumListRespCopy.getList().size() );
        TaskSummary taskSum = jaxbTaskSumListResp.getList().get(0);
        TaskSummary taskSumCopy = jaxbTaskSumListRespCopy.getList().get(0);
        ComparePair.compareObjectsViaFields(taskSum, taskSumCopy, 
                "potentialOwners", // null
                "createdOn", "activationTime", "expirationTime",
                "subTaskStrategy"); // dates
    }
    
    @Test
    public void genericResponseTest() throws Exception {
        JaxbGenericResponse resp = new JaxbGenericResponse();
        resp.setMessage("error");
        resp.setStatus(JaxbRequestStatus.SUCCESS);
        resp.setUrl("http://here");

        testRoundTrip(resp);
    }

    @Test
    public void exceptionTest() throws Exception {
        Assume.assumeFalse(getType().equals(TestType.YAML));
        
        JaxbExceptionResponse resp = new JaxbExceptionResponse();
        resp.setMessage("error");
        resp.setStatus(JaxbRequestStatus.SUCCESS);
        resp.setUrl("http://here");
       
        RuntimeException re = new RuntimeException();
        resp.setCause(re);

        testRoundTrip(resp);
    }
    
    @Test
    public void variablesResponseTest() throws Exception {
        JaxbVariablesResponse resp = new JaxbVariablesResponse();

        testRoundTrip(resp);

        Map<String, String> vars = new HashMap<String, String>();
        vars.put("one", "two");
        resp.setVariables(vars);

        testRoundTrip(resp);
    }

    @Test
    public void historyLogListTest() throws Exception {
        JaxbHistoryLogList resp = new JaxbHistoryLogList();

        testRoundTrip(resp);

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

        testRoundTrip(resp);
    }

    @Test
    public void auditCommandsTest() throws Exception {
        FindProcessInstanceCommand cmd = new FindProcessInstanceCommand(23);

        testRoundTrip(cmd);
    }

    @Test
    public void factHandleTest() throws Exception {
        // Don't run with YAML?
        Assume.assumeFalse(getType().equals(TestType.YAML));

        KieSession ksession = createKnowledgeSession(null);

        InsertObjectCommand cmd = new InsertObjectCommand("The Sky is Green");
        FactHandle factHandle = ksession.execute(cmd);
        JaxbOtherResponse jor = new JaxbOtherResponse(factHandle, 0, cmd);
        testRoundTrip(jor);
    }

    @Test
    public void processInstanceWithVariablesTest() throws Exception {
        this.setupDataSource = true;
        this.sessionPersistence = true;
        super.setUp();
        
        RuntimeEngine runtimeEngine = createRuntimeManager("BPMN2-StringStructureRef.bpmn2").getRuntimeEngine(null);
        KieSession ksession = runtimeEngine.getKieSession();

        Map<String, Object> params = new HashMap<String, Object>();
        String val = "initial-val";
        params.put("test", val);
        ProcessInstance processInstance = ksession.startProcess("StructureRef");
        assertTrue(processInstance.getState() == ProcessInstance.STATE_ACTIVE);

        Map<String, Object> res = new HashMap<String, Object>();
        res.put("testHT", "test value");
//        ksession.getWorkItemManager().completeWorkItem(workItemHandler.getWorkItem().getId(), res);

        Map<String, String> map = new HashMap<String, String>();
        map.put("test", "initial-val");

        JaxbProcessInstanceWithVariablesResponse jpiwvr = new JaxbProcessInstanceWithVariablesResponse(processInstance, map);
        testRoundTrip(jpiwvr);

        JaxbProcessInstanceListResponse jpilp = new JaxbProcessInstanceListResponse();
        List<ProcessInstance> procInstList = new ArrayList<ProcessInstance>();
        procInstList.add(new JaxbProcessInstanceResponse(processInstance));
        jpilp.setResult(procInstList);
        testRoundTrip(jpilp);
    }

    @Test
    public void workItemObjectTest() throws Exception {
        // Don't run with YAML?
        Assume.assumeFalse(getType().equals(TestType.YAML));

        JaxbWorkItem workitemObject = new JaxbWorkItem();
        workitemObject.setId(35l);
        workitemObject.setName("Clau");
        workitemObject.setState(0);
        workitemObject.setProcessInstanceId(1l);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("test", "driving");
        workitemObject.setParameters(params);
        JaxbWorkItem roundTripWorkItem = testRoundTrip(workitemObject);
        ComparePair.compareObjectsViaFields(workitemObject, roundTripWorkItem);
    }

    @Test
    public void serializingPrimitiveArraysTest() throws Exception  {
        // Don't run with JSON: /execute is only JAXB
        Assume.assumeFalse(getType().equals(TestType.JSON));

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
        Command<?> newCmd = testRoundTrip(req).getCommands().get(0);
        assertNotNull(newCmd);
    }

    @Test
    // JBPM-4170
    public void nodeInstanceLogNpeTest() throws Exception { 
        NodeInstanceLog nodeLog = new NodeInstanceLog();
        JaxbNodeInstanceLog jaxbNodeLog = new JaxbNodeInstanceLog(nodeLog);
        testRoundTrip(jaxbNodeLog);
    }
    
    @Test
    public void deploymentObjectsTest() throws Exception {
        Assume.assumeFalse(getType().equals(TestType.YAML));
        
        // for test at end, fill during test
        JaxbDeploymentUnitList depUnitList = new JaxbDeploymentUnitList();
       
        // dep jobs
        JaxbDeploymentJobResult jaxbJob = new JaxbDeploymentJobResult();
        testRoundTrip(jaxbJob);

        // complex dep jobs
        KModuleDeploymentUnit kDepUnit = new KModuleDeploymentUnit("org", "jar", "1.0", "kbase", "ksession" );
        kDepUnit.setStrategy(RuntimeStrategy.PER_PROCESS_INSTANCE);
        
        JaxbDeploymentUnit depUnit = new JaxbDeploymentUnit(kDepUnit.getGroupId(), kDepUnit.getArtifactId(), kDepUnit.getArtifactId());
        depUnit.setKbaseName(kDepUnit.getKbaseName());
        depUnit.setKsessionName(kDepUnit.getKsessionName());
        depUnit.setStrategy(kDepUnit.getStrategy());
        depUnit.setStatus(JaxbDeploymentStatus.NONEXISTENT);
        depUnitList.getDeploymentUnitList().add(depUnit);

        jaxbJob = new JaxbDeploymentJobResult(null, "test", depUnit, "deploy");
        jaxbJob.setIdentifier(23L);
        jaxbJob.setSuccess(false);
        JaxbDeploymentJobResult copyJaxbJob = testRoundTrip(jaxbJob);
        ComparePair.compareObjectsViaFields(jaxbJob, copyJaxbJob, "jobId", "identifier");
        
        depUnit = new JaxbDeploymentUnit("g", "a", "v");
        depUnit.setKbaseName("kbase");
        depUnit.setKsessionName("ksession");
        depUnit.setStatus(JaxbDeploymentStatus.DEPLOY_FAILED);
        depUnit.setStrategy(RuntimeStrategy.PER_PROCESS_INSTANCE);
        depUnitList.getDeploymentUnitList().add(depUnit);
        
        JaxbDeploymentUnit copyDepUnit = testRoundTrip(depUnit);
        
        ComparePair.compareObjectsViaFields(depUnit, copyDepUnit, "identifier");

        JaxbDeploymentJobResult depJob = new JaxbDeploymentJobResult(null, "testing stuff", copyDepUnit, "test");
        depJob.setSuccess(true); 
        JaxbDeploymentJobResult copyDepJob = testRoundTrip(depJob);
        
        ComparePair.compareObjectsViaFields(copyDepJob, depJob, "jobId", "identifier");
        
        JaxbDeploymentUnitList roundTripUnitList = testRoundTrip(depUnitList);
        ComparePair.compareObjectsViaFields(depUnitList.getDeploymentUnitList().get(0), roundTripUnitList.getDeploymentUnitList().get(0), "jobId", "identifier");
    }
    
    @Test
    public void processInstanceLogTest() throws Exception {
        Assume.assumeFalse(getType().equals(TestType.YAML));
        
        ProcessInstanceLog origLog = new ProcessInstanceLog(54, "org.hospital.patient.triage");
        origLog.setDuration(65l);
        origLog.setDuration(234l);
        origLog.setEnd(new Date((new Date()).getTime() + 1000));
        origLog.setExternalId("testDomainId");
        origLog.setIdentity("identityNotMemory");
        
        // nullable
        origLog.setStatus(2);
        origLog.setOutcome("descriptiveErrorCodeOfAnError");
        origLog.setParentProcessInstanceId(65l);
        
        origLog.setProcessName("org.process.not.technical");
        origLog.setProcessVersion("v3.14");
        
        JaxbProcessInstanceLog xmlLog = new JaxbProcessInstanceLog(origLog);
        xmlLog.setCommandName("test-cmd");
        xmlLog.setIndex(2);
        JaxbProcessInstanceLog newXmlLog = testRoundTrip(xmlLog);
        ComparePair.compareOrig(xmlLog, newXmlLog, JaxbProcessInstanceLog.class);
        
        ProcessInstanceLog newLog = newXmlLog.getResult();
        ComparePair.compareOrig(origLog, newLog, ProcessInstanceLog.class);
    }
    
    @Test
    public void processInstanceLogNillable() throws Exception {
        Assume.assumeFalse(getType().equals(TestType.YAML));
        
        ProcessInstanceLog origLog = new ProcessInstanceLog(54, "org.hospital.patient.triage");
        origLog.setDuration(65l);
        origLog.setEnd(new Date((new Date()).getTime() + 1000));
        origLog.setExternalId("testDomainId");
        origLog.setIdentity("identityNotMemory");
        
        // nullable/nillable
        // origLog.setStatus(2);
        // origLog.setOutcome("descriptiveErrorCodeOfAnError");
        // origLog.setParentProcessInstanceId(65l);
        
        origLog.setProcessName("org.process.not.technical");
        origLog.setProcessVersion("v3.14");
        
        JaxbProcessInstanceLog xmlLog = new JaxbProcessInstanceLog(origLog);
        JaxbProcessInstanceLog newXmlLog = testRoundTrip(xmlLog);
        
        assertEquals( xmlLog.getProcessInstanceId(), newXmlLog.getProcessInstanceId() );
        assertEquals( xmlLog.getProcessId(), newXmlLog.getProcessId() );
        
        assertEquals( xmlLog.getDuration(), newXmlLog.getDuration() );
        assertEquals( xmlLog.getEnd(), newXmlLog.getEnd() );
        assertEquals( xmlLog.getExternalId(), newXmlLog.getExternalId() );
        assertEquals( xmlLog.getIdentity(), newXmlLog.getIdentity() );
        
        assertEquals( xmlLog.getStatus(), newXmlLog.getStatus() );
        assertEquals( xmlLog.getOutcome(), newXmlLog.getOutcome() );
        assertEquals( xmlLog.getParentProcessInstanceId(), newXmlLog.getParentProcessInstanceId() );
        
        assertEquals( xmlLog.getProcessName(), newXmlLog.getProcessName() );
        assertEquals( xmlLog.getProcessVersion(), newXmlLog.getProcessVersion() );
    }
    
    @Test
    public void nodeInstanceLogTest() throws Exception {
        Assume.assumeFalse(getType().equals(TestType.YAML));
        
        int type = 0;
        long processInstanceId = 23;
        String processId = "org.hospital.doctor.review";
        String nodeInstanceId = "1-1";
        String nodeId = "1";
        String nodeName = "notification";
        
        NodeInstanceLog origLog = new NodeInstanceLog(type, processInstanceId, processId, nodeInstanceId, nodeId, nodeName);
        
        origLog.setWorkItemId(78l);
        origLog.setConnection("link");
        origLog.setExternalId("not-internal-num");
        origLog.setNodeType("the-sort-of-point");
        
        JaxbNodeInstanceLog xmlLog = new JaxbNodeInstanceLog(origLog);
        xmlLog.setCommandName("test-cmd");
        xmlLog.setIndex(2);
        JaxbNodeInstanceLog newXmlLog = testRoundTrip(xmlLog);
        ComparePair.compareOrig(xmlLog, newXmlLog, JaxbNodeInstanceLog.class);
        
        NodeInstanceLog newLog = newXmlLog.getResult();
        ComparePair.compareOrig(origLog, newLog, NodeInstanceLog.class);
    }
    
    @Test
    public void variableInstanceLogTest() throws Exception {
        Assume.assumeFalse(getType().equals(TestType.YAML));
        
        long processInstanceId = 23;
        String processId = "org.hospital.intern.rounds";
        String variableInstanceId = "patientNum-1";
        String variableId = "patientNum";
        String value = "33";
        String oldValue = "32";
        
        VariableInstanceLog origLog 
            = new VariableInstanceLog(processInstanceId, processId, variableInstanceId, variableId, value, oldValue);
        
        origLog.setExternalId("outside-identity-representation");
        origLog.setOldValue("previous-data-that-this-variable-contains");
        origLog.setValue("the-new-data-that-has-been-put-in-this-variable");
        origLog.setVariableId("shortend-representation-of-this-representation");
        origLog.setVariableInstanceId("id-instance-variable");
       
        JaxbVariableInstanceLog xmlLog = new JaxbVariableInstanceLog(origLog);
        xmlLog.setCommandName("test-cmd");
        xmlLog.setIndex(2);
        JaxbVariableInstanceLog newXmlLog = testRoundTrip(xmlLog);
        ComparePair.compareOrig(xmlLog, newXmlLog, JaxbVariableInstanceLog.class);
        
        VariableInstanceLog newLog = newXmlLog.getResult();
        ComparePair.compareOrig(origLog, newLog, VariableInstanceLog.class);
    }
    
    @Test
    public void processIdAndProcessDefinitionTest() throws Exception {
        // JaxbProcessDefinition
        ProcessAssetDesc assetDesc = new ProcessAssetDesc(
                "org.test.proc.id", "The Name Of The Process", 
                "1.999.23.Final", "org.test.proc", 
                "RuleFlow", KnowledgeType.PROCESS.toString(),
                "org.test.proc",
                "org.test.proc:procs:1.999.Final");
        
        JaxbProcessDefinition jaxbProcDef = new JaxbProcessDefinition();
        jaxbProcDef.setDeploymentId(assetDesc.getDeploymentId());
        jaxbProcDef.setId(assetDesc.getId());
        jaxbProcDef.setName(assetDesc.getName());
        jaxbProcDef.setPackageName(assetDesc.getPackageName());
        jaxbProcDef.setVersion(assetDesc.getVersion());
        Map<String, String> forms = new HashMap<String, String>();
        forms.put( "locationForm", "GPS: street: post code: city: state: land: planet: universe: ");
        jaxbProcDef.setForms(forms);
       
        JaxbProcessDefinition copyJaxbProcDef = testRoundTrip(jaxbProcDef);
        ComparePair.compareObjectsViaFields(jaxbProcDef, copyJaxbProcDef);
    }
   
}
