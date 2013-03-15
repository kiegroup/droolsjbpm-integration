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

import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.examples.RecursiveElementNameAndTextQualifier;
import org.drools.core.command.impl.GenericCommand;
import org.drools.core.command.runtime.BatchExecutionCommandImpl;
import org.drools.core.command.runtime.GetGlobalCommand;
import org.drools.core.command.runtime.SetGlobalCommand;
import org.drools.core.command.runtime.rule.InsertObjectCommand;
import org.drools.pipeline.camel.Person;
import org.drools.compiler.runtime.pipeline.impl.DroolsJaxbHelperProviderImpl;
import org.junit.Test;
import org.kie.KnowledgeBase;
import org.kie.KnowledgeBaseFactory;
import org.kie.command.Command;
import org.kie.command.CommandFactory;
import org.kie.runtime.ExecutionResults;
import org.kie.runtime.StatefulKnowledgeSession;
import org.kie.runtime.rule.FactHandle;

public class JaxbTest {

    @Test
    public void test1() throws Exception {
        JAXBContext jaxbContext = getJaxbContext();

        List<GenericCommand< ? >> cmds = new ArrayList<GenericCommand< ? >>();
        cmds.add( new InsertObjectCommand( new Person( "darth",
                                                       21 ),
                                           "p" ) );
        cmds.add( new GetGlobalCommand( "xxx" ) );
        cmds.add( new SetGlobalCommand( "yyy",
                                        new Person( "yoda",
                                                    21 ) ) );
        BatchExecutionCommandImpl batch = new BatchExecutionCommandImpl( cmds );

        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty( Marshaller.JAXB_FORMATTED_OUTPUT,
                                Boolean.TRUE );
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        marshaller.marshal( batch,
                            baos );

        System.out.println( baos );

        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        BatchExecutionCommandImpl batch2 = (BatchExecutionCommandImpl) unmarshaller.unmarshal( new ByteArrayInputStream( baos.toByteArray() ) );
        baos = new ByteArrayOutputStream();
        marshaller.marshal( batch2,
                            baos );
        System.out.println( baos );
    }

    private JAXBContext getJaxbContext() throws JAXBException {
        List<String> classesName = new ArrayList();
        //        classesName.add("org.kie.drools.AddressType");
        //        classesName.add("org.kie.drools.ObjectFactory");
        //        classesName.add("org.kie.drools.Person");
        classesName.add( "org.drools.pipeline.camel.Person" );

        //jaxbDataFormat = new JaxbDataFormat();
        //jaxbDataFormat.setContextPath( contextPath )
        Set<String> set = new HashSet<String>();
        for ( String clsName : DroolsJaxbHelperProviderImpl.JAXB_ANNOTATED_CMD ) {
            set.add( clsName.substring( 0,
                                        clsName.lastIndexOf( '.' ) ) );
        }

        for ( String clsName : classesName ) {
            set.add( clsName.substring( 0,
                                        clsName.lastIndexOf( '.' ) ) );
        }

        StringBuilder sb = new StringBuilder();
        for ( String pkgName : set ) {
            sb.append( pkgName );
            sb.append( ':' );
        }

        System.out.println( "context path: " + sb.toString() );
        //        jaxbDataFormat.setContextPath( sb.toString() );
        //        jaxbDataFormat.setPrettyPrint( true );
        return JAXBContext.newInstance( sb.toString() );
    }

    @Test
    public void testFactHandleMarshall() throws JAXBException {
        JAXBContext jaxbContext = getJaxbContext();

        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();

        FactHandle fh1 = ksession.insert( new Person( "darth", 105 ) );

        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
                Boolean.TRUE);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        marshaller.marshal( fh1,
                baos );

        assertXMLEqual("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n<fact-handle external-form=\"" + fh1.toExternalForm() + "\"/>", new String(baos.toByteArray()));

        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        FactHandle fh2 = ( FactHandle ) unmarshaller.unmarshal( new StringReader( baos.toString() ) );
        assertEquals( fh1, fh2);
    }

    @Test
    public void testExecutionResults() throws JAXBException {
        JAXBContext jaxbContext = getJaxbContext();

        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();

        List<Command> commands = new ArrayList<Command>();
        commands.add(CommandFactory.newInsert(new Person("darth", 105), "p"));
        commands.add(CommandFactory.newFireAllRules());

        ExecutionResults res1 = ksession.execute( CommandFactory.newBatchExecution(commands) );

        Marshaller marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT,
                Boolean.TRUE);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        marshaller.marshal( res1,
                baos );

        // note it's using xsi:type
        System.out.println(new String(baos.toByteArray()));

        Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
        ExecutionResults res2 = ( ExecutionResults ) unmarshaller.unmarshal( new StringReader( baos.toString() ) );
    }

    public void assertXMLEqual(String expectedXml,
                               String resultXml) {
        try {
            Diff diff = new Diff( expectedXml,
                    resultXml );
            diff.overrideElementQualifier( new RecursiveElementNameAndTextQualifier() );
            XMLAssert.assertXMLEqual(diff,
                    true);
        } catch ( Exception e ) {
            throw new RuntimeException( "XML Assertion failure",
                    e );
        }
    }
}
