/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.remote.services.jaxb;

import static org.junit.Assert.*;
import static org.jbpm.query.QueryBuilderCoverageTestUtil.hackTheDatabaseMetadataLoggerBecauseTheresALogbackXmlInTheClasspath;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;

import org.drools.core.command.runtime.process.StartProcessCommand;
import org.drools.core.command.runtime.rule.InsertObjectCommand;
import org.drools.core.common.DisconnectedFactHandle;
import org.jbpm.services.task.commands.GetTaskAssignedAsBusinessAdminCommand;
import org.jbpm.services.task.commands.GetTasksByProcessInstanceIdCommand;
import org.jbpm.services.task.commands.TaskCommand;
import org.jbpm.test.JbpmJUnitBaseTestCase;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.command.Command;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.api.task.model.TaskSummary;
import org.kie.internal.process.CorrelationProperty;
import org.kie.remote.client.jaxb.ClientJaxbSerializationProvider;
import org.kie.remote.client.jaxb.ConversionUtil;
import org.kie.remote.jaxb.gen.AbortProcessInstanceCommand;
import org.kie.remote.jaxb.gen.AddContentFromUserCommand;
import org.kie.remote.jaxb.gen.I18NText;
import org.kie.remote.jaxb.gen.JaxbStringObjectPairArray;
import org.kie.remote.jaxb.gen.util.JaxbStringObjectPair;
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

@SuppressWarnings("unchecked")
public class JaxbRemoteServicesSerializationTest extends JbpmJUnitBaseTestCase {

    protected static final Logger logger = LoggerFactory.getLogger(JaxbRemoteServicesSerializationTest.class);

    @Before
    public void modifyLogging() {
        hackTheDatabaseMetadataLoggerBecauseTheresALogbackXmlInTheClasspath();
    }

    // @formatter:off
    private static Reflections reflections = new Reflections(
                                                             ClasspathHelper.forPackage("org.kie.remote.services.jaxb"),
                                                             ClasspathHelper.forPackage("org.kie.services.client.serialization.jaxb.rest"),
                                                             new TypeAnnotationsScanner(), new FieldAnnotationsScanner(), new MethodAnnotationsScanner(), new SubTypesScanner());

    // @formatter:on

    protected SerializationProvider jaxbProvider = ServerJaxbSerializationProvider.newInstance();
    {
        ((JaxbSerializationProvider) jaxbProvider).setPrettyPrint(true);
    }

    public void addClassesToSerializationProvider( Class<?>... extraClass ) {
        ((JaxbSerializationProvider) jaxbProvider).addJaxbClassesAndReinitialize(extraClass);
    }

    public <T> T testRoundTrip( T in ) throws Exception {
        String xmlObject = jaxbProvider.serialize(in);
        logger.debug(xmlObject);
        return (T) jaxbProvider.deserialize(xmlObject);
    }

    private Object clientServicesRoundTrip( Object in ) throws Exception {
        String xmlObject = ClientJaxbSerializationProvider.newInstance().serialize(in);
        logger.debug(xmlObject);
        return jaxbProvider.deserialize(xmlObject);
    }

