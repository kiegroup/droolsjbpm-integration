/**
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

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.examples.RecursiveElementNameAndTextQualifier;
import org.drools.ChangeCollector;
import org.drools.Cheese;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.KnowledgeBaseFactoryService;
import org.drools.Person;
import org.drools.TestVariable;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.KnowledgeBuilderFactoryService;
import org.drools.builder.ResourceType;
import org.drools.command.runtime.rule.ModifyCommand;
import org.drools.common.InternalFactHandle;
import org.drools.common.InternalRuleBase;
import org.drools.definition.KnowledgePackage;
import org.drools.grid.GridNode;
import org.drools.grid.impl.GridImpl;
import org.drools.grid.service.directory.WhitePages;
import org.drools.grid.service.directory.impl.WhitePagesImpl;
import org.drools.impl.StatefulKnowledgeSessionImpl;
import org.drools.io.Resource;
import org.drools.io.ResourceFactory;
import org.drools.runtime.ExecutionResults;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.StatelessKnowledgeSession;
import org.drools.runtime.help.BatchExecutionHelper;
import org.drools.runtime.pipeline.Action;
import org.drools.runtime.pipeline.KnowledgeRuntimeCommand;
import org.drools.runtime.pipeline.Pipeline;
import org.drools.runtime.pipeline.PipelineFactory;
import org.drools.runtime.pipeline.ResultHandler;
import org.drools.runtime.pipeline.Transformer;
import org.drools.runtime.process.ProcessInstance;
import org.drools.runtime.process.WorkItem;
import org.drools.runtime.process.WorkItemHandler;
import org.drools.runtime.process.WorkItemManager;
import org.drools.runtime.process.WorkflowProcessInstance;
import org.drools.runtime.rule.FactHandle;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.process.instance.context.variable.VariableScopeInstance;
import org.xml.sax.SAXException;

import com.thoughtworks.xstream.XStream;

public class JSonBatchExecutionTest {

    @Before
    public void setUp() throws Exception {
        XMLUnit.setIgnoreComments( true );
        XMLUnit.setIgnoreWhitespace( true );
        XMLUnit.setIgnoreAttributeOrder( true );
        XMLUnit.setNormalizeWhitespace( true );
        XMLUnit.setNormalize( true );
    }

    private void assertXMLEqual(String expectedXml,
                                String resultXml) {
        try {
            Diff diff = new Diff( expectedXml,
                                  resultXml );
            diff.overrideElementQualifier( new RecursiveElementNameAndTextQualifier() );
            XMLAssert.assertXMLEqual( diff,
                                      true );
        } catch ( Exception e ) {
            throw new RuntimeException( "XML Assertion failure",
                                        e );
        }
    }

    @Test
    public void testListenForChanges() throws Exception {

        String str = "";
        str += "package org.drools \n";
        str += "import org.drools.Cheese \n";
        str += "import org.drools.ChangeCollector \n";
        str += "rule rule1 \n";
        str += "  when \n";
        str += "    $c : Cheese(price==25) \n";
        str += " \n";
        str += "  then \n";
        str += "end\n";

        str += "rule rule2 \n";
        str += "  when \n";
        str += "    p : Person(name=='mic') \n";
        str += "    c : Cheese(price != 42) \n";
        str += " \n";
        str += "  then \n";
        str += "    c.setPrice( 42 ); \n";
        str += "    update(c); \n";
        str += "end\n";

        str += "rule rule3 \n";
        str += "  when \n";
        str += "    p : Person(name=='mark') \n";
        str += "    c : Cheese(price == 42) \n";
        str += " \n";
        str += "  then \n";
        str += "    retract(c); \n";
        str += "end\n";

        str += "rule ruleBootStrap \n";
        str += "salience 10000\n";
        str += "  when \n";
        str += "    $c : ChangeCollector() \n";
        str += " \n";
        str += "  then \n";
        str += "    kcontext.getKnowledgeRuntime().addEventListener($c); \n";
        str += "end\n";

        str += "rule ruleCleanup \n";
        str += "salience -10000\n";
        str += "  when \n";
        str += "    $c : ChangeCollector() \n";
        str += " \n";
        str += "  then \n";
        str += "    kcontext.getKnowledgeRuntime().removeEventListener($c); \n";
        str += "    retract($c); \n";
        str += "end\n";

        String inXml = "";
        inXml += "{\"batch-execution\":{\"commands\":[";
        inXml += "{\"insert\":{\"object\":{\"org.drools.Cheese\":{\"type\":\"stilton\",\"price\":25,\"oldPrice\":0}}, \"out-identifier\":\"outStilton\" } }";
        inXml += ", {\"fire-all-rules\":\"\"}";
        inXml += "]}}";        
        inXml = roundTripFromXml( inXml );    
                
        StatefulKnowledgeSession ksession = getSessionStateful( ResourceFactory.newByteArrayResource( str.getBytes() ) );
        ResultHandlerImpl resultHandler = new ResultHandlerImpl();
        getPipelineStateful( ksession ).insert( inXml,
                                                resultHandler );

        inXml = "";
        inXml += "{\"batch-execution\":{\"commands\":[";
        inXml += "   {\"insert\":{\"object\":{\"org.drools.Person\":{\"name\":\"mic\"} }, \"out-identifier\":\"person\" } }";
        inXml += ",  {\"insert\":{\"object\":{\"org.drools.ChangeCollector\":{} }, \"out-identifier\":\"changes\" } }";        
        inXml += ",  {\"fire-all-rules\":\"\"}";
        inXml += "]}}";        
        inXml = roundTripFromXml( inXml );    
                        
        
        resultHandler = new ResultHandlerImpl();
        getPipelineStateful( ksession ).insert( inXml,
                                                resultHandler );
        String outXml = (String) resultHandler.getObject();
        outXml = roundTripFromXml( outXml );
        
        ExecutionResults result = (ExecutionResults) BatchExecutionHelper.newJSonMarshaller().fromXML( outXml );
        result = ( ExecutionResults ) roundTripFromObject( result );        
        ChangeCollector collector = ( ChangeCollector ) result.getValue( "changes" );
        Cheese c = ( Cheese ) collector.getChanges().get( 0 );
        assertEquals( 42, c.getPrice() );

        inXml = "";
        inXml += "{\"batch-execution\":{\"commands\":[";
        inXml += "   {\"insert\":{\"object\":{\"org.drools.Person\":{\"name\":\"mark\"} }, \"out-identifier\":\"person\" } }";
        inXml += ",  {\"insert\":{\"object\":{\"org.drools.ChangeCollector\":{} }, \"out-identifier\":\"changes\" } }";        
        inXml += ",  {\"fire-all-rules\":\"\"}";
        inXml += "]}}";  
        inXml = roundTripFromXml( inXml );
        
        resultHandler = new ResultHandlerImpl();
        getPipelineStateful( ksession ).insert( inXml,
                                                resultHandler );
        
        outXml = (String) resultHandler.getObject();
        outXml = roundTripFromXml( outXml );
        
        result = (ExecutionResults) BatchExecutionHelper.newJSonMarshaller().fromXML( outXml );
        result = ( ExecutionResults ) roundTripFromObject( result );        
        collector = ( ChangeCollector ) result.getValue( "changes" );
        assertEquals( "stilton", collector.getRetracted().get( 0 ) );        

    }

    @Test
    public void testInsertWithDefaults() throws Exception {
        String str = "";
        str += "package org.drools \n";
        str += "import org.drools.Cheese \n";
        str += "rule rule1 \n";
        str += "  when \n";
        str += "    $c : Cheese() \n";
        str += " \n";
        str += "  then \n";
        str += "    $c.setPrice( $c.getPrice() + 5 ); \n";
        str += "end\n";

        String inXml = "";
        inXml += "{\"batch-execution\":{\"commands\":[";
        inXml += "{\"insert\":{\"object\":{\"org.drools.Cheese\":{\"type\":\"stilton\",\"price\":25,\"oldPrice\":0}}, \"out-identifier\":\"outStilton\" } }";
        inXml += ", {\"fire-all-rules\":\"\"}";
        inXml += "]}}";        
        inXml = roundTripFromXml( inXml );    
        

        StatefulKnowledgeSession ksession = getSessionStateful( ResourceFactory.newByteArrayResource( str.getBytes() ) );
        ResultHandlerImpl resultHandler = new ResultHandlerImpl();
        getPipelineStateful( ksession ).insert( inXml,
                                                resultHandler );
        String outXml = (String) resultHandler.getObject();
        outXml = roundTripFromXml( outXml );        

        ExecutionResults result = (ExecutionResults) BatchExecutionHelper.newJSonMarshaller().fromXML( outXml );
        result = ( ExecutionResults ) roundTripFromObject( result );
        
        Cheese stilton = (Cheese) result.getValue( "outStilton" );
        assertEquals( 30,
                      stilton.getPrice() );

        FactHandle factHandle = (FactHandle) result.getFactHandle( "outStilton" );
        stilton = (Cheese) ksession.getObject( factHandle );
        assertEquals( 30,
                      stilton.getPrice() );        

//        String expectedXml = "";
//        expectedXml = "{\"execution-results\":{\"results\":{\"outStilton\":{\"org.drools.Cheese\":{\"type\":\"stilton\",\"oldPrice\":0,\"price\":30}}},\"fact-handles\":{\"outStilton\":\"" + ((InternalFactHandle) result.getFactHandle( "outStilton" )).toExternalForm() + "\"}}}";
//        String expectedXml = "";
//        expectedXml += "<execution-results>\n";
//        expectedXml += "  <result identifier=\"outStilton\">\n";
//        expectedXml += "    <org.drools.Cheese>\n";
//        expectedXml += "      <type>stilton</type>\n";
//        expectedXml += "      <oldPrice>0</oldPrice>\n";
//        expectedXml += "      <price>30</price>\n";
//        expectedXml += "    </org.drools.Cheese>\n";
//        expectedXml += "  </result>\n";
//        expectedXml += "  <fact-handle identifier=\"outStilton\" externalForm=\"" + ((InternalFactHandle) result.getFactHandle( "outStilton" )).toExternalForm() + "\" /> \n";
//        expectedXml += "</execution-results>\n";
//
//        assertXMLEqual( expectedXml,
//                        outXml );
//        JSONAssert.assertEquals( expectedXml, outXml );
    }

    @Test
    public void testInsertWithReturnObjectFalse() throws Exception {
        String str = "";
        str += "package org.drools \n";
        str += "import org.drools.Cheese \n";
        str += "rule rule1 \n";
        str += "  when \n";
        str += "    $c : Cheese() \n";
        str += " \n";
        str += "  then \n";
        str += "    $c.setPrice( $c.getPrice() + 5 ); \n";
        str += "end\n";

        String inXml = "";
        inXml += "{\"batch-execution\":{\"commands\":[";
        inXml += "{\"insert\":{\"object\":{\"org.drools.Cheese\":{\"type\":\"stilton\",\"price\":25,\"oldPrice\":0}}, \"return-object\":false, \"out-identifier\":\"outStilton\" } }";
        inXml += ", {\"fire-all-rules\":\"\"}";
        inXml += "]}}";        
        inXml = roundTripFromXml( inXml ); 

        StatefulKnowledgeSession ksession = getSessionStateful( ResourceFactory.newByteArrayResource( str.getBytes() ) );
        ResultHandlerImpl resultHandler = new ResultHandlerImpl();
        getPipelineStateful( ksession ).insert( inXml,
                                                resultHandler );
        String outXml = (String) resultHandler.getObject();
        outXml = roundTripFromXml( outXml ); 

        ExecutionResults result = (ExecutionResults) BatchExecutionHelper.newJSonMarshaller().fromXML( outXml );
        result = ( ExecutionResults ) roundTripFromObject ( result );
        assertNull( result.getValue( "outStilton" ) );

        FactHandle factHandle = (FactHandle) result.getFactHandle( "outStilton" );
        Cheese stilton = (Cheese) ksession.getObject( factHandle );
        assertEquals( 30,
                      stilton.getPrice() );

//        String expectedXml = "";
//        expectedXml += "<execution-results>\n";
//        expectedXml += "  <fact-handle identifier=\"outStilton\" externalForm=\"" + ((InternalFactHandle) result.getFactHandle( "outStilton" )).toExternalForm() + "\" /> \n";
//        expectedXml += "</execution-results>\n";
//
//        assertXMLEqual( expectedXml,
//                        outXml );
    }

    @Test
    public void testGetObject() throws Exception {
        String str = "";
        str += "package org.drools \n";
        str += "import org.drools.Cheese \n";
        str += "rule rule1 \n";
        str += "  when \n";
        str += "    $c : Cheese() \n";
        str += " \n";
        str += "  then \n";
        str += "    $c.setPrice( $c.getPrice() + 5 ); \n";
        str += "end\n";

        String inXml = "";
        inXml += "{\"batch-execution\":{\"commands\":[";
        inXml += "{\"insert\":{\"object\":{\"org.drools.Cheese\":{\"type\":\"stilton\",\"price\":25,\"oldPrice\":0}}, \"out-identifier\":\"outStilton\" } }";
        inXml += ", {\"fire-all-rules\":{\"max\":10}}";
        inXml += "]}}";        
        inXml = roundTripFromXml( inXml );
        

        StatefulKnowledgeSession ksession = getSessionStateful( ResourceFactory.newByteArrayResource( str.getBytes() ) );
        ResultHandlerImpl resultHandler = new ResultHandlerImpl();
        getPipelineStateful( ksession ).insert( inXml,
                                                resultHandler );
        String outXml = (String) resultHandler.getObject();        
        outXml = roundTripFromXml( outXml );

        ExecutionResults result = (ExecutionResults) BatchExecutionHelper.newJSonMarshaller().fromXML( outXml );
        result = ( ExecutionResults) roundTripFromObject( result );
        
        Cheese stilton = (Cheese) result.getValue( "outStilton" );
        assertEquals( 30,
                      stilton.getPrice() );

        FactHandle factHandle = (FactHandle) result.getFactHandle( "outStilton" );
        inXml = "";
        inXml += "{\"batch-execution\":{\"commands\":[";
        inXml += "  {\"get-object\":{ ";
        inXml += "      \"out-identifier\":'outStilton',";
        inXml += "      \"fact-handle\":'" + factHandle.toExternalForm() + "'}}";
        inXml += "]}}";   
        inXml = roundTripFromXml( inXml );        
      
        getPipelineStateful( ksession ).insert( inXml,
                                                resultHandler );
        outXml = (String) resultHandler.getObject();
        outXml = roundTripFromXml( outXml );
        
        result = (ExecutionResults) BatchExecutionHelper.newJSonMarshaller().fromXML( outXml );
        result = ( ExecutionResults) roundTripFromObject( result );
        stilton = (Cheese) result.getValue( "outStilton" );
        assertEquals( 30,
                      stilton.getPrice() );
    }

    @Test
    public void testRetractObject() throws Exception {
        String str = "";
        str += "package org.drools \n";
        str += "import org.drools.Cheese \n";
        str += "rule rule1 \n";
        str += "  when \n";
        str += "    $c : Cheese() \n";
        str += " \n";
        str += "  then \n";
        str += "    $c.setPrice( $c.getPrice() + 5 ); \n";
        str += "end\n";

        String inXml = "";
        inXml += "{\"batch-execution\":{\"commands\":[";
        inXml += "{\"insert\":{\"object\":{\"org.drools.Cheese\":{\"type\":\"stilton\",\"price\":25,\"oldPrice\":0}}, \"out-identifier\":\"outStilton\" } }";
        inXml += ", {\"fire-all-rules\":{\"max\":10}}";
        inXml += "]}}";        
        inXml = roundTripFromXml( inXml );

        StatefulKnowledgeSession ksession = getSessionStateful( ResourceFactory.newByteArrayResource( str.getBytes() ) );
        ResultHandlerImpl resultHandler = new ResultHandlerImpl();
        getPipelineStateful( ksession ).insert( inXml,
                                                resultHandler );
        String outXml = (String) resultHandler.getObject();
        outXml = roundTripFromXml( outXml );

        ExecutionResults result = (ExecutionResults) BatchExecutionHelper.newJSonMarshaller().fromXML( outXml );
        result = ( ExecutionResults) roundTripFromObject( result );
        
        Cheese stilton = (Cheese) result.getValue( "outStilton" );
        assertEquals( 30,
                      stilton.getPrice() );

        FactHandle factHandle = (FactHandle) result.getFactHandle( "outStilton" );
        inXml = "";
        inXml += "{\"batch-execution\":{\"commands\":[";
        inXml += "  { \"retract\":{\"fact-handle\":'" + factHandle.toExternalForm() + "'}}";
        inXml += "]}}";   
        inXml = roundTripFromXml( inXml );
        getPipelineStateful( ksession ).insert( inXml,
                                                resultHandler );

        inXml = "";
        inXml += "{\"batch-execution\":{\"commands\":[";
        inXml += "  {\"get-object\":{ ";
        inXml += "      \"out-identifier\":'outStilton',";
        inXml += "      \"fact-handle\":'" + factHandle.toExternalForm() + "'}}";
        inXml += "]}}";   
        inXml = roundTripFromXml( inXml );
        getPipelineStateful( ksession ).insert( inXml,
                                                resultHandler );
        outXml = (String) resultHandler.getObject();
        result = (ExecutionResults) BatchExecutionHelper.newJSonMarshaller().fromXML( outXml );
        result = ( ExecutionResults) roundTripFromObject( result );
        assertNull( result.getValue( "outStilton" ) );
    }

    @Test
    public void testModifyObject() throws Exception {
        String str = "";
        str += "package org.drools \n";
        str += "import org.drools.Cheese \n";
        str += "rule rule1 \n";
        str += "  when \n";
        str += "    $c : Cheese() \n";
        str += " \n";
        str += "  then \n";
        str += "    $c.setPrice( $c.getPrice() + 5 ); \n";
        str += "end\n";

        String inXml = "";
        inXml += "{\"batch-execution\":{\"commands\":[";
        inXml += "{\"insert\":{\"object\":{\"org.drools.Cheese\":{\"type\":\"stilton\",\"price\":25,\"oldPrice\":0}}, \"out-identifier\":\"outStilton\" } }";
        inXml += ", {\"fire-all-rules\":\"\"}";
        inXml += "]}}";        
        inXml = roundTripFromXml( inXml );

        StatefulKnowledgeSession ksession = getSessionStateful( ResourceFactory.newByteArrayResource( str.getBytes() ) );
        ResultHandlerImpl resultHandler = new ResultHandlerImpl();
        getPipelineStateful( ksession ).insert( inXml,
                                                resultHandler );
        String outXml = (String) resultHandler.getObject();
        outXml = roundTripFromXml( outXml );

        ExecutionResults result = (ExecutionResults) BatchExecutionHelper.newJSonMarshaller().fromXML( outXml );
        result = ( ExecutionResults ) roundTripFromObject( result );
        Cheese stilton = (Cheese) result.getValue( "outStilton" );
        assertEquals( 30,
                      stilton.getPrice() );

        FactHandle factHandle = ((FactHandle) result.getFactHandle( "outStilton" ));
//
//        String expectedXml = "";
//        expectedXml += "<execution-results>\n";
//        expectedXml += "  <result identifier=\"outStilton\">\n";
//        expectedXml += "    <org.drools.Cheese>\n";
//        expectedXml += "      <type>stilton</type>\n";
//        expectedXml += "      <oldPrice>0</oldPrice>\n";
//        expectedXml += "      <price>30</price>\n";
//        expectedXml += "    </org.drools.Cheese>\n";
//        expectedXml += "  </result>\n";
//        expectedXml += "  <fact-handle identifier=\"outStilton\" externalForm=\"" + factHandle.toExternalForm() + "\" /> \n";
//        expectedXml += "</execution-results>\n";
//
//        assertXMLEqual( expectedXml,
//                        outXml );
        
        inXml = "";
        inXml += "{\"batch-execution\":{\"commands\":[";
        inXml += "  {\"modify\":{\"fact-handle\":'" + factHandle.toExternalForm() + "'";
        inXml += ",                \"setters\":[{\"accessor\":\"oldPrice\",\"set\":42}, {\"accessor\":\"price\",\"set\":50}]";
        inXml += "} }";        
        inXml += ", {\"fire-all-rules\":\"\"}";
        inXml += "]}}";   
        inXml = roundTripFromXml( inXml );        
        
        getPipelineStateful( ksession ).insert( inXml,
                                                resultHandler );
        inXml = "";
        inXml += "{\"batch-execution\":{\"commands\":[";
        inXml += "  {\"get-object\":{ ";
        inXml += "      \"out-identifier\":'outCheddar',";
        inXml += "      \"fact-handle\":'" + factHandle.toExternalForm() + "'}}";
        inXml += "]}}";   
        inXml = roundTripFromXml( inXml );
        
        getPipelineStateful( ksession ).insert( inXml,
                                                resultHandler );
        outXml = (String) resultHandler.getObject();
        outXml = roundTripFromXml( outXml );
        result = (ExecutionResults) BatchExecutionHelper.newJSonMarshaller().fromXML( outXml );
        result = ( ExecutionResults ) roundTripFromObject ( result );
        Cheese cheddar = (Cheese) result.getValue( "outCheddar" );
        assertEquals( 42,
                      cheddar.getOldPrice() );
        assertEquals( 55,
                      cheddar.getPrice() );

        //now test for code injection:
        ModifyCommand.ALLOW_MODIFY_EXPRESSIONS = false;        
        inXml = "";
        inXml += "{\"batch-execution\":{\"commands\":[";
        inXml += "  {\"modify\":{\"fact-handle\":'" + factHandle.toExternalForm() + "'";
        inXml += ",                \"setters\":[{\"accessor\":\"type\",\"set\":\"44; System.exit(1);\"}, {\"accessor\":\"price\",\"set\":50}]";
        inXml += "} }";        
        inXml += ", {\"fire-all-rules\":\"\"}";
        inXml += "]}}";   
        inXml = roundTripFromXml( inXml );           
        getPipelineStateful( ksession ).insert( inXml,
                                                resultHandler );
        outXml = (String) resultHandler.getObject();
        result = (ExecutionResults) BatchExecutionHelper.newJSonMarshaller().fromXML( outXml );
        ModifyCommand.ALLOW_MODIFY_EXPRESSIONS = true;

    }

    @Test
    public void testInsertElements() throws Exception {
        String str = "";
        str += "package org.drools \n";
        str += "import org.drools.Cheese \n";
        str += "global java.util.List list1 \n";
        str += "rule rule1 \n";
        str += "  when \n";
        str += "    $c : Cheese() \n";
        str += " \n";
        str += "  then \n";
        str += "    $c.setPrice( $c.getPrice() + 5 ); \n";
        str += "     list1.add( $c );";
        str += "end\n";
        
        String inXml = "";
        inXml += "{\"batch-execution\":{\"commands\":[";
        inXml += "   {\"set-global\":{\"identifier\":\"list1\",\"out\"=true";
        inXml += "                   ,\"object\":{\"list\":{\"object\":[]}}";
        inXml += "   } } "; //        
        inXml += ",  {\"insert-elements\":{\"objects\":[";        
        inXml += "   {   \"org.drools.Cheese\":{\"type\":\"stilton\",\"price\":25,\"oldPrice\":0}}, ";
        inXml += "   {   \"org.drools.Cheese\":{\"type\":\"stilton\",\"price\":30,\"oldPrice\":0}} ";        
        inXml += "   ]}}";
        inXml += "]}}";                 
        inXml = roundTripFromXml( inXml );         

        StatelessKnowledgeSession ksession = getSession2( ResourceFactory.newByteArrayResource( str.getBytes() ) );
        ResultHandlerImpl resultHandler = new ResultHandlerImpl();
        getPipeline( ksession ).insert( inXml,
                                        resultHandler );
        String outXml = (String) resultHandler.getObject();                
        outXml = roundTripFromXml( outXml );
        
//        String expectedXml = "";
//        expectedXml += "<execution-results>\n";
//        expectedXml += "  <result identifier='list'>\n";
//        expectedXml += "    <list>\n";
//        expectedXml += "      <org.drools.Cheese>\n";
//        expectedXml += "        <type>stilton</type>\n";
//        expectedXml += "        <price>35</price>\n";
//        expectedXml += "        <oldPrice>0</oldPrice>\n";
//        expectedXml += "      </org.drools.Cheese>\n";
//        expectedXml += "      <org.drools.Cheese>\n";
//        expectedXml += "        <type>stilton</type>\n";
//        expectedXml += "        <price>30</price>\n";
//        expectedXml += "        <oldPrice>0</oldPrice>\n";
//        expectedXml += "      </org.drools.Cheese>\n";
//        expectedXml += "    </list>\n";
//        expectedXml += "  </result>\n";
//        expectedXml += "</execution-results>\n";
//
//        assertXMLEqual( expectedXml,
//                        outXml );
//
        ExecutionResults result = (ExecutionResults) BatchExecutionHelper.newJSonMarshaller().fromXML( outXml );
        result = ( ExecutionResults ) roundTripFromObject( result );
        List list = (List) result.getValue( "list1" );
        Cheese stilton25 = new Cheese( "stilton",
                                       30 );
        Cheese stilton30 = new Cheese( "stilton",
                                       35 );

        Set expectedList = new HashSet();
        expectedList.add( stilton25 );
        expectedList.add( stilton30 );

        assertEquals( expectedList,
                      new HashSet( list ) );
    }

    @Test
    public void testFactHandleReturn() throws Exception {
        String str = "";
        str += "package org.drools \n";
        str += "import org.drools.Cheese \n";
        str += "global java.util.List list1 \n";
        str += "rule rule1 \n";
        str += "  when \n";
        str += "    $c : Cheese() \n";
        str += " \n";
        str += "  then \n";
        str += "    System.err.println(42); \n";
        str += "end\n";

        String inXml = "";
        inXml += "{\"batch-execution\":{\"commands\":[";
        inXml += "  {\"get-global\":{\"identifier\":\"list1\",\"out-identifier\":\"out-list\"}}";               
        inXml += "]}}";        
        inXml = roundTripFromXml( inXml );          

        StatefulKnowledgeSession ksession = getSessionStateful( ResourceFactory.newByteArrayResource( str.getBytes() ) );
        FactHandle fh = ksession.insert( new Person( "mic",
                                                     42 ) );
        List<FactHandle> list = new ArrayList<FactHandle>();
        list.add( fh );

        ksession.setGlobal( "list1",
                            list );

        ResultHandlerImpl resultHandler = new ResultHandlerImpl();
        getPipelineStateful( ksession ).insert( inXml,
                                                resultHandler );
        getPipelineStateful( ksession ).insert( inXml,
                                                resultHandler );
        String outXml = (String) resultHandler.getObject();
        outXml = roundTripFromXml( outXml );
        
        ExecutionResults result = (ExecutionResults) BatchExecutionHelper.newJSonMarshaller().fromXML( outXml );
        result = ( ExecutionResults ) roundTripFromObject( result );
        List outList = ( List ) result.getValue( "out-list" );
        assertEquals( 1, outList.size() );
        assertEquals( fh.toExternalForm(), ((FactHandle)outList.get(0)).toExternalForm() );
        assertNotSame( fh, outList.get( 0 ));
        

//        System.err.println( outXml );
//        String expectedXml = "";
//        expectedXml += "<execution-results>\n" + "  <result identifier=\"out-list\">\n" + "    <list>\n" + "      <fact-handle externalForm=\"" + fh.toExternalForm() + "\"/>\n" + "    </list>\n" + "  </result>\n" + "</execution-results>";
//
//        assertXMLEqual( expectedXml,
//                        outXml );

    }

    @Test
    public void testInsertElementsWithReturnObjects() throws Exception {
        String str = "";
        str += "package org.drools \n";
        str += "import org.drools.Cheese \n";
        str += "global java.util.List list1 \n";
        str += "rule rule1 \n";
        str += "  when \n";
        str += "    $c : Cheese() \n";
        str += " \n";
        str += "  then \n";
        str += "    $c.setPrice( $c.getPrice() + 5 ); \n";
        str += "    list1.add( $c );";
        str += "end\n";
        
        String inXml = "";
        inXml += "{\"batch-execution\":{\"commands\":[";
        inXml += "   {\"set-global\":{\"identifier\":\"list1\",\"out\"=true";
        inXml += "                   ,\"object\":{\"list\":{\"object\":[]}}";
        inXml += "   } } "; //        
        inXml += ",  {\"insert-elements\":{\"out-identifier\":\"myfacts\",\"return-objects\":true,\"objects\":[";        
        inXml += "   {   \"org.drools.Cheese\":{\"type\":\"stilton\",\"price\":25,\"oldPrice\":0}}, ";
        inXml += "   {   \"org.drools.Cheese\":{\"type\":\"stilton\",\"price\":30,\"oldPrice\":0}} ";        
        inXml += "   ]}}";
        inXml += ", {\"fire-all-rules\":\"\"}";        
        inXml += "]}}";                 
        inXml = roundTripFromXml( inXml );        

        StatefulKnowledgeSession ksession = getSessionStateful( ResourceFactory.newByteArrayResource( str.getBytes() ) );
        ResultHandlerImpl resultHandler = new ResultHandlerImpl();
        getPipelineStateful( ksession ).insert( inXml,
                                                resultHandler );
        String outXml = (String) resultHandler.getObject();
        outXml = roundTripFromXml( outXml );
        
        ExecutionResults result = (ExecutionResults) BatchExecutionHelper.newJSonMarshaller().fromXML( outXml );
        result = ( ExecutionResults) roundTripFromObject( result );
        
        List list1 = ( List ) result.getValue( "list1" );
        assertEquals( 2, list1.size() );
        assertTrue( list1.contains( new Cheese("stilton", 35) ) );
        assertTrue( list1.contains( new Cheese("stilton", 30) ) );          
        
        List myFacts = ( List ) result.getValue( "myfacts" );
        assertEquals( 2, list1.size() );
        assertTrue( myFacts.contains( new Cheese("stilton", 35) ) );
        assertTrue( myFacts.contains( new Cheese("stilton", 30) ) );        
        
        List factHandles = ( List ) result.getFactHandle( "myfacts" );
        List list = new ArrayList();
        list.add( ksession.getObject( ((InternalFactHandle)factHandles.get( 0 )) ) );
        list.add( ksession.getObject( ((InternalFactHandle)factHandles.get( 1 )) ) );
        assertTrue( list.contains( new Cheese("stilton", 35) ) );
        assertTrue( list.contains( new Cheese("stilton", 30) ) );          


//        String expectedXml = "";
//        expectedXml += "<execution-results>\n";
//        expectedXml += "  <result identifier='list'>\n";
//        expectedXml += "    <list>\n";
//        expectedXml += "      <org.drools.Cheese>\n";
//        expectedXml += "        <type>stilton</type>\n";
//        expectedXml += "        <price>35</price>\n";
//        expectedXml += "        <oldPrice>0</oldPrice>\n";
//        expectedXml += "      </org.drools.Cheese>\n";
//        expectedXml += "      <org.drools.Cheese>\n";
//        expectedXml += "        <type>stilton</type>\n";
//        expectedXml += "        <price>30</price>\n";
//        expectedXml += "        <oldPrice>0</oldPrice>\n";
//        expectedXml += "      </org.drools.Cheese>\n";
//        expectedXml += "    </list>\n";
//        expectedXml += "  </result>\n";
//
//        expectedXml += "  <result identifier=\"myfacts\">\n";
//        expectedXml += "  <list>\n";
//        expectedXml += "    <org.drools.Cheese reference=\"../../../result/list/org.drools.Cheese[2]\"/>\n";
//        expectedXml += "    <org.drools.Cheese reference=\"../../../result/list/org.drools.Cheese\"/>\n";
//        expectedXml += "  </list>\n";
//        expectedXml += "  </result>\n";
//        expectedXml += "  <fact-handles identifier=\"myfacts\">\n";
//        for ( FactHandle factHandle : factHandles ) {
//            if ( ((Cheese) ksession.getObject( factHandle )).getPrice() == 30 ) {
//                expectedXml += "  <fact-handle externalForm=\"" + factHandle.toExternalForm() + "\"/>\n";
//            }
//        }
//
//        for ( FactHandle factHandle : factHandles ) {
//            if ( ((Cheese) ksession.getObject( factHandle )).getPrice() == 35 ) {
//                expectedXml += "  <fact-handle externalForm=\"" + factHandle.toExternalForm() + "\"/>\n";
//            }
//        }
//        expectedXml += "  </fact-handles>\n";
//
//        expectedXml += "</execution-results>\n";
//
//        assertXMLEqual( expectedXml,
//                        outXml );
    }

    @Test
    public void testSetGlobal() throws Exception {
        String str = "";
        str += "package org.drools \n";
        str += "import org.drools.Cheese \n";
        str += "global java.util.List list1 \n";
        str += "global java.util.List list2 \n";
        str += "global java.util.List list3 \n";
        str += "rule rule1 \n";
        str += "  when \n";
        str += "    $c : Cheese() \n";
        str += " \n";
        str += "  then \n";
        str += "    $c.setPrice( 30 ); \n";
        str += "    list1.add( $c ); \n";
        str += "    list2.add( $c ); \n";
        str += "    list3.add( $c ); \n";
        str += "end\n";
        
        String inXml = "";
        inXml += "{\"batch-execution\":{\"commands\":[";
        inXml += " {\"set-global\":{\"identifier\":\"list1\"";
        inXml += "                  ,\"object\":{\"list\":{\"object\":[]}}";
        inXml += "} } ";   
        inXml += ", {\"set-global\":{\"identifier\":\"list2\",\"out\"=true";
        inXml += "                  ,\"object\":{\"list\":{\"object\":[]}}";
        inXml += "} } ";
        inXml += ", {\"set-global\":{\"identifier\":\"list3\",\"out-identifier\"=\"outList3\"";
        inXml += "                  ,\"object\":{\"list\":{\"object\":[]}}";
        inXml += "} } ";        
        inXml += ", {\"insert\":{\"object\":{\"org.drools.Cheese\":{\"type\":\"stilton\",\"price\":5}}, \"out-identifier\":\"outStilton\" } }";
        inXml += "]}}";        
        inXml = roundTripFromXml( inXml ); 

        StatelessKnowledgeSession ksession = getSession2( ResourceFactory.newByteArrayResource( str.getBytes() ) );
        ResultHandlerImpl resultHandler = new ResultHandlerImpl();
        getPipeline( ksession ).insert( inXml,
                                        resultHandler );

        String outXml = (String) resultHandler.getObject();
        outXml = roundTripFromXml( outXml );

//        String expectedXml = "";
//        expectedXml += "<execution-results>\n";
//        expectedXml += "  <result identifier='list2'>\n";
//        expectedXml += "    <list>\n";
//        expectedXml += "      <org.drools.Cheese>\n";
//        expectedXml += "        <type>stilton</type>\n";
//        expectedXml += "        <price>30</price>\n";
//        expectedXml += "        <oldPrice>0</oldPrice>\n";
//        expectedXml += "      </org.drools.Cheese>\n";
//        expectedXml += "    </list>\n";
//        expectedXml += "  </result>\n";
//        expectedXml += "  <result identifier='outList3'>\n";
//        expectedXml += "    <list>\n";
//        expectedXml += "      <org.drools.Cheese reference='../../../result/list/org.drools.Cheese'/>\n";
//        expectedXml += "    </list>\n";
//        expectedXml += "  </result>\n";
//        expectedXml += "</execution-results>\n";
//
//        assertXMLEqual( expectedXml,
//                        outXml );
//
        ExecutionResults result = (ExecutionResults) BatchExecutionHelper.newJSonMarshaller().fromXML( outXml );
        Cheese stilton = new Cheese( "stilton",
                                     30 );

        assertNull( result.getValue( "list1" ) );

        List list2 = (List) result.getValue( "list2" );
        assertEquals( 1,
                      list2.size() );
        assertEquals( stilton,
                      list2.get( 0 ) );

        List list3 = (List) result.getValue( "outList3" );
        assertEquals( 1,
                      list3.size() );
        assertEquals( stilton,
                      list3.get( 0 ) );
    }

    @Test
    public void testGetGlobal() throws Exception {
        String str = "";
        str += "package org.drools \n";
        str += "import org.drools.Cheese \n";
        str += "global java.util.List list1 \n";
        str += "rule rule1 \n";
        str += "  when \n";
        str += "    $c : Cheese() \n";
        str += " \n";
        str += "  then \n";
        str += "    list1.add( $c ); \n";
        str += "end\n";
        
        String inXml = "";
        inXml += "{\"batch-execution\":{\"commands\":[";
        inXml += "   {\"set-global\":{\"identifier\":\"list1\",\"out\"=true";
        inXml += "                   ,\"object\":{\"list\":{\"object\":[]}}";
        inXml += "   } } "; //        
        inXml += ",  {\"insert\":{\"object\":{\"org.drools.Cheese\":{\"type\":\"stilton\",\"price\":25,\"oldPrice\":0}}, \"out-identifier\":\"outStilton\" } }";
        inXml += ",  {\"get-global\":{\"identifier\":\"list1\",\"out-identifier\"=\"out-list\"}}";               
        inXml += "]}}";        
        inXml = roundTripFromXml( inXml );         

        StatelessKnowledgeSession ksession = getSession2( ResourceFactory.newByteArrayResource( str.getBytes() ) );
        ResultHandlerImpl resultHandler = new ResultHandlerImpl();
        getPipeline( ksession ).insert( inXml,
                                        resultHandler );
        String outXml = (String) resultHandler.getObject();
        outXml = roundTripFromXml( outXml );
        
        ExecutionResults result = (ExecutionResults) BatchExecutionHelper.newJSonMarshaller().fromXML( outXml );
        result = ( ExecutionResults ) roundTripFromObject( result );
        List resultsList = ( List ) result.getValue("out-list");
        assertEquals(1, resultsList.size() );
        assertEquals(new Cheese("stilton",25), resultsList.get(0));

//        String expectedXml = "";
//        expectedXml += "<execution-results>\n";
//        expectedXml += "  <result identifier=\"out-list\">\n";
//        expectedXml += "    <list>\n";
//        expectedXml += "      <org.drools.Cheese>\n";
//        expectedXml += "        <type>stilton</type>\n";
//        expectedXml += "        <price>25</price>\n";
//        expectedXml += "        <oldPrice>0</oldPrice>\n";
//        expectedXml += "      </org.drools.Cheese>\n";
//        expectedXml += "    </list>\n";
//        expectedXml += "  </result>\n";
//        expectedXml += "</execution-results>\n";
//
//        assertXMLEqual( expectedXml,
//                        outXml );
    }

    @Test
    public void testGetObjects() throws Exception {
        String str = "";
        str += "package org.drools \n";
        str += "import org.drools.Cheese \n";
        str += "rule rule1 \n";
        str += "  when \n";
        str += "    $c : Cheese() \n";
        str += " \n";
        str += "  then \n";
        str += "    $c.setPrice( $c.getPrice() + 5 ); \n";
        str += "end\n";

        String inXml ="";
        inXml =  "{\"batch-execution\":{\"commands\":[";
        inXml += "  {\"insert-elements\":{\"objects\":[";        
        inXml += "   {   \"org.drools.Cheese\":{\"type\":\"stilton\",\"price\":25,\"oldPrice\":0}}, ";
        inXml += "   {   \"org.drools.Cheese\":{\"type\":\"stilton\",\"price\":30,\"oldPrice\":0}} ";        
        inXml += "   ]}}";
        inXml += ",  {\"get-objects\":{\"out-identifier\":\"list1\"}}";        
        inXml += "]}}";     
        inXml = roundTripFromXml( inXml );
        
        StatelessKnowledgeSession ksession = getSession2( ResourceFactory.newByteArrayResource( str.getBytes() ) );
        ResultHandlerImpl resultHandler = new ResultHandlerImpl();
        getPipeline( ksession ).insert( inXml,
                                        resultHandler );
        String outXml = (String) resultHandler.getObject();
        outXml = roundTripFromXml( outXml );

//        String expectedXml = "";
//        expectedXml += "<execution-results>";
//        expectedXml += "  <result identifier='list'>";
//        expectedXml += "    <list>";
//        expectedXml += "      <org.drools.Cheese>";
//        expectedXml += "        <type>stilton</type>";
//        expectedXml += "        <price>30</price>";
//        expectedXml += "        <oldPrice>0</oldPrice>";
//        expectedXml += "      </org.drools.Cheese>";
//        expectedXml += "      <org.drools.Cheese>";
//        expectedXml += "        <type>stilton</type>";
//        expectedXml += "        <price>35</price>";
//        expectedXml += "        <oldPrice>0</oldPrice>";
//        expectedXml += "      </org.drools.Cheese>";
//        expectedXml += "    </list>";
//        expectedXml += "  </result>";
//        expectedXml += "</execution-results>";
//
//        assertXMLEqual( expectedXml,
//                        outXml );
//
        ExecutionResults result = (ExecutionResults) BatchExecutionHelper.newJSonMarshaller().fromXML( outXml );
        result = ( ExecutionResults ) roundTripFromObject( result ); 
        List list = (List) result.getValue( "list1" );
        Cheese stilton25 = new Cheese( "stilton",
                                       30 );
        Cheese stilton30 = new Cheese( "stilton",
                                       35 );

        Set expectedList = new HashSet();
        expectedList.add( stilton25 );
        expectedList.add( stilton30 );

        assertEquals( expectedList,
                      new HashSet( list ) );
    }

    @Test
    public void testQuery() throws Exception {
        String str = "";
        str += "package org.drools.test  \n";
        str += "import org.drools.Cheese \n";
        str += "query cheeses \n";
        str += "    stilton : Cheese(type == 'stilton') \n";
        str += "    cheddar : Cheese(type == 'cheddar', price == stilton.price) \n";
        str += "end\n";
        str += "query cheesesWithParams(String a, String b) \n";
        str += "    stilton : Cheese(type == a) \n";
        str += "    cheddar : Cheese(type == b, price == stilton.price) \n";
        str += "end\n";

        String inXml ="";
        inXml =  "{\"batch-execution\":{\"commands\":[";
        inXml += "  {\"insert-elements\":{\"objects\":[";        
        inXml += "   {   \"org.drools.Cheese\":{\"type\":\"stilton\",\"price\":2}}, ";
        inXml += "   {   \"org.drools.Cheese\":{\"type\":\"cheddar\",\"price\":1}}, ";
        inXml += "   {   \"org.drools.Cheese\":{\"type\":\"cheddar\",\"price\":2}}, ";
        inXml += "   {   \"org.drools.Cheese\":{\"type\":\"stilton\",\"price\":1}}  ";        
        inXml += "   ]}}";
        inXml += ",  {\"query\":{\"out-identifier\":\"cheeses\",\"name\":\"cheeses\"}}";        
        inXml += "]}}";     
        inXml = roundTripFromXml( inXml );        
        
        StatefulKnowledgeSession ksession = getSessionStateful( ResourceFactory.newByteArrayResource( str.getBytes() ) );
        ResultHandlerImpl resultHandler = new ResultHandlerImpl();
        getPipelineStateful( ksession ).insert( inXml,
                                                resultHandler );
        String outXml = (String) resultHandler.getObject();        
        outXml = roundTripFromXml( outXml );

//        Iterator<QueryResultsRow> it1 = ksession.getQueryResults( "cheeses" ).iterator();
//        Iterator<QueryResultsRow> it2 = ksession.getQueryResults( "cheesesWithParams",
//                                                                  new String[]{"stilton", "cheddar"} ).iterator();
//        QueryResultsRow row = null;
//
//        String expectedXml = "";
//        expectedXml += "<execution-results>\n";
//        expectedXml += "  <result identifier='cheeses'>\n";
//        expectedXml += "    <query-results>\n";
//        expectedXml += "      <identifiers>\n";
//        expectedXml += "        <identifier>stilton</identifier>\n";
//        expectedXml += "        <identifier>cheddar</identifier>\n";
//        expectedXml += "      </identifiers>\n";
//        expectedXml += "      <row>\n";
//        row = it1.next();
//        expectedXml += "        <org.drools.Cheese>\n";
//        expectedXml += "          <type>stilton</type>\n";
//        expectedXml += "          <price>1</price>\n";
//        expectedXml += "          <oldPrice>0</oldPrice>\n";
//        expectedXml += "        </org.drools.Cheese>\n";
//        expectedXml += "        <fact-handle externalForm='" + row.getFactHandle( "stilton" ).toExternalForm() + "' />";
//        expectedXml += "        <org.drools.Cheese>\n";
//        expectedXml += "          <type>cheddar</type>\n";
//        expectedXml += "          <price>1</price>\n";
//        expectedXml += "          <oldPrice>0</oldPrice>\n";
//        expectedXml += "        </org.drools.Cheese>\n";
//        expectedXml += "        <fact-handle externalForm='" + row.getFactHandle( "cheddar" ).toExternalForm() + "' />";
//        expectedXml += "      </row>\n";
//        expectedXml += "      <row>\n";
//        row = it1.next();
//        expectedXml += "        <org.drools.Cheese>\n";
//        expectedXml += "          <type>stilton</type>\n";
//        expectedXml += "          <price>2</price>\n";
//        expectedXml += "          <oldPrice>0</oldPrice>\n";
//        expectedXml += "        </org.drools.Cheese>\n";
//        expectedXml += "        <fact-handle externalForm='" + row.getFactHandle( "stilton" ).toExternalForm() + "' />";
//        expectedXml += "        <org.drools.Cheese>\n";
//        expectedXml += "          <type>cheddar</type>\n";
//        expectedXml += "          <price>2</price>\n";
//        expectedXml += "          <oldPrice>0</oldPrice>\n";
//        expectedXml += "        </org.drools.Cheese>\n";
//        expectedXml += "        <fact-handle externalForm='" + row.getFactHandle( "cheddar" ).toExternalForm() + "' />";
//        expectedXml += "      </row>\n";
//        expectedXml += "    </query-results>\n";
//        expectedXml += "  </result>\n";
//        expectedXml += "  <result identifier='cheeses2'>\n";
//        expectedXml += "    <query-results>\n";
//        expectedXml += "      <identifiers>\n";
//        expectedXml += "        <identifier>stilton</identifier>\n";
//        expectedXml += "        <identifier>cheddar</identifier>\n";
//        expectedXml += "      </identifiers>\n";
//        expectedXml += "      <row>\n";
//        row = it2.next();
//        expectedXml += "        <org.drools.Cheese reference=\"../../../../result/query-results/row/org.drools.Cheese\"/>\n";
//        expectedXml += "        <fact-handle externalForm='" + row.getFactHandle( "stilton" ).toExternalForm() + "' />";
//        expectedXml += "        <org.drools.Cheese reference=\"../../../../result/query-results/row/org.drools.Cheese[2]\"/>\n";
//        expectedXml += "        <fact-handle externalForm='" + row.getFactHandle( "cheddar" ).toExternalForm() + "' />";
//        expectedXml += "      </row>\n";
//        expectedXml += "      <row>\n";
//        row = it2.next();
//        expectedXml += "        <org.drools.Cheese reference=\"../../../../result/query-results/row[2]/org.drools.Cheese\"/>\n";
//        expectedXml += "        <fact-handle externalForm='" + row.getFactHandle( "stilton" ).toExternalForm() + "' />";
//        expectedXml += "        <org.drools.Cheese reference=\"../../../../result/query-results/row[2]/org.drools.Cheese[2]\"/>\n";
//        expectedXml += "        <fact-handle externalForm='" + row.getFactHandle( "cheddar" ).toExternalForm() + "' />";
//        expectedXml += "      </row>\n";
//        expectedXml += "    </query-results>\n";
//        expectedXml += "  </result>\n";
//        expectedXml += "</execution-results>\n";
//
//        assertXMLEqual( expectedXml,
//                        outXml );
//
        ExecutionResults batchResult = (ExecutionResults) BatchExecutionHelper.newJSonMarshaller().fromXML( outXml );
        batchResult = ( ExecutionResults ) roundTripFromObject( batchResult );

        Cheese stilton1 = new Cheese( "stilton",
                                      1 );
        Cheese cheddar1 = new Cheese( "cheddar",
                                      1 );
        Cheese stilton2 = new Cheese( "stilton",
                                      2 );
        Cheese cheddar2 = new Cheese( "cheddar",
                                      2 );

        Set set = new HashSet();
        List list = new ArrayList();
        list.add( stilton1 );
        list.add( cheddar1 );
        set.add( list );

        list = new ArrayList();
        list.add( stilton2 );
        list.add( cheddar2 );
        set.add( list );

        org.drools.runtime.rule.QueryResults results = (org.drools.runtime.rule.QueryResults) batchResult.getValue( "cheeses" );
        assertEquals( 2,
                      results.size() );
        assertEquals( 2,
                      results.getIdentifiers().length );
        Set newSet = new HashSet();
        for ( org.drools.runtime.rule.QueryResultsRow result : results ) {
            list = new ArrayList();
            list.add( result.get( "stilton" ) );
            list.add( result.get( "cheddar" ) );
            newSet.add( list );
        }
        assertEquals( set,
                      newSet );
    }

    @Test
    public void testManualFireAllRules() throws Exception {
        String str = "";
        str += "package org.drools \n";
        str += "import org.drools.Cheese \n";
        str += "global java.util.List list1 \n";
        str += "rule rule1 \n";
        str += "  when \n";
        str += "    $c : Cheese() \n";
        str += " \n";
        str += "  then \n";
        str += "    $c.setPrice( $c.getPrice() + 5 ); \n";
        str += "    list1.add( $c );";
        str += "end\n";

        String inXml = "";
        inXml += "{\"batch-execution\":{\"commands\":[";
        inXml += "   {\"set-global\":{\"identifier\":\"list1\",\"out\"=true";
        inXml += "                   ,\"object\":{\"list\":{\"object\":[]}}";
        inXml += "   } } "; //        
        inXml += ",  {\"insert-elements\":{\"objects\":[";        
        inXml += "   {   \"org.drools.Cheese\":{\"type\":\"stilton\",\"price\":25,\"oldPrice\":0}}, ";
        inXml += "   {   \"org.drools.Cheese\":{\"type\":\"stilton\",\"price\":30,\"oldPrice\":0}} ";        
        inXml += "   ]}}";
        inXml += ", {\"fire-all-rules\":\"\"}";  
        inXml += ", {\"insert\":{\"object\":{\"org.drools.Cheese\":{\"type\":\"brie\",\"price\":10,\"oldPrice\":0}}, \"out-identifier\":\"outBrie\" } }";        
        inXml += "]}}";                 
        inXml = roundTripFromXml( inXml );         
        
        StatelessKnowledgeSession ksession = getSession2( ResourceFactory.newByteArrayResource( str.getBytes() ) );
        ResultHandlerImpl resultHandler = new ResultHandlerImpl();
        getPipeline( ksession ).insert( inXml,
                                        resultHandler );
        String outXml = (String) resultHandler.getObject();

        FactHandle factHandle = (FactHandle) ((ExecutionResults) BatchExecutionHelper.newJSonMarshaller().fromXML( outXml )).getFactHandle( "outBrie" );

//        String expectedXml = "";
//        expectedXml += "<execution-results>\n";
//        expectedXml += "  <result identifier='list'>\n";
//        expectedXml += "    <list>\n";
//        expectedXml += "      <org.drools.Cheese>\n";
//        expectedXml += "        <type>stilton</type>\n";
//        expectedXml += "        <price>35</price>\n";
//        expectedXml += "        <oldPrice>0</oldPrice>\n";
//        expectedXml += "      </org.drools.Cheese>\n";
//        expectedXml += "      <org.drools.Cheese>\n";
//        expectedXml += "        <type>stilton</type>\n";
//        expectedXml += "        <price>30</price>\n";
//        expectedXml += "        <oldPrice>0</oldPrice>\n";
//        expectedXml += "      </org.drools.Cheese>\n";
//        expectedXml += "    </list>\n";
//        expectedXml += "  </result>\n";
//        expectedXml += "  <result identifier='outBrie'>\n";
//        expectedXml += "    <org.drools.Cheese>\n";
//        expectedXml += "      <type>brie</type>\n";
//        expectedXml += "      <price>10</price>\n";
//        expectedXml += "      <oldPrice>5</oldPrice>\n";
//        expectedXml += "    </org.drools.Cheese>\n";
//        expectedXml += "  </result>\n";
//        expectedXml += "  <fact-handle identifier=\"outBrie\" externalForm=\"" + factHandle.toExternalForm() + "\" /> \n";
//        expectedXml += "</execution-results>\n";
//        assertXMLEqual( expectedXml,
//                        outXml );

        ExecutionResults result = (ExecutionResults) BatchExecutionHelper.newJSonMarshaller().fromXML( outXml );
        result = (ExecutionResults) roundTripFromObject( result );

        // brie should not have been added to the list
        List list = (List) result.getValue( "list1" );
        Cheese stilton25 = new Cheese( "stilton",
                                       30 );
        Cheese stilton30 = new Cheese( "stilton",
                                       35 );

        Set expectedList = new HashSet();
        expectedList.add( stilton25 );
        expectedList.add( stilton30 );

        assertEquals( expectedList,
                      new HashSet( list ) );

        // brie should not have changed
        Cheese brie10 = new Cheese( "brie",
                                    10 );
        brie10.setOldPrice( 5 );
        assertEquals( brie10,
                      result.getValue( "outBrie" ) );
    }

    @Test
    public void testProcess() throws SAXException,
                             IOException {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        String str = "";
        str += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        str += "<process xmlns=\"http://drools.org/drools-5.0/process\"\n";
        str += "         xmlns:xs=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        str += "         xs:schemaLocation=\"http://drools.org/drools-5.0/process drools-processes-5.0.xsd\"\n";
        str += "         type=\"RuleFlow\" name=\"flow\" id=\"org.drools.actions\" package-name=\"org.drools\" version=\"1\" >\n";
        str += "\n";
        str += "  <header>\n";
        str += "    <imports>\n";
        str += "      <import name=\"org.drools.TestVariable\" />\n";
        str += "    </imports>\n";
        str += "    <globals>\n";
        str += "      <global identifier=\"list1\" type=\"java.util.List\" />\n";
        str += "    </globals>\n";
        str += "    <variables>\n";
        str += "      <variable name=\"person\" >\n";
        str += "        <type name=\"org.drools.process.core.datatype.impl.type.ObjectDataType\" className=\"TestVariable\" />\n";
        str += "      </variable>\n";
        str += "    </variables>\n";
        str += "  </header>\n";
        str += "\n";
        str += "  <nodes>\n";
        str += "    <start id=\"1\" name=\"Start\" />\n";
        str += "    <actionNode id=\"2\" name=\"MyActionNode\" >\n";
        str += "      <action type=\"expression\" dialect=\"mvel\" >System.out.println(\"Triggered\");\n";
        str += "list1.add(person.name);\n";
        str += "</action>\n";
        str += "    </actionNode>\n";
        str += "    <end id=\"3\" name=\"End\" />\n";
        str += "  </nodes>\n";
        str += "\n";
        str += "  <connections>\n";
        str += "    <connection from=\"1\" to=\"2\" />\n";
        str += "    <connection from=\"2\" to=\"3\" />\n";
        str += "  </connections>\n" + "\n";
        str += "</process>";

        Reader source = new StringReader( str );
        kbuilder.add( ResourceFactory.newReaderResource( source ),
                      ResourceType.DRF );
        if ( kbuilder.hasErrors() ) {
            fail( kbuilder.getErrors().toString() );
        }

        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );

        StatelessKnowledgeSession ksession = kbase.newStatelessKnowledgeSession();
        List<String> list = new ArrayList<String>();
        ksession.setGlobal( "list1",
                            list );
        TestVariable person = new TestVariable( "John Doe" );
        
        String inXml = "";
        inXml += "{\"batch-execution\":{\"commands\":[";
        inXml += "   {\"start-process\":";
        inXml += "      {\"process-id\":\"org.drools.actions\",";
        inXml += "     \"parameters\":[";
        inXml += "        {\"identifier\":\"person\",";
        inXml += "         \"object\":{\"@class\":\"org.drools.TestVariable\",";
        inXml += "                     \"name\":\"John Doe\"}}]}},";
        inXml += "    {\"get-global\":{\"identifier\":\"list1\",\"out-identifier\":\"out-list\"}}]}}";       
        inXml = roundTripFromXml( inXml );
        
        ResultHandlerImpl resultHandler = new ResultHandlerImpl();
        getPipeline( ksession ).insert( inXml,
                                        resultHandler );
        String outXml = (String) resultHandler.getObject();
        outXml = roundTripFromXml( outXml ); 
        
        ExecutionResults result = (ExecutionResults) BatchExecutionHelper.newJSonMarshaller().fromXML( outXml );
        result = (ExecutionResults) roundTripFromObject( result );
        list = (List) result.getValue( "out-list" );
        
        assertEquals( 1,
                      list.size() );
        assertEquals( "John Doe",
                      list.get( 0 ) );

//        String expectedXml = "";
//        expectedXml += "<execution-results>\n";
//        expectedXml += "  <result identifier=\"out-list\">\n";
//        expectedXml += "    <list>\n";
//        expectedXml += "      <string>John Doe</string>\n";
//        expectedXml += "    </list>\n";
//        expectedXml += "  </result>\n";
//        expectedXml += "</execution-results>\n";
//
//        assertXMLEqual( expectedXml,
//                        outXml );
    }

    @Test
    public void testProcessInstanceSignalEvent() throws Exception {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        String str = "";
        str += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        str += "<process xmlns=\"http://drools.org/drools-5.0/process\"\n";
        str += "         xmlns:xs=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        str += "         xs:schemaLocation=\"http://drools.org/drools-5.0/process drools-processes-5.0.xsd\"\n";
        str += "         type=\"RuleFlow\" name=\"flow\" id=\"org.drools.event\" package-name=\"org.drools\" version=\"1\" >\n";
        str += "\n";
        str += "  <header>\n";
        str += "    <variables>\n";
        str += "      <variable name=\"MyVar\" >\n";
        str += "        <type name=\"org.drools.process.core.datatype.impl.type.StringDataType\" />\n";
        str += "        <value>SomeText</value>\n";
        str += "      </variable>\n";
        str += "    </variables>\n";
        str += "  </header>\n";
        str += "\n";
        str += "  <nodes>\n";
        str += "    <start id=\"1\" name=\"Start\" />\n";
        str += "    <eventNode id=\"2\" name=\"Event\" variableName=\"MyVar\" >\n";
        str += "      <eventFilters>\n";
        str += "        <eventFilter type=\"eventType\" eventType=\"MyEvent\" />\n";
        str += "      </eventFilters>\n";
        str += "    </eventNode>\n";
        str += "    <join id=\"3\" name=\"Join\" type=\"1\" />\n";
        str += "    <end id=\"4\" name=\"End\" />\n";
        str += "  </nodes>\n";
        str += "\n";
        str += "  <connections>\n";
        str += "    <connection from=\"1\" to=\"3\" />\n";
        str += "    <connection from=\"2\" to=\"3\" />\n";
        str += "    <connection from=\"3\" to=\"4\" />\n";
        str += "  </connections>\n";
        str += "\n";
        str += "</process>";

        kbuilder.add( ResourceFactory.newByteArrayResource( str.getBytes() ),
                      ResourceType.DRF );

        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );

        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();

        ProcessInstance processInstance = ksession.startProcess( "org.drools.event" );
        assertEquals( ProcessInstance.STATE_ACTIVE,
                      processInstance.getState() );
        
        String inXml = "";
        inXml += "{\"batch-execution\":{\"commands\":[";
        inXml += "{\"signal-event\":{\"process-instance-id\":" + processInstance.getId() +",\"event-type\":\"MyEvent\"";
        inXml += "                   ,\"object\":{\"string\":[\"MyValue\"]} } }";
        inXml += "]}}";        
        inXml = roundTripFromXml( inXml );         

        getPipelineStateful( ksession ).insert( inXml,
                                                new ResultHandlerImpl() );

        assertEquals( ProcessInstance.STATE_COMPLETED,
                      processInstance.getState() );
        assertEquals( "MyValue",
                      ((VariableScopeInstance) ((org.jbpm.process.instance.ProcessInstance) processInstance).getContextInstance( VariableScope.VARIABLE_SCOPE )).getVariable( "MyVar" ) );
    }

    @Test
    public void testProcessRuntimeSignalEvent() throws Exception {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        String str = "";
        str += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        str += "<process xmlns=\"http://drools.org/drools-5.0/process\"\n";
        str += "         xmlns:xs=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        str += "         xs:schemaLocation=\"http://drools.org/drools-5.0/process drools-processes-5.0.xsd\"\n";
        str += "         type=\"RuleFlow\" name=\"flow\" id=\"org.drools.event\" package-name=\"org.drools\" version=\"1\" >\n";
        str += "\n";
        str += "  <header>\n";
        str += "    <variables>\n";
        str += "      <variable name=\"MyVar\" >\n";
        str += "        <type name=\"org.drools.process.core.datatype.impl.type.StringDataType\" />\n";
        str += "        <value>SomeText</value>\n";
        str += "      </variable>\n";
        str += "    </variables>\n";
        str += "  </header>\n";
        str += "\n";
        str += "  <nodes>\n";
        str += "    <start id=\"1\" name=\"Start\" />\n";
        str += "    <eventNode id=\"2\" name=\"Event\" scope=\"external\" variableName=\"MyVar\" >\n";
        str += "      <eventFilters>\n";
        str += "        <eventFilter type=\"eventType\" eventType=\"MyEvent\" />\n";
        str += "      </eventFilters>\n";
        str += "    </eventNode>\n";
        str += "    <join id=\"3\" name=\"Join\" type=\"1\" />\n";
        str += "    <end id=\"4\" name=\"End\" />\n";
        str += "  </nodes>\n";
        str += "\n";
        str += "  <connections>\n";
        str += "    <connection from=\"1\" to=\"3\" />\n";
        str += "    <connection from=\"2\" to=\"3\" />\n";
        str += "    <connection from=\"3\" to=\"4\" />\n";
        str += "  </connections>\n";
        str += "\n";
        str += "</process>";

        kbuilder.add( ResourceFactory.newByteArrayResource( str.getBytes() ),
                      ResourceType.DRF );

        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );

        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();

        ProcessInstance processInstance = ksession.startProcess( "org.drools.event" );
        assertEquals( ProcessInstance.STATE_ACTIVE,
                      processInstance.getState() );

        String inXml = "";
        inXml += "{\"batch-execution\":{\"commands\":[";
        inXml += "   {\"signal-event\":{\"event-type\":\"MyEvent\"";
        inXml += "                    ,\"object\":{\"string\":[\"MyValue\"]} } }";
        inXml += "]}}";        
        inXml = roundTripFromXml( inXml );             

        getPipelineStateful( ksession ).insert( inXml,
                                                new ResultHandlerImpl() );

        assertEquals( ProcessInstance.STATE_COMPLETED,
                      processInstance.getState() );
        assertEquals( "MyValue",
                      ((VariableScopeInstance) ((org.jbpm.process.instance.ProcessInstance) processInstance).getContextInstance( VariableScope.VARIABLE_SCOPE )).getVariable( "MyVar" ) );
    }

    @Test
    public void testCompleteWorkItem() {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        String str = "";
        str += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        str += "<process xmlns=\"http://drools.org/drools-5.0/process\"\n";
        str += "         xmlns:xs=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        str += "         xs:schemaLocation=\"http://drools.org/drools-5.0/process drools-processes-5.0.xsd\"\n";
        str += "         type=\"RuleFlow\" name=\"flow\" id=\"org.drools.actions\" package-name=\"org.drools\" version=\"1\" >\n";
        str += "\n";
        str += "  <header>\n";
        str += "    <variables>\n";
        str += "      <variable name=\"UserName\" >\n";
        str += "        <type name=\"org.drools.process.core.datatype.impl.type.StringDataType\" />\n";
        str += "        <value>John Doe</value>\n";
        str += "      </variable>\n";
        str += "      <variable name=\"Person\" >\n";
        str += "        <type name=\"org.drools.process.core.datatype.impl.type.ObjectDataType\" className=\"org.drools.Person\" />\n";
        str += "      </variable>\n";
        str += "      <variable name=\"MyObject\" >\n";
        str += "        <type name=\"org.drools.process.core.datatype.impl.type.ObjectDataType\" className=\"java.lang.Object\" />\n";
        str += "      </variable>\n";
        str += "      <variable name=\"Number\" >\n";
        str += "        <type name=\"org.drools.process.core.datatype.impl.type.IntegerDataType\" />\n";
        str += "      </variable>\n";
        str += "    </variables>\n";
        str += "  </header>\n";
        str += "\n";
        str += "  <nodes>\n";
        str += "    <start id=\"1\" name=\"Start\" />\n";
        str += "    <workItem id=\"2\" name=\"HumanTask\" >\n";
        str += "      <work name=\"Human Task\" >\n";
        str += "        <parameter name=\"ActorId\" >\n";
        str += "          <type name=\"org.drools.process.core.datatype.impl.type.StringDataType\" />\n";
        str += "          <value>#{UserName}</value>\n";
        str += "        </parameter>\n";
        str += "        <parameter name=\"Content\" >\n";
        str += "          <type name=\"org.drools.process.core.datatype.impl.type.StringDataType\" />\n";
        str += "          <value>#{Person.name}</value>\n";
        str += "        </parameter>\n";
        str += "        <parameter name=\"TaskName\" >\n";
        str += "          <type name=\"org.drools.process.core.datatype.impl.type.StringDataType\" />\n";
        str += "          <value>Do something</value>\n";
        str += "        </parameter>\n";
        str += "        <parameter name=\"Priority\" >\n";
        str += "          <type name=\"org.drools.process.core.datatype.impl.type.StringDataType\" />\n";
        str += "        </parameter>\n";
        str += "        <parameter name=\"Comment\" >\n";
        str += "          <type name=\"org.drools.process.core.datatype.impl.type.StringDataType\" />\n";
        str += "        </parameter>\n";
        str += "        <parameter name=\"Attachment\" >\n";
        str += "          <type name=\"org.drools.process.core.datatype.impl.type.ObjectDataType\" className=\"java.lang.Object\" />\n";
        str += "        </parameter>\n";
        str += "      </work>\n";
        str += "      <mapping type=\"in\" from=\"MyObject\" to=\"Attachment\" />";
        str += "      <mapping type=\"in\" from=\"Person.name\" to=\"Comment\" />";
        str += "      <mapping type=\"out\" from=\"Result\" to=\"MyObject\" />";
        str += "      <mapping type=\"out\" from=\"Result.length()\" to=\"Number\" />";
        str += "    </workItem>\n";
        str += "    <end id=\"3\" name=\"End\" />\n";
        str += "  </nodes>\n";
        str += "\n";
        str += "  <connections>\n";
        str += "    <connection from=\"1\" to=\"2\" />\n";
        str += "    <connection from=\"2\" to=\"3\" />\n";
        str += "  </connections>\n";
        str += "\n";
        str += "</process>";        
        
        Reader source = new StringReader( str );
        kbuilder.add( ResourceFactory.newReaderResource( source ),
                      ResourceType.DRF );

        Collection<KnowledgePackage> kpkgs = kbuilder.getKnowledgePackages();
        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addKnowledgePackages( kpkgs );
        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();

        TestWorkItemHandler handler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler( "Human Task",
                                                               handler );
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put( "UserName",
                        "John Doe" );
        Person person = new Person();
        person.setName( "John Doe" );
        parameters.put( "Person",
                        person );
        WorkflowProcessInstance processInstance = (WorkflowProcessInstance) ksession.startProcess( "org.drools.actions",
                                                                                                   parameters );
        assertEquals( ProcessInstance.STATE_ACTIVE,
                      processInstance.getState() );
        WorkItem workItem = handler.getWorkItem();
        assertNotNull( workItem );
        assertEquals( "John Doe",
                      workItem.getParameter( "ActorId" ) );
        assertEquals( "John Doe",
                      workItem.getParameter( "Content" ) );
        assertEquals( "John Doe",
                      workItem.getParameter( "Comment" ) );

        assertEquals( WorkItem.PENDING,
                      workItem.getState() );

        String inXml = "";
        inXml = "{\"complete-work-item\":{\"id\":" + workItem.getId() + "}}";                
        inXml = roundTripFromXml( inXml );
        getPipelineStateful( ksession ).insert( inXml,
                                                new ResultHandlerImpl() );

        assertEquals( WorkItem.COMPLETED,
                      workItem.getState() );

        assertEquals( ProcessInstance.STATE_COMPLETED,
                      processInstance.getState() );

        parameters = new HashMap<String, Object>();
        parameters.put( "UserName",
                        "Jane Doe" );
        parameters.put( "MyObject",
                        "SomeString" );
        person = new Person();
        person.setName( "Jane Doe" );
        parameters.put( "Person",
                        person );
        processInstance = (WorkflowProcessInstance) ksession.startProcess( "org.drools.actions",
                                                                           parameters );
        assertEquals( ProcessInstance.STATE_ACTIVE,
                      processInstance.getState() );
        workItem = handler.getWorkItem();
        assertNotNull( workItem );
        assertEquals( "Jane Doe",
                      workItem.getParameter( "ActorId" ) );
        assertEquals( "SomeString",
                      workItem.getParameter( "Attachment" ) );
        assertEquals( "Jane Doe",
                      workItem.getParameter( "Content" ) );
        assertEquals( "Jane Doe",
                      workItem.getParameter( "Comment" ) );

        assertEquals( WorkItem.PENDING,
                      workItem.getState() );
           
        inXml = "{\"complete-work-item\":{\"id\":" + workItem.getId() + ",\"results\":{\"identifier\":\"Result\",\"object\":{\"@class\":\"string\",\"$\":\"SomeOtherString\"}}}}";
        inXml = roundTripFromXml( inXml );
        
        getPipelineStateful( ksession ).insert( inXml,
                                                new ResultHandlerImpl() );

        assertEquals( WorkItem.COMPLETED,
                      workItem.getState() );

        assertEquals( ProcessInstance.STATE_COMPLETED,
                      processInstance.getState() );
        assertEquals( "SomeOtherString",
                      processInstance.getVariable( "MyObject" ) );
        assertEquals( 15,
                      processInstance.getVariable( "Number" ) );
    }

    @Test
    public void testAbortWorkItem() {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        String str = "";
        str += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        str += "<process xmlns=\"http://drools.org/drools-5.0/process\"\n";
        str += "         xmlns:xs=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        str += "         xs:schemaLocation=\"http://drools.org/drools-5.0/process drools-processes-5.0.xsd\"\n";
        str += "         type=\"RuleFlow\" name=\"flow\" id=\"org.drools.actions\" package-name=\"org.drools\" version=\"1\" >\n";
        str += "\n";
        str += "  <header>\n";
        str += "    <variables>\n";
        str += "      <variable name=\"UserName\" >\n";
        str += "        <type name=\"org.drools.process.core.datatype.impl.type.StringDataType\" />\n";
        str += "        <value>John Doe</value>\n";
        str += "      </variable>\n";
        str += "      <variable name=\"Person\" >\n";
        str += "        <type name=\"org.drools.process.core.datatype.impl.type.ObjectDataType\" className=\"org.drools.Person\" />\n";
        str += "      </variable>\n";
        str += "      <variable name=\"MyObject\" >\n";
        str += "        <type name=\"org.drools.process.core.datatype.impl.type.ObjectDataType\" className=\"java.lang.Object\" />\n";
        str += "      </variable>\n";
        str += "      <variable name=\"Number\" >\n";
        str += "        <type name=\"org.drools.process.core.datatype.impl.type.IntegerDataType\" />\n";
        str += "      </variable>\n";
        str += "    </variables>\n";
        str += "  </header>\n";
        str += "\n";
        str += "  <nodes>\n";
        str += "    <start id=\"1\" name=\"Start\" />\n";
        str += "    <workItem id=\"2\" name=\"HumanTask\" >\n";
        str += "      <work name=\"Human Task\" >\n";
        str += "        <parameter name=\"ActorId\" >\n";
        str += "          <type name=\"org.drools.process.core.datatype.impl.type.StringDataType\" />\n";
        str += "          <value>#{UserName}</value>\n";
        str += "        </parameter>\n";
        str += "        <parameter name=\"Content\" >\n";
        str += "          <type name=\"org.drools.process.core.datatype.impl.type.StringDataType\" />\n";
        str += "          <value>#{Person.name}</value>\n";
        str += "        </parameter>\n";
        str += "        <parameter name=\"TaskName\" >\n";
        str += "          <type name=\"org.drools.process.core.datatype.impl.type.StringDataType\" />\n";
        str += "          <value>Do something</value>\n";
        str += "        </parameter>\n";
        str += "        <parameter name=\"Priority\" >\n";
        str += "          <type name=\"org.drools.process.core.datatype.impl.type.StringDataType\" />\n";
        str += "        </parameter>\n";
        str += "        <parameter name=\"Comment\" >\n";
        str += "          <type name=\"org.drools.process.core.datatype.impl.type.StringDataType\" />\n";
        str += "        </parameter>\n";
        str += "        <parameter name=\"Attachment\" >\n";
        str += "          <type name=\"org.drools.process.core.datatype.impl.type.ObjectDataType\" className=\"java.lang.Object\" />\n";
        str += "        </parameter>\n";
        str += "      </work>\n";
        str += "      <mapping type=\"in\" from=\"MyObject\" to=\"Attachment\" />";
        str += "      <mapping type=\"in\" from=\"Person.name\" to=\"Comment\" />";
        str += "      <mapping type=\"out\" from=\"Result\" to=\"MyObject\" />";
        str += "      <mapping type=\"out\" from=\"Result.length()\" to=\"Number\" />";
        str += "    </workItem>\n";
        str += "    <end id=\"3\" name=\"End\" />\n";
        str += "  </nodes>\n";
        str += "\n";
        str += "  <connections>\n";
        str += "    <connection from=\"1\" to=\"2\" />\n";
        str += "    <connection from=\"2\" to=\"3\" />\n";
        str += "  </connections>\n";
        str += "\n";
        str += "</process>";

        Reader source = new StringReader( str );
        kbuilder.add( ResourceFactory.newReaderResource( source ),
                      ResourceType.DRF );

        Collection<KnowledgePackage> kpkgs = kbuilder.getKnowledgePackages();
        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addKnowledgePackages( kpkgs );
        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();

        TestWorkItemHandler handler = new TestWorkItemHandler();
        ksession.getWorkItemManager().registerWorkItemHandler( "Human Task",
                                                               handler );
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put( "UserName",
                        "John Doe" );
        Person person = new Person();
        person.setName( "John Doe" );
        parameters.put( "Person",
                        person );
        WorkflowProcessInstance processInstance = (WorkflowProcessInstance) ksession.startProcess( "org.drools.actions",
                                                                                                   parameters );
        assertEquals( ProcessInstance.STATE_ACTIVE,
                      processInstance.getState() );
        WorkItem workItem = handler.getWorkItem();
        assertNotNull( workItem );

        assertEquals( WorkItem.PENDING,
                      workItem.getState() );

        String inXml = "{\"abort-work-item\":{\"id\":" + workItem.getId() + "}}";
        inXml = roundTripFromXml( inXml );
        getPipelineStateful( ksession ).insert( inXml,
                                                new ResultHandlerImpl() );

        assertEquals( WorkItem.ABORTED,
                      workItem.getState() );
    }

    public static class TestWorkItemHandler
        implements
        WorkItemHandler {
        private WorkItem workItem;

        public void executeWorkItem(WorkItem workItem,
                                    WorkItemManager manager) {
            this.workItem = workItem;
        }

        public void abortWorkItem(WorkItem workItem,
                                  WorkItemManager manager) {
        }

        public WorkItem getWorkItem() {
            return workItem;
        }
    }

    @Test
    public void testInsertObjectWithDeclaredFact() throws Exception {
        String str = "";
        str += "package org.foo \n";
        str += "declare Whee \n\ttype: String\n\tprice: Integer\n\toldPrice: Integer\nend\n";
        str += "rule rule1 \n";
        str += "  when \n";
        str += "    $c : Whee() \n";
        str += " \n";
        str += "  then \n";
        str += "    $c.setPrice( $c.getPrice() + 5 ); \n";
        str += "end\n";

        String inXml = "";
        inXml += "{\"batch-execution\":{\"commands\":[";
        inXml += "{\"insert\":{\"object\":{\"org.foo.Whee\":{\"type\":\"stilton\",\"price\":25,\"oldPrice\":0}}, \"out-identifier\":\"outStilton\" } }";
        inXml += ", {\"fire-all-rules\":\"\"}";
        inXml += "]}}";        
        //inXml = roundTripFromXml( inXml );   // can't round trip, as dosn't have the correct class loader       
        
        StatefulKnowledgeSession ksession = getSessionStateful( ResourceFactory.newByteArrayResource( str.getBytes() ) );
        ResultHandlerImpl resultHandler = new ResultHandlerImpl();
        getPipelineStateful( ksession ).insert( inXml,
                                                resultHandler );
        String outXml = (String) resultHandler.getObject();

        ClassLoader cl = ((InternalRuleBase) ((StatefulKnowledgeSessionImpl) ksession).getRuleBase()).getRootClassLoader();
        XStream xstream = BatchExecutionHelper.newJSonMarshaller();
        xstream.setClassLoader( cl );
        FactHandle factHandle = (FactHandle) ((ExecutionResults) xstream.fromXML( outXml )).getFactHandle( "outStilton" );        
        assertNotNull( factHandle );
        
        Object object = ((ExecutionResults) xstream.fromXML( outXml )).getValue( "outStilton" );        
        assertEquals( "org.foo.Whee", object.getClass().getName() );

//        String expectedXml = "";
//        expectedXml += "<execution-results>\n";
//        expectedXml += "  <result identifier=\"outStilton\">\n";
//        expectedXml += "    <org.foo.Whee>\n";
//        expectedXml += "      <type>stilton</type>\n";
//        expectedXml += "      <oldPrice>0</oldPrice>\n";
//        expectedXml += "      <price>30</price>\n";
//        expectedXml += "    </org.foo.Whee>\n";
//        expectedXml += "  </result>\n";
//        expectedXml += "  <fact-handle identifier=\"outStilton\" externalForm=\"" + factHandle.toExternalForm() + "\" /> \n";
//        expectedXml += "</execution-results>\n";
//
//        assertXMLEqual( expectedXml,
//                        outXml );

    }

    @Test
    public void testInsertObjectStateful() throws Exception {
        String str = "";
        str += "package org.foo \n";
        str += "declare Whee \n\ttype: String\n\tprice: Integer\n\toldPrice: Integer\nend\n";
        str += "rule rule1 \n";
        str += "  when \n";
        str += "    $c : Whee(price < 30) \n";
        str += " \n";
        str += "  then \n";
        str += "    $c.setPrice( $c.getPrice() + 5 ); \n update($c);\n";
        str += "end\n";
        str += "query results\n";
        str += "    w: Whee(price > 0)";
        str += "end\n";
        
        String inXml = "";
        inXml += "{\"batch-execution\":{\"commands\":[";
        inXml += "{\"insert\":{\"object\":{\"org.foo.Whee\":{\"type\":\"stilton\",\"price\":25,\"oldPrice\":0}} } }";
        inXml += "]}}";        
        //inXml = roundTripFromXml( inXml );   // can't round trip, as dosn't have the correct class loader           

        StatefulKnowledgeSession ksession = getSessionStateful( ResourceFactory.newByteArrayResource( str.getBytes() ) );
        ResultHandlerImpl resultHandler = new ResultHandlerImpl();
        getPipelineStateful( ksession ).insert( inXml,
                                                resultHandler );
        inXml = "{\"batch-execution\":{\"commands\":[";
        inXml += "   {\"query\":{\"out-identifier\":\"matchingthings\",\"name\":\"results\"}}";
        inXml += "]}}";         
        getPipelineStateful( ksession ).insert( inXml,
                                                resultHandler );
        String outXml = (String) resultHandler.getObject();

        //we have not fired the rules yet
        assertTrue( outXml.indexOf( "\"price\":25" ) >= 0 );

        getPipelineStateful( ksession ).insert( "{\"fire-all-rules\":\"\"}",
                                                resultHandler );

        //ok lets try that again...
        inXml = "{\"batch-execution\":{\"commands\":[";
        inXml += "   {\"query\":{\"out-identifier\":\"matchingthings\",\"name\":\"results\"}}";
        inXml += "]}}";          
        getPipelineStateful( ksession ).insert( inXml,
                                                resultHandler );
        outXml = (String) resultHandler.getObject();
        assertTrue( outXml.indexOf( "\"price\":30" ) >= 0 );
    }

    @Test
    public void testVsmPipeline() throws Exception {
        String str = "";
        str += "package org.drools \n";
        str += "import org.drools.Cheese \n";
        str += "rule rule1 \n";
        str += "  when \n";
        str += "    $c : Cheese() \n";
        str += " \n";
        str += "  then \n";
        str += "    $c.setPrice( $c.getPrice() + 5 ); \n";
        str += "end\n";

        String inXml = "";
        inXml += "{\"batch-execution\":{\"lookup\":\"ksession1\", \"commands\":[";
        inXml += "{\"insert\":{\"object\":{\"org.drools.Cheese\":{\"type\":\"stilton\",\"price\":25,\"oldPrice\":0}}, \"out-identifier\":\"outStilton\" } }";
        inXml += ", {\"fire-all-rules\":\"\"}";
        inXml += "]}}";        
        inXml = roundTripFromXml( inXml );        
        
        GridImpl grid = new GridImpl( new HashMap() );
        grid.addService( WhitePages.class, new WhitePagesImpl() );
        GridNode node = grid.createGridNode( "local" );
        
        StatefulKnowledgeSession ksession = getExecutionNodeSessionStateful(node, ResourceFactory.newByteArrayResource( str.getBytes() ) );

        node.set( "ksession1", ksession );

        XStreamResolverStrategy xstreamStrategy = new XStreamResolverStrategy() {
            public XStream lookup(String name) {
                return BatchExecutionHelper.newJSonMarshaller();
            }
        };

        ResultHandlerImpl resultHandler = new ResultHandlerImpl();
        getPipelineSessionStateful(node, xstreamStrategy).insert(inXml, resultHandler);
        String outXml = (String) resultHandler.getObject();

        ExecutionResults result = (ExecutionResults) BatchExecutionHelper.newJSonMarshaller().fromXML( outXml );
        Cheese stilton = (Cheese) result.getValue( "outStilton" );
        assertEquals( 30,
                      stilton.getPrice() );

        FactHandle factHandle = (FactHandle) result.getFactHandle( "outStilton" );
        stilton = (Cheese) ksession.getObject( factHandle );
        assertEquals( 30,
                      stilton.getPrice() );

//        String expectedXml = "";
//        expectedXml += "<execution-results>\n";
//        expectedXml += "  <result identifier=\"outStilton\">\n";
//        expectedXml += "    <org.drools.Cheese>\n";
//        expectedXml += "      <type>stilton</type>\n";
//        expectedXml += "      <oldPrice>0</oldPrice>\n";
//        expectedXml += "      <price>30</price>\n";
//        expectedXml += "    </org.drools.Cheese>\n";
//        expectedXml += "  </result>\n";
//        expectedXml += "  <fact-handle identifier=\"outStilton\" externalForm=\"" + ((InternalFactHandle) result.getFactHandle( "outStilton" )).toExternalForm() + "\" /> \n";
//        expectedXml += "</execution-results>\n";
//
//        assertXMLEqual( expectedXml,
//                        outXml );
    }

    private Pipeline getPipeline(StatelessKnowledgeSession ksession) {
        Action executeResultHandler = PipelineFactory.newExecuteResultHandler();

        Action assignResult = PipelineFactory.newAssignObjectAsResult();
        assignResult.setReceiver( executeResultHandler );

        Transformer outTransformer = PipelineFactory.newXStreamToXmlTransformer( BatchExecutionHelper.newJSonMarshaller() );
        outTransformer.setReceiver( assignResult );

        KnowledgeRuntimeCommand batchExecution = PipelineFactory.newCommandExecutor();
        batchExecution.setReceiver( outTransformer );

        Transformer inTransformer = PipelineFactory.newXStreamFromXmlTransformer( BatchExecutionHelper.newJSonMarshaller() );
        inTransformer.setReceiver( batchExecution );

        Pipeline pipeline = PipelineFactory.newStatelessKnowledgeSessionPipeline( ksession );
        pipeline.setReceiver( inTransformer );

        return pipeline;
    }

 

    private Pipeline getPipelineStateful(StatefulKnowledgeSession ksession) {
        Action executeResultHandler = PipelineFactory.newExecuteResultHandler();

        Action assignResult = PipelineFactory.newAssignObjectAsResult();
        assignResult.setReceiver( executeResultHandler );

        Transformer outTransformer = PipelineFactory.newXStreamToXmlTransformer( BatchExecutionHelper.newJSonMarshaller() );
        outTransformer.setReceiver( assignResult );

        KnowledgeRuntimeCommand batchExecution = PipelineFactory.newCommandExecutor();
        batchExecution.setReceiver( outTransformer );

        Transformer inTransformer = PipelineFactory.newXStreamFromXmlTransformer( BatchExecutionHelper.newJSonMarshaller() );
        inTransformer.setReceiver( batchExecution );

        Pipeline pipeline = PipelineFactory.newStatefulKnowledgeSessionPipeline( ksession );
        pipeline.setReceiver( inTransformer );

        return pipeline;
    }
    
    private Pipeline getPipelineSessionStateful(GridNode node, XStreamResolverStrategy xstreamResolverStrategy) {
        Action executeResultHandler = PipelineFactory.newExecuteResultHandler();

        Action assignResult = PipelineFactory.newAssignObjectAsResult();
        assignResult.setReceiver( executeResultHandler );

        //Transformer outTransformer = PipelineFactory.newXStreamToXmlTransformer( BatchExecutionHelper.newXStreamMarshaller() );
        Transformer outTransformer = new XStreamToXmlGridTransformer();
        outTransformer.setReceiver( assignResult );

        KnowledgeRuntimeCommand batchExecution = PipelineFactory.newCommandExecutor();
        batchExecution.setReceiver( outTransformer );

        //Transformer inTransformer = PipelineFactory.newXStreamFromXmlTransformer( BatchExecutionHelper.newXStreamMarshaller() );
        Transformer inTransformer = new XStreamFromXmlGridTransformer( xstreamResolverStrategy );
        inTransformer.setReceiver( batchExecution );

//        Transformer domTransformer = new ToXmlNodeTransformer();
//        domTransformer.setReceiver( inTransformer );

        //Pipeline pipeline = PipelineFactory.newStatefulKnowledgeSessionPipeline( ksession );
        Pipeline pipeline = new ExecutionNodePipelineImpl( node );

        pipeline.setReceiver( inTransformer );

        return pipeline;
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

    private StatelessKnowledgeSession getSession2(Resource resource) throws Exception {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add( resource,
                      ResourceType.DRL );

        if ( kbuilder.hasErrors() ) {
            fail( kbuilder.getErrors().toString() );
        }

        assertFalse( kbuilder.hasErrors() );
        Collection<KnowledgePackage> pkgs = kbuilder.getKnowledgePackages();

        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();

        kbase.addKnowledgePackages( pkgs );
        StatelessKnowledgeSession session = kbase.newStatelessKnowledgeSession();

        return session;
    }

    private StatefulKnowledgeSession getSessionStateful(Resource resource) throws Exception {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add( resource,
                      ResourceType.DRL );

        if ( kbuilder.hasErrors() ) {
            System.out.println( kbuilder.getErrors() );
        }

        assertFalse( kbuilder.hasErrors() );
        Collection<KnowledgePackage> pkgs = kbuilder.getKnowledgePackages();

        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();

        kbase.addKnowledgePackages( pkgs );
        StatefulKnowledgeSession session = kbase.newStatefulKnowledgeSession();

        return session;
    }

    private StatefulKnowledgeSession getExecutionNodeSessionStateful(GridNode node, Resource resource) throws Exception {
        KnowledgeBuilder kbuilder = node.get(KnowledgeBuilderFactoryService.class).newKnowledgeBuilder();
        kbuilder.add( resource,
                      ResourceType.DRL );

        if ( kbuilder.hasErrors() ) {
            System.out.println( kbuilder.getErrors() );
        }

        assertFalse( kbuilder.hasErrors() );
        Collection<KnowledgePackage> pkgs = kbuilder.getKnowledgePackages();

        KnowledgeBase kbase = node.get(KnowledgeBaseFactoryService.class).newKnowledgeBase();

        kbase.addKnowledgePackages( pkgs );
        StatefulKnowledgeSession session = kbase.newStatefulKnowledgeSession();

        return session;
    }
    
    public String roundTripFromXml(String inXml) {
       Object object =  BatchExecutionHelper.newJSonMarshaller().fromXML( inXml );
        inXml = BatchExecutionHelper.newJSonMarshaller().toXML( object );
        object =  BatchExecutionHelper.newJSonMarshaller().fromXML( inXml );
        return BatchExecutionHelper.newJSonMarshaller().toXML( object );        
    }
    
    public Object roundTripFromObject(Object object) {
        String xml = BatchExecutionHelper.newJSonMarshaller().toXML( object );
        object =  BatchExecutionHelper.newJSonMarshaller().fromXML( xml );
        xml = BatchExecutionHelper.newJSonMarshaller().toXML( object );
        return BatchExecutionHelper.newJSonMarshaller().fromXML( xml );          
        
    }

}
