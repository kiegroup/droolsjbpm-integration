package org.drools.runtime.pipeline.impl;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatelessKnowledgeSession;
import org.drools.runtime.pipeline.Action;
import org.drools.runtime.pipeline.Expression;
import org.drools.runtime.pipeline.KnowledgeRuntimeCommand;
import org.drools.runtime.pipeline.Pipeline;
import org.drools.runtime.pipeline.PipelineFactory;
import org.drools.runtime.pipeline.ResultHandler;
import org.drools.runtime.pipeline.Transformer;

import com.thoughtworks.xstream.XStream;

public class XStreamStatelessSessionTest extends TestCase {

    public void testXstreamDirectRoot() throws Exception {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        kbuilder.add( ResourceFactory.newClassPathResource( "test_XStreamDirectRoot.drl",
                                                            XStreamStatefulSessionTest.class ),
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

        XStream xstream = new XStream();
        Transformer transformer = PipelineFactory.newXStreamFromXmlTransformer( xstream );
        transformer.setReceiver( insertStage );

        Pipeline pipeline = PipelineFactory.newStatelessKnowledgeSessionPipeline( ksession );
        pipeline.setReceiver( transformer );

        ResultHandlerImpl resultHandler = new ResultHandlerImpl();
        pipeline.insert( getClass().getResourceAsStream( "XStreamDirectRoot.xml" ),
                         resultHandler );

        assertEquals( 1,
                      list.size() );

        assertEquals( "example.OrderItem",
                      list.get( 0 ).getClass().getName() );
    }

    public void testXstreamNestedIterable() throws Exception {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        kbuilder.add( ResourceFactory.newClassPathResource( "test_XStreamNestedIterable.drl",
                                                            XStreamStatefulSessionTest.class ),
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

        XStream xstream = new XStream();
        Transformer transformer = PipelineFactory.newXStreamFromXmlTransformer( xstream );
        transformer.setReceiver( insertElementsStage );

        Pipeline pipeline = PipelineFactory.newStatelessKnowledgeSessionPipeline( ksession );
        pipeline.setReceiver( transformer );

        ResultHandlerImpl resultHandler = new ResultHandlerImpl();
        pipeline.insert( getClass().getResourceAsStream( "XStreamNestedIterable.xml" ),
                         resultHandler );

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
