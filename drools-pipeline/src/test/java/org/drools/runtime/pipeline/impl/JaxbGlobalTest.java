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

package org.drools.runtime.pipeline.impl;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.JaxbConfiguration;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.builder.help.KnowledgeBuilderHelper;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.pipeline.Action;
import org.drools.runtime.pipeline.KnowledgeRuntimeCommand;
import org.drools.runtime.pipeline.Pipeline;
import org.drools.runtime.pipeline.PipelineFactory;
import org.drools.runtime.pipeline.ResultHandler;
import org.drools.runtime.pipeline.Transformer;
import org.drools.runtime.rule.FactHandle;
import org.drools.core.util.StringUtils;

import com.sun.tools.xjc.Language;
import com.sun.tools.xjc.Options;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class JaxbGlobalTest {
    @Test
    public void testGlobal() throws Exception {
        Options xjcOpts = new Options();
        xjcOpts.setSchemaLanguage( Language.XMLSCHEMA );
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        JaxbConfiguration jaxbConf = KnowledgeBuilderFactory.newJaxbConfiguration( xjcOpts, "xsd" );
        
        kbuilder.add( ResourceFactory.newClassPathResource( "order.xsd",
                                                            getClass() ), ResourceType.XSD,
                                                            jaxbConf );

        assertFalse( kbuilder.hasErrors() );

        kbuilder.add( ResourceFactory.newClassPathResource( "test_Jaxb.drl",
                                                            getClass() ),
                      ResourceType.DRL );

        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );

        assertFalse( kbuilder.hasErrors() );

        assertFalse( kbuilder.hasErrors() );

        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();

        KnowledgeRuntimeCommand setGlobalStage = PipelineFactory.newStatefulKnowledgeSessionSetGlobal( "order" );

        JAXBContext jaxbCtx = KnowledgeBuilderHelper.newJAXBContext( jaxbConf.getClasses().toArray( new String[jaxbConf.getClasses().size()] ),
                                                                     kbase );
        
        Unmarshaller unmarshaller = jaxbCtx.createUnmarshaller();
        Transformer transformer = PipelineFactory.newJaxbFromXmlTransformer( unmarshaller );
        transformer.setReceiver(setGlobalStage );

        Pipeline pipeline = PipelineFactory.newStatefulKnowledgeSessionPipeline( ksession );
        pipeline.setReceiver( transformer );

        String xml = StringUtils.readFileAsString( new InputStreamReader( getClass().getResourceAsStream( "order.xml" ) ) );
        
        ResultHandlerImpl resultHandler = new ResultHandlerImpl();
        pipeline.insert( xml,
                         resultHandler );

        // now round trip that global
        Action executeResult = PipelineFactory.newExecuteResultHandler();
        
        Action assignAsResult = PipelineFactory.newAssignObjectAsResult();
        assignAsResult.setReceiver( executeResult );
        
        //transformer = PipelineFactory.newXStreamToXmlTransformer( xstream );
        Marshaller marshaller = jaxbCtx.createMarshaller();
        transformer = PipelineFactory.newJaxbToXmlTransformer( marshaller );
        transformer.setReceiver( assignAsResult );
        
        KnowledgeRuntimeCommand getGlobalStage = PipelineFactory.newStatefulKnowledgeSessionGetGlobal( );
        getGlobalStage.setReceiver( transformer );

        pipeline = PipelineFactory.newStatefulKnowledgeSessionPipeline( ksession );
        pipeline.setReceiver( getGlobalStage );
        
        resultHandler = new ResultHandlerImpl();
        pipeline.insert( "order", resultHandler );
        
        assertEqualsIgnoreWhitespace( xml, (String) resultHandler.getObject() );
    }
    
    private static void assertEqualsIgnoreWhitespace(final String expected,
                                                     final String actual) {
               final String cleanExpected = expected.replaceAll( "\\s+",
                                                                 "" );
               final String cleanActual = actual.replaceAll( "\\s+",
                                                             "" );
               assertEquals( cleanExpected,
                             cleanActual );
           }
    
    public static class ResultHandlerImpl
    implements
    ResultHandler {
    Object object;

    public void handleResult(Object object) {
        this.object = object;
    }

    public Object getObject() {
        return this.object;
    }
}

}
