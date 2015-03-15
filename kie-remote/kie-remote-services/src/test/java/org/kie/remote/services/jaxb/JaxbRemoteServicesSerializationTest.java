package org.kie.remote.services.jaxb;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;

import org.drools.core.command.runtime.process.GetProcessInstanceByCorrelationKeyCommand;
import org.drools.core.command.runtime.process.StartProcessCommand;
import org.drools.core.command.runtime.rule.HaltCommand;
import org.drools.core.command.runtime.rule.InsertObjectCommand;
import org.drools.core.common.DisconnectedFactHandle;
import org.hibernate.tool.hbm2ddl.DatabaseMetadata;
import org.jbpm.persistence.correlation.CorrelationKeyInfo;
import org.jbpm.persistence.correlation.CorrelationPropertyInfo;
import org.jbpm.services.task.commands.GetTaskAssignedAsBusinessAdminCommand;
import org.jbpm.services.task.commands.GetTasksByProcessInstanceIdCommand;
import org.jbpm.services.task.commands.StartTaskCommand;
import org.jbpm.services.task.commands.TaskCommand;
import org.jbpm.test.JbpmJUnitBaseTestCase;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.command.Command;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.api.task.model.TaskSummary;
import org.kie.remote.services.AcceptedServerCommands;
import org.kie.services.client.serialization.JaxbSerializationProvider;
import org.kie.services.client.serialization.SerializationProvider;
import org.kie.services.client.serialization.jaxb.impl.AbstractJaxbCommandResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbOtherResponse;
import org.reflections.Reflections;
import org.reflections.scanners.FieldAnnotationsScanner;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.scanners.TypeAnnotationsScanner;
import org.reflections.util.ClasspathHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;

@SuppressWarnings("unchecked")
public class JaxbRemoteServicesSerializationTest  extends JbpmJUnitBaseTestCase {

    protected static final Logger logger = LoggerFactory.getLogger(JaxbRemoteServicesSerializationTest.class);
   
    private static Reflections reflections = new Reflections(
            ClasspathHelper.forPackage("org.kie.remote.services.jaxb"),
            ClasspathHelper.forPackage("org.kie.services.client.serialization.jaxb.rest"),
            new TypeAnnotationsScanner(), new FieldAnnotationsScanner(), new MethodAnnotationsScanner(), new SubTypesScanner());
   

    protected SerializationProvider jaxbProvider = ServerJaxbSerializationProvider.newInstance();
    { 
        ((JaxbSerializationProvider) jaxbProvider).setPrettyPrint(true);
    }

    public void addClassesToSerializationProvider(Class<?>... extraClass) {
        ((JaxbSerializationProvider) jaxbProvider).addJaxbClassesAndReinitialize(extraClass);
    }

    public <T> T testRoundTrip(T in) throws Exception {
        String xmlObject = jaxbProvider.serialize(in);
        logger.debug(xmlObject);
        return (T) jaxbProvider.deserialize(xmlObject);
    }

    /**
     * If you think this test is a mistake: beware, this test is smarter than you.
     */
    @Test
    public void acceptedCommandsCanBeSerializedTest() throws Exception {
        Field commandsField = JaxbCommandsRequest.class.getDeclaredField("commands");
        XmlElements xmlElemsAnno = (XmlElements) commandsField.getAnnotations()[0];
        XmlElement[] xmlElems = xmlElemsAnno.value();

        Set<Class> cmdSet = getAcceptedCommandClassSet();
        assertEquals(AcceptedServerCommands.class.getSimpleName() + " contains a different set of Commands than "
                + JaxbCommandsRequest.class.getSimpleName(), cmdSet.size(), xmlElems.length);
        Set<String> xmlElemNameSet = new HashSet<String>();
        for (XmlElement xmlElemAnno : xmlElems) {
            Class cmdClass = xmlElemAnno.type();
            String name = xmlElemAnno.name();
            assertTrue(name + " is used twice as a name.", xmlElemNameSet.add(name));
            assertTrue(cmdClass.getSimpleName() + " is present in " + AcceptedServerCommands.class.getSimpleName() + " but not in "
                    + JaxbCommandsRequest.class.getSimpleName(), cmdSet.remove(cmdClass));
        }
        for (Class cmdClass : cmdSet) {
            logger.error("Missing: " + cmdClass.getSimpleName());
        }
        assertEquals("See output for classes in " + AcceptedServerCommands.class.getSimpleName() + " that are not in "
                + JaxbCommandsRequest.class.getSimpleName(), 0, cmdSet.size());
    }
 
