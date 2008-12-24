package org.drools.runtime.pipeline.impl;

import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.stream.StreamSource;

import junit.framework.TestCase;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatelessKnowledgeSession;
import org.drools.runtime.dataloader.StatelessKnowledgeSessionDataLoader;
import org.drools.runtime.dataloader.impl.StatelessKnowledgeSessionDataLoaderImpl;
import org.drools.runtime.pipeline.Expression;
import org.drools.runtime.pipeline.PipelineFactory;
import org.drools.runtime.pipeline.Splitter;
import org.drools.runtime.pipeline.Transformer;

import com.thoughtworks.xstream.XStream;

public class DroolsXStreamStatelessSessionTest extends TestCase {

    public void testSmooksDirectRoot() throws Exception {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        kbuilder.add( ResourceFactory.newClassPathResource( "test_XStreamDirectRoot.drl",
                                                            DroolsXStreamStatefulSessionTest.class ),
                      ResourceType.DRL );

        assertFalse( kbuilder.hasErrors() );

        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );

        StatelessKnowledgeSession ksession = kbase.newStatelessKnowledgeSession();
        List list = new ArrayList();
        ksession.setGlobal( "list",
                            list );

        XStream xstream = new XStream();
        Transformer transformer = PipelineFactory.newXStreamTransformer( xstream );
        transformer.addReceiver( PipelineFactory.newStatelessKnowledgeSessionReceiverAdapter() );

        StatelessKnowledgeSessionDataLoader dataLoader = new StatelessKnowledgeSessionDataLoaderImpl( ksession,
                                                                                                      transformer );
        dataLoader.executeObject( getClass().getResourceAsStream( "XStreamDirectRoot.xml" ) );

        assertEquals( 1,
                      list.size() );

        assertEquals( "example.OrderItem",
                      list.get( 0 ).getClass().getName() );
    }

    public void testSmooksNestedIterable() throws Exception {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        kbuilder.add( ResourceFactory.newClassPathResource( "test_XStreamNestedIterable.drl",
                                                            DroolsXStreamStatefulSessionTest.class ),
                      ResourceType.DRL );

        assertFalse( kbuilder.hasErrors() );

        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );

        StatelessKnowledgeSession ksession = kbase.newStatelessKnowledgeSession();
        List list = new ArrayList();
        ksession.setGlobal( "list",
                            list );

        XStream xstream = new XStream();
        Transformer transformer = PipelineFactory.newXStreamTransformer( xstream );
        Expression expression = PipelineFactory.newMvelExpression( "this" );
        transformer.addReceiver( expression );
        Splitter splitter = PipelineFactory.newIterateSplitter();
        expression.addReceiver( splitter );
        splitter.addReceiver( PipelineFactory.newStatelessKnowledgeSessionReceiverAdapter() );

        StatelessKnowledgeSessionDataLoader dataLoader = new StatelessKnowledgeSessionDataLoaderImpl( ksession,
                                                                                                      transformer );
        dataLoader.executeIterable( getClass().getResourceAsStream( "XStreamNestedIterable.xml" ) );

        assertEquals( 2,
                      list.size() );

        assertEquals( "example.OrderItem",
                      list.get( 0 ).getClass().getName() );
        assertEquals( "example.OrderItem",
                      list.get( 1 ).getClass().getName() );

        assertNotSame( list.get( 0 ),
                       list.get( 1 ) );
    }

}
