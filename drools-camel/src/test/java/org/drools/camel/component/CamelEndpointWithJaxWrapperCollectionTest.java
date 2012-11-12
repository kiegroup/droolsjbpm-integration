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

package org.drools.camel.component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;

import javax.naming.Context;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JaxbDataFormat;
import org.drools.command.impl.GenericCommand;
import org.drools.command.runtime.BatchExecutionCommandImpl;
import org.drools.command.runtime.GetGlobalCommand;
import org.drools.command.runtime.SetGlobalCommand;
import org.drools.command.runtime.rule.FireAllRulesCommand;
import org.drools.command.runtime.rule.InsertObjectCommand;
import org.drools.concurrent.CommandExecutor;
import org.drools.impl.KnowledgeBaseImpl;
import org.drools.pipeline.camel.Person;
import org.drools.pipeline.camel.WrappedList;
import org.drools.reteoo.ReteooRuleBase;

import com.sun.tools.xjc.Language;
import com.sun.tools.xjc.Options;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.KnowledgeBase;
import org.kie.KnowledgeBaseFactoryService;
import org.kie.builder.KnowledgeBuilder;
import org.kie.builder.KnowledgeBuilderFactoryService;
import org.kie.builder.ResourceType;
import org.kie.builder.help.KnowledgeBuilderHelper;
import org.kie.io.ResourceFactory;
import org.kie.runtime.ExecutionResults;
import org.kie.runtime.StatefulKnowledgeSession;
import org.kie.runtime.rule.FactHandle;

import static org.junit.Assert.*;

public class CamelEndpointWithJaxWrapperCollectionTest extends DroolsCamelTestSupport {

    private String      handle;
    private JAXBContext jaxbContext;
    private RouteBuilder routeBuilder;

    @Test
    public void testWorkingSetGlobalTestSessionSetAndGetGlobal() throws Exception {

        BatchExecutionCommandImpl cmd = new BatchExecutionCommandImpl();
        cmd.setLookup( "ksession1" );

        SetGlobalCommand setGlobal = new SetGlobalCommand( "list",
                                                           new WrappedList() );
        setGlobal.setOutIdentifier( "list" );

        cmd.getCommands().add( setGlobal );
        cmd.getCommands().add( new InsertObjectCommand( new Person( "baunax" ) ) );
        cmd.getCommands().add( new FireAllRulesCommand() );
        cmd.getCommands().add( new GetGlobalCommand( "list" ) );

        Marshaller marshaller = getJaxbContext().createMarshaller();
        marshaller.setProperty( "jaxb.formatted.output",
                                true );
        StringWriter xml = new StringWriter();
        marshaller.marshal( cmd,
                            xml );

        System.out.println( xml.toString() );

        byte[] response = (byte[]) template.requestBody( "direct:test-with-session",
                                                         xml.toString() );
        assertNotNull( response );
        System.out.println( "response:\n" + new String( response ) );
        Unmarshaller unmarshaller = getJaxbContext().createUnmarshaller();
        ExecutionResults res = (ExecutionResults) unmarshaller.unmarshal( new ByteArrayInputStream( response ) );
        WrappedList resp = (WrappedList) res.getValue( "list" );
        assertNotNull( resp );

        assertEquals( resp.size(),
                      2 );
        assertEquals( "baunax",
                      resp.get( 0 ).getName() );
        assertEquals( "Hadrian",
                      resp.get( 1 ).getName() );

    }

    @Override
    protected void configureDroolsContext(Context jndiContext) {
        Person me = new Person();
        me.setName( "Hadrian" );

        String rule = "";
        rule += "package org.kie.pipeline.camel \n";
        rule += "import org.kie.pipeline.camel.Person\n";
        rule += "import org.kie.pipeline.camel.WrappedList\n";
        rule += "global WrappedList list\n";
        rule += "rule rule1 \n";
        rule += "  when \n";
        rule += "    $p : Person() \n";
        rule += " \n";
        rule += "  then \n";
        rule += "    System.out.println(\"executed\"); \n";
        rule += "    list.add($p); \n";
        rule += "end\n";

        StatefulKnowledgeSession ksession = registerKnowledgeRuntime( "ksession1",
                                                                      rule );
        InsertObjectCommand cmd = new InsertObjectCommand( me );
        cmd.setOutIdentifier( "camel-rider" );
        cmd.setReturnObject( false );
        BatchExecutionCommandImpl script = new BatchExecutionCommandImpl( Arrays.asList( new GenericCommand< ? >[]{cmd} ) );

        ExecutionResults results = ksession.execute( script );
        handle = ((FactHandle) results.getFactHandle( "camel-rider" )).toExternalForm();
    }

