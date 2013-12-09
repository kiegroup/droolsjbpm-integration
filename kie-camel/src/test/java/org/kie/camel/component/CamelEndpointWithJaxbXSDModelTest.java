/*
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.camel.component;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.List;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.xml.bind.Marshaller;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JaxbDataFormat;
import org.drools.core.command.runtime.BatchExecutionCommandImpl;
import org.drools.core.command.runtime.rule.FireAllRulesCommand;
import org.drools.core.command.runtime.rule.InsertObjectCommand;
import org.drools.core.common.InternalRuleBase;
import org.drools.core.impl.KnowledgeBaseImpl;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.io.KieResources;
import org.kie.api.runtime.KieSession;
import org.kie.internal.builder.JaxbConfiguration;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.ExecutionResults;

import com.sun.tools.xjc.Language;
import com.sun.tools.xjc.Options;

public class CamelEndpointWithJaxbXSDModelTest extends KieCamelTestSupport {

    private ClassLoader    classLoader;

    @Test
    public void testSessionInsert() throws Exception {
        // These 2 classes around defined by person.xsd, not as a class file
        Class< ? > personClass = classLoader.loadClass( "org.drools.model.Person" );
        assertNotNull( personClass.getPackage() );
        Class< ? > addressClass = classLoader.loadClass( "org.drools.model.AddressType" );
        assertNotNull( addressClass.getPackage() );
        Object baunax = personClass.newInstance();
        Object lucaz = personClass.newInstance();

        Method setName = personClass.getMethod( "setName",
                                                String.class );
        setName.invoke( baunax,
                        "baunax" );
        setName.invoke( lucaz,
                        "lucaz" );

        Method setAddress = personClass.getMethod( "setAddress",
                                                   addressClass );
        Method setStreet = addressClass.getMethod( "setStreet",
                                                   String.class );
        Method setPostalCode = addressClass.getMethod( "setPostalCode",
                                                       BigInteger.class );
        Object lucazAddress = addressClass.newInstance();
        setStreet.invoke( lucazAddress,
                          "Unknow 342" );
        setPostalCode.invoke( lucazAddress,
                              new BigInteger( "1234" ) );

        Object baunaxAddress = addressClass.newInstance();
        setStreet.invoke( baunaxAddress,
                          "New Street 123" );
        setPostalCode.invoke( baunaxAddress,
                              new BigInteger( "5678" ) );

        setAddress.invoke( lucaz,
                           lucazAddress );
        setAddress.invoke( baunax,
                           baunaxAddress );

        BatchExecutionCommandImpl cmd = new BatchExecutionCommandImpl();
        cmd.setLookup( "ksession1" );
        cmd.getCommands().add( new InsertObjectCommand( lucaz,
                                                        "lucaz" ) );
        cmd.getCommands().add( new InsertObjectCommand( baunax,
                                                        "baunax" ) );
        cmd.getCommands().add( new FireAllRulesCommand() );

        StringWriter xmlReq = new StringWriter();
        Marshaller marshaller = getJaxbContext().createMarshaller();
        marshaller.setProperty( "jaxb.formatted.output",
                                true );
        marshaller.marshal( cmd,
                            xmlReq );

        System.out.println( xmlReq.toString() );

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

        byte[] xmlResp = (byte[]) template.requestBody( "direct:test-with-session",
                                                        xmlReq.toString() );
        assertNotNull( xmlResp );
        System.out.println( new String( xmlResp ) );

        ExecutionResults resp = (ExecutionResults) getJaxbContext().createUnmarshaller().unmarshal( new ByteArrayInputStream( xmlResp ) );
        assertNotNull( resp );

        assertEquals( 2,
                      resp.getIdentifiers().size() );
        assertNotNull( resp.getValue( "lucaz" ) );
        assertNotNull( resp.getValue( "baunax" ) );

        assertNotNull( resp.getFactHandle( "lucaz" ) );
        assertNotNull( resp.getFactHandle( "baunax" ) );
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        routeBuilder = new RouteBuilder() {
            public void configure() throws Exception {
                JaxbDataFormat def = new JaxbDataFormat();
                def.setPrettyPrint(true);
                def.setContextPath("org.kie.pipeline.camel");

                from("direct:test-with-session").policy(new KiePolicy()).
                        unmarshal(def).to("kie:ksession1").marshal(def);
            }
        };
        return routeBuilder;
    }

    @Override
    protected void configureDroolsContext(Context jndiContext) {
        String rule = "";
        rule += "package org.kie.pipeline.camel.test \n";
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

        registerKnowledgeRuntime( "ksession1",
                                  rule );
    }

    @Override
    protected KieSession registerKnowledgeRuntime(String identifier, String rule) {
        KieServices ks = KieServices.Factory.get();
        KieFileSystem kfs = ks.newKieFileSystem();
        KieResources kieResources = ks.getResources();

        Options xjcOpts = new Options();
        xjcOpts.setSchemaLanguage( Language.XMLSCHEMA );

        JaxbConfiguration jaxbConfiguration = KnowledgeBuilderFactory.newJaxbConfiguration(xjcOpts, "xsd");

        kfs.write(kieResources.newClassPathResource("person.xsd", getClass())
                              .setResourceType(ResourceType.XSD)
                              .setConfiguration(jaxbConfiguration));

        if ( rule != null && rule.length() > 0 ) {
            kfs.write( "src/main/resources/rule.drl", rule );
        }

        KieBuilder kieBuilder = ks.newKieBuilder( kfs ).buildAll();

        List<Message> errors = kieBuilder.getResults().getMessages(Message.Level.ERROR);
        if (!errors.isEmpty()) {
            fail("" + errors);
        }

        KieSession ksession = ks.newKieContainer(ks.getRepository().getDefaultReleaseId()).newKieSession();
        classLoader = ((InternalRuleBase) ((KnowledgeBaseImpl) ksession.getKieBase()).getRuleBase()).getRootClassLoader();

        try {
            jndiContext.bind( identifier, ksession );
        } catch (NamingException e) {
            throw new RuntimeException(e);
        }

        return ksession;
    }
}
