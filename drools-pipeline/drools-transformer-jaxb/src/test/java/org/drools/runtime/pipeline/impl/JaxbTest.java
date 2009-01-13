package org.drools.runtime.pipeline.impl;

/*
 * Copyright 2005 JBoss Inc
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import junit.framework.TestCase;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.builder.help.KnowledgeBuilderHelper;
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
import org.drools.runtime.pipeline.impl.StatefulKnowledgeSessionPipelineTest.ResultHandlerImpl;
import org.drools.runtime.rule.FactHandle;

import com.sun.tools.xjc.Language;
import com.sun.tools.xjc.Options;

public class JaxbTest extends TestCase {

    //    public void testModelLoad() throws Exception {
    //        Options xjcOpts = new Options();
    //        xjcOpts.setSchemaLanguage( Language.XMLSCHEMA );
    //        PackageBuilder pkgBuilder = new PackageBuilder();
    //
    //        InputStream stream = getClass().getResourceAsStream( "test.xsd" );
    //        String[] classNames = DroolsJaxbHelper.addModel( new InputStreamReader( stream ),
    //                                                         pkgBuilder,
    //                                                         xjcOpts,
    //                                                         "xsd" );
    //
    //        assertFalse( pkgBuilder.hasErrors() );
    //
    //        RuleBase rb = RuleBaseFactory.newRuleBase();
    //        rb.addPackage( pkgBuilder.getPackage() );
    //
    //        JAXBContext jaxbCtx = DroolsJaxbHelper.newInstance( classNames,
    //                                                            rb );
    //        Unmarshaller unmarshaller = jaxbCtx.createUnmarshaller();
    //        JAXBElement elm = ( JAXBElement ) unmarshaller.unmarshal( getClass().getResourceAsStream( "data.xml" ) );
    //       
    //        assertEquals( "com.oracle.sample3.USAddress",
    //                      elm.getValue().getClass().getName() );        
    //    }

    public void testDirectRoot() throws Exception {
        Options xjcOpts = new Options();
        xjcOpts.setSchemaLanguage( Language.XMLSCHEMA );
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        String[] classNames = KnowledgeBuilderHelper.addXsdModel( ResourceFactory.newClassPathResource( "order.xsd",
                                                                                                        getClass() ),
                                                                  kbuilder,
                                                                  xjcOpts,
                                                                  "xsd" );

        assertFalse( kbuilder.hasErrors() );

        kbuilder.add( ResourceFactory.newClassPathResource( "test_Jaxb.drl",
                                                            getClass() ),
                      ResourceType.DRL );

        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );

        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
        List list1 = new ArrayList();
        ksession.setGlobal( "list1",
                            list1 );
        
        Action executeResultHandler = PipelineFactory.newExecuteResultHandler();

        KnowledgeRuntimeCommand insertStage = PipelineFactory.newStatefulKnowledgeSessionInsert();
        insertStage.setReceiver( executeResultHandler );
        
        JAXBContext jaxbCtx = KnowledgeBuilderHelper.newJAXBContext( classNames,
                                                                     kbase );
        Unmarshaller unmarshaller = jaxbCtx.createUnmarshaller();
        Transformer transformer = PipelineFactory.newJaxbFromXmlTransformer( unmarshaller );
        transformer.setReceiver(insertStage );
        
        Pipeline pipeline = PipelineFactory.newStatefulKnowledgeSessionPipeline( ksession );
        pipeline.setReceiver( transformer );        
        
        ResultHandlerImpl resultHandler = new ResultHandlerImpl();
        pipeline.insert( new StreamSource( getClass().getResourceAsStream( "order.xml" ) ), resultHandler );
        ksession.fireAllRules();
        
        Map<FactHandle, Object> handles = ( Map<FactHandle, Object>  ) resultHandler.getObject();

        ksession.fireAllRules();

        assertEquals( 1,
                      handles.size() );
        assertEquals( 1,
                      list1.size() );

        assertEquals( "org.drools.model.order.Order",
                      list1.get( 0 ).getClass().getName() );
    }

    public void testNestedIterable() throws Exception {
        Options xjcOpts = new Options();
        xjcOpts.setSchemaLanguage( Language.XMLSCHEMA );
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        String[] classNames = KnowledgeBuilderHelper.addXsdModel( ResourceFactory.newClassPathResource( "order.xsd",
                                                                                                        getClass() ),
                                                                  kbuilder,
                                                                  xjcOpts,
                                                                  "xsd" );

        assertFalse( kbuilder.hasErrors() );

        kbuilder.add( ResourceFactory.newClassPathResource( "test_Jaxb.drl",
                                                            getClass() ),
                      ResourceType.DRL );

        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );

        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();
        List list1 = new ArrayList();
        List list2 = new ArrayList();
        ksession.setGlobal( "list1",
                            list1 );
        ksession.setGlobal( "list2",
                            list2 );
        
        
        Action executeResultHandler = PipelineFactory.newExecuteResultHandler();

        KnowledgeRuntimeCommand insertStage = PipelineFactory.newStatefulKnowledgeSessionInsert();
        insertStage.setReceiver( executeResultHandler );
        
        Splitter splitter = PipelineFactory.newIterateSplitter();
        splitter.setReceiver( insertStage );
        
        Expression expression = PipelineFactory.newMvelExpression( "this.orderItem" );
        expression.setReceiver( splitter );
        
        JAXBContext jaxbCtx = KnowledgeBuilderHelper.newJAXBContext( classNames,
                                                                     kbase );
        Unmarshaller unmarshaller = jaxbCtx.createUnmarshaller();
        Transformer transformer = PipelineFactory.newJaxbFromXmlTransformer( unmarshaller );        
        transformer.setReceiver( expression );
        
        Pipeline pipeline = PipelineFactory.newStatefulKnowledgeSessionPipeline( ksession );
        pipeline.setReceiver( transformer );
        
        ResultHandlerImpl resultHandler = new ResultHandlerImpl();
        pipeline.insert( new StreamSource( getClass().getResourceAsStream( "order.xml" ) ), resultHandler );

        Map<FactHandle, Object> handles = ( Map<FactHandle, Object>  ) resultHandler.getObject();
        ksession.fireAllRules();

        assertEquals( 2,
                      handles.size() );
        assertEquals( 1,
                      list1.size() );
        assertEquals( 1,
                      list2.size() );

        assertEquals( "org.drools.model.order.Order$OrderItem",
                      list1.get( 0 ).getClass().getName() );

        assertEquals( "org.drools.model.order.Order$OrderItem",
                      list2.get( 0 ).getClass().getName() );

        assertNotSame( list1.get( 0 ),
                       list2.get( 0 ) );
    }
    
    public static class ResultHandlerImpl implements ResultHandler {
        Object object;
        public void handleResult(Object object) {
           this.object = object;             
        }
        public Object getObject() {
            return this.object;
        }       
    } 
}
