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
import org.drools.common.InternalRuleBase;
import org.drools.impl.KnowledgeBaseImpl;
import org.drools.impl.StatefulKnowledgeSessionImpl;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.dataloader.DataLoaderFactory;
import org.drools.runtime.dataloader.StatefulKnowledgeSessionDataLoader;
import org.drools.runtime.dataloader.impl.StatefulKnowledgeSessionDataLoaderImpl;
import org.drools.runtime.dataloader.impl.EntryPointReceiverAdapter;
import org.drools.runtime.pipeline.Expression;
import org.drools.runtime.pipeline.PipelineFactory;
import org.drools.runtime.pipeline.Splitter;
import org.drools.runtime.pipeline.Transformer;
import org.drools.runtime.pipeline.impl.IterateSplitter;
import org.drools.runtime.pipeline.impl.MvelExpression;
import org.drools.runtime.rule.FactHandle;

import com.thoughtworks.xstream.XStream;

public class DroolsXStreamStatefulSessionTest extends TestCase {

    public void testDirectRoot() throws Exception {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        kbuilder.add( ResourceFactory.newClassPathResource( "test_XStreamDirectRoot.drl",
                                                            DroolsXStreamStatefulSessionTest.class ),
                      ResourceType.DRL );

        assertFalse( kbuilder.hasErrors() );

        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );

        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
        List list = new ArrayList();
        ksession.setGlobal( "list",
                            list );

        XStream xstream = new XStream();
        Transformer transformer = PipelineFactory.newXStreamTransformer( xstream );
        transformer.addReceiver( PipelineFactory.newEntryPointReceiverAdapter() );

        StatefulKnowledgeSessionDataLoader dataLoader = DataLoaderFactory.newStatefulKnowledgeSessionDataLoader( ksession,
                                                                                                                 transformer );
        Map<FactHandle, Object> handles = dataLoader.insert( getClass().getResourceAsStream( "XStreamDirectRoot.xml" ) );
        ksession.fireAllRules();

        assertEquals( 1,
                      handles.size() );
        assertEquals( 1,
                      list.size() );

        assertEquals( "example.OrderItem",
                      list.get( 0 ).getClass().getName() );
    }

    public void testNestedIterable() throws Exception {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        kbuilder.add( ResourceFactory.newClassPathResource( "test_XStreamNestedIterable.drl",
                                                            DroolsXStreamStatefulSessionTest.class ),
                      ResourceType.DRL );

        assertFalse( kbuilder.hasErrors() );

        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );

        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
        List list = new ArrayList();
        ksession.setGlobal( "list",
                            list );

        XStream xstream = new XStream();
        Transformer transformer = PipelineFactory.newXStreamTransformer( xstream );
        Expression expression = PipelineFactory.newMvelExpression( "this" );
        transformer.addReceiver( expression );
        Splitter splitter = PipelineFactory.newIterateSplitter();
        expression.addReceiver( splitter );
        splitter.addReceiver( PipelineFactory.newEntryPointReceiverAdapter() );

        StatefulKnowledgeSessionDataLoader dataLoader = DataLoaderFactory.newStatefulKnowledgeSessionDataLoader( ksession,
                                                                                                                 transformer );

        Map<FactHandle, Object> handles = dataLoader.insert( getClass().getResourceAsStream( "XStreamNestedIterable.xml" ) );
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

}
