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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.transform.stream.StreamSource;

import org.custommonkey.xmlunit.Diff;
import org.drools.runtime.pipeline.Action;
import org.drools.runtime.pipeline.KnowledgeRuntimeCommand;
import org.drools.runtime.pipeline.Pipeline;
import org.drools.runtime.pipeline.PipelineFactory;
import org.drools.runtime.pipeline.ResultHandler;
import org.drools.runtime.pipeline.Transformer;
import org.junit.Ignore;
import org.milyn.Smooks;

import com.thoughtworks.xstream.XStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.KnowledgeBase;
import org.kie.KnowledgeBaseFactory;
import org.kie.builder.KnowledgeBuilder;
import org.kie.builder.KnowledgeBuilderFactory;
import org.kie.builder.ResourceType;
import org.kie.io.ResourceFactory;
import org.kie.runtime.StatefulKnowledgeSession;
import org.kie.runtime.rule.FactHandle;

import static org.junit.Assert.*;

public class SmooksGlobalTest {

    @Test @Ignore
    public void testGlobal() throws Exception {
        String xml = "<org.kie.runtime.pipeline.impl.Root><children><example.OrderItem><price>8.9</price><quantity>2</quantity><productId>111</productId></example.OrderItem><example.OrderItem><price>5.2</price><quantity>7</quantity><productId>222</productId></example.OrderItem></children></org.kie.runtime.pipeline.impl.Root>";

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
        
        assertTrue( new Diff( xml, (String) resultHandler.getObject() ).similar() );
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
