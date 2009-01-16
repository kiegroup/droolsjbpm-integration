package org.drools.runtime.pipeline.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.transform.stream.StreamSource;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.pipeline.Action;
import org.drools.runtime.pipeline.KnowledgeRuntimeCommand;
import org.drools.runtime.pipeline.Pipeline;
import org.drools.runtime.pipeline.PipelineFactory;
import org.drools.runtime.pipeline.ResultHandler;
import org.drools.runtime.pipeline.Transformer;
import org.drools.runtime.rule.FactHandle;
import org.milyn.Smooks;

import com.thoughtworks.xstream.XStream;

import junit.framework.TestCase;

public class SmooksGlobalTest extends TestCase {
    public void testGlobal() throws Exception {
        String xml = "<org.drools.runtime.pipeline.impl.Root><children><example.OrderItem><price>8.9</price><quantity>2</quantity><productId>111</productId></example.OrderItem><example.OrderItem><price>5.2</price><quantity>7</quantity><productId>222</productId></example.OrderItem></children></org.drools.runtime.pipeline.impl.Root>";

        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        kbuilder.add( ResourceFactory.newClassPathResource( "test_SmooksNestedIterable.drl",
                                                            SmooksGlobalTest.class ),
                      ResourceType.DRL );

        assertFalse( kbuilder.hasErrors() );

        assertFalse( kbuilder.hasErrors() );

        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );

        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();

        KnowledgeRuntimeCommand setGlobalStage = PipelineFactory.newStatefulKnowledgeSessionSetGlobal( "root" );

        Smooks smooks = new Smooks( getClass().getResourceAsStream( "smooks-config.xml" ) );

        Transformer transformer = PipelineFactory.newSmooksFromSourceTransformer( smooks,
                                                                                  "root" );
        transformer.setReceiver( setGlobalStage );

        Pipeline pipeline = PipelineFactory.newStatefulKnowledgeSessionPipeline( ksession );
        pipeline.setReceiver( transformer );

        ResultHandlerImpl resultHandler = new ResultHandlerImpl();
        pipeline.insert( new StreamSource( getClass().getResourceAsStream( "SmooksNestedIterable.xml" ) ),
                         resultHandler );

        // now round trip that global

        Action executeResult = PipelineFactory.newExecuteResultHandler();

        Action assignAsResult = PipelineFactory.newAssignObjectAsResult();
        assignAsResult.setReceiver( executeResult );

        transformer = PipelineFactory.newSmooksToSourceTransformer( smooks );
        transformer.setReceiver( assignAsResult );

        pipeline = PipelineFactory.newStatefulKnowledgeSessionPipeline( ksession );
        pipeline.setReceiver( transformer );

        resultHandler = new ResultHandlerImpl();
        pipeline.insert( ksession.getGlobal( "root" ),
                         resultHandler );

        assertEquals( xml,
                      resultHandler.getObject() );
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
