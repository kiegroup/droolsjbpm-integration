package org.drools.runtime.pipeline.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.transform.stream.StreamSource;

import junit.framework.TestCase;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.pipeline.Action;
import org.drools.runtime.pipeline.Expression;
import org.drools.runtime.pipeline.KnowledgeRuntimeCommand;
import org.drools.runtime.pipeline.Pipeline;
import org.drools.runtime.pipeline.PipelineFactory;
import org.drools.runtime.pipeline.ResultHandler;
import org.drools.runtime.pipeline.Splitter;
import org.drools.runtime.pipeline.Transformer;
import org.drools.runtime.rule.FactHandle;
import org.milyn.Smooks;
import org.milyn.io.StreamUtils;

public class DroolsSmookStatefulSessionTest extends TestCase {

    public void testDirectRoot() throws Exception {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        kbuilder.add( ResourceFactory.newClassPathResource( "test_SmooksDirectRoot.drl",
                                                            DroolsSmookStatefulSessionTest.class ),
                      ResourceType.DRL );

        assertFalse( kbuilder.hasErrors() );

        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );

        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
        List list = new ArrayList();
        ksession.setGlobal( "list",
                            list );

        Action executeResultHandler = PipelineFactory.newExecuteResultHandler();

        KnowledgeRuntimeCommand insertStage = PipelineFactory.newStatefulKnowledgeSessionInsert();
        insertStage.setReceiver( executeResultHandler );

        // Instantiate Smooks with the config...
        Smooks smooks = new Smooks( getClass().getResourceAsStream( "smooks-config.xml" ) );

        Transformer transformer = PipelineFactory.newSmooksTransformer( smooks,
                                                                        "orderItem" );
        transformer.setReceiver( insertStage );

        Pipeline pipeline = PipelineFactory.newStatefulKnowledgeSessionPipeline( ksession );
        pipeline.setReceiver( transformer );

        ResultHandlerImpl resultHandler = new ResultHandlerImpl();
        pipeline.insert( new StreamSource( getClass().getResourceAsStream( "SmooksDirectRoot.xml" ) ),
                         resultHandler );
        ksession.fireAllRules();

        Map<FactHandle, Object> handles = (Map<FactHandle, Object>) resultHandler.getObject();

        assertEquals( 1,
                      handles.size() );
        assertEquals( 1,
                      list.size() );

        assertEquals( "example.OrderItem",
                      list.get( 0 ).getClass().getName() );
    }

    public void testNestedIterable() throws Exception {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        kbuilder.add( ResourceFactory.newClassPathResource( "test_SmooksNestedIterable.drl",
                                                            DroolsSmookStatefulSessionTest.class ),
                      ResourceType.DRL );

        assertFalse( kbuilder.hasErrors() );

        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );

        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
        List list = new ArrayList();
        ksession.setGlobal( "list",
                            list );

        Action executeResultHandler = PipelineFactory.newExecuteResultHandler();

        KnowledgeRuntimeCommand insertStage = PipelineFactory.newStatefulKnowledgeSessionInsert();
        insertStage.setReceiver( executeResultHandler );

        Splitter splitter = PipelineFactory.newIterateSplitter();
        splitter.setReceiver( insertStage );

        Expression expression = PipelineFactory.newMvelExpression( "children" );
        expression.setReceiver( splitter );

        // Instantiate Smooks with the config...
        Smooks smooks = new Smooks( getClass().getResourceAsStream( "smooks-config.xml" ) );

        Transformer transformer = PipelineFactory.newSmooksTransformer( smooks,
                                                                        "root" );
        transformer.setReceiver( expression );

        Pipeline pipeline = PipelineFactory.newStatefulKnowledgeSessionPipeline( ksession );
        pipeline.setReceiver( transformer );

        ResultHandlerImpl resultHandler = new ResultHandlerImpl();
        pipeline.insert( new StreamSource( getClass().getResourceAsStream( "SmooksNestedIterable.xml" ) ),
                         resultHandler );

        Map<FactHandle, Object> handles = (Map<FactHandle, Object>) resultHandler.getObject();
        ksession.fireAllRules();

        assertEquals( 2,
                      handles.size() );
        assertEquals( 2,
                      list.size() );

        assertEquals( "example.OrderItem",
                      list.get( 0 ).getClass().getName() );
        assertEquals( "example.OrderItem",
                      list.get( 1 ).getClass().getName() );

        assertNotSame( list.get( 0 ),
                       list.get( 1 ) );
    }

    private static byte[] readInputMessage(InputStream stream) {
        try {
            return StreamUtils.readStream( stream );
        } catch ( IOException e ) {
            e.printStackTrace();
            return "<no-message/>".getBytes();
        }
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
