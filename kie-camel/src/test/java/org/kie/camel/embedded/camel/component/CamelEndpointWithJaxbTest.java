/*
 * Copyright 2010 JBoss Inc
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.camel.embedded.camel.component;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.naming.NamingException;
import javax.xml.bind.Marshaller;

import com.sun.tools.xjc.Language;
import com.sun.tools.xjc.Options;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JaxbDataFormat;
import org.drools.core.command.runtime.BatchExecutionCommandImpl;
import org.drools.core.command.runtime.process.StartProcessCommand;
import org.drools.core.command.runtime.rule.FireAllRulesCommand;
import org.drools.core.command.runtime.rule.GetObjectCommand;
import org.drools.core.command.runtime.rule.InsertElementsCommand;
import org.drools.core.command.runtime.rule.InsertObjectCommand;
import org.drools.core.command.runtime.rule.QueryCommand;
import org.drools.core.common.DefaultFactHandle;
import org.drools.core.runtime.rule.impl.FlatQueryResults;
import org.junit.Ignore;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.command.Command;
import org.kie.api.io.KieResources;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.ExecutionResults;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.api.runtime.rule.QueryResultsRow;
import org.kie.camel.embedded.component.KiePolicy;
import org.kie.pipeline.camel.Person;
import org.kie.internal.builder.JaxbConfiguration;
import org.kie.internal.builder.KnowledgeBuilderFactory;

public class CamelEndpointWithJaxbTest extends KieCamelTestSupport {

    private String handle;

    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void testSessionInsert() throws Exception {

        BatchExecutionCommandImpl cmd = new BatchExecutionCommandImpl();
        cmd.setLookup("ksession1");
        cmd.addCommand(new InsertObjectCommand(new Person("lucaz", 25), "person1"));
        cmd.addCommand(new InsertObjectCommand(new Person("hadrian", 25), "person2"));
        cmd.addCommand(new InsertObjectCommand(new Person("baunax", 21), "person3"));
        cmd.addCommand(new FireAllRulesCommand());

        StringWriter xmlReq = new StringWriter();
        Marshaller marshaller = getJaxbContext().createMarshaller();
        marshaller.setProperty("jaxb.formatted.output", true);
        marshaller.marshal(cmd, xmlReq);

        logger.debug(xmlReq.toString());

        byte[] xmlResp = (byte[])template.requestBody("direct:test-with-session", xmlReq.toString());
        assertNotNull(xmlResp);
        logger.debug(new String(xmlResp));

        ExecutionResults resp = (ExecutionResults)getJaxbContext().createUnmarshaller().unmarshal(new ByteArrayInputStream(xmlResp));
        assertNotNull(resp);

        assertEquals(3, resp.getIdentifiers().size());
        assertNotNull(resp.getValue("person1"));
        assertNotNull(resp.getValue("person2"));
        assertNotNull(resp.getValue("person3"));

        assertNotNull(resp.getFactHandle("person1"));
        assertNotNull(resp.getFactHandle("person2"));
        assertNotNull(resp.getFactHandle("person3"));
    }

    @Test
    public void testSessionGetObject() throws Exception {

        BatchExecutionCommandImpl cmd = new BatchExecutionCommandImpl();
        cmd.setLookup("ksession1");
        cmd.addCommand(new GetObjectCommand(DefaultFactHandle.createFromExternalFormat(handle), "hadrian"));

        StringWriter xmlReq = new StringWriter();
        Marshaller marshaller = getJaxbContext().createMarshaller();
        marshaller.setProperty("jaxb.formatted.output", true);
        marshaller.marshal(cmd, xmlReq);

        logger.debug(xmlReq.toString());

        byte[] xmlResp = (byte[])template.requestBody("direct:test-with-session", xmlReq.toString());

        ExecutionResults resp = (ExecutionResults)getJaxbContext().createUnmarshaller().unmarshal(new ByteArrayInputStream(xmlResp));
        assertNotNull(resp);

        assertEquals(1, resp.getIdentifiers().size());

        assertNotNull(resp.getValue("hadrian"));
    }

    @Test
    @Ignore("error in drools session")
    public void testSessionModify() throws Exception {
        String cmd = "";
        cmd += "<batch-execution lookup='ksession1'>\n";
        cmd += "   <modify>\n";
        cmd += "      <fact-handle>" + handle + "</fact-handle>\n";
        cmd += "      <setters accessor='name' value='salaboy' />\n";
        cmd += "   </modify>\n";
        cmd += "</batch-execution>\n";

        String outXml = new String((byte[])template.requestBody("direct:test-with-session", cmd));

        String expectedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
        expectedXml += "<execution-results xmlns:ns2=\"http://drools.org/model\">\n";
        expectedXml += "    <results/>\n";
        expectedXml += "    <facts/>\n";
        expectedXml += "</execution-results>\n";

        assertXMLEqual(expectedXml, outXml);

        cmd = "<batch-execution lookup='ksession1'>\n";
        cmd += "   <get-object out-identifier='rider'>\n";
        cmd += "     <fact-handle>" + handle + "</fact-handle>\n";
        cmd += "   </get-object>\n";
        cmd += "</batch-execution>\n";

        byte[] xmlResp = (byte[])template.requestBody("direct:test-with-session", cmd.toString());
        assertNotNull(xmlResp);

        ExecutionResults resp = (ExecutionResults)getJaxbContext().createUnmarshaller().unmarshal(new ByteArrayInputStream(xmlResp));
        assertNotNull(resp);

        Person person = (Person)resp.getValue("rider");

        assertEquals("salaboy", person.getName());

    }

    @Test
    public void testSessionRetractObject() throws Exception {

        String cmd = "";
        cmd += "<batch-execution lookup='ksession1'>\n";
        cmd += "   <retract>\n";
        cmd += "     <fact-handle>" + handle + "</fact-handle>\n";
        cmd += "   </retract>\n";
        cmd += "</batch-execution>";

        String outXml = new String((byte[])template.requestBody("direct:test-with-session", cmd));

        logger.debug(outXml);

        assertNotNull(outXml);

    }

    @Test
    public void testInsertElements() throws Exception {

        BatchExecutionCommandImpl cmd = new BatchExecutionCommandImpl();
        cmd.setLookup("ksession1");
        InsertElementsCommand elems = new InsertElementsCommand("elems");
        elems.getObjects().add(new Person("lucaz", 25));
        elems.getObjects().add(new Person("hadrian", 25));
        elems.getObjects().add(new Person("baunax", 21));
        elems.getObjects().add("xxx");

        cmd.addCommand(elems);
        cmd.addCommand(new FireAllRulesCommand());

        StringWriter xmlReq = new StringWriter();
        Marshaller marshaller = getJaxbContext().createMarshaller();
        marshaller.setProperty("jaxb.formatted.output", true);
        marshaller.marshal(cmd, xmlReq);

        byte[] xmlResp = (byte[])template.requestBody("direct:test-with-session", xmlReq.toString());
        assertNotNull(xmlResp);

        ExecutionResults resp = (ExecutionResults)getJaxbContext().createUnmarshaller().unmarshal(new ByteArrayInputStream(xmlResp));
        assertNotNull(resp);

        assertEquals(1, resp.getIdentifiers().size());
        List<Person> list = (List<Person>)resp.getValue("elems");
        assertEquals("lucaz", list.get(0).getName());
        assertEquals("hadrian", list.get(1).getName());
        assertEquals("baunax", list.get(2).getName());

    }

    @Test
    public void testQuery() throws Exception {
        BatchExecutionCommandImpl cmd = new BatchExecutionCommandImpl();
        cmd.setLookup("ksession1");
        cmd.addCommand(new InsertObjectCommand(new Person("lucaz")));
        cmd.addCommand(new InsertObjectCommand(new Person("hadrian")));
        cmd.addCommand(new InsertObjectCommand(new Person("baunax", 43)));
        cmd.addCommand(new InsertObjectCommand(new Person("baunax", 21)));
        cmd.addCommand(new QueryCommand("persons", "persons", null));
        cmd.addCommand(new QueryCommand("person", "personWithName", new String[] {"baunax"}));

        StringWriter xmlReq = new StringWriter();
        Marshaller marshaller = getJaxbContext().createMarshaller();
        marshaller.setProperty("jaxb.formatted.output", true);
        marshaller.marshal(cmd, xmlReq);

        logger.debug(xmlReq.toString());

        byte[] xmlResp = (byte[])template.requestBody("direct:test-with-session", xmlReq.toString());
        assertNotNull(xmlResp);
        logger.debug(new String(xmlResp));

        ExecutionResults resp = (ExecutionResults)getJaxbContext().createUnmarshaller().unmarshal(new ByteArrayInputStream(xmlResp));
        assertNotNull(resp);

        FlatQueryResults personQuery = (FlatQueryResults)resp.getValue("person");
        assertEquals(2, personQuery.size());
        FlatQueryResults personsQuery = (FlatQueryResults)resp.getValue("persons");
        assertEquals(5, personsQuery.size());

        Iterator<QueryResultsRow> iterator = personQuery.iterator();
        QueryResultsRow row = iterator.next();
        Person person = (Person)row.get("$p");

        assertEquals("baunax", person.getName());
    }

    @Test
    public void testProcess() throws Exception {

        BatchExecutionCommandImpl cmd = new BatchExecutionCommandImpl();
        cmd.setLookup("ksession1");

        StartProcessCommand start = new StartProcessCommand("org.drools.actions", "process-instance-id");
        start.putParameter("person", new Person("lucaz", 25));
        start.putParameter("person2", new Person("hadrian", 25));
        start.putParameter("person3", new Person("baunax", 21));

        cmd.addCommand(start);

        StringWriter xmlReq = new StringWriter();
        Marshaller marshaller = getJaxbContext().createMarshaller();
        marshaller.setProperty("jaxb.formatted.output", true);
        marshaller.marshal(cmd, xmlReq);

        logger.debug(xmlReq.toString());

        byte[] xmlResp = (byte[])template.requestBody("direct:test-with-session", xmlReq.toString());
        assertNotNull(xmlResp);
        logger.debug(new String(xmlResp));
        ExecutionResults resp = (ExecutionResults)getJaxbContext().createUnmarshaller().unmarshal(new ByteArrayInputStream(xmlResp));
        assertNotNull(resp);
        assertNotNull(resp.getValue("process-instance-id"));
    }

    @Test
    public void testProcessInstanceSignalEvent() throws Exception {

        String processId = "org.drools.core.event";

        String cmd = "";
        cmd += "<batch-execution lookup='ksession1'>\n";
        cmd += "  <start-process processId='" + processId + "'>\n";
        cmd += "  </start-process>\n";
        cmd += "</batch-execution>\n";

        logger.debug(cmd);

        String outXml = new String((byte[])template.requestBody("direct:test-with-session", cmd));

        assertNotNull(outXml);

        int processInstanceId = 1;

        cmd = "";
        cmd += "<batch-execution lookup='ksession1'>\n";
        cmd += "   <signal-event process-instance-id= '" + processInstanceId + "' event-type='MyEvent'>";
        cmd += "      <string>MyValue</string>";
        cmd += "   </signal-event>";
        cmd += "</batch-execution>\n";

        outXml = new String((byte[])template.requestBody("direct:test-with-session", cmd));

        logger.debug(outXml);
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        routeBuilder = new RouteBuilder() {
            public void configure() throws Exception {
                JaxbDataFormat def = new JaxbDataFormat();
                def.setPrettyPrint(true);
                // TODO does not work: def.setContextPath( "org.drools.camel.testdomain:org.drools.pipeline.camel" );
                def.setContextPath("org.kie.pipeline.camel");

                from("direct:test-with-session").policy(new KiePolicy()).unmarshal(def).to("kie-local:ksession1").marshal(def);
                from("direct:test-no-session").policy(new KiePolicy()).unmarshal(def).to("kie-local:dynamic").marshal(def);
            }
        };
        return routeBuilder;
    }

    @Override
    protected void configureDroolsContext(javax.naming.Context jndiContext) {
        Person me = new Person();
        me.setName("Hadrian");

        String rule = "";
        rule += "package org.kie.pipeline.camel \n";
        rule += "import org.kie.pipeline.camel.Person \n";
        rule += "global java.util.List list \n";
        rule += "query persons \n";
        rule += "   $p : Person(name != null) \n";
        rule += "end \n";
        rule += "query personWithName(String param)\n";
        rule += "   $p : Person(name == param) \n";
        rule += "end \n";
        rule += "rule rule1 \n";
        rule += "  when \n";
        rule += "    $p : Person() \n";
        rule += " \n";
        rule += "  then \n";
        rule += "    System.out.println(\"executed\"); \n";
        rule += "end\n";

        KieSession ksession = registerKnowledgeRuntime("ksession1", rule);
        InsertObjectCommand cmd = new InsertObjectCommand(me);
        cmd.setOutIdentifier("camel-rider");
        cmd.setReturnObject(false);
        BatchExecutionCommandImpl script = new BatchExecutionCommandImpl(Arrays.asList(new Command<?>[] {cmd}));

        ExecutionResults results = ksession.execute(script);
        handle = ((FactHandle)results.getFactHandle("camel-rider")).toExternalForm();
    }

    @Override
    protected KieSession registerKnowledgeRuntime(String identifier, String rule) {
        KieServices ks = KieServices.Factory.get();
        KieFileSystem kfs = ks.newKieFileSystem();
        KieResources kieResources = ks.getResources();

        Options xjcOpts = new Options();
        xjcOpts.setSchemaLanguage(Language.XMLSCHEMA);

        JaxbConfiguration jaxbConfiguration = KnowledgeBuilderFactory.newJaxbConfiguration(xjcOpts, "xsd");

        kfs.write(kieResources.newClassPathResource("person.xsd", getClass()).setResourceType(ResourceType.XSD).setConfiguration(jaxbConfiguration));

        if (rule != null && rule.length() > 0) {
            kfs.write("src/main/resources/rule.drl", rule);
        }

        String process1 = "";
        process1 += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        process1 += "<process xmlns=\"http://drools.org/drools-5.0/process\"\n";
        process1 += "         xmlns:xs=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        process1 += "         xs:schemaLocation=\"http://drools.org/drools-5.0/process drools-processes-5.0.xsd\"\n";
        process1 += "         type=\"RuleFlow\" name=\"flow\" id=\"org.drools.actions\" package-name=\"org.drools\" version=\"1\" >\n";
        process1 += "\n";
        process1 += "  <header>\n";
        process1 += "    <imports>\n";
        process1 += "      <import name=\"org.kie.camel.embedded.camel.testdomain.Person\" />\n";
        process1 += "    </imports>\n";
        process1 += "    <globals>\n";
        process1 += "      <global identifier=\"list\" type=\"java.util.List\" />\n";
        process1 += "    </globals>\n";
        process1 += "    <variables>\n";
        process1 += "      <variable name=\"person\" >\n";
        process1 += "        <type name=\"org.drools.core.process.core.datatype.impl.type.ObjectDataType\" className=\"Person\" />\n";
        process1 += "      </variable>\n";
        process1 += "    </variables>\n";
        process1 += "  </header>\n";
        process1 += "\n";
        process1 += "  <nodes>\n";
        process1 += "    <start id=\"1\" name=\"Start\" />\n";
        process1 += "    <actionNode id=\"2\" name=\"MyActionNode\" >\n";
        process1 += "      <action type=\"expression\" dialect=\"mvel\" >System.out.println(\"Triggered\");\n";
        // process1 += "list.add(person.name);\n";
        process1 += "</action>\n";
        process1 += "    </actionNode>\n";
        process1 += "    <end id=\"3\" name=\"End\" />\n";
        process1 += "  </nodes>\n";
        process1 += "\n";
        process1 += "  <connections>\n";
        process1 += "    <connection from=\"1\" to=\"2\" />\n";
        process1 += "    <connection from=\"2\" to=\"3\" />\n";
        process1 += "  </connections>\n" + "\n";
        process1 += "</process>";

        kfs.write("src/main/resources/process1.rf", process1);

        String process2 = "";
        process2 += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        process2 += "<process xmlns=\"http://drools.org/drools-5.0/process\"\n";
        process2 += "         xmlns:xs=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        process2 += "         xs:schemaLocation=\"http://drools.org/drools-5.0/process drools-processes-5.0.xsd\"\n";
        process2 += "         type=\"RuleFlow\" name=\"flow\" id=\"org.drools.core.event\" package-name=\"org.drools\" version=\"1\" >\n";
        process2 += "\n";
        process2 += "  <header>\n";
        process2 += "    <variables>\n";
        process2 += "      <variable name=\"MyVar\" >\n";
        process2 += "        <type name=\"org.drools.core.process.core.datatype.impl.type.StringDataType\" />\n";
        process2 += "        <value>SomeText</value>\n";
        process2 += "      </variable>\n";
        process2 += "    </variables>\n";
        process2 += "  </header>\n";
        process2 += "\n";
        process2 += "  <nodes>\n";
        process2 += "    <start id=\"1\" name=\"Start\" />\n";
        process2 += "    <eventNode id=\"2\" name=\"Event\" variableName=\"MyVar\" >\n";
        process2 += "      <eventFilters>\n";
        process2 += "        <eventFilter type=\"eventType\" eventType=\"MyEvent\" />\n";
        process2 += "      </eventFilters>\n";
        process2 += "    </eventNode>\n";
        process2 += "    <join id=\"3\" name=\"Join\" type=\"1\" />\n";
        process2 += "    <end id=\"4\" name=\"End\" />\n";
        process2 += "  </nodes>\n";
        process2 += "\n";
        process2 += "  <connections>\n";
        process2 += "    <connection from=\"1\" to=\"3\" />\n";
        process2 += "    <connection from=\"2\" to=\"3\" />\n";
        process2 += "    <connection from=\"3\" to=\"4\" />\n";
        process2 += "  </connections>\n";
        process2 += "\n";
        process2 += "</process>";

        kfs.write("src/main/resources/process2.rf", process2);

        KieBuilder kieBuilder;
        try {
            kieBuilder = ks.newKieBuilder(kfs).buildAll();
        } catch(Exception e) {
            throw e;
        }

        List<Message> errors = kieBuilder.getResults().getMessages(Message.Level.ERROR);
        if (!errors.isEmpty()) {
            fail("" + errors);
        }

        KieSession ksession = ks.newKieContainer(ks.getRepository().getDefaultReleaseId()).newKieSession();

        try {
            jndiContext.bind(identifier, ksession);
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }

        return ksession;
    }
}
