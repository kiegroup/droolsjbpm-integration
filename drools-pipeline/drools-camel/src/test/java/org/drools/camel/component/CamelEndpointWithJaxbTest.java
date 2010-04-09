package org.drools.camel.component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.apache.camel.builder.RouteBuilder;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactoryService;
import org.drools.builder.DirectoryLookupFactoryService;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactoryService;
import org.drools.builder.ResourceType;
import org.drools.builder.help.KnowledgeBuilderHelper;
import org.drools.command.runtime.BatchExecutionCommand;
import org.drools.command.runtime.process.StartProcessCommand;
import org.drools.command.runtime.rule.FireAllRulesCommand;
import org.drools.command.runtime.rule.GetObjectCommand;
import org.drools.command.runtime.rule.InsertElementsCommand;
import org.drools.command.runtime.rule.InsertObjectCommand;
import org.drools.command.runtime.rule.QueryCommand;
import org.drools.common.DisconnectedFactHandle;
import org.drools.io.ResourceFactory;
import org.drools.pipeline.camel.Person;
import org.drools.runtime.ExecutionResults;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.FactHandle;
import org.drools.runtime.rule.QueryResultsRow;
import org.drools.runtime.rule.impl.FlatQueryResults;

import com.sun.tools.xjc.Language;
import com.sun.tools.xjc.Options;

/**
 * 
 * @author Lucas Amador
 *
 */
public class CamelEndpointWithJaxbTest extends DroolsCamelTestSupport {

	private String handle;
	private JAXBContext jaxbContext;

	public void testSessionInsert() throws Exception {

		BatchExecutionCommand cmd = new BatchExecutionCommand();
		cmd.setLookup("ksession1");
		cmd.getCommands().add(new InsertObjectCommand(new Person("lucaz", 25), "person1"));
		cmd.getCommands().add(new InsertObjectCommand(new Person("hadrian", 25), "person2"));
		cmd.getCommands().add(new InsertObjectCommand(new Person("baunax", 21), "person3"));
		cmd.getCommands().add(new FireAllRulesCommand());
		
		StringWriter xmlReq = new StringWriter();
		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty("jaxb.formatted.output", true);
		marshaller.marshal(cmd, xmlReq);
		
		System.out.println(xmlReq.toString());
		
		byte[] xmlResp = (byte[]) template.requestBodyAndHeader("direct:test-with-session", xmlReq.toString(), "jaxb-context", jaxbContext);
		assertNotNull(xmlResp);
		System.out.println(new String(xmlResp));

		ExecutionResults resp = (ExecutionResults) jaxbContext.createUnmarshaller().unmarshal(new ByteArrayInputStream(xmlResp));
		assertNotNull(resp);
		
		assertEquals(3, resp.getIdentifiers().size());
		assertNotNull(resp.getValue("person1"));
		assertNotNull(resp.getValue("person2"));
		assertNotNull(resp.getValue("person3"));
		
		assertNotNull(resp.getFactHandle("person1"));
		assertNotNull(resp.getFactHandle("person2"));
		assertNotNull(resp.getFactHandle("person3"));
	}
	
	public void testSessionGetObject() throws Exception {
		
		BatchExecutionCommand cmd = new BatchExecutionCommand();
		cmd.setLookup("ksession1");
		cmd.getCommands().add(new GetObjectCommand(new DisconnectedFactHandle(handle), "hadrian"));
		
		StringWriter xmlReq = new StringWriter();
		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty("jaxb.formatted.output", true);
		marshaller.marshal(cmd, xmlReq);
		
		System.out.println(xmlReq.toString());
		
		byte[] xmlResp = (byte[]) template.requestBodyAndHeader("direct:test-with-session", xmlReq.toString(), "jaxb-context", jaxbContext);
		
		ExecutionResults resp = (ExecutionResults) jaxbContext.createUnmarshaller().unmarshal(new ByteArrayInputStream(xmlResp));
		assertNotNull(resp);
		
		assertEquals(1, resp.getIdentifiers().size());
		
		assertNotNull(resp.getValue("hadrian"));
	}
	
