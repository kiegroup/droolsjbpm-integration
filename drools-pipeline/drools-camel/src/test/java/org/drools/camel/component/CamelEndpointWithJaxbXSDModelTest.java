package org.drools.camel.component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.apache.camel.builder.RouteBuilder;
import org.drools.KnowledgeBase;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.ResourceType;
import org.drools.builder.help.KnowledgeBuilderHelper;
import org.drools.command.runtime.BatchExecutionCommand;
import org.drools.command.runtime.rule.FireAllRulesCommand;
import org.drools.command.runtime.rule.InsertObjectCommand;
import org.drools.common.InternalRuleBase;
import org.drools.impl.KnowledgeBaseImpl;
import org.drools.io.ResourceFactory;
import org.drools.rule.DroolsCompositeClassLoader;
import org.drools.runtime.ExecutionResults;
import org.drools.runtime.StatefulKnowledgeSession;

import com.sun.tools.xjc.Language;
import com.sun.tools.xjc.Options;

/**
 * 
 * @author Lucas Amador
 * @author Pablo Nussembaum
 *
 */
public class CamelEndpointWithJaxbXSDModelTest extends DroolsCamelTestSupport {

	private JAXBContext jaxbContext;
	private DroolsCompositeClassLoader classLoader;

	public void testSessionInsert() throws Exception {
		Class<?> personClass = classLoader.loadClass("org.drools.model.Person");
		assertNotNull(personClass.getPackage());
		Class<?> addressClass = classLoader.loadClass("org.drools.model.AddressType");
		assertNotNull(addressClass.getPackage());
		Object baunax = personClass.newInstance();
		Object lucaz = personClass.newInstance();
		
		Method setName = personClass.getMethod("setName", String.class);
		setName.invoke(baunax, "baunax");
		setName.invoke(lucaz, "lucaz");

		Method setAddress = personClass.getMethod("setAddress", addressClass);
		Method setStreet = addressClass.getMethod("setStreet", String.class);
		Method setPostalCode = addressClass.getMethod("setPostalCode", BigInteger.class);
		Object lucazAddress = addressClass.newInstance();
		setStreet.invoke(lucazAddress, "Unknow 342");
		setPostalCode.invoke(lucazAddress, new BigInteger("1234"));
		
		Object baunaxAddress = addressClass.newInstance();
		setStreet.invoke(baunaxAddress, "New Street 123");
		setPostalCode.invoke(baunaxAddress, new BigInteger("5678"));
		
		setAddress.invoke(lucaz, lucazAddress);
		setAddress.invoke(baunax, baunaxAddress);
		
		BatchExecutionCommand cmd = new BatchExecutionCommand();
		cmd.setLookup("ksession1");
		cmd.getCommands().add(new InsertObjectCommand(lucaz, "lucaz"));
		cmd.getCommands().add(new InsertObjectCommand(baunax, "baunax"));
		cmd.getCommands().add(new FireAllRulesCommand());
		
		StringWriter xmlReq = new StringWriter();
		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty("jaxb.formatted.output", true);
		marshaller.marshal(cmd, xmlReq);
		
		System.out.println(xmlReq.toString());
		
		String xmlCmd = "";
		xmlCmd += "<batch-execution lookup='ksession1'>\n";
		xmlCmd += "   <insert out-identifier='lucaz'>\n";
		xmlCmd += "    <object>\n";
		xmlCmd += "      <Person xmlns='http://drools.org/model' >\n";
		xmlCmd += "         <name>lucaz</name>\n";
		xmlCmd += "         <age>25</age>\n";
		xmlCmd += "      </Person>\n";
		xmlCmd += "    </object>\n";
		xmlCmd += "   </insert>\n";
		xmlCmd += "   <insert out-identifier='baunax'>\n";
		xmlCmd += "    <object>\n";
		xmlCmd += "      <Person xmlns='http://drools.org/model' >\n";
		xmlCmd += "         <name>baunax</name>\n";
		xmlCmd += "         <age>21</age>\n";
		xmlCmd += "      </Person>\n";
		xmlCmd += "    </object>\n";
		xmlCmd += "   </insert>\n";
		xmlCmd += "   <fire-all-rules />";
		xmlCmd += "</batch-execution>\n";

		byte[] xmlResp = (byte[]) template.requestBodyAndHeader("direct:test-with-session", xmlReq.toString(), "jaxb-context", jaxbContext);
		assertNotNull(xmlResp);
		System.out.println(new String(xmlResp));

		ExecutionResults resp = (ExecutionResults) jaxbContext.createUnmarshaller().unmarshal(new ByteArrayInputStream(xmlResp));
		assertNotNull(resp);
		
		assertEquals(2, resp.getIdentifiers().size());
		assertNotNull(resp.getValue("lucaz"));
		assertNotNull(resp.getValue("baunax"));
		
		assertNotNull(resp.getFactHandle("lucaz"));
		assertNotNull(resp.getFactHandle("baunax"));
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
		String rule = "";
		rule += "package org.drools \n";
		rule += "import org.drools.model.Person \n";
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

		registerKnowledgeRuntime("ksession1", rule);
	}

	@Override
	protected StatefulKnowledgeSession registerKnowledgeRuntime(String identifier, String rule) {
		KnowledgeBuilder kbuilder = serviceManager.getKnowledgeBuilderFactoryService().newKnowledgeBuilder();

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
        process1 += "list.add(person.name);\n";
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

		KnowledgeBase kbase = serviceManager.getKnowledgeBaseFactoryService().newKnowledgeBase();
		
		classLoader = ((InternalRuleBase) ((KnowledgeBaseImpl) kbase).getRuleBase()).getRootClassLoader();
		kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());

		// Add object model to classes array
		List<String> allClasses = new ArrayList<String>(Arrays.asList(classNames));		

		try {
			jaxbContext = KnowledgeBuilderHelper.newJAXBContext( allClasses.toArray(new String[allClasses.size()]), kbase );
		} catch (Exception e) {
			LOG.info("Errors while creating JAXB Context. ", e);
			e.printStackTrace();
			throw new RuntimeException(e);
		}

		StatefulKnowledgeSession session = kbase.newStatefulKnowledgeSession();
		serviceManager.register(identifier, session);
		return session;
	}

}
