package org.drools.runtime.pipeline.impl;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLTestCase;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.examples.RecursiveElementNameAndTextQualifier;
import org.drools.Cheese;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.RuleBase;
import org.drools.RuleBaseFactory;
import org.drools.StatefulSession;
import org.drools.TestVariable;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.common.InternalFactHandle;
import org.drools.common.InternalRuleBase;
import org.drools.compiler.PackageBuilder;
import org.drools.definition.KnowledgePackage;
import org.drools.impl.StatefulKnowledgeSessionImpl;
import org.drools.integrationtests.SerializationHelper;
import org.drools.io.Resource;
import org.drools.io.ResourceFactory;
import org.drools.process.command.SignalEventCommand;
import org.drools.process.core.context.variable.VariableScope;
import org.drools.process.instance.context.variable.VariableScopeInstance;
import org.drools.rule.Package;
import org.drools.runtime.ExecutionResults;
import org.drools.runtime.StatelessKnowledgeSession;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.help.BatchExecutionHelper;
import org.drools.runtime.pipeline.Action;
import org.drools.runtime.pipeline.KnowledgeRuntimeCommand;
import org.drools.runtime.pipeline.Pipeline;
import org.drools.runtime.pipeline.PipelineFactory;
import org.drools.runtime.pipeline.ResultHandler;
import org.drools.runtime.pipeline.Transformer;
import org.drools.runtime.process.ProcessInstance;
import org.drools.runtime.rule.FactHandle;
import org.xml.sax.SAXException;

import com.thoughtworks.xstream.XStream;

public class XStreamBatchExecutionTest extends TestCase {

    protected void setUp() throws Exception {
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

    public void testInsertObjectWithDefaults() throws Exception {
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
        inXml += "<batch-execution>";
        inXml += "  <insert out-identifier='outStilton'>";
        inXml += "    <org.drools.Cheese>";
        inXml += "      <type>stilton</type>";
        inXml += "      <price>25</price>";
        inXml += "      <oldPrice>0</oldPrice>";
        inXml += "    </org.drools.Cheese>";
        inXml += "  </insert>";
        inXml += "  <fire-all-rules />";
        inXml += "</batch-execution>";

        StatefulKnowledgeSession ksession = getSessionStateful( ResourceFactory.newByteArrayResource( str.getBytes() ) );
        ResultHandlerImpl resultHandler = new ResultHandlerImpl();
        getPipelineStateful( ksession ).insert( inXml,
                                                resultHandler );
        String outXml = (String) resultHandler.getObject();

        ExecutionResults result = (ExecutionResults) BatchExecutionHelper.newXStreamMarshaller().fromXML( outXml );
        Cheese stilton = (Cheese) result.getValue( "outStilton" );
        assertEquals( 30,
                      stilton.getPrice() );

        FactHandle factHandle = (FactHandle) result.getFactHandle( "outStilton" );
        stilton = (Cheese) ksession.getObject( factHandle );
        assertEquals( 30,
                      stilton.getPrice() );

        String expectedXml = "";
        expectedXml += "<execution-results>\n";
        expectedXml += "  <result identifier=\"outStilton\">\n";
        expectedXml += "    <org.drools.Cheese>\n";
        expectedXml += "      <type>stilton</type>\n";
        expectedXml += "      <oldPrice>0</oldPrice>\n";
        expectedXml += "      <price>30</price>\n";
        expectedXml += "    </org.drools.Cheese>\n";
        expectedXml += "  </result>\n";
        expectedXml += "  <fact-handle identifier=\"outStilton\" externalForm=\"" + ((InternalFactHandle) result.getFactHandle( "outStilton" )).toExternalForm() + "\" /> \n";
        expectedXml += "</execution-results>\n";

        assertXMLEqual( expectedXml,
                        outXml );
    }

    public void testInsertObjectWithReturnObjectFalse() throws Exception {
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
        inXml += "<batch-execution>";
        inXml += "  <insert out-identifier='outStilton' return-object='false'>";
        inXml += "    <org.drools.Cheese>";
        inXml += "      <type>stilton</type>";
        inXml += "      <price>25</price>";
        inXml += "      <oldPrice>0</oldPrice>";
        inXml += "    </org.drools.Cheese>";
        inXml += "  </insert>";
        inXml += "  <fire-all-rules />";
        inXml += "</batch-execution>";

        StatefulKnowledgeSession ksession = getSessionStateful( ResourceFactory.newByteArrayResource( str.getBytes() ) );
        ResultHandlerImpl resultHandler = new ResultHandlerImpl();
        getPipelineStateful( ksession ).insert( inXml,
                                                resultHandler );
        String outXml = (String) resultHandler.getObject();

        ExecutionResults result = (ExecutionResults) BatchExecutionHelper.newXStreamMarshaller().fromXML( outXml );
        assertNull( result.getValue( "outStilton" ) );

        FactHandle factHandle = (FactHandle) result.getFactHandle( "outStilton" );
        Cheese stilton = (Cheese) ksession.getObject( factHandle );
        assertEquals( 30,
                      stilton.getPrice() );

        String expectedXml = "";
        expectedXml += "<execution-results>\n";
        expectedXml += "  <fact-handle identifier=\"outStilton\" externalForm=\"" + ((InternalFactHandle) result.getFactHandle( "outStilton" )).toExternalForm() + "\" /> \n";
        expectedXml += "</execution-results>\n";

        assertXMLEqual( expectedXml,
                        outXml );
    }

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
        inXml += "<batch-execution>";
        inXml += "  <insert out-identifier='outStilton'>";
        inXml += "    <org.drools.Cheese>";
        inXml += "      <type>stilton</type>";
        inXml += "      <price>25</price>";
        inXml += "      <oldPrice>0</oldPrice>";
        inXml += "    </org.drools.Cheese>";
        inXml += "  </insert>";
        inXml += "  <fire-all-rules />";
        inXml += "</batch-execution>";

        StatefulKnowledgeSession ksession = getSessionStateful( ResourceFactory.newByteArrayResource( str.getBytes() ) );
        ResultHandlerImpl resultHandler = new ResultHandlerImpl();
        getPipelineStateful( ksession ).insert( inXml,
                                                resultHandler );
        String outXml = (String) resultHandler.getObject();

        ExecutionResults result = (ExecutionResults) BatchExecutionHelper.newXStreamMarshaller().fromXML( outXml );
        Cheese stilton = (Cheese) result.getValue( "outStilton" );
        assertEquals( 30,
                      stilton.getPrice() );

        FactHandle factHandle = (FactHandle) result.getFactHandle( "outStilton" );
        inXml = "";
        inXml += "<batch-execution>";
        inXml += "  <get-object out-identifier='outStilton' factHandle='" + factHandle.toExternalForm() + "' />";
        inXml += "</batch-execution>";
        getPipelineStateful( ksession ).insert( inXml,
                                                resultHandler );
        outXml = (String) resultHandler.getObject();
        result = (ExecutionResults) BatchExecutionHelper.newXStreamMarshaller().fromXML( outXml );
        stilton = (Cheese) result.getValue( "outStilton" );
        assertEquals( 30,
                      stilton.getPrice() );
    }

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
        inXml += "<batch-execution>";
        inXml += "  <insert out-identifier='outStilton'>";
        inXml += "    <org.drools.Cheese>";
        inXml += "      <type>stilton</type>";
        inXml += "      <price>25</price>";
        inXml += "      <oldPrice>0</oldPrice>";
        inXml += "    </org.drools.Cheese>";
        inXml += "  </insert>";
        inXml += "  <fire-all-rules />";
        inXml += "</batch-execution>";