    @Test
    public void allCommandResponseTypesNeedXmlElemIdTest() throws Exception {
        Field commandsField = JaxbCommandsResponse.class.getDeclaredField("responses");
        XmlElements xmlElemsAnno = (XmlElements) commandsField.getAnnotations()[0];
        XmlElement[] xmlElems = xmlElemsAnno.value();

        Set<Class<?>> cmdSet = new HashSet<Class<?>>();
        { 
            Set<Class<? extends JaxbCommandResponse>> subTypes = reflections.getSubTypesOf(JaxbCommandResponse.class);
            cmdSet.addAll(subTypes);
        }
        {
            Set<Class<? extends AbstractJaxbCommandResponse>> subTypes = reflections.getSubTypesOf(AbstractJaxbCommandResponse.class);
            cmdSet.addAll(subTypes);
        }
        cmdSet.remove(AbstractJaxbCommandResponse.class);

        int numAnnos = xmlElems.length;
        int numClass = cmdSet.size();

        Set<String> xmlElemNameSet = new HashSet<String>();
        for (XmlElement xmlElemAnno : xmlElems) {
            Class cmdClass = xmlElemAnno.type();
            String name = xmlElemAnno.name();
            assertTrue(name + " is used twice as a name.", xmlElemNameSet.add(name));
            assertTrue(cmdClass.getSimpleName() + " is present in " + JaxbCommandsResponse.class.getSimpleName() + " but does not "
                    + "implement " + JaxbCommandResponse.class.getSimpleName(), cmdSet.remove(cmdClass));
        }
        for (Class cmdClass : cmdSet) {
            logger.error("Missing: " + cmdClass.getSimpleName());
        }
        assertTrue("See above output for difference between " + JaxbCommandResponse.class.getSimpleName() + " implementations "
                + "and classes listed in " + JaxbCommandsResponse.class.getSimpleName(), cmdSet.size() == 0);

        assertEquals((numClass > numAnnos ? "Not all classes" : "Non " + JaxbCommandResponse.class.getSimpleName() + " classes")
                + " are listed in the " + JaxbCommandResponse.class.getSimpleName() + ".response @XmlElements list.", numClass,
                numAnnos);
    }

    private Set<Class> getAcceptedCommandClassSet() throws Exception { 
        Field commandSetField = AcceptedServerCommands.class.getDeclaredField("acceptedCommands");
        commandSetField.setAccessible(true);
        return new HashSet<Class>((Set<Class>) commandSetField.get(null));
    }
    
    private void checkIfClassShouldBeInCmdListTypes( Class origClass, ParameterizedType genericSuperClassOrInterface,
            Map<Class, Class> cmdListTypesMap, Set<Class> classesChecked ) {
        Type returnType = genericSuperClassOrInterface.getActualTypeArguments()[0];
        // check that (generic) superclass has a parameterized type parameter
        // i.e. OrigClass extends ThatCommand<ParamTypeParam<InnerType>>
        if( !(returnType instanceof ParameterizedType) ) {
            // No parameterized type for generica super class
            // i.e. OrigClass extends ThatCommand<TypeParam>
            classesChecked.remove(origClass);
            return;
        }
        // If type parameter is a list, then do the checks on the cmdListType map
        Type listType = ((ParameterizedType) returnType).getRawType();
        if( List.class.isAssignableFrom((Class) listType) || Collection.class.isAssignableFrom((Class) listType) ) {
            assertTrue("Cmd list type set should include " + origClass.getSimpleName(), cmdListTypesMap.containsKey(origClass));
            Type listTypeType = ((ParameterizedType) returnType).getActualTypeArguments()[0];
            Class cmdListTypesKeyType = cmdListTypesMap.get(origClass);
            assertEquals("Expected cmd list type for " + origClass.getSimpleName(), cmdListTypesKeyType, listTypeType);
            classesChecked.remove(origClass);
        } else {
            fail(origClass.getSimpleName() + "/" + ((Class) ((ParameterizedType) returnType).getRawType()).getSimpleName());
            classesChecked.remove(origClass);
        }
    }
   
    @Test
    public void commandRequestTest() throws Exception {
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
   
    private Object getField(String fieldName, Class<?> clazz, Object obj) throws Exception {
        Field field = clazz.getDeclaredField(fieldName);
        field.setAccessible(true);
        return field.get(obj);
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
    public void serializingPrimitiveArraysTest() throws Exception  {
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
    public void unsupportedCommandsTest() {
        try {
            new JaxbCommandsRequest(new HaltCommand());
            fail( "An exception should have been thrown" );
        } catch (Exception e) {
            assertTrue(e instanceof UnsupportedOperationException);
        }
        Command [] cmdArrs = { new HaltCommand() };
        List<Command> cmds = Arrays.asList(cmdArrs);
        try {
            new JaxbCommandsRequest(cmds);
            fail( "An exception should have been thrown" );
        } catch (Exception e) {
            assertTrue(e instanceof UnsupportedOperationException);
        }
        JaxbCommandsRequest req = new JaxbCommandsRequest();
        try {
            req.setCommands(cmds);
            fail( "An exception should have been thrown" );
        } catch (Exception e) {
            assertTrue(e instanceof UnsupportedOperationException);
        }
    }

    @Test
    public void factHandleTest() throws Exception {
        RuntimeEngine runtimeEngine = createRuntimeManager("BPMN2-StringStructureRef.bpmn2").getRuntimeEngine(null);
        KieSession ksession = runtimeEngine.getKieSession();

        InsertObjectCommand cmd = new InsertObjectCommand("The Sky is Green");
        FactHandle factHandle = ksession.execute(cmd);
       
        addClassesToSerializationProvider(DisconnectedFactHandle.class);
        JaxbOtherResponse jor = new JaxbOtherResponse(DisconnectedFactHandle.newFrom(factHandle), 0, cmd);
        testRoundTrip(jor);
    }
}