	public void FIXMEtestSessionModify() throws Exception {
		String cmd = "";
		cmd += "<batch-execution lookup='ksession1'>\n";
		cmd += "   <modify factHandle='" + handle + "'>\n";
		cmd += "      <set accessor='name' value='\"salaboy\"' />\n";
		cmd += "   </modify>\n";
		cmd += "</batch-execution>\n";
		
		String outXml = new String((byte[])template.requestBodyAndHeader("direct:test-with-session", cmd, "jaxb-context", jaxbContext));
		
		String expectedXml = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n";
		expectedXml += "<execution-results xmlns:ns2=\"http://drools.org/model\">\n";
		expectedXml += "    <results/>\n";
		expectedXml += "    <facts/>\n";
		expectedXml += "</execution-results>\n";
		
		assertEquals(expectedXml, outXml);
		
		cmd = "<batch-execution lookup='ksession1'>\n";
		cmd += "   <get-object out-identifier='rider' factHandle='" + handle + "'/>\n";
		cmd += "</batch-execution>\n";

		byte[] xmlResp = (byte[]) template.requestBodyAndHeader("direct:test-with-session", cmd.toString(), "jaxb-context", jaxbContext);
		assertNotNull(xmlResp);
		System.out.println(new String(xmlResp));

		ExecutionResults resp = (ExecutionResults) jaxbContext.createUnmarshaller().unmarshal(new ByteArrayInputStream(xmlResp));
		assertNotNull(resp);
		
		Person person = (Person) resp.getValue("rider");
		System.out.println(person.getName());
		
		assertEquals("salaboy", person.getName());
		
	}

	public void testSessionRetractObject() throws Exception {

		String cmd =  "";
		cmd += "<batch-execution lookup='ksession1'>\n"; 
		cmd += "   <retract factHandle='" + handle + "' />\n"; 
		cmd += "</batch-execution>";

		String outXml = new String((byte[])template.requestBodyAndHeader("direct:test-with-session", cmd, "jaxb-context", jaxbContext));

		System.out.println(outXml);

		assertNotNull(outXml);

	}

	public void testInsertElements() throws Exception {
		
		BatchExecutionCommand cmd = new BatchExecutionCommand();
		cmd.setLookup("ksession1");
		InsertElementsCommand elems = new InsertElementsCommand("elems");
		elems.getObjects().add(new Person("lucaz", 25));
		elems.getObjects().add(new Person("hadrian", 25));
		elems.getObjects().add(new Person("baunax", 21));
		
		cmd.getCommands().add(elems);
		cmd.getCommands().add(new FireAllRulesCommand());
		
		StringWriter xmlReq = new StringWriter();
		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty("jaxb.formatted.output", true);
		marshaller.marshal(cmd, xmlReq);
		
		System.out.println(xmlReq.toString());
		
		byte[] xmlResp = (byte[]) template.requestBodyAndHeader("direct:test-with-session", xmlReq.toString(), "jaxb-context", jaxbContext);
		assertNotNull(xmlResp);
		System.out.println(new String(xmlResp));

		ExecutionResults resp = (ExecutionResults) jaxbContext.createUnmarshaller().unmarshal(new ByteArrayInputStream(xmlResp));
		assertNotNull(resp);
		
		assertEquals(1, resp.getIdentifiers().size());
		List<Person> list = (List<Person>) resp.getValue("elems");
		assertEquals("lucaz", list.get(0).getName());
		assertEquals("hadrian", list.get(1).getName());
		assertEquals("baunax", list.get(2).getName());
		
	}
	
