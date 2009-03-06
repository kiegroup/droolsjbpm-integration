package org.drools.runtime.pipeline.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.stream.StreamSource;

import junit.framework.TestCase;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.common.InternalFactHandle;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatelessKnowledgeSession;
import org.drools.runtime.help.BatchExecutionHelper;
import org.drools.runtime.pipeline.Action;
import org.drools.runtime.pipeline.Expression;
import org.drools.runtime.pipeline.Join;
import org.drools.runtime.pipeline.KnowledgeRuntimeCommand;
import org.drools.runtime.pipeline.Pipeline;
import org.drools.runtime.pipeline.PipelineFactory;
import org.drools.runtime.pipeline.Receiver;
import org.drools.runtime.pipeline.ResultHandler;
import org.drools.runtime.pipeline.Splitter;
import org.drools.runtime.pipeline.Transformer;
import org.drools.runtime.rule.FactHandle;
import org.milyn.Smooks;
import org.milyn.io.StreamUtils;

public class SmookStatelessSessionTest extends TestCase {

    public void testSmooksDirectRoot() throws Exception {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        kbuilder.add( ResourceFactory.newClassPathResource( "test_SmooksDirectRoot.drl",
                                                            SmookStatefulSessionTest.class ),
                      ResourceType.DRL );

        assertFalse( kbuilder.hasErrors() );

        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );

        StatelessKnowledgeSession ksession = kbase.newStatelessKnowledgeSession();
        List list = new ArrayList();
        ksession.setGlobal( "list",
                            list );

        Action executeResultHandler = PipelineFactory.newExecuteResultHandler();
        
        Action assignResult = PipelineFactory.newAssignObjectAsResult();
        assignResult.setReceiver( executeResultHandler );

        KnowledgeRuntimeCommand batchExecution = PipelineFactory.newBatchExecutor();
        batchExecution.setReceiver( assignResult );
        
        KnowledgeRuntimeCommand insertStage = PipelineFactory.newInsertObjectCommand();
        insertStage.setReceiver( batchExecution );

        // Instantiate Smooks with the config...
        Smooks smooks = new Smooks( getClass().getResourceAsStream( "smooks-config.xml" ) );

        Transformer transformer = PipelineFactory.newSmooksFromSourceTransformer( smooks,
                                                                                  "orderItem" );
        transformer.setReceiver( insertStage );

        Pipeline pipeline = PipelineFactory.newStatelessKnowledgeSessionPipeline( ksession );
        pipeline.setReceiver( transformer );

        ResultHandlerImpl resultHandler = new ResultHandlerImpl();
        pipeline.insert( new StreamSource( getClass().getResourceAsStream( "SmooksDirectRoot.xml" ) ),
                         resultHandler );

        assertEquals( 1,
                      list.size() );

        assertEquals( "example.OrderItem",
                      list.get( 0 ).getClass().getName() );
    }

    public void testSmooksNestedIterable() throws Exception {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        kbuilder.add( ResourceFactory.newClassPathResource( "test_SmooksNestedIterable.drl",
                                                            SmookStatefulSessionTest.class ),
                      ResourceType.DRL );

        assertFalse( kbuilder.hasErrors() );

        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );

        StatelessKnowledgeSession ksession = kbase.newStatelessKnowledgeSession();
        List list = new ArrayList();
        ksession.setGlobal( "list",
                            list );

        Action executeResultHandler = PipelineFactory.newExecuteResultHandler();
        
        Action assignResult = PipelineFactory.newAssignObjectAsResult();
        assignResult.setReceiver( executeResultHandler );

        KnowledgeRuntimeCommand batchExecution = PipelineFactory.newBatchExecutor();
        batchExecution.setReceiver( assignResult );
        
        KnowledgeRuntimeCommand insertElementsStage = PipelineFactory.newInsertElementsCommand();
        insertElementsStage.setReceiver( batchExecution );
        
        Expression mvelExpression = PipelineFactory.newMvelExpression( "this.children" );
        mvelExpression.setReceiver( insertElementsStage );

        // Instantiate Smooks with the config...
        Smooks smooks = new Smooks( getClass().getResourceAsStream( "smooks-config.xml" ) );

        Transformer transformer = PipelineFactory.newSmooksFromSourceTransformer( smooks,
                                                                                  "root" );
        transformer.setReceiver( mvelExpression );

        Pipeline pipeline = PipelineFactory.newStatelessKnowledgeSessionPipeline( ksession );
        pipeline.setReceiver( transformer );

        ResultHandlerImpl resultHandler = new ResultHandlerImpl();
        pipeline.insert( new StreamSource( getClass().getResourceAsStream( "SmooksNestedIterable.xml" ) ),
                         resultHandler );

        assertEquals( 2,
                      list.size() );

        assertEquals( "example.OrderItem",
                      list.get( 0 ).getClass().getName() );
        assertEquals( "example.OrderItem",
                      list.get( 1 ).getClass().getName() );

        assertNotSame( list.get( 0 ),
                       list.get( 1 ) );
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