        StatefulKnowledgeSession ksession = getSessionStateful( ResourceFactory.newByteArrayResource( str.getBytes() ) );
        ResultHandlerImpl resultHandler = new ResultHandlerImpl();
        getPipelineStateful( ksession ).insert( inXml,
                                                resultHandler );
        String outXml = (String) resultHandler.getObject();

        ExecutionResults result = (ExecutionResults) BatchExecutionHelper.newXStreamMarshaller().fromXML( outXml );
        Cheese stilton = (Cheese) result.getValue( "outStilton" );
        assertEquals( 30,
                      stilton.getPrice() );

        FactHandle factHandle = (FactHandle) result.getFactHandle( "outStilton" );
        inXml = "";
        inXml += "<batch-execution>";
        inXml += "  <retract factHandle='" + factHandle.toExternalForm() + "' />";
        inXml += "</batch-execution>";
        getPipelineStateful( ksession ).insert( inXml,
                                                resultHandler );

        inXml = "";
        inXml += "<batch-execution>";
        inXml += "  <get-object out-identifier='outStilton' factHandle='" + factHandle.toExternalForm() + "' />";
        inXml += "</batch-execution>";
        getPipelineStateful( ksession ).insert( inXml,
                                                resultHandler );
        outXml = (String) resultHandler.getObject();
        result = (ExecutionResults) BatchExecutionHelper.newXStreamMarshaller().fromXML( outXml );
        assertNull( result.getValue( "outStilton" ) );
    }

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
        inXml += "<batch-execution>";
        inXml += "  <insert out-identifier='outStilton'>";
        inXml += "    <org.drools.Cheese>";
        inXml += "      <type>stilton</type>";
        inXml += "      <price>25</price>";
        inXml += "      <oldPrice>0</oldPrice>";
        inXml += "    </org.drools.Cheese>";
        inXml += "  </insert>";
        inXml += "  <fire-all-rules />";
        inXml += "</batch-execution>";

        StatefulKnowledgeSession ksession = getSessionStateful( ResourceFactory.newByteArrayResource( str.getBytes() ) );
        ResultHandlerImpl resultHandler = new ResultHandlerImpl();
        getPipelineStateful( ksession ).insert( inXml,
                                                resultHandler );
        String outXml = (String) resultHandler.getObject();

        ExecutionResults result = (ExecutionResults) BatchExecutionHelper.newXStreamMarshaller().fromXML( outXml );
        Cheese stilton = (Cheese) result.getValue( "outStilton" );
        assertEquals( 30,
                      stilton.getPrice() );

        FactHandle factHandle = ((FactHandle) result.getFactHandle( "outStilton" ));

        String expectedXml = "";
        expectedXml += "<execution-results>\n";
        expectedXml += "  <result identifier=\"outStilton\">\n";
        expectedXml += "    <org.drools.Cheese>\n";
        expectedXml += "      <type>stilton</type>\n";
        expectedXml += "      <oldPrice>0</oldPrice>\n";
        expectedXml += "      <price>30</price>\n";
        expectedXml += "    </org.drools.Cheese>\n";
        expectedXml += "  </result>\n";
        expectedXml += "  <fact-handle identifier=\"outStilton\" externalForm=\"" + factHandle.toExternalForm() + "\" /> \n";
        expectedXml += "</execution-results>\n";

        assertXMLEqual( expectedXml,
                        outXml );

        inXml = "";
        inXml += "<batch-execution>";
        inXml += "  <modify factHandle='" + factHandle.toExternalForm() + "'> <set accessor='type' value='\"cheddar\"' /><set accessor='price' value='50' /></modify>";
        inXml += "  <fire-all-rules />";
        inXml += "</batch-execution>";
        getPipelineStateful( ksession ).insert( inXml,
                                                resultHandler );

        inXml = "";
        inXml += "<batch-execution>";
        inXml += "  <get-object out-identifier='outCheddar' factHandle='" + factHandle.toExternalForm() + "' />";
        inXml += "</batch-execution>";
        getPipelineStateful( ksession ).insert( inXml,
                                                resultHandler );
        outXml = (String) resultHandler.getObject();
        result = (ExecutionResults) BatchExecutionHelper.newXStreamMarshaller().fromXML( outXml );
        Cheese cheddar = (Cheese) result.getValue( "outCheddar" );
        assertEquals( "cheddar",
                      cheddar.getType() );
        assertEquals( 55,
                      cheddar.getPrice() );

    }

    public void testInsertElements() throws Exception {
        String str = "";
        str += "package org.drools \n";
        str += "import org.drools.Cheese \n";
        str += "global java.util.List list \n";
        str += "rule rule1 \n";
        str += "  when \n";
        str += "    $c : Cheese() \n";
        str += " \n";
        str += "  then \n";
        str += "    $c.setPrice( $c.getPrice() + 5 ); \n";
        str += "     list.add( $c );";
        str += "end\n";

        String inXml = "";
        inXml += "<batch-execution>";
        inXml += "  <set-global identifier='list' out='true'>";
        inXml += "    <list/>";
        inXml += "  </set-global>";
        inXml += "  <insert-elements>";
        inXml += "    <org.drools.Cheese>";
        inXml += "      <type>stilton</type>";
        inXml += "      <price>25</price>";
        inXml += "      <oldPrice>0</oldPrice>";
        inXml += "    </org.drools.Cheese>";
        inXml += "    <org.drools.Cheese>";
        inXml += "      <type>stilton</type>";
        inXml += "      <price>30</price>";
        inXml += "      <oldPrice>0</oldPrice>";
        inXml += "    </org.drools.Cheese>";
        inXml += "  </insert-elements>";
        inXml += "</batch-execution>";

        StatelessKnowledgeSession ksession = getSession2( ResourceFactory.newByteArrayResource( str.getBytes() ) );
        ResultHandlerImpl resultHandler = new ResultHandlerImpl();
        getPipeline( ksession ).insert( inXml,
                                        resultHandler );
        String outXml = (String) resultHandler.getObject();

        String expectedXml = "";
        expectedXml += "<execution-results>\n";
        expectedXml += "  <result identifier='list'>\n";
        expectedXml += "    <list>\n";
        expectedXml += "      <org.drools.Cheese>\n";
        expectedXml += "        <type>stilton</type>\n";
        expectedXml += "        <price>35</price>\n";
        expectedXml += "        <oldPrice>0</oldPrice>\n";
        expectedXml += "      </org.drools.Cheese>\n";
        expectedXml += "      <org.drools.Cheese>\n";
        expectedXml += "        <type>stilton</type>\n";
        expectedXml += "        <price>30</price>\n";
        expectedXml += "        <oldPrice>0</oldPrice>\n";
        expectedXml += "      </org.drools.Cheese>\n";
        expectedXml += "    </list>\n";
        expectedXml += "  </result>\n";
        expectedXml += "</execution-results>\n";

        assertXMLEqual( expectedXml,
                        outXml );

        ExecutionResults result = (ExecutionResults) BatchExecutionHelper.newXStreamMarshaller().fromXML( outXml );

        List list = (List) result.getValue( "list" );
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
        inXml += "<batch-execution>";
        inXml += "  <set-global identifier='list1'>";
        inXml += "    <list/>";
        inXml += "  </set-global>";
        inXml += "  <set-global identifier='list2' out='true'>";
        inXml += "    <list/>";
        inXml += "  </set-global>";
        inXml += "  <set-global identifier='list3' out-identifier='outList3'>";
        inXml += "    <list/>";
        inXml += "  </set-global>";
        inXml += "  <insert>";
        inXml += "    <org.drools.Cheese>";
        inXml += "      <type>stilton</type>";
        inXml += "      <price>5</price>";
        inXml += "      <oldPrice>0</oldPrice>";
        inXml += "  </org.drools.Cheese>";
        inXml += "  </insert>";
        inXml += "</batch-execution>";

        StatelessKnowledgeSession ksession = getSession2( ResourceFactory.newByteArrayResource( str.getBytes() ) );
        ResultHandlerImpl resultHandler = new ResultHandlerImpl();
        getPipeline( ksession ).insert( inXml,
                                        resultHandler );

        String outXml = (String) resultHandler.getObject();

        String expectedXml = "";
        expectedXml += "<execution-results>\n";
        expectedXml += "  <result identifier='list2'>\n";
        expectedXml += "    <list>\n";
        expectedXml += "      <org.drools.Cheese>\n";
        expectedXml += "        <type>stilton</type>\n";
        expectedXml += "        <price>30</price>\n";
        expectedXml += "        <oldPrice>0</oldPrice>\n";
        expectedXml += "      </org.drools.Cheese>\n";
        expectedXml += "    </list>\n";
        expectedXml += "  </result>\n";
        expectedXml += "  <result identifier='outList3'>\n";
        expectedXml += "    <list>\n";
        expectedXml += "      <org.drools.Cheese reference='../../../result/list/org.drools.Cheese'/>\n";
        expectedXml += "    </list>\n";
        expectedXml += "  </result>\n";
        expectedXml += "</execution-results>\n";

        assertXMLEqual( expectedXml,
                        outXml );

        ExecutionResults result = (ExecutionResults) BatchExecutionHelper.newXStreamMarshaller().fromXML( outXml );
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

    public void testGetGlobal() throws Exception {
        String str = "";
        str += "package org.drools \n";
        str += "import org.drools.Cheese \n";
        str += "global java.util.List list \n";
        str += "rule rule1 \n";
        str += "  when \n";
        str += "    $c : Cheese() \n";
        str += " \n";
        str += "  then \n";
        str += "    list.add( $c ); \n";
        str += "end\n";

        String inXml = "";
        inXml += "<batch-execution>";
        inXml += "  <set-global identifier='list'>";
        inXml += "    <list/>";
        inXml += "  </set-global>";
        inXml += "  <insert>";
        inXml += "    <org.drools.Cheese>";
        inXml += "      <type>stilton</type>";
        inXml += "      <price>25</price>";
        inXml += "      <oldPrice>0</oldPrice>";
        inXml += "    </org.drools.Cheese>";
        inXml += "  </insert>";
        inXml += "  <get-global identifier='list' out-identifier='out-list'/>";
        inXml += "</batch-execution>";

        StatelessKnowledgeSession ksession = getSession2( ResourceFactory.newByteArrayResource( str.getBytes() ) );
        ResultHandlerImpl resultHandler = new ResultHandlerImpl();
        getPipeline( ksession ).insert( inXml,
                                        resultHandler );
        String outXml = (String) resultHandler.getObject();

        String expectedXml = "";
        expectedXml += "<execution-results>\n";
        expectedXml += "  <result identifier=\"out-list\">\n";
        expectedXml += "    <list>\n";
        expectedXml += "      <org.drools.Cheese>\n";
        expectedXml += "        <type>stilton</type>\n";
        expectedXml += "        <price>25</price>\n";
        expectedXml += "        <oldPrice>0</oldPrice>\n";
        expectedXml += "      </org.drools.Cheese>\n";
        expectedXml += "    </list>\n";
        expectedXml += "  </result>\n";
        expectedXml += "</execution-results>\n";

        assertXMLEqual( expectedXml,
                        outXml );
    }

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

        String inXml = "";
        inXml += "<batch-execution>";
        inXml += "  <insert-elements>";
        inXml += "    <org.drools.Cheese>";
        inXml += "      <type>stilton</type>";
        inXml += "      <price>25</price>";
        inXml += "      <oldPrice>0</oldPrice>";
        inXml += "    </org.drools.Cheese>";
        inXml += "    <org.drools.Cheese>";
        inXml += "      <type>stilton</type>";
        inXml += "      <price>30</price>";
        inXml += "      <oldPrice>0</oldPrice>";
        inXml += "    </org.drools.Cheese>";
        inXml += "  </insert-elements>";
        inXml += "  <get-objects out-identifier='list' />";
        inXml += "</batch-execution>";

        StatelessKnowledgeSession ksession = getSession2( ResourceFactory.newByteArrayResource( str.getBytes() ) );
        ResultHandlerImpl resultHandler = new ResultHandlerImpl();
        getPipeline( ksession ).insert( inXml,
                                        resultHandler );
        String outXml = (String) resultHandler.getObject();

        String expectedXml = "";
        expectedXml += "<execution-results>";
        expectedXml += "  <result identifier='list'>";
        expectedXml += "    <list>";
        expectedXml += "      <org.drools.Cheese>";
        expectedXml += "        <type>stilton</type>";
        expectedXml += "        <price>30</price>";
        expectedXml += "        <oldPrice>0</oldPrice>";
        expectedXml += "      </org.drools.Cheese>";
        expectedXml += "      <org.drools.Cheese>";
        expectedXml += "        <type>stilton</type>";
        expectedXml += "        <price>35</price>";
        expectedXml += "        <oldPrice>0</oldPrice>";
        expectedXml += "      </org.drools.Cheese>";
        expectedXml += "    </list>";
        expectedXml += "  </result>";
        expectedXml += "</execution-results>";

        assertXMLEqual( expectedXml,
                        outXml );

        ExecutionResults result = (ExecutionResults) BatchExecutionHelper.newXStreamMarshaller().fromXML( outXml );
        List list = (List) result.getValue( "list" );
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

        String inXml = "";
        inXml += "<batch-execution>";
        inXml += "  <insert>";
        inXml += "    <org.drools.Cheese>";
        inXml += "      <type>stilton</type>";
        inXml += "      <price>1</price>";
        inXml += "      <oldPrice>0</oldPrice>";
        inXml += "    </org.drools.Cheese>";
        inXml += "  </insert>";
        inXml += "  <insert>";
        inXml += "    <org.drools.Cheese>";
        inXml += "      <type>stilton</type>";
        inXml += "      <price>2</price>";
        inXml += "      <oldPrice>0</oldPrice>";
        inXml += "    </org.drools.Cheese>";
        inXml += "  </insert>";
        inXml += "  <insert>";
        inXml += "    <org.drools.Cheese>";
        inXml += "      <type>cheddar</type>";
        inXml += "      <price>1</price>";
        inXml += "      <oldPrice>0</oldPrice>";
        inXml += "    </org.drools.Cheese>";
        inXml += "  </insert>";
        inXml += "  <insert>";
        inXml += "    <org.drools.Cheese>";
        inXml += "      <type>cheddar</type>";
        inXml += "      <price>2</price>";
        inXml += "      <oldPrice>0</oldPrice>";
        inXml += "    </org.drools.Cheese>";
        inXml += "  </insert>";
        inXml += "  <query out-identifier='cheeses' name='cheeses'/>";
        inXml += "  <query out-identifier='cheeses2' name='cheesesWithParams'>";
        inXml += "    <string>stilton</string>";
        inXml += "    <string>cheddar</string>";
        inXml += "  </query>";
        inXml += "</batch-execution>";

        StatelessKnowledgeSession ksession = getSession2( ResourceFactory.newByteArrayResource( str.getBytes() ) );
        ResultHandlerImpl resultHandler = new ResultHandlerImpl();
        getPipeline( ksession ).insert( inXml,
                                        resultHandler );
        String outXml = (String) resultHandler.getObject();

        String expectedXml = "";
        expectedXml += "<execution-results>\n";
        expectedXml += "  <result identifier='cheeses'>\n";
        expectedXml += "    <query-results>\n";
        expectedXml += "      <identifiers>\n";
        expectedXml += "        <identifier>stilton</identifier>\n";
        expectedXml += "        <identifier>cheddar</identifier>\n";
        expectedXml += "      </identifiers>\n";
        expectedXml += "      <row>\n";
        expectedXml += "        <org.drools.Cheese>\n";
        expectedXml += "          <type>stilton</type>\n";
        expectedXml += "          <price>2</price>\n";
        expectedXml += "          <oldPrice>0</oldPrice>\n";
        expectedXml += "        </org.drools.Cheese>\n";
        expectedXml += "        <org.drools.Cheese>\n";
        expectedXml += "          <type>cheddar</type>\n";
        expectedXml += "          <price>2</price>\n";
        expectedXml += "          <oldPrice>0</oldPrice>\n";
        expectedXml += "        </org.drools.Cheese>\n";
        expectedXml += "      </row>\n";
        expectedXml += "      <row>\n";
        expectedXml += "        <org.drools.Cheese>\n";
        expectedXml += "          <type>stilton</type>\n";
        expectedXml += "          <price>1</price>\n";
        expectedXml += "          <oldPrice>0</oldPrice>\n";
        expectedXml += "        </org.drools.Cheese>\n";
        expectedXml += "        <org.drools.Cheese>\n";
        expectedXml += "          <type>cheddar</type>\n";
        expectedXml += "          <price>1</price>\n";
        expectedXml += "          <oldPrice>0</oldPrice>\n";
        expectedXml += "        </org.drools.Cheese>\n";
        expectedXml += "      </row>\n";
        expectedXml += "    </query-results>\n";
        expectedXml += "  </result>\n";
        expectedXml += "  <result identifier='cheeses2'>\n";
        expectedXml += "    <query-results>\n";
        expectedXml += "      <identifiers>\n";
        expectedXml += "        <identifier>stilton</identifier>\n";
        expectedXml += "        <identifier>cheddar</identifier>\n";
        expectedXml += "      </identifiers>\n";
        expectedXml += "      <row>\n";
        expectedXml += "        <org.drools.Cheese reference=\"../../../../result/query-results/row/org.drools.Cheese\"/>\n";
        expectedXml += "        <org.drools.Cheese reference=\"../../../../result/query-results/row/org.drools.Cheese[2]\"/>\n";
        expectedXml += "      </row>\n";
        expectedXml += "      <row>\n";
        expectedXml += "        <org.drools.Cheese reference=\"../../../../result/query-results/row[2]/org.drools.Cheese\"/>\n";
        expectedXml += "        <org.drools.Cheese reference=\"../../../../result/query-results/row[2]/org.drools.Cheese[2]\"/>\n";
        expectedXml += "      </row>\n";
        expectedXml += "    </query-results>\n";
        expectedXml += "  </result>\n";
        expectedXml += "</execution-results>\n";

        assertXMLEqual( expectedXml,
                        outXml );

        ExecutionResults batchResult = (ExecutionResults) BatchExecutionHelper.newXStreamMarshaller().fromXML( outXml );

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

    public void testManualFireAllRules() throws Exception {
        String str = "";
        str += "package org.drools \n";
        str += "import org.drools.Cheese \n";
        str += "global java.util.List list \n";
        str += "rule rule1 \n";
        str += "  when \n";
        str += "    $c : Cheese() \n";
        str += " \n";
        str += "  then \n";
        str += "    $c.setPrice( $c.getPrice() + 5 ); \n";
        str += "    list.add( $c );";
        str += "end\n";

        String inXml = "";
        inXml += "<batch-execution>";
        inXml += "  <set-global identifier='list' out='true'>";
        inXml += "    <list/>";
        inXml += "  </set-global>";
        inXml += "  <insert-elements>";
        inXml += "    <org.drools.Cheese>";
        inXml += "      <type>stilton</type>";
        inXml += "      <price>25</price>";
        inXml += "      <oldPrice>0</oldPrice>";
        inXml += "    </org.drools.Cheese>";
        inXml += "    <org.drools.Cheese>";
        inXml += "      <type>stilton</type>";
        inXml += "      <price>30</price>";
        inXml += "      <oldPrice>0</oldPrice>";
        inXml += "    </org.drools.Cheese>";
        inXml += "  </insert-elements>";
        inXml += "  <fire-all-rules />";
        inXml += "  <insert out-identifier='outBrie'>";
        inXml += "    <org.drools.Cheese>";
        inXml += "      <type>brie</type>";
        inXml += "      <price>10</price>";
        inXml += "      <oldPrice>5</oldPrice>";
        inXml += "    </org.drools.Cheese>";
        inXml += "  </insert>";
        inXml += "</batch-execution>";

        StatelessKnowledgeSession ksession = getSession2( ResourceFactory.newByteArrayResource( str.getBytes() ) );
        ResultHandlerImpl resultHandler = new ResultHandlerImpl();
        getPipeline( ksession ).insert( inXml,
                                        resultHandler );
        String outXml = (String) resultHandler.getObject();

        FactHandle factHandle = (FactHandle) ((ExecutionResults) BatchExecutionHelper.newXStreamMarshaller().fromXML( outXml )).getFactHandle( "outBrie" );

        String expectedXml = "";
        expectedXml += "<execution-results>\n";
        expectedXml += "  <result identifier='list'>\n";
        expectedXml += "    <list>\n";
        expectedXml += "      <org.drools.Cheese>\n";
        expectedXml += "        <type>stilton</type>\n";
        expectedXml += "        <price>35</price>\n";
        expectedXml += "        <oldPrice>0</oldPrice>\n";
        expectedXml += "      </org.drools.Cheese>\n";
        expectedXml += "      <org.drools.Cheese>\n";
        expectedXml += "        <type>stilton</type>\n";
        expectedXml += "        <price>30</price>\n";
        expectedXml += "        <oldPrice>0</oldPrice>\n";
        expectedXml += "      </org.drools.Cheese>\n";
        expectedXml += "    </list>\n";
        expectedXml += "  </result>\n";
        expectedXml += "  <result identifier='outBrie'>\n";
        expectedXml += "    <org.drools.Cheese>\n";
        expectedXml += "      <type>brie</type>\n";
        expectedXml += "      <price>10</price>\n";
        expectedXml += "      <oldPrice>5</oldPrice>\n";
        expectedXml += "    </org.drools.Cheese>\n";
        expectedXml += "  </result>\n";
        expectedXml += "  <fact-handle identifier=\"outBrie\" externalForm=\"" + factHandle.toExternalForm() + "\" /> \n";
        expectedXml += "</execution-results>\n";
        assertXMLEqual( expectedXml,
                        outXml );

        ExecutionResults result = (ExecutionResults) BatchExecutionHelper.newXStreamMarshaller().fromXML( outXml );

        // brie should not have been added to the list
        List list = (List) result.getValue( "list" );
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
        str += "      <global identifier=\"list\" type=\"java.util.List\" />\n";
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
        str += "list.add(person.name);\n";
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
        ksession.setGlobal( "list",
                            list );
        TestVariable person = new TestVariable( "John Doe" );

        String inXml = "";
        inXml += "<batch-execution>";
        inXml += "  <start-process processId='org.drools.actions'>";
        inXml += "    <parameter identifier='person'>";
        inXml += "       <org.drools.TestVariable>";
        inXml += "         <name>John Doe</name>";
        inXml += "    </org.drools.TestVariable>";
        inXml += "    </parameter>";
        inXml += "  </start-process>";
        inXml += "  <get-global identifier='list' out-identifier='out-list'/>";
        inXml += "</batch-execution>";

        ResultHandlerImpl resultHandler = new ResultHandlerImpl();
        getPipeline( ksession ).insert( inXml,
                                        resultHandler );
        String outXml = (String) resultHandler.getObject();

        assertEquals( 1,
                      list.size() );
        assertEquals( "John Doe",
                      list.get( 0 ) );

        String expectedXml = "";
        expectedXml += "<execution-results>\n";
        expectedXml += "  <result identifier=\"out-list\">\n";
        expectedXml += "    <list>\n";
        expectedXml += "      <string>John Doe</string>\n";
        expectedXml += "    </list>\n";
        expectedXml += "  </result>\n";
        expectedXml += "</execution-results>\n";

        assertXMLEqual( expectedXml,
                        outXml );
    }

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

        kbuilder.add( ResourceFactory.newByteArrayResource( str.getBytes() ), ResourceType.DRF );       
        
        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );
        
        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();        
        
        ProcessInstance processInstance = ksession.startProcess( "org.drools.event" );
        assertEquals( ProcessInstance.STATE_ACTIVE,
                      processInstance.getState() );
                       
        String inXml = "";
        inXml += "<signal-event process-instance-id= '" + processInstance.getId() + "' event-type='MyEvent'>";
        inXml += "    <string>MyValue</string>";
        inXml += "</signal-event>";
        
        getPipelineStateful( ksession ).insert( inXml, new ResultHandlerImpl() );

        assertEquals( ProcessInstance.STATE_COMPLETED,
                      processInstance.getState() );
        assertEquals( "MyValue",
                      ((VariableScopeInstance) ((org.drools.process.instance.ProcessInstance) processInstance).getContextInstance( VariableScope.VARIABLE_SCOPE )).getVariable( "MyVar" ) );
    }

    // FIXME for krisv
    public void FIXME_testProcessRuntimeSignalEvent() throws Exception {
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

        kbuilder.add( ResourceFactory.newByteArrayResource( str.getBytes() ), ResourceType.DRF );       
        
        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );
        
        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();        
        
        ProcessInstance processInstance = ksession.startProcess( "org.drools.event" );
        assertEquals( ProcessInstance.STATE_ACTIVE,
                      processInstance.getState() );
                       
        String inXml = "";
        inXml += "<signal-event event-type='MyEvent'>";
        inXml += "    <string>MyValue</string>";
        inXml += "</signal-event>";
        
        getPipelineStateful( ksession ).insert( inXml, new ResultHandlerImpl() );

        assertEquals( ProcessInstance.STATE_COMPLETED,
                      processInstance.getState() );
        assertEquals( "MyValue",
                      ((VariableScopeInstance) ((org.drools.process.instance.ProcessInstance) processInstance).getContextInstance( VariableScope.VARIABLE_SCOPE )).getVariable( "MyVar" ) );
    }    
    
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
        inXml += "<batch-execution>";
        inXml += "  <insert out-identifier='outStilton'>";
        inXml += "    <org.foo.Whee>";
        inXml += "      <type>stilton</type>";
        inXml += "      <price>25</price>";
        inXml += "      <oldPrice>0</oldPrice>";
        inXml += "    </org.foo.Whee>";
        inXml += "  </insert>";
        inXml += "  <fire-all-rules />";
        inXml += "</batch-execution>";

        StatefulKnowledgeSession ksession = getSessionStateful( ResourceFactory.newByteArrayResource( str.getBytes() ) );
        ResultHandlerImpl resultHandler = new ResultHandlerImpl();
        getPipelineStateful( ksession ).insert( inXml,
                                                resultHandler );
        String outXml = (String) resultHandler.getObject();

        ClassLoader cl = ((InternalRuleBase) ((StatefulKnowledgeSessionImpl) ksession).getRuleBase()).getRootClassLoader();
        XStream xstream = BatchExecutionHelper.newXStreamMarshaller();
        xstream.setClassLoader( cl );
        FactHandle factHandle = (FactHandle) ((ExecutionResults) xstream.fromXML( outXml )).getFactHandle( "outStilton" );

        String expectedXml = "";
        expectedXml += "<execution-results>\n";
        expectedXml += "  <result identifier=\"outStilton\">\n";
        expectedXml += "    <org.foo.Whee>\n";
        expectedXml += "      <type>stilton</type>\n";
        expectedXml += "      <oldPrice>0</oldPrice>\n";
        expectedXml += "      <price>30</price>\n";
        expectedXml += "    </org.foo.Whee>\n";
        expectedXml += "  </result>\n";
        expectedXml += "  <fact-handle identifier=\"outStilton\" externalForm=\"" + factHandle.toExternalForm() + "\" /> \n";
        expectedXml += "</execution-results>\n";

        System.out.println( expectedXml );
        System.out.println( outXml );

        assertXMLEqual( expectedXml,
                        outXml );

    }

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
        str += "    w: Whee(price == 30)";
        str += "end\n";

        String inXml = "";
        inXml += "<batch-execution>";
        inXml += "  <insert>";
        inXml += "    <org.foo.Whee>";
        inXml += "      <type>stilton</type>";
        inXml += "      <price>25</price>";
        inXml += "      <oldPrice>0</oldPrice>";
        inXml += "    </org.foo.Whee>";
        inXml += "  </insert>";
        inXml += "</batch-execution>";

        StatefulKnowledgeSession ksession = getSessionStateful( ResourceFactory.newByteArrayResource( str.getBytes() ) );
        ResultHandlerImpl resultHandler = new ResultHandlerImpl();
        getPipelineStateful( ksession ).insert( inXml,
                                                resultHandler );

        getPipelineStateful( ksession ).insert( "<batch-execution><query out-identifier='matchingthings' name='results'/></batch-execution>",
                                                resultHandler );
        String outXml = (String) resultHandler.getObject();

        //we have not fired the rules yet
        assertFalse( outXml.indexOf( "<price>30</price>" ) > -1 );

        //lets send a command to execute them then
        inXml = "";
        inXml += "<batch-execution>";
        inXml += "  <fire-all-rules max='100'/>";
        inXml += "</batch-execution>";
        getPipelineStateful( ksession ).insert( inXml,
                                                resultHandler );
        //ksession.fireAllRules();

        //ok lets try that again...
        getPipelineStateful( ksession ).insert( "<batch-execution><query out-identifier='matchingthings' name='results'/></batch-execution>",
                                                resultHandler );
        outXml = (String) resultHandler.getObject();
        assertTrue( outXml.indexOf( "<price>30</price>" ) > -1 );
    }

    private Pipeline getPipeline(StatelessKnowledgeSession ksession) {
        Action executeResultHandler = PipelineFactory.newExecuteResultHandler();

        Action assignResult = PipelineFactory.newAssignObjectAsResult();
        assignResult.setReceiver( executeResultHandler );

        Transformer outTransformer = PipelineFactory.newXStreamToXmlTransformer( BatchExecutionHelper.newXStreamMarshaller() );
        outTransformer.setReceiver( assignResult );

        KnowledgeRuntimeCommand batchExecution = PipelineFactory.newCommandExecutor();
        batchExecution.setReceiver( outTransformer );

        Transformer inTransformer = PipelineFactory.newXStreamFromXmlTransformer( BatchExecutionHelper.newXStreamMarshaller() );
        inTransformer.setReceiver( batchExecution );

        Pipeline pipeline = PipelineFactory.newStatelessKnowledgeSessionPipeline( ksession );
        pipeline.setReceiver( inTransformer );

        return pipeline;
    }

    private Pipeline getPipelineStateful(StatefulKnowledgeSession ksession) {
        Action executeResultHandler = PipelineFactory.newExecuteResultHandler();

        Action assignResult = PipelineFactory.newAssignObjectAsResult();
        assignResult.setReceiver( executeResultHandler );

        Transformer outTransformer = PipelineFactory.newXStreamToXmlTransformer( BatchExecutionHelper.newXStreamMarshaller() );
        outTransformer.setReceiver( assignResult );

        KnowledgeRuntimeCommand batchExecution = PipelineFactory.newCommandExecutor();
        batchExecution.setReceiver( outTransformer );

        Transformer inTransformer = PipelineFactory.newXStreamFromXmlTransformer( BatchExecutionHelper.newXStreamMarshaller() );
        inTransformer.setReceiver( batchExecution );

        Pipeline pipeline = PipelineFactory.newStatefulKnowledgeSessionPipeline( ksession );
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
            System.out.println( kbuilder.getErrors() );
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

}
