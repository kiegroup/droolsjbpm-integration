package org.drools.camel.component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.camel.builder.RouteBuilder;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactoryService;
import org.drools.builder.DirectoryLookupFactoryService;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactoryService;
import org.drools.builder.ResourceType;
import org.drools.builder.help.KnowledgeBuilderHelper;
import org.drools.command.runtime.BatchExecutionCommand;
import org.drools.command.runtime.GetGlobalCommand;
import org.drools.command.runtime.SetGlobalCommand;
import org.drools.command.runtime.rule.FireAllRulesCommand;
import org.drools.command.runtime.rule.InsertObjectCommand;
import org.drools.io.ResourceFactory;
import org.drools.pipeline.camel.Person;
import org.drools.runtime.ExecutionResults;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.rule.FactHandle;

import com.sun.tools.xjc.Language;
import com.sun.tools.xjc.Options;

/**
 * 
 * @author Lucas Amador
 * @author Pablo Nussembaum
 */
public class CamelEndpointWithJaxWrapperCollectionTest extends DroolsCamelTestSupport {

	private String handle;
	private JAXBContext jaxbContext;

	public void testWorkingSetGlobalTestSessionSetAndGetGlobal() throws Exception {

		BatchExecutionCommand cmd = new BatchExecutionCommand();
		cmd.setLookup("ksession1");
		
		SetGlobalCommand setGlobal = new SetGlobalCommand("list", new WrappedList());
		setGlobal.setOut(true);
		
		cmd.getCommands().add(setGlobal);
		cmd.getCommands().add(new InsertObjectCommand(new Person("baunax")));
		cmd.getCommands().add(new FireAllRulesCommand());
		cmd.getCommands().add(new GetGlobalCommand("list"));
		
		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty("jaxb.formatted.output", true);
		StringWriter xml = new StringWriter();
		marshaller.marshal(cmd, xml);

		System.out.println(xml.toString());
		
		byte[] response = (byte[]) template.requestBodyAndHeader("direct:test-with-session", xml.toString(), "jaxb-context", jaxbContext);
		assertNotNull(response);
		System.out.println("response:\n" + new String(response));
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		ExecutionResults res = (ExecutionResults) unmarshaller.unmarshal(new ByteArrayInputStream(response));
		WrappedList resp = (WrappedList) res.getValue("list");
		assertNotNull(resp);
		
		assertEquals(resp.size(), 2);
		assertEquals("baunax", resp.get(0).getName());
		assertEquals("Hadrian", resp.get(1).getName());

	}
	
	@XmlRootElement(name="list")
	@XmlAccessorType(XmlAccessType.FIELD)
	public static class WrappedList {
//		@XmlElementWrapper(name="list")
        @XmlElements({@XmlElement(name="org.drools.pipeline.camel.Person", type=Person.class)})
		private List<Person> people = new ArrayList<Person>();

		public void add(int index, Person element) {
			people.add(index, element);
		}

		public boolean add(Person e) {
			return people.add(e);
		}

		public boolean addAll(Collection<? extends Person> c) {
			return people.addAll(c);
		}

		public boolean addAll(int index, Collection<? extends Person> c) {
			return people.addAll(index, c);
		}

		public void clear() {
			people.clear();
		}

		public boolean contains(Object o) {
			return people.contains(o);
		}

		public boolean containsAll(Collection<?> c) {
			return people.containsAll(c);
		}

		public boolean equals(Object o) {
			return people.equals(o);
		}

		public Person get(int index) {
			return people.get(index);
		}

		public int hashCode() {
			return people.hashCode();
		}

		public int indexOf(Object o) {
			return people.indexOf(o);
		}

		public boolean isEmpty() {
			return people.isEmpty();
		}

		public Iterator<Person> iterator() {
			return people.iterator();
		}

		public int lastIndexOf(Object o) {
			return people.lastIndexOf(o);
		}

		public ListIterator<Person> listIterator() {
			return people.listIterator();
		}

		public ListIterator<Person> listIterator(int index) {
			return people.listIterator(index);
		}

		public Person remove(int index) {
			return people.remove(index);
		}

		public boolean remove(Object o) {
			return people.remove(o);
		}

		public boolean removeAll(Collection<?> c) {
			return people.removeAll(c);
		}

		public boolean retainAll(Collection<?> c) {
			return people.retainAll(c);
		}

		public Person set(int index, Person element) {
			return people.set(index, element);
		}

		public int size() {
			return people.size();
		}

		public List<Person> subList(int fromIndex, int toIndex) {
			return people.subList(fromIndex, toIndex);
		}

		public Object[] toArray() {
			return people.toArray();
		}

		public <T> T[] toArray(T[] a) {
			return people.toArray(a);
		}
		
	}

	@Override
	protected void configureDroolsContext() {
		Person me = new Person();
		me.setName("Hadrian");

		String rule = "";
		rule += "package org.drools \n";
		rule += "import org.drools.pipeline.camel.Person\n";
		rule += "import org.drools.camel.component.CamelEndpointWithJaxWrapperCollectionTest.WrappedList\n";
		rule += "global org.drools.camel.component.CamelEndpointWithJaxWrapperCollectionTest.WrappedList list\n";
		rule += "rule rule1 \n";
		rule += "  when \n";
		rule += "    $p : Person() \n";
		rule += " \n";
		rule += "  then \n";
		rule += "    System.out.println(\"executed\"); \n";
		rule += "    list.add($p); \n";
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
        process1 += "      <global identifier=\"list\" type=\"org.drools.camel.component.CamelEndpointWithJaxWrapperCollectionTest.WrappedList\" />\n";
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
        	LOG.info("Errors while adding process rule 1. ", kbuilder.getErrors());
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
		allClasses.add(Person.class.getName());
		allClasses.add(WrappedList.class.getName());

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

	@Override
	protected RouteBuilder createRouteBuilder() throws Exception {
		return new RouteBuilder() {
			public void configure() throws Exception {
				from("direct:test-with-session").to("drools:sm/ksession1?dataFormat=drools-jaxb");
				from("direct:test-no-session").to("drools:sm?dataFormat=drools-jaxb");
			}
		};
	}
}