	public void testQuery() throws Exception {
		BatchExecutionCommand cmd = new BatchExecutionCommand();
		cmd.setLookup("ksession1");
		cmd.getCommands().add(new InsertObjectCommand(new Person("lucaz")));
		cmd.getCommands().add(new InsertObjectCommand(new Person("hadrian")));
		cmd.getCommands().add(new InsertObjectCommand(new Person("baunax", 43)));
		cmd.getCommands().add(new InsertObjectCommand(new Person("baunax", 21)));
		cmd.getCommands().add(new QueryCommand("persons", "persons", null));
		cmd.getCommands().add(new QueryCommand("person", "personWithName", new String[] {"baunax"} ));
		
		StringWriter xmlReq = new StringWriter();
		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty("jaxb.formatted.output", true);
		marshaller.marshal(cmd, xmlReq);
		
		System.out.println(xmlReq.toString());
		
		byte[] xmlResp = (byte[]) template.requestBodyAndHeader("direct:test-with-session", xmlReq.toString(), "jaxb-context", jaxbContext);
		assertNotNull(xmlResp);
		System.out.println(new String(xmlResp));
		
		ExecutionResults resp = (ExecutionResults) jaxbContext.createUnmarshaller().unmarshal(new ByteArrayInputStream(xmlResp));
		assertNotNull(resp);
		
		FlatQueryResults personQuery = (FlatQueryResults) resp.getValue("person");
		assertEquals(2, personQuery.size());
		FlatQueryResults personsQuery = (FlatQueryResults) resp.getValue("persons");
		assertEquals(5, personsQuery.size());
		
		Iterator<QueryResultsRow> iterator = personQuery.iterator();
		QueryResultsRow row = iterator.next();
		Person person = (Person) row.get("$p");
		
		assertEquals("baunax", person.getName());
	}

	public void testProcess() throws Exception {

		BatchExecutionCommand cmd = new BatchExecutionCommand();
		cmd.setLookup("ksession1");
		
		StartProcessCommand start = new StartProcessCommand("org.drools.actions");
		start.putParameter("person", new Person("lucaz", 25));
		start.putParameter("person2", new Person("hadrian", 25));
		start.putParameter("person3", new Person("baunax", 21));
		
		cmd.getCommands().add(start);
		
		StringWriter xmlReq = new StringWriter();
		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty("jaxb.formatted.output", true);
		marshaller.marshal(cmd, xmlReq);
		
		System.out.println(xmlReq.toString());

		byte[] xmlResp = (byte[])template.requestBodyAndHeader("direct:test-with-session", xmlReq.toString(), "jaxb-context", jaxbContext);
		assertNotNull(xmlResp);
		System.out.println(new String(xmlResp));
		Object resp = jaxbContext.createUnmarshaller().unmarshal(new ByteArrayInputStream(xmlResp));
		assertNotNull(resp);
	}
	
	
	public void testProcessInstanceSignalEvent() throws Exception {
		
		String processId = "org.drools.event";
		
		String cmd = "";
		cmd += "<batch-execution lookup='ksession1'>\n";
		cmd += "  <start-process processId='" + processId + "'>\n";
		cmd += "  </start-process>\n";
		cmd += "</batch-execution>\n";
		
		System.out.println(cmd);

		String outXml = new String((byte[])template.requestBodyAndHeader("direct:test-with-session", cmd, "jaxb-context", jaxbContext));
		
		assertNotNull(outXml);
		
		int processInstanceId = 1;
		
		cmd = "";
		cmd += "<batch-execution lookup='ksession1'>\n";
		cmd += "   <signal-event process-instance-id= '" + processInstanceId + "' event-type='MyEvent'>";
        cmd += "      <string>MyValue</string>";
        cmd += "   </signal-event>";
        cmd += "</batch-execution>\n";
		
		outXml = new String((byte[])template.requestBodyAndHeader("direct:test-with-session", cmd, "jaxb-context", jaxbContext));
		
		System.out.println(outXml);
	}

	@Override
	protected RouteBuilder createRouteBuilder() throws Exception {
		return new RouteBuilder() {
			public void configure() throws Exception {
				from("direct:test-with-session").to("drools:sm/ksession1?dataFormat=drools-jaxb");
				from("direct:test-no-session").to("drools:sm?dataFormat=drools-jaxb");
			}
		};
	}

	@Override
	protected void configureDroolsContext() {
		Person me = new Person();
		me.setName("Hadrian");

		String rule = "";
		rule += "package org.drools \n";
		rule += "import org.drools.pipeline.camel.Person \n";
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

		StatefulKnowledgeSession ksession = registerKnowledgeRuntime("ksession1", rule);
		InsertObjectCommand cmd = new InsertObjectCommand(me);
		cmd.setOutIdentifier("camel-rider");
		cmd.setReturnObject(false);
		ExecutionResults results = ksession.execute(cmd);
		handle = ((FactHandle)results.getFactHandle("camel-rider")).toExternalForm();
	}

