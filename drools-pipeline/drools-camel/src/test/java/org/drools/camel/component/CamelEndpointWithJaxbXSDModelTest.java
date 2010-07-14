package org.drools.camel.component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.naming.Context;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JaxbDataFormat;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactoryService;
import org.drools.builder.DirectoryLookupFactoryService;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactoryService;
import org.drools.builder.ResourceType;
import org.drools.builder.help.KnowledgeBuilderHelper;
import org.drools.command.runtime.BatchExecutionCommand;
import org.drools.command.runtime.rule.FireAllRulesCommand;
import org.drools.command.runtime.rule.InsertObjectCommand;
import org.drools.common.InternalRuleBase;
import org.drools.impl.KnowledgeBaseImpl;
import org.drools.io.ResourceFactory;
import org.drools.reteoo.ReteooRuleBase;
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
		Marshaller marshaller = getJaxbContext().createMarshaller();
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

		byte[] xmlResp = (byte[]) template.requestBody("direct:test-with-session", xmlReq.toString() );
		assertNotNull(xmlResp);
		System.out.println(new String(xmlResp));

		ExecutionResults resp = (ExecutionResults) getJaxbContext().createUnmarshaller().unmarshal(new ByteArrayInputStream(xmlResp));
		assertNotNull(resp);
		
		assertEquals(2, resp.getIdentifiers().size());
		assertNotNull(resp.getValue("lucaz"));
		assertNotNull(resp.getValue("baunax"));
		
		assertNotNull(resp.getFactHandle("lucaz"));
		assertNotNull(resp.getFactHandle("baunax"));
	}
	
    public JAXBContext getJaxbContext() {
        if ( this.jaxbContext == null ) {
            JaxbDataFormat def = new JaxbDataFormat();
            def.setPrettyPrint( true );
            def.setContextPath( "org.drools.model:org.drools.pipeline.camel" );
    
            // create a jaxbContext for the test to use outside of Camel.
            StatefulKnowledgeSession ksession1 = (StatefulKnowledgeSession) node.get( DirectoryLookupFactoryService.class ).lookup( "ksession1" );
            KnowledgeBase kbase = ksession1.getKnowledgeBase();
            ClassLoader originalCl = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader( ((ReteooRuleBase) ((KnowledgeBaseImpl) kbase).getRuleBase()).getRootClassLoader() );
                def = DroolsPolicy.augmentJaxbDataFormatDefinition( def );
                
                org.apache.camel.converter.jaxb.JaxbDataFormat jaxbDataformat = ( org.apache.camel.converter.jaxb.JaxbDataFormat ) def.getDataFormat(  this.context.getRoutes().get( 0 ).getRouteContext() );
                
                
                jaxbContext = jaxbDataformat.getContext();
            } catch ( JAXBException e ) {
                throw new RuntimeException( e );
            } finally {
                Thread.currentThread().setContextClassLoader( originalCl );
            }   
        }
        
        return jaxbContext;
    }	
	
	@Override
	protected RouteBuilder createRouteBuilder() throws Exception {
		return new RouteBuilder() {
			public void configure() throws Exception {
                JaxbDataFormat def = new JaxbDataFormat();
                def.setPrettyPrint( true );
                def.setContextPath( "org.drools.model:org.drools.pipeline.camel" );
                
				from("direct:test-with-session").policy( new DroolsPolicy() ).
				    unmarshal(def).to("drools:node/ksession1").marshal(def);
			}
		};
	}

	@Override
	protected void configureDroolsContext(Context jndiContext) {
		String rule = "";
		rule += "package org.drools.pipeline.camel.test \n";
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
		KnowledgeBuilder kbuilder = node.get(KnowledgeBuilderFactoryService.class).newKnowledgeBuilder();

		Options xjcOpts = new Options();
		xjcOpts.setSchemaLanguage( Language.XMLSCHEMA );


		try {
			KnowledgeBuilderHelper.addXsdModel( ResourceFactory.newClassPathResource("person.xsd", getClass()),
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
	
		assertFalse(kbuilder.hasErrors());

		KnowledgeBase kbase = node.get(KnowledgeBaseFactoryService.class).newKnowledgeBase();		
		kbase.addKnowledgePackages(kbuilder.getKnowledgePackages());
        classLoader = ((InternalRuleBase) ((KnowledgeBaseImpl) kbase).getRuleBase()).getRootClassLoader();		

		StatefulKnowledgeSession session = kbase.newStatefulKnowledgeSession();
		node.get(DirectoryLookupFactoryService.class).register(identifier, session);
		return session;
	}

}