    /**
     * If you think this test is a mistake: beware, this test is smarter than
     * you.
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
        for( XmlElement xmlElemAnno : xmlElems ) {
            Class cmdClass = xmlElemAnno.type();
            String name = xmlElemAnno.name();
            assertTrue(name + " is used twice as a name.", xmlElemNameSet.add(name));
            assertTrue(cmdClass.getSimpleName() + " is present in " + AcceptedServerCommands.class.getSimpleName() + " but not in "
                    + JaxbCommandsRequest.class.getSimpleName(), cmdSet.remove(cmdClass));
        }
        for( Class cmdClass : cmdSet ) {
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
        for( XmlElement xmlElemAnno : xmlElems ) {
            Class cmdClass = xmlElemAnno.type();
            String name = xmlElemAnno.name();
            assertTrue(name + " is used twice as a name.", xmlElemNameSet.add(name));
            assertTrue(cmdClass.getSimpleName() + " is present in " + JaxbCommandsResponse.class.getSimpleName() + " but does not "
                    + "implement " + JaxbCommandResponse.class.getSimpleName(), cmdSet.remove(cmdClass));
        }
        for( Class cmdClass : cmdSet ) {
            logger.error("Missing: " + cmdClass.getSimpleName());
        }
        assertTrue("See above output for difference between " + JaxbCommandResponse.class.getSimpleName() + " implementations "
                + "and classes listed in " + JaxbCommandsResponse.class.getSimpleName(), cmdSet.size() == 0);

        assertEquals((numClass > numAnnos ? "Not all classes" : "Non " + JaxbCommandResponse.class.getSimpleName() + " classes")
                     + " are listed in the " + JaxbCommandResponse.class.getSimpleName() + ".response @XmlElements list.",
                     numClass,
                     numAnnos);
    }

    private Set<Class> getAcceptedCommandClassSet() throws Exception {
        Field commandSetField = AcceptedServerCommands.class.getDeclaredField("acceptedCommands");
        commandSetField.setAccessible(true);
        return new HashSet<Class>((Set<Class>) commandSetField.get(null));
    }

    @Test
    public void requestObjectsHaveIdenticalCommandLists() throws Exception {
        XmlElements [] xmlElemsAnno = null;
        {
            Field field  = JaxbCommandsRequest.class.getDeclaredField("commands");
            XmlElements serverXmlElementsAnno = field.getAnnotation(XmlElements.class);
            field  = org.kie.remote.client.jaxb.JaxbCommandsRequest.class.getDeclaredField("commands");
            XmlElements clientXmlElementsAnno = field.getAnnotation(XmlElements.class);

            XmlElements [] tempXmlElemsAnnoArr = { serverXmlElementsAnno, clientXmlElementsAnno };
            xmlElemsAnno = tempXmlElemsAnnoArr;
        }

        Comparator<XmlElement> xmlElemAlphaCmptr = new Comparator<XmlElement>() {

            @Override
            public int compare( XmlElement o1, XmlElement o2 ) {
                if( o1 == o2 ) { return 0; }
                else if ( o1 == null || o1.name() == null) { return -1; }
                else if ( o2 == null || o2.name() == null) { return 1; }
                else {
                    return o1.name().compareTo(o2.name());
                }
            }
        };

        Set<XmlElement> [] xmlElemSet = new Set[2];
        Map<String, XmlElement> [] xmlElemMap = new Map[2];

        for( int i = 0; i < xmlElemsAnno.length; ++i ) {
            String type = i == 0 ? "server" : "client";
            xmlElemSet[i] = new TreeSet<XmlElement>(xmlElemAlphaCmptr);
            xmlElemSet[i].addAll(Arrays.asList(xmlElemsAnno[i].value()));
            xmlElemMap[i] = new TreeMap<String, XmlElement>();
            for( XmlElement xmlElemAnno : xmlElemSet[i] ) {
               XmlElement prevValue = xmlElemMap[i].put(xmlElemAnno.name(), xmlElemAnno);
               String name = prevValue == null ? "" : prevValue.name();
               assertNull( type + " commands request contains duplicate @XmlElemnt for :" + name, prevValue );
            }

        }

        assertEquals( "Server @XmlElements size is not equal with client", xmlElemSet[0].size(), xmlElemSet[1].size() );

        for( int i = 0; i < xmlElemSet.length; ++i ) {
            String type = i == 0 ? "server" : "client";
            int other = ( i + 1 ) % 2;
            String otherType = other == 0 ? "server" : "client";

            for( XmlElement xmlElemAnno : xmlElemSet[i] ) {
                assertTrue( otherType + " " + JaxbCommandsRequest.class.getSimpleName() + " does not contain "  + xmlElemAnno.name()
                    + " @" + XmlElement.class.getSimpleName() + "(\"" + xmlElemAnno.name() + "\")",
                    xmlElemMap[other].containsKey(xmlElemAnno.name()) );
                String className = xmlElemAnno.type().getSimpleName();
                XmlElement otherXmlElemAnno = xmlElemMap[other].get(xmlElemAnno.name());
                String otherClassName = otherXmlElemAnno.type().getSimpleName();
                assertEquals( "Dissimilar class values for @XmlElemen(\"" + xmlElemAnno.name() + "\")",
                        className, otherClassName);
            }
        }
    }

    @Test
    public void commandRequestTest() throws Exception {
        String userId = "krisv";
        long taskId = 1;
        org.kie.remote.jaxb.gen.StartTaskCommand stCmd = new org.kie.remote.jaxb.gen.StartTaskCommand();
        stCmd.setTaskId(taskId);
        stCmd.setUserId("krisv");
        org.kie.remote.client.jaxb.JaxbCommandsRequest clientReq = new org.kie.remote.client.jaxb.JaxbCommandsRequest("test", stCmd);
        Command<?> newCmd = ((JaxbCommandsRequest) clientServicesRoundTrip(clientReq)).getCommands().get(0);
        assertNotNull(newCmd);
        assertEquals("taskId is not equal", taskId, getField("taskId", TaskCommand.class, newCmd));
        assertEquals("userId is not equal", userId, getField("userId", TaskCommand.class, newCmd));

        clientReq = new org.kie.remote.client.jaxb.JaxbCommandsRequest();
        clientReq.setUser("krampus");
        List<Command> cmds = new ArrayList<Command>();
        clientReq.setCommands(cmds);
        clientReq.setDeploymentId("depId");
        clientReq.setProcessInstanceId(43l);
        clientReq.setVersion("6.0.1.0");
        AbortProcessInstanceCommand apCmd = new AbortProcessInstanceCommand();
        apCmd.setProcessInstanceId(23l);
        clientReq.getCommands().add(apCmd);

        JaxbCommandsRequest servicesReq = (JaxbCommandsRequest) clientServicesRoundTrip(clientReq);
        cmds = servicesReq.getCommands();
        org.drools.core.command.runtime.process.AbortProcessInstanceCommand realApCmd
            = (org.drools.core.command.runtime.process.AbortProcessInstanceCommand) cmds.get(0);
        assertEquals(apCmd.getProcessInstanceId(), realApCmd.getProcessInstanceId());

        org.kie.remote.jaxb.gen.GetProcessInstanceByCorrelationKeyCommand gpibckCmd
        = new org.kie.remote.jaxb.gen.GetProcessInstanceByCorrelationKeyCommand();
        String corrPropVal1 = "val";
        String corrPropVal2 = "prop";
        gpibckCmd.setCorrelationKey(corrPropVal1 + ":" + corrPropVal2);
        clientReq = new org.kie.remote.client.jaxb.JaxbCommandsRequest("test", gpibckCmd);
        JaxbCommandsRequest serviceReq = (JaxbCommandsRequest) clientServicesRoundTrip(clientReq);

        assertEquals( "Number of commands in request", 1, serviceReq.getCommands().size());

        org.drools.core.command.runtime.process.GetProcessInstanceByCorrelationKeyCommand realGpicbckCmd
            = (org.drools.core.command.runtime.process.GetProcessInstanceByCorrelationKeyCommand)
            serviceReq.getCommands().get(0);
        List<CorrelationProperty<?>> corrProps = realGpicbckCmd.getCorrelationKey().getProperties();
        assertEquals( "Number of correlation properties in request", 2, corrProps.size());
        assertEquals( "Correlation property value in request", corrPropVal1, corrProps.get(0).getValue());
        assertEquals( "Correlation property value in request", corrPropVal2, corrProps.get(1).getValue());

    }

    private Object getField( String fieldName, Class<?> clazz, Object obj ) throws Exception {
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
        assertEquals(2, ((JaxbCommandsResponse) newResp).getResponses().size());
    }

    @Test
    public void serializingPrimitiveArraysTest() throws Exception {
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("url", "http://soaptest.parasoft.com/calculator.wsdl");
        parameters.put("namespace", "http://www.parasoft.com/wsdl/calculator/");
        parameters.put("interface", "Calculator");
        parameters.put("operation", "add");
        Float [] arrayParam = new Float[] { 9.0f, 12.0f };
        parameters.put("parameters", arrayParam);

        addClassesToSerializationProvider(arrayParam.getClass());

        org.kie.remote.jaxb.gen.StartProcessCommand cmd = new org.kie.remote.jaxb.gen.StartProcessCommand();
        cmd.setProcessId("proc.with.array.params");
        cmd.setParameter(ConversionUtil.convertMapToJaxbStringObjectPairArray(parameters));
        org.kie.remote.client.jaxb.JaxbCommandsRequest clientReq = new org.kie.remote.client.jaxb.JaxbCommandsRequest("test", cmd);
        JaxbCommandsRequest serviceReq = (JaxbCommandsRequest) clientServicesRoundTrip(clientReq);
        StartProcessCommand newCmd = (StartProcessCommand) serviceReq.getCommands().get(0);
        assertNotNull(newCmd);

        Float [] arrayParamCopy = (Float[]) newCmd.getParameters().get("parameters");
        assertArrayEquals(arrayParam, arrayParamCopy);
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

    @Test
    public void addContentFromUserCmdTest() throws Exception {
        AddContentFromUserCommand cmd = new AddContentFromUserCommand();
        cmd.setTaskId(23l);
        cmd.setUserId("user");
        String key = "key";
        Object val = "val";
        cmd.setOutputContentMap(new JaxbStringObjectPairArray());
        cmd.getOutputContentMap().getItems().add(new JaxbStringObjectPair(key, val));

        String xmlStr = ClientJaxbSerializationProvider.newInstance().serialize(cmd);
        // @formatter:off
        org.jbpm.services.task.commands.AddContentFromUserCommand serverCmd
            = (org.jbpm.services.task.commands.AddContentFromUserCommand)
            ServerJaxbSerializationProvider.newInstance().deserialize(xmlStr);
        // @formatter:on

        assertEquals("cmd map key-val", val, serverCmd.getOutputContentMap().get(key));
    }

    @Test
    public void mapSetParameterSerializationTest() throws Exception {
        Set<String> strSet = new HashSet<String>(3);
        strSet.add("north");
        strSet.add("city");
        strSet.add("fields");

        List<Long> longList = new ArrayList<Long>(2);
        longList.add(25l);
        longList.add(39l);

        Map<String, I18NText> textMap = new HashMap<String, I18NText>(2);
        I18NText keyText = new I18NText();
        keyText.setId(25l);
        keyText.setLanguage("nl-FR");
        keyText.setText("Lekker koese?");
        textMap.put("key", keyText);
        I18NText lockText = new I18NText();
        lockText.setId(25l);
        lockText.setLanguage("nl-NL");
        lockText.setText("Eet smakkelijk!");
        textMap.put("lock", lockText);

        Map<String, Object> mapMap = new HashMap<String, Object>(1);
        mapMap.put("map", textMap);

        Set<Map> setMap = new HashSet<Map>();
        setMap.add(textMap);

        // @formatter:off
        Object [][] paramArr = {
                {"one", "a"},
                {"two", 2l},
                {"thr", new Integer[] { 59, 2195 } },
                { "set" , strSet },
                { "list", longList },
                { "map", textMap },
                { "mapmap", mapMap },
                { "setmap", setMap }
        };
        // @formatter:on


        org.kie.remote.jaxb.gen.StartProcessCommand spCmd = new org.kie.remote.jaxb.gen.StartProcessCommand();
        spCmd.setProcessId("org.map.set.process");
        Map<String, Object> origParams = new HashMap<String, Object>(paramArr.length);
        JaxbStringObjectPairArray jaxbParams = new JaxbStringObjectPairArray();
        spCmd.setParameter(jaxbParams);
        for( int i = 0; i < paramArr.length; ++i ) {
            jaxbParams.getItems().add(new JaxbStringObjectPair(paramArr[i][0].toString(), paramArr[i][1]));
            origParams.put(paramArr[i][0].toString(), paramArr[i][1]);
        }

        org.kie.remote.client.jaxb.JaxbCommandsRequest clientReq
            = new org.kie.remote.client.jaxb.JaxbCommandsRequest("org.deployment.id", spCmd);
        JaxbCommandsRequest serviceReq = (JaxbCommandsRequest) clientServicesRoundTrip(clientReq);

        StartProcessCommand cmd = (StartProcessCommand) serviceReq.getCommands().get(0);
        Map<String, Object> params = cmd.getParameters();
        for( Entry<String, Object> entry : params.entrySet() ) {
            assertNotNull( entry.getKey(), entry.getValue() );
        }
        assertEquals( "num params", origParams.size(), params.size());

        Object obj = params.get("list");
        assertTrue( "Expected a list: " + obj.getClass(), List.class.isAssignableFrom(obj.getClass()));
        obj = params.get("set");
        assertTrue( "Expected a set: " + obj.getClass(), Set.class.isAssignableFrom(obj.getClass()));
        obj = params.get("map");
        assertTrue( "Expected a map: " + obj.getClass(), Map.class.isAssignableFrom(obj.getClass()));
        obj = params.get("mapmap");
        assertTrue( "Expected a map of a map: " + obj.getClass(), Map.class.isAssignableFrom(obj.getClass()));
        obj = ((Map) obj).get("map");
        assertTrue( "Expected a map: " + obj.getClass(), Map.class.isAssignableFrom(obj.getClass()));
        obj = params.get("setmap");
        assertTrue( "Expected a set of a map: " + obj.getClass(), Set.class.isAssignableFrom(obj.getClass()));
        obj = ((Set) obj).iterator().next();
        assertTrue( "Expected a map: " + obj.getClass(), Map.class.isAssignableFrom(obj.getClass()));
    }
}