    @Override
    protected StatefulKnowledgeSession registerKnowledgeRuntime(String identifier,
                                                                String rule) {
        KnowledgeBuilder kbuilder = node.get( KnowledgeBuilderFactoryService.class ).newKnowledgeBuilder();

        Options xjcOpts = new Options();
        xjcOpts.setSchemaLanguage( Language.XMLSCHEMA );

        try {
            KnowledgeBuilderHelper.addXsdModel( ResourceFactory.newClassPathResource( "person.xsd",
                                                                                      getClass() ),
                                                kbuilder,
                                                xjcOpts,
                                                "xsd" );
        } catch ( IOException e ) {
            LOG.error( "Errors while adding xsd model. ",
                       kbuilder.getErrors() );
        }

        assertFalse( kbuilder.hasErrors() );

        if ( rule != null && rule.length() > 0 ) {
            kbuilder.add( ResourceFactory.newByteArrayResource( rule.getBytes() ),
                          ResourceType.DRL );

            if ( kbuilder.hasErrors() ) {
                LOG.info( "Errors while adding rule. ",
                          kbuilder.getErrors() );
            }
        }

        assertFalse( kbuilder.hasErrors() );

        KnowledgeBase kbase = node.get( KnowledgeBaseFactoryService.class ).newKnowledgeBase();
        kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );

        StatefulKnowledgeSession session = kbase.newStatefulKnowledgeSession();
        node.set( identifier,
                  session );
        return session;
    }

    public JAXBContext getJaxbContext() {
        if ( this.jaxbContext == null ) {
            JaxbDataFormat def = new JaxbDataFormat();
            def.setPrettyPrint( true );
            // TODO does not work: def.setContextPath( "org.kie.camel.testdomain:org.kie.pipeline.camel" );
            def.setContextPath( "org.kie.pipeline.camel" );

            // create a jaxbContext for the test to use outside of Camel.
            StatefulKnowledgeSession ksession1 = (StatefulKnowledgeSession) node.get( "ksession1",
                                                                                      CommandExecutor.class );
            KnowledgeBase kbase = ksession1.getKnowledgeBase();
            ClassLoader originalCl = Thread.currentThread().getContextClassLoader();
            try {
                Thread.currentThread().setContextClassLoader( ((ReteooRuleBase) ((KnowledgeBaseImpl) kbase).getRuleBase()).getRootClassLoader() );
                def = DroolsPolicy.augmentJaxbDataFormatDefinition( def );

                org.apache.camel.converter.jaxb.JaxbDataFormat jaxbDataformat = (org.apache.camel.converter.jaxb.JaxbDataFormat) def.getDataFormat( this.context.getRoutes().get( 0 ).getRouteContext() );

                jaxbDataformat.setCamelContext(routeBuilder.getContext());
                try {
                    jaxbDataformat.start();
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
                jaxbContext = jaxbDataformat.getContext();
            } finally {
                Thread.currentThread().setContextClassLoader( originalCl );
            }
        }

        return jaxbContext;
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        routeBuilder = new RouteBuilder() {
            public void configure() throws Exception {
                JaxbDataFormat def = new JaxbDataFormat();
                def.setPrettyPrint(true);
                // TODO does not work: def.setContextPath( "org.kie.camel.testdomain:org.kie.pipeline.camel" );
                def.setContextPath("org.kie.pipeline.camel");

                from("direct:test-with-session").policy(new DroolsPolicy()).
                        unmarshal(def).to("drools:node/ksession1").marshal(def);
            }
        };
        return routeBuilder;
    }
}