	@Override
	protected StatefulKnowledgeSession registerKnowledgeRuntime(String identifier, String rule) {
		KnowledgeBuilder kbuilder = node.get(KnowledgeBuilderFactoryService.class).newKnowledgeBuilder();

		Options xjcOpts = new Options();
		xjcOpts.setSchemaLanguage( Language.XMLSCHEMA );

		String classNames[] = null;

		try {
			classNames = KnowledgeBuilderHelper.addXsdModel( ResourceFactory.newClassPathResource("person.xsd", getClass()),
					kbuilder,
					xjcOpts,
			"xsd" );
		} catch (IOException e) {
			LOG.error("Errors while adding xsd model. ", kbuilder.getErrors());
		}

		assertFalse( kbuilder.hasErrors() );

		if (rule != null && rule.length() > 0) {
			kbuilder.add(ResourceFactory.newByteArrayResource(rule.getBytes()), ResourceType.DRL);

			if (kbuilder.hasErrors()) {
				LOG.info("Errors while adding rule. ", kbuilder.getErrors());
			}
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
        process1 += "      <import name=\"org.drools.model.Person\" />\n";
        process1 += "    </imports>\n";
        process1 += "    <globals>\n";
        process1 += "      <global identifier=\"list\" type=\"java.util.List\" />\n";
        process1 += "    </globals>\n";
        process1 += "    <variables>\n";
        process1 += "      <variable name=\"person\" >\n";
        process1 += "        <type name=\"org.drools.process.core.datatype.impl.type.ObjectDataType\" className=\"Person\" />\n";
        process1 += "      </variable>\n";
        process1 += "    </variables>\n";
        process1 += "  </header>\n";
        process1 += "\n";
        process1 += "  <nodes>\n";
        process1 += "    <start id=\"1\" name=\"Start\" />\n";
        process1 += "    <actionNode id=\"2\" name=\"MyActionNode\" >\n";
        process1 += "      <action type=\"expression\" dialect=\"mvel\" >System.out.println(\"Triggered\");\n";
//        process1 += "list.add(person.name);\n";
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
        
        kbuilder.add(ResourceFactory.newByteArrayResource(process1.getBytes()), ResourceType.DRF);

        if (kbuilder.hasErrors()) {
        	System.out.println("Errors while adding process rule 1. " + kbuilder.getErrors());
        }

		assertFalse(kbuilder.hasErrors());
        
        String process2 = "";
        process2 += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        process2 += "<process xmlns=\"http://drools.org/drools-5.0/process\"\n";
        process2 += "         xmlns:xs=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        process2 += "         xs:schemaLocation=\"http://drools.org/drools-5.0/process drools-processes-5.0.xsd\"\n";
        process2 += "         type=\"RuleFlow\" name=\"flow\" id=\"org.drools.event\" package-name=\"org.drools\" version=\"1\" >\n";
        process2 += "\n";
        process2 += "  <header>\n";
        process2 += "    <variables>\n";
        process2 += "      <variable name=\"MyVar\" >\n";
        process2 += "        <type name=\"org.drools.process.core.datatype.impl.type.StringDataType\" />\n";
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
        
        kbuilder.add(ResourceFactory.newByteArrayResource(process2.getBytes()), ResourceType.DRF);

        if (kbuilder.hasErrors()) {
        	LOG.info("Errors while adding process rule 2. ", kbuilder.getErrors());
        }

		assertFalse(kbuilder.hasErrors());

		KnowledgeBase kbase = node.get(KnowledgeBaseFactoryService.class).newKnowledgeBase();
		kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());

		// Add object model to classes array
		List<String> allClasses = new ArrayList<String>(Arrays.asList(classNames));		
		allClasses.add("org.drools.pipeline.camel.Person");

		try {
			jaxbContext = KnowledgeBuilderHelper.newJAXBContext( allClasses.toArray(new String[allClasses.size()]), kbase );
		} catch (Exception e) {
			LOG.info("Errors while creating JAXB Context. ", e);
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		StatefulKnowledgeSession session = kbase.newStatefulKnowledgeSession();
		node.get(DirectoryLookupFactoryService.class).register(identifier, session);
		return session;
	}

}
