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

package org.kie.camel.component;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.naming.Context;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.examples.RecursiveElementNameAndTextQualifier;
import org.kie.api.KieServices;
import org.kie.api.builder.KieBuilder;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.builder.Message;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.camel.testdomain.Cheese;
import org.kie.camel.testdomain.Person;
import org.kie.camel.testdomain.TestVariable;
import org.drools.core.command.impl.CommandBasedStatefulKnowledgeSession;
import org.drools.core.command.runtime.rule.ModifyCommand;
import org.drools.core.common.InternalFactHandle;
import org.drools.core.common.InternalRuleBase;
import org.drools.core.impl.KnowledgeBaseImpl;
import org.drools.core.impl.StatefulKnowledgeSessionImpl;
import org.drools.core.impl.StatelessKnowledgeSessionImpl;
import org.drools.core.reteoo.ReteooRuleBase;
import org.jbpm.process.core.context.variable.VariableScope;
import org.jbpm.process.instance.context.variable.VariableScopeInstance;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.kie.camel.testdomain.Cheese;
import org.kie.camel.testdomain.Person;
import org.kie.camel.testdomain.TestVariable;
import org.kie.internal.io.ResourceFactory;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.CommandExecutor;
import org.kie.api.runtime.ExecutionResults;
import org.kie.internal.runtime.helper.BatchExecutionHelper;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.api.runtime.process.WorkflowProcessInstance;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.api.runtime.rule.QueryResultsRow;
import org.xml.sax.SAXException;

import com.thoughtworks.xstream.XStream;

public class XStreamBatchExecutionTest extends CamelTestSupport {
    protected CommandExecutor exec;
    protected CommandExecutor exec2;

    protected Context createJndiContext() throws Exception {
        Context context = super.createJndiContext();
        context.bind( "ksession1", this.exec );
        context.bind( "ksession2", this.exec2 );
        return context;
    }

    @Override
    protected RouteBuilder createRouteBuilder() throws Exception {
        return new RouteBuilder() {
            public void configure() throws Exception {
                from( "direct:exec" ).policy( new KiePolicy() ).unmarshal( "xstream" ).to( "kie://ksession1" ).marshal( "xstream" );
                from( "direct:execWithLookup" ).policy( new KiePolicy() ).unmarshal( "xstream" ).to( "kie://dynamic" ).marshal( "xstream" );
                from( "direct:unmarshal" ).policy( new KiePolicy() ).unmarshal( "xstream" );
                from( "direct:marshal" ).policy( new KiePolicy() ).marshal( "xstream" );
            }
        };
    }

    public void setExec(CommandExecutor exec) {
        this.exec = exec;
        try {
            super.setUp();
        } catch ( Exception e ) {
            throw new RuntimeException( e );
        }
    }
    public void setExec2(CommandExecutor exec) {
        this.exec2 = exec;
        try {
            super.setUp();
        } catch ( Exception e ) {
            throw new RuntimeException( e );
        }
    }

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
        str += "package org.kie.camel.testdomain \n";
        str += "import org.kie.camel.testdomain.Cheese \n";
        str += "import org.kie.camel.testdomain.ChangeCollector \n";
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
        inXml += "<batch-execution>";
        inXml += "  <insert out-identifier='outStilton'>";
        inXml += "    <org.kie.camel.testdomain.Cheese>";
        inXml += "      <type>stilton</type>";
        inXml += "      <price>25</price>";
        inXml += "      <oldPrice>0</oldPrice>";
        inXml += "    </org.kie.camel.testdomain.Cheese>";
        inXml += "  </insert>";
        inXml += "  <fire-all-rules />";
        inXml += "</batch-execution>";

        KieSession ksession = getKieSession( ResourceFactory.newByteArrayResource( str.getBytes() ) );
        setExec( ksession );

        String outXml = template.requestBody( "direct:exec",
                                              inXml,
                                              String.class );
        ExecutionResults result = template.requestBody( "direct:unmarshal",
                                                        outXml,
                                                        ExecutionResults.class );

        inXml = "";
        inXml += "<batch-execution>";
        inXml += "  <insert out-identifier='person'>";
        inXml += "    <org.kie.camel.testdomain.Person>";
        inXml += "      <name>mic</name>";
        inXml += "    </org.kie.camel.testdomain.Person>";
        inXml += "  </insert>";
        inXml += "  <insert out-identifier='changes'>";
        inXml += "    <org.kie.camel.testdomain.ChangeCollector/>";
        inXml += "  </insert>";
        inXml += "  <fire-all-rules />";
        inXml += "</batch-execution>";

        outXml = template.requestBody( "direct:exec",
                                       inXml,
                                       String.class );

        assertTrue( outXml.indexOf( "<changes>" ) > -1 );

        inXml = "";
        inXml += "<batch-execution>";
        inXml += "  <insert out-identifier='person'>";
        inXml += "    <org.kie.camel.testdomain.Person>";
        inXml += "      <name>mark</name>";
        inXml += "    </org.kie.camel.testdomain.Person>";
        inXml += "  </insert>";
        inXml += "  <insert out-identifier='changes'>";
        inXml += "    <org.kie.camel.testdomain.ChangeCollector/>";
        inXml += "  </insert>";
        inXml += "  <fire-all-rules />";
        inXml += "</batch-execution>";

        outXml = template.requestBody( "direct:exec",
                                       inXml,
                                       String.class );

        assertTrue( outXml.indexOf( "<retracted>" ) > -1 );

    }

    @Test
    public void testInsertWithDefaults() throws Exception {
        String str = "";
        str += "package org.drools \n";
        str += "import org.kie.camel.testdomain.Cheese \n";
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
        inXml += "    <org.kie.camel.testdomain.Cheese>";
        inXml += "      <type>stilton</type>";
        inXml += "      <price>25</price>";
        inXml += "      <oldPrice>0</oldPrice>";
        inXml += "    </org.kie.camel.testdomain.Cheese>";
        inXml += "  </insert>";
        inXml += "  <fire-all-rules />";
        inXml += "</batch-execution>";

        KieSession ksession = getKieSession( ResourceFactory.newByteArrayResource( str.getBytes() ) );
        setExec( ksession );

        String outXml = template.requestBody( "direct:exec",
                                              inXml,
                                              String.class );
        ExecutionResults result = template.requestBody( "direct:unmarshal",
                                                        outXml,
                                                        ExecutionResults.class );

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
        expectedXml += "    <org.kie.camel.testdomain.Cheese>\n";
        expectedXml += "      <type>stilton</type>\n";
        expectedXml += "      <oldPrice>0</oldPrice>\n";
        expectedXml += "      <price>30</price>\n";
        expectedXml += "    </org.kie.camel.testdomain.Cheese>\n";
        expectedXml += "  </result>\n";
        expectedXml += "  <fact-handle identifier=\"outStilton\" external-form=\"" + ((InternalFactHandle) result.getFactHandle( "outStilton" )).toExternalForm() + "\" /> \n";
        expectedXml += "</execution-results>\n";

        assertXMLEqual( expectedXml,
                        outXml );
    }

    @Test
    public void testInsertWithReturnObjectFalse() throws Exception {
        String str = "";
        str += "package org.drools \n";
        str += "import org.kie.camel.testdomain.Cheese \n";
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
        inXml += "    <org.kie.camel.testdomain.Cheese>";
        inXml += "      <type>stilton</type>";
        inXml += "      <price>25</price>";
        inXml += "      <oldPrice>0</oldPrice>";
        inXml += "    </org.kie.camel.testdomain.Cheese>";
        inXml += "  </insert>";
        inXml += "  <fire-all-rules />";
        inXml += "</batch-execution>";

        KieSession ksession = getKieSession( ResourceFactory.newByteArrayResource( str.getBytes() ) );
        setExec( ksession );

        String outXml = template.requestBody( "direct:exec",
                                              inXml,
                                              String.class );
        ExecutionResults result = template.requestBody( "direct:unmarshal",
                                                        outXml,
                                                        ExecutionResults.class );

        assertNull( result.getValue( "outStilton" ) );

        FactHandle factHandle = (FactHandle) result.getFactHandle( "outStilton" );
        Cheese stilton = (Cheese) ksession.getObject( factHandle );
        assertEquals( 30,
                      stilton.getPrice() );

        String expectedXml = "";
        expectedXml += "<execution-results>\n";
        expectedXml += "  <fact-handle identifier=\"outStilton\" external-form=\"" + ((InternalFactHandle) result.getFactHandle( "outStilton" )).toExternalForm() + "\" /> \n";
        expectedXml += "</execution-results>\n";

        assertXMLEqual( expectedXml,
                        outXml );
    }

    @Test
    public void testGetObject() throws Exception {
        String str = "";
        str += "package org.drools \n";
        str += "import org.kie.camel.testdomain.Cheese \n";
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
        inXml += "    <org.kie.camel.testdomain.Cheese>";
        inXml += "      <type>stilton</type>";
        inXml += "      <price>25</price>";
        inXml += "      <oldPrice>0</oldPrice>";
        inXml += "    </org.kie.camel.testdomain.Cheese>";
        inXml += "  </insert>";
        inXml += "  <fire-all-rules />";
        inXml += "</batch-execution>";

        KieSession ksession = getKieSession( ResourceFactory.newByteArrayResource( str.getBytes() ) );
        setExec( ksession );

        String outXml = template.requestBody( "direct:exec",
                                              inXml,
                                              String.class );
        ExecutionResults result = template.requestBody( "direct:unmarshal",
                                                        outXml,
                                                        ExecutionResults.class );

        Cheese stilton = (Cheese) result.getValue( "outStilton" );
        assertEquals( 30,
                      stilton.getPrice() );

        FactHandle factHandle = (FactHandle) result.getFactHandle( "outStilton" );
        inXml = "";
        inXml += "<batch-execution>";
        inXml += "  <get-object out-identifier='outStilton' fact-handle='" + factHandle.toExternalForm() + "' />";
        inXml += "</batch-execution>";
        outXml = template.requestBody( "direct:exec",
                                       inXml,
                                       String.class );
        result = template.requestBody( "direct:unmarshal",
                                       outXml,
                                       ExecutionResults.class );
        stilton = (Cheese) result.getValue( "outStilton" );
        assertEquals( 30,
                      stilton.getPrice() );
    }

    @Test
    public void testRetractObject() throws Exception {
        String str = "";
        str += "package org.drools \n";
        str += "import org.kie.camel.testdomain.Cheese \n";
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
        inXml += "    <org.kie.camel.testdomain.Cheese>";
        inXml += "      <type>stilton</type>";
        inXml += "      <price>25</price>";
        inXml += "      <oldPrice>0</oldPrice>";
        inXml += "    </org.kie.camel.testdomain.Cheese>";
        inXml += "  </insert>";
        inXml += "  <fire-all-rules />";
        inXml += "</batch-execution>";

        KieSession ksession = getKieSession( ResourceFactory.newByteArrayResource( str.getBytes() ) );
        setExec( ksession );

        String outXml = template.requestBody( "direct:exec",
                                              inXml,
                                              String.class );
        ExecutionResults result = template.requestBody( "direct:unmarshal",
                                                        outXml,
                                                        ExecutionResults.class );

        Cheese stilton = (Cheese) result.getValue( "outStilton" );
        assertEquals( 30,
                      stilton.getPrice() );

        FactHandle factHandle = (FactHandle) result.getFactHandle( "outStilton" );
        inXml = "";
        inXml += "<batch-execution>";
        inXml += "  <retract fact-handle='" + factHandle.toExternalForm() + "' />";
        inXml += "</batch-execution>";
        template.requestBody( "direct:exec",
                              inXml,
                              String.class );

        inXml = "";
        inXml += "<batch-execution>";
        inXml += "  <get-object out-identifier='outStilton' fact-handle='" + factHandle.toExternalForm() + "' />";
        inXml += "</batch-execution>";

        outXml = template.requestBody( "direct:exec",
                                       inXml,
                                       String.class );
        result = template.requestBody( "direct:unmarshal",
                                       outXml,
                                       ExecutionResults.class );
        assertNull( result.getValue( "outStilton" ) );
    }

    @Test
    public void testModifyObject() throws Exception {
        String str = "";
        str += "package org.drools \n";
        str += "import org.kie.camel.testdomain.Cheese \n";
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
        inXml += "    <org.kie.camel.testdomain.Cheese>";
        inXml += "      <type>stilton</type>";
        inXml += "      <price>25</price>";
        inXml += "      <oldPrice>0</oldPrice>";
        inXml += "    </org.kie.camel.testdomain.Cheese>";
        inXml += "  </insert>";
        inXml += "  <fire-all-rules />";
        inXml += "</batch-execution>";

        KieSession ksession = getKieSession( ResourceFactory.newByteArrayResource( str.getBytes() ) );
        setExec( ksession );

        String outXml = template.requestBody( "direct:exec",
                                              inXml,
                                              String.class );
        ExecutionResults result = template.requestBody( "direct:unmarshal",
                                                        outXml,
                                                        ExecutionResults.class );
        Cheese stilton = (Cheese) result.getValue( "outStilton" );
        assertEquals( 30,
                      stilton.getPrice() );

        FactHandle factHandle = ((FactHandle) result.getFactHandle( "outStilton" ));

        String expectedXml = "";
        expectedXml += "<execution-results>\n";
        expectedXml += "  <result identifier=\"outStilton\">\n";
        expectedXml += "    <org.kie.camel.testdomain.Cheese>\n";
        expectedXml += "      <type>stilton</type>\n";
        expectedXml += "      <oldPrice>0</oldPrice>\n";
        expectedXml += "      <price>30</price>\n";
        expectedXml += "    </org.kie.camel.testdomain.Cheese>\n";
        expectedXml += "  </result>\n";
        expectedXml += "  <fact-handle identifier=\"outStilton\" external-form=\"" + factHandle.toExternalForm() + "\" /> \n";
        expectedXml += "</execution-results>\n";

        assertXMLEqual( expectedXml,
                        outXml );

        inXml = "";
        inXml += "<batch-execution>";
        inXml += "  <modify fact-handle='" + factHandle.toExternalForm() + "'> <set accessor='oldPrice' value='42' /><set accessor='price' value='50' /></modify>";
        inXml += "  <fire-all-rules />";
        inXml += "</batch-execution>";
        template.requestBody( "direct:exec",
                              inXml,
                              String.class );

        inXml = "";
        inXml += "<batch-execution>";
        inXml += "  <get-object out-identifier='outCheddar' fact-handle='" + factHandle.toExternalForm() + "' />";
        inXml += "</batch-execution>";
        setExec( ksession );

        outXml = template.requestBody( "direct:exec",
                                       inXml,
                                       String.class );
        result = template.requestBody( "direct:unmarshal",
                                       outXml,
                                       ExecutionResults.class );

        Cheese cheddar = (Cheese) result.getValue( "outCheddar" );
        assertEquals( 42,
                      cheddar.getOldPrice() );
        assertEquals( 55,
                      cheddar.getPrice() );

        //now test for code injection:
        ModifyCommand.ALLOW_MODIFY_EXPRESSIONS = false;
        inXml = "";
        inXml += "<batch-execution>";
        inXml += "  <modify fact-handle='" + factHandle.toExternalForm() + "'> <set accessor='type' value='44\"; System.exit(1);' /><set accessor='price' value='50' /></modify>";
        inXml += "  <fire-all-rules />";
        inXml += "</batch-execution>";

        outXml = template.requestBody( "direct:exec",
                                       inXml,
                                       String.class );
        result = template.requestBody( "direct:unmarshal",
                                       outXml,
                                       ExecutionResults.class );

        result = (ExecutionResults) BatchExecutionHelper.newXStreamMarshaller().fromXML( outXml );
        ModifyCommand.ALLOW_MODIFY_EXPRESSIONS = true;

    }

    @Test
    public void testInsertElements() throws Exception {
        String str = "";
        str += "package org.drools \n";
        str += "import org.kie.camel.testdomain.Cheese \n";
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
        inXml += "  <set-global identifier='list' out-identifier='list' return-objects='true'>";
        inXml += "    <list/>";
        inXml += "  </set-global>";
        inXml += "  <insert-elements>";
        inXml += "    <org.kie.camel.testdomain.Cheese>";
        inXml += "      <type>stilton</type>";
        inXml += "      <price>25</price>";
        inXml += "      <oldPrice>0</oldPrice>";
        inXml += "    </org.kie.camel.testdomain.Cheese>";
        inXml += "    <org.kie.camel.testdomain.Cheese>";
        inXml += "      <type>stilton</type>";
        inXml += "      <price>30</price>";
        inXml += "      <oldPrice>0</oldPrice>";
        inXml += "    </org.kie.camel.testdomain.Cheese>";
        inXml += "  </insert-elements>";
        inXml += "</batch-execution>";

        StatelessKieSession ksession = getStatelessKieSession( ResourceFactory.newByteArrayResource( str.getBytes() ) );
        setExec( ksession );

        String outXml = template.requestBody( "direct:exec",
                                              inXml,
                                              String.class );

        String expectedXml = "";
        expectedXml += "<execution-results>\n";
        expectedXml += "  <result identifier='list'>\n";
        expectedXml += "    <list>\n";
        expectedXml += "      <org.kie.camel.testdomain.Cheese>\n";
        expectedXml += "        <type>stilton</type>\n";
        expectedXml += "        <price>35</price>\n";
        expectedXml += "        <oldPrice>0</oldPrice>\n";
        expectedXml += "      </org.kie.camel.testdomain.Cheese>\n";
        expectedXml += "      <org.kie.camel.testdomain.Cheese>\n";
        expectedXml += "        <type>stilton</type>\n";
        expectedXml += "        <price>30</price>\n";
        expectedXml += "        <oldPrice>0</oldPrice>\n";
        expectedXml += "      </org.kie.camel.testdomain.Cheese>\n";
        expectedXml += "    </list>\n";
        expectedXml += "  </result>\n";
        expectedXml += "</execution-results>\n";

        assertXMLEqual( expectedXml,
                        outXml );

        ExecutionResults result = template.requestBody( "direct:unmarshal",
                                                        outXml,
                                                        ExecutionResults.class );

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

    @Test
    public void testFactHandleReturn() throws Exception {
        String str = "";
        str += "package org.drools \n";
        str += "import org.kie.camel.testdomain.Cheese \n";
        str += "global java.util.List list \n";
        str += "rule rule1 \n";
        str += "  when \n";
        str += "    $c : Cheese() \n";
        str += " \n";
        str += "  then \n";
        str += "    System.err.println(42); \n";
        str += "end\n";

        String inXml = "";
        inXml += "<batch-execution>";
        inXml += "  <get-global identifier='list' out-identifier='out-list'/>";
        inXml += "</batch-execution>";

        KieSession ksession = getKieSession( ResourceFactory.newByteArrayResource( str.getBytes() ) );
        FactHandle fh = ksession.insert( new Person( "mic",
                                                     42 ) );
        List<FactHandle> list = new ArrayList<FactHandle>();
        list.add( fh );

        ksession.setGlobal( "list",
                            list );

        setExec( ksession );

        String outXml = template.requestBody( "direct:exec",
                                              inXml,
                                              String.class );

        System.err.println( outXml );
        String expectedXml = "";
        expectedXml += "<execution-results>\n" + "  <result identifier=\"out-list\">\n" + "    <list>\n" + "      <fact-handle external-form=\"" + fh.toExternalForm() + "\"/>\n" + "    </list>\n" + "  </result>\n" + "</execution-results>";

        assertXMLEqual( expectedXml,
                        outXml );

    }

    @Test
    public void testInsertElementsWithReturnObjects() throws Exception {
        String str = "";
        str += "package org.drools \n";
        str += "import org.kie.camel.testdomain.Cheese \n";
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
        inXml += "  <set-global identifier='list' out-identifier='list' >";
        inXml += "    <list/>";
        inXml += "  </set-global>";
        inXml += "  <insert-elements out-identifier='myfacts' return-objects='true'>";
        inXml += "    <org.kie.camel.testdomain.Cheese>";
        inXml += "      <type>stilton</type>";
        inXml += "      <price>25</price>";
        inXml += "      <oldPrice>0</oldPrice>";
        inXml += "    </org.kie.camel.testdomain.Cheese>";
        inXml += "    <org.kie.camel.testdomain.Cheese>";
        inXml += "      <type>stilton</type>";
        inXml += "      <price>30</price>";
        inXml += "      <oldPrice>0</oldPrice>";
        inXml += "    </org.kie.camel.testdomain.Cheese>";
        inXml += "  </insert-elements>";
        inXml += "  <fire-all-rules/>";
        inXml += "</batch-execution>";

        KieSession ksession = getKieSession( ResourceFactory.newByteArrayResource( str.getBytes() ) );
        setExec( ksession );

        String outXml = template.requestBody( "direct:exec",
                                              inXml,
                                              String.class );

        Collection< ? extends FactHandle> factHandles = ksession.getFactHandles();

        String expectedXml = "";
        expectedXml += "<execution-results>\n";
        expectedXml += "  <result identifier='list'>\n";
        expectedXml += "    <list>\n";
        expectedXml += "      <org.kie.camel.testdomain.Cheese>\n";
        expectedXml += "        <type>stilton</type>\n";
        expectedXml += "        <price>35</price>\n";
        expectedXml += "        <oldPrice>0</oldPrice>\n";
        expectedXml += "      </org.kie.camel.testdomain.Cheese>\n";
        expectedXml += "      <org.kie.camel.testdomain.Cheese>\n";
        expectedXml += "        <type>stilton</type>\n";
        expectedXml += "        <price>30</price>\n";
        expectedXml += "        <oldPrice>0</oldPrice>\n";
        expectedXml += "      </org.kie.camel.testdomain.Cheese>\n";
        expectedXml += "    </list>\n";
        expectedXml += "  </result>\n";

        expectedXml += "  <result identifier=\"myfacts\">\n";
        expectedXml += "  <list>\n";
        expectedXml += "    <org.kie.camel.testdomain.Cheese reference=\"../../../result/list/org.kie.camel.testdomain.Cheese[2]\"/>\n";
        expectedXml += "    <org.kie.camel.testdomain.Cheese reference=\"../../../result/list/org.kie.camel.testdomain.Cheese\"/>\n";
        expectedXml += "  </list>\n";
        expectedXml += "  </result>\n";
        expectedXml += "  <fact-handles identifier=\"myfacts\">\n";
        for ( FactHandle factHandle : factHandles ) {
            if ( ((Cheese) ksession.getObject( factHandle )).getPrice() == 30 ) {
                expectedXml += "  <fact-handle external-form=\"" + factHandle.toExternalForm() + "\"/>\n";
            }
        }

        for ( FactHandle factHandle : factHandles ) {
            if ( ((Cheese) ksession.getObject( factHandle )).getPrice() == 35 ) {
                expectedXml += "  <fact-handle external-form=\"" + factHandle.toExternalForm() + "\"/>\n";
            }
        }
        expectedXml += "  </fact-handles>\n";

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

    @Test
    public void testSetGlobal() throws Exception {
        String str = "";
        str += "package org.drools \n";
        str += "import org.kie.camel.testdomain.Cheese \n";
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
        inXml += "  <set-global identifier='list2' out-identifier='list2'>";
        inXml += "    <list/>";
        inXml += "  </set-global>";
        inXml += "  <set-global identifier='list3' out-identifier='outList3'>";
        inXml += "    <list/>";
        inXml += "  </set-global>";
        inXml += "  <insert>";
        inXml += "    <org.kie.camel.testdomain.Cheese>";
        inXml += "      <type>stilton</type>";
        inXml += "      <price>5</price>";
        inXml += "      <oldPrice>0</oldPrice>";
        inXml += "  </org.kie.camel.testdomain.Cheese>";
        inXml += "  </insert>";
        inXml += "</batch-execution>";

        StatelessKieSession ksession = getStatelessKieSession( ResourceFactory.newByteArrayResource( str.getBytes() ) );
        setExec( ksession );

        String outXml = template.requestBody( "direct:exec",
                                              inXml,
                                              String.class );

        String expectedXml = "";
        expectedXml += "<execution-results>\n";
        expectedXml += "  <result identifier='list2'>\n";
        expectedXml += "    <list>\n";
        expectedXml += "      <org.kie.camel.testdomain.Cheese>\n";
        expectedXml += "        <type>stilton</type>\n";
        expectedXml += "        <price>30</price>\n";
        expectedXml += "        <oldPrice>0</oldPrice>\n";
        expectedXml += "      </org.kie.camel.testdomain.Cheese>\n";
        expectedXml += "    </list>\n";
        expectedXml += "  </result>\n";
        expectedXml += "  <result identifier='outList3'>\n";
        expectedXml += "    <list>\n";
        expectedXml += "      <org.kie.camel.testdomain.Cheese reference='../../../result/list/org.kie.camel.testdomain.Cheese'/>\n";
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

    @Test
    public void testGetGlobal() throws Exception {
        String str = "";
        str += "package org.drools \n";
        str += "import org.kie.camel.testdomain.Cheese \n";
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
        inXml += "    <org.kie.camel.testdomain.Cheese>";
        inXml += "      <type>stilton</type>";
        inXml += "      <price>25</price>";
        inXml += "      <oldPrice>0</oldPrice>";
        inXml += "    </org.kie.camel.testdomain.Cheese>";
        inXml += "  </insert>";
        inXml += "  <get-global identifier='list' out-identifier='out-list'/>";
        inXml += "</batch-execution>";

        StatelessKieSession ksession = getStatelessKieSession( ResourceFactory.newByteArrayResource( str.getBytes() ) );
        setExec( ksession );

        String outXml = template.requestBody( "direct:exec",
                                              inXml,
                                              String.class );

        String expectedXml = "";
        expectedXml += "<execution-results>\n";
        expectedXml += "  <result identifier=\"out-list\">\n";
        expectedXml += "    <list>\n";
        expectedXml += "      <org.kie.camel.testdomain.Cheese>\n";
        expectedXml += "        <type>stilton</type>\n";
        expectedXml += "        <price>25</price>\n";
        expectedXml += "        <oldPrice>0</oldPrice>\n";
        expectedXml += "      </org.kie.camel.testdomain.Cheese>\n";
        expectedXml += "    </list>\n";
        expectedXml += "  </result>\n";
        expectedXml += "</execution-results>\n";

        assertXMLEqual( expectedXml,
                        outXml );
    }

    @Ignore("The result XML structure has changed 08-MON-2011 -Rikkola-")
    @Test
    public void FIXME_testQuery() throws Exception {
        String str = "";
        str += "package org.drools.test  \n";
        str += "import org.kie.camel.testdomain.Cheese \n";
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
        inXml += "    <org.kie.camel.testdomain.Cheese>";
        inXml += "      <type>stilton</type>";
        inXml += "      <price>1</price>";
        inXml += "      <oldPrice>0</oldPrice>";
        inXml += "    </org.kie.camel.testdomain.Cheese>";
        inXml += "  </insert>";
        inXml += "  <insert>";
        inXml += "    <org.kie.camel.testdomain.Cheese>";
        inXml += "      <type>stilton</type>";
        inXml += "      <price>2</price>";
        inXml += "      <oldPrice>0</oldPrice>";
        inXml += "    </org.kie.camel.testdomain.Cheese>";
        inXml += "  </insert>";
        inXml += "  <insert>";
        inXml += "    <org.kie.camel.testdomain.Cheese>";
        inXml += "      <type>cheddar</type>";
        inXml += "      <price>1</price>";
        inXml += "      <oldPrice>0</oldPrice>";
        inXml += "    </org.kie.camel.testdomain.Cheese>";
        inXml += "  </insert>";
        inXml += "  <insert>";
        inXml += "    <org.kie.camel.testdomain.Cheese>";
        inXml += "      <type>cheddar</type>";
        inXml += "      <price>2</price>";
        inXml += "      <oldPrice>0</oldPrice>";
        inXml += "    </org.kie.camel.testdomain.Cheese>";
        inXml += "  </insert>";
        inXml += "  <query out-identifier='cheeses' name='cheeses'/>";
        inXml += "  <query out-identifier='cheeses2' name='cheesesWithParams'>";
        inXml += "    <string>stilton</string>";
        inXml += "    <string>cheddar</string>";
        inXml += "  </query>";
        inXml += "</batch-execution>";

        KieSession ksession = getKieSession( ResourceFactory.newByteArrayResource( str.getBytes() ) );
        setExec( ksession );

        String outXml = template.requestBody( "direct:exec",
                inXml,
                String.class );

        Iterator<QueryResultsRow> it1 = ksession.getQueryResults( "cheeses" ).iterator();
        Iterator<QueryResultsRow> it2 = ksession.getQueryResults( "cheesesWithParams",
                new String[]{"stilton", "cheddar"} ).iterator();
        QueryResultsRow row = null;

        String expectedXml = "";
        expectedXml += "<execution-results>\n";
        expectedXml += "  <result identifier='cheeses'>\n";
        expectedXml += "    <query-results>\n";
        expectedXml += "      <identifiers>\n";
        expectedXml += "        <identifier>stilton</identifier>\n";
        expectedXml += "        <identifier>cheddar</identifier>\n";
        expectedXml += "      </identifiers>\n";
        expectedXml += "      <row>\n";
        row = it1.next();
        expectedXml += "        <org.kie.camel.testdomain.Cheese>\n";
        expectedXml += "          <type>stilton</type>\n";
        expectedXml += "          <price>1</price>\n";
        expectedXml += "          <oldPrice>0</oldPrice>\n";
        expectedXml += "        </org.kie.camel.testdomain.Cheese>\n";
        expectedXml += "        <fact-handle external-form='" + row.getFactHandle( "stilton" ).toExternalForm() + "' />";
        expectedXml += "        <org.kie.camel.testdomain.Cheese>\n";
        expectedXml += "          <type>cheddar</type>\n";
        expectedXml += "          <price>1</price>\n";
        expectedXml += "          <oldPrice>0</oldPrice>\n";
        expectedXml += "        </org.kie.camel.testdomain.Cheese>\n";
        expectedXml += "        <fact-handle external-form='" + row.getFactHandle( "cheddar" ).toExternalForm() + "' />";
        expectedXml += "      </row>\n";
        expectedXml += "      <row>\n";
        row = it1.next();
        expectedXml += "        <org.kie.camel.testdomain.Cheese>\n";
        expectedXml += "          <type>stilton</type>\n";
        expectedXml += "          <price>2</price>\n";
        expectedXml += "          <oldPrice>0</oldPrice>\n";
        expectedXml += "        </org.kie.camel.testdomain.Cheese>\n";
        expectedXml += "        <fact-handle external-form='" + row.getFactHandle( "stilton" ).toExternalForm() + "' />";
        expectedXml += "        <org.kie.camel.testdomain.Cheese>\n";
        expectedXml += "          <type>cheddar</type>\n";
        expectedXml += "          <price>2</price>\n";
        expectedXml += "          <oldPrice>0</oldPrice>\n";
        expectedXml += "        </org.kie.camel.testdomain.Cheese>\n";
        expectedXml += "        <fact-handle external-form='" + row.getFactHandle( "cheddar" ).toExternalForm() + "' />";
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
        row = it2.next();
        expectedXml += "        <org.kie.camel.testdomain.Cheese reference=\"../../../../result/query-results/row/org.kie.camel.testdomain.Cheese\"/>\n";
        expectedXml += "        <fact-handle external-form='" + row.getFactHandle( "stilton" ).toExternalForm() + "' />";
        expectedXml += "        <org.kie.camel.testdomain.Cheese reference=\"../../../../result/query-results/row/org.kie.camel.testdomain.Cheese[2]\"/>\n";
        expectedXml += "        <fact-handle external-form='" + row.getFactHandle( "cheddar" ).toExternalForm() + "' />";
        expectedXml += "      </row>\n";
        expectedXml += "      <row>\n";
        row = it2.next();
        expectedXml += "        <org.kie.camel.testdomain.Cheese reference=\"../../../../result/query-results/row[2]/org.kie.camel.testdomain.Cheese\"/>\n";
        expectedXml += "        <fact-handle external-form='" + row.getFactHandle( "stilton" ).toExternalForm() + "' />";
        expectedXml += "        <org.kie.camel.testdomain.Cheese reference=\"../../../../result/query-results/row[2]/org.kie.camel.testdomain.Cheese[2]\"/>\n";
        expectedXml += "        <fact-handle external-form='" + row.getFactHandle( "cheddar" ).toExternalForm() + "' />";
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

        org.kie.api.runtime.rule.QueryResults results = (org.kie.api.runtime.rule.QueryResults) batchResult.getValue( "cheeses" );
        assertEquals( 2,
                results.size() );
        assertEquals( 2,
                results.getIdentifiers().length );
        Set newSet = new HashSet();
        for ( QueryResultsRow result : results ) {
            list = new ArrayList();
            list.add( result.get( "stilton" ) );
            list.add( result.get( "cheddar" ) );
            newSet.add( list );
        }
        assertEquals( set,
                newSet );
    }

    @Test
    public void testGetObjects() throws Exception {
        String str = "";
        str += "package org.drools \n";
        str += "import org.kie.camel.testdomain.Cheese \n";
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
        inXml += "    <org.kie.camel.testdomain.Cheese>";
        inXml += "      <type>stilton</type>";
        inXml += "      <price>25</price>";
        inXml += "      <oldPrice>0</oldPrice>";
        inXml += "    </org.kie.camel.testdomain.Cheese>";
        inXml += "    <org.kie.camel.testdomain.Cheese>";
        inXml += "      <type>stilton</type>";
        inXml += "      <price>30</price>";
        inXml += "      <oldPrice>0</oldPrice>";
        inXml += "    </org.kie.camel.testdomain.Cheese>";
        inXml += "  </insert-elements>";
        inXml += "  <get-objects out-identifier='list' />";
        inXml += "</batch-execution>";

        StatelessKieSession ksession = getStatelessKieSession( ResourceFactory.newByteArrayResource( str.getBytes() ) );
        setExec( ksession );

        String outXml = template.requestBody( "direct:exec",
                                              inXml,
                                              String.class );

        String expectedXml = "";
        expectedXml += "<execution-results>";
        expectedXml += "  <result identifier='list'>";
        expectedXml += "    <list>";
        expectedXml += "      <org.kie.camel.testdomain.Cheese>";
        expectedXml += "        <type>stilton</type>";
        expectedXml += "        <price>30</price>";
        expectedXml += "        <oldPrice>0</oldPrice>";
        expectedXml += "      </org.kie.camel.testdomain.Cheese>";
        expectedXml += "      <org.kie.camel.testdomain.Cheese>";
        expectedXml += "        <type>stilton</type>";
        expectedXml += "        <price>35</price>";
        expectedXml += "        <oldPrice>0</oldPrice>";
        expectedXml += "      </org.kie.camel.testdomain.Cheese>";
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

    @Test
    public void testManualFireAllRules() throws Exception {
        String str = "";
        str += "package org.drools \n";
        str += "import org.kie.camel.testdomain.Cheese \n";
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
        inXml += "  <set-global identifier='list' out-identifier='list'>";
        inXml += "    <list/>";
        inXml += "  </set-global>";
        inXml += "  <insert-elements>";
        inXml += "    <org.kie.camel.testdomain.Cheese>";
        inXml += "      <type>stilton</type>";
        inXml += "      <price>25</price>";
        inXml += "      <oldPrice>0</oldPrice>";
        inXml += "    </org.kie.camel.testdomain.Cheese>";
        inXml += "    <org.kie.camel.testdomain.Cheese>";
        inXml += "      <type>stilton</type>";
        inXml += "      <price>30</price>";
        inXml += "      <oldPrice>0</oldPrice>";
        inXml += "    </org.kie.camel.testdomain.Cheese>";
        inXml += "  </insert-elements>";
        inXml += "  <fire-all-rules />";
        inXml += "  <insert out-identifier='outBrie'>";
        inXml += "    <org.kie.camel.testdomain.Cheese>";
        inXml += "      <type>brie</type>";
        inXml += "      <price>10</price>";
        inXml += "      <oldPrice>5</oldPrice>";
        inXml += "    </org.kie.camel.testdomain.Cheese>";
        inXml += "  </insert>";
        inXml += "</batch-execution>";

        KieSession ksession = getKieSession( ResourceFactory.newByteArrayResource( str.getBytes() ) );
        setExec( ksession );

        String outXml = template.requestBody( "direct:exec",
                                              inXml,
                                              String.class );

        FactHandle factHandle = (FactHandle) ((ExecutionResults) BatchExecutionHelper.newXStreamMarshaller().fromXML( outXml )).getFactHandle( "outBrie" );

        String expectedXml = "";
        expectedXml += "<execution-results>\n";
        expectedXml += "  <result identifier='list'>\n";
        expectedXml += "    <list>\n";
        expectedXml += "      <org.kie.camel.testdomain.Cheese>\n";
        expectedXml += "        <type>stilton</type>\n";
        expectedXml += "        <price>35</price>\n";
        expectedXml += "        <oldPrice>0</oldPrice>\n";
        expectedXml += "      </org.kie.camel.testdomain.Cheese>\n";
        expectedXml += "      <org.kie.camel.testdomain.Cheese>\n";
        expectedXml += "        <type>stilton</type>\n";
        expectedXml += "        <price>30</price>\n";
        expectedXml += "        <oldPrice>0</oldPrice>\n";
        expectedXml += "      </org.kie.camel.testdomain.Cheese>\n";
        expectedXml += "    </list>\n";
        expectedXml += "  </result>\n";
        expectedXml += "  <result identifier='outBrie'>\n";
        expectedXml += "    <org.kie.camel.testdomain.Cheese>\n";
        expectedXml += "      <type>brie</type>\n";
        expectedXml += "      <price>10</price>\n";
        expectedXml += "      <oldPrice>5</oldPrice>\n";
        expectedXml += "    </org.kie.camel.testdomain.Cheese>\n";
        expectedXml += "  </result>\n";
        expectedXml += "  <fact-handle identifier=\"outBrie\" external-form=\"" + factHandle.toExternalForm() + "\" /> \n";
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

    @Test
    public void testProcess() throws SAXException,
                             IOException {
        String str = "";
        str += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        str += "<process xmlns=\"http://drools.org/drools-5.0/process\"\n";
        str += "         xmlns:xs=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        str += "         xs:schemaLocation=\"http://drools.org/drools-5.0/process drools-processes-5.0.xsd\"\n";
        str += "         type=\"RuleFlow\" name=\"flow\" id=\"org.drools.actions\" package-name=\"org.drools\" version=\"1\" >\n";
        str += "\n";
        str += "  <header>\n";
        str += "    <imports>\n";
        str += "      <import name=\"org.kie.camel.testdomain.TestVariable\" />\n";
        str += "    </imports>\n";
        str += "    <globals>\n";
        str += "      <global identifier=\"list\" type=\"java.util.List\" />\n";
        str += "    </globals>\n";
        str += "    <variables>\n";
        str += "      <variable name=\"person\" >\n";
        str += "        <type name=\"org.drools.core.process.core.datatype.impl.type.ObjectDataType\" className=\"TestVariable\" />\n";
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

        StatelessKieSession ksession = getStatelessKieSessionFromResource(ResourceFactory.newByteArrayResource(str.getBytes())
                                                                                         .setSourcePath("src/main/resources/rule.rf")
                                                                                         .setResourceType(ResourceType.DRF));

        setExec( ksession );
        List<String> list = new ArrayList<String>();
        ksession.setGlobal( "list",
                            list );
        TestVariable person = new TestVariable( "John Doe" );

        String inXml = "";
        inXml += "<batch-execution>";
        inXml += "  <start-process processId='org.drools.actions'>";
        inXml += "    <parameter identifier='person'>";
        inXml += "       <org.kie.camel.testdomain.TestVariable>";
        inXml += "         <name>John Doe</name>";
        inXml += "    </org.kie.camel.testdomain.TestVariable>";
        inXml += "    </parameter>";
        inXml += "  </start-process>";
        inXml += "  <get-global identifier='list' out-identifier='out-list'/>";
        inXml += "</batch-execution>";

        String outXml = template.requestBody( "direct:exec",
                                              inXml,
                                              String.class );

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

    @Test
    public void testProcessInstanceSignalEvent() throws Exception {
        String str = "";
        str += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        str += "<process xmlns=\"http://drools.org/drools-5.0/process\"\n";
        str += "         xmlns:xs=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        str += "         xs:schemaLocation=\"http://drools.org/drools-5.0/process drools-processes-5.0.xsd\"\n";
        str += "         type=\"RuleFlow\" name=\"flow\" id=\"org.drools.core.event\" package-name=\"org.drools\" version=\"1\" >\n";
        str += "\n";
        str += "  <header>\n";
        str += "    <variables>\n";
        str += "      <variable name=\"MyVar\" >\n";
        str += "        <type name=\"org.drools.core.process.core.datatype.impl.type.StringDataType\" />\n";
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

        KieSession ksession = getKieSessionFromResource(ResourceFactory.newByteArrayResource(str.getBytes())
                                                                       .setSourcePath("src/main/resources/rule.rf")
                                                                       .setResourceType(ResourceType.DRF));

        ProcessInstance processInstance = ksession.startProcess( "org.drools.core.event" );
        assertEquals( ProcessInstance.STATE_ACTIVE,
                      processInstance.getState() );

        String inXml = "";
        inXml += "<signal-event process-instance-id= '" + processInstance.getId() + "' event-type='MyEvent'>";
        inXml += "    <string>MyValue</string>";
        inXml += "</signal-event>";

        setExec( ksession );

        String outXml = template.requestBody( "direct:exec",
                                              inXml,
                                              String.class );

        assertEquals( ProcessInstance.STATE_COMPLETED,
                      processInstance.getState() );
        assertEquals( "MyValue",
                      ((VariableScopeInstance) ((org.jbpm.process.instance.ProcessInstance) processInstance).getContextInstance( VariableScope.VARIABLE_SCOPE )).getVariable( "MyVar" ) );
    }

    @Test
    public void testProcessRuntimeSignalEvent() throws Exception {
        String str = "";
        str += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        str += "<process xmlns=\"http://drools.org/drools-5.0/process\"\n";
        str += "         xmlns:xs=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
        str += "         xs:schemaLocation=\"http://drools.org/drools-5.0/process drools-processes-5.0.xsd\"\n";
        str += "         type=\"RuleFlow\" name=\"flow\" id=\"org.drools.core.event\" package-name=\"org.drools\" version=\"1\" >\n";
        str += "\n";
        str += "  <header>\n";
        str += "    <variables>\n";
        str += "      <variable name=\"MyVar\" >\n";
        str += "        <type name=\"org.drools.core.process.core.datatype.impl.type.StringDataType\" />\n";
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

        KieSession ksession = getKieSessionFromResource(ResourceFactory.newByteArrayResource(str.getBytes())
                                                                       .setSourcePath("src/main/resources/rule.rf")
                                                                       .setResourceType(ResourceType.DRF));

        ProcessInstance processInstance = ksession.startProcess( "org.drools.core.event" );
        assertEquals( ProcessInstance.STATE_ACTIVE,
                      processInstance.getState() );

        String inXml = "";
        inXml += "<signal-event event-type='MyEvent'>";
        inXml += "    <string>MyValue</string>";
        inXml += "</signal-event>";

        setExec( ksession );

        String outXml = template.requestBody( "direct:exec",
                                              inXml,
                                              String.class );

        assertEquals( ProcessInstance.STATE_COMPLETED,
                      processInstance.getState() );
        assertEquals( "MyValue",
                      ((VariableScopeInstance) ((org.jbpm.process.instance.ProcessInstance) processInstance).getContextInstance( VariableScope.VARIABLE_SCOPE )).getVariable( "MyVar" ) );
    }

    @Test
    public void testCompleteWorkItem() {
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
        str += "        <type name=\"org.drools.core.process.core.datatype.impl.type.StringDataType\" />\n";
        str += "        <value>John Doe</value>\n";
        str += "      </variable>\n";
        str += "      <variable name=\"Person\" >\n";
        str += "        <type name=\"org.drools.core.process.core.datatype.impl.type.ObjectDataType\" className=\"org.kie.camel.testdomain.Person\" />\n";
        str += "      </variable>\n";
        str += "      <variable name=\"MyObject\" >\n";
        str += "        <type name=\"org.drools.core.process.core.datatype.impl.type.StringDataType\" className=\"java.lang.String\" />\n";
        str += "      </variable>\n";
        str += "      <variable name=\"Number\" >\n";
        str += "        <type name=\"org.drools.core.process.core.datatype.impl.type.IntegerDataType\" />\n";
        str += "      </variable>\n";
        str += "    </variables>\n";
        str += "  </header>\n";
        str += "\n";
        str += "  <nodes>\n";
        str += "    <start id=\"1\" name=\"Start\" />\n";
        str += "    <workItem id=\"2\" name=\"HumanTask\" >\n";
        str += "      <work name=\"Human Task\" >\n";
        str += "        <parameter name=\"ActorId\" >\n";
        str += "          <type name=\"org.drools.core.process.core.datatype.impl.type.StringDataType\" />\n";
        str += "          <value>#{UserName}</value>\n";
        str += "        </parameter>\n";
        str += "        <parameter name=\"Content\" >\n";
        str += "          <type name=\"org.drools.core.process.core.datatype.impl.type.StringDataType\" />\n";
        str += "          <value>#{Person.name}</value>\n";
        str += "        </parameter>\n";
        str += "        <parameter name=\"TaskName\" >\n";
        str += "          <type name=\"org.drools.core.process.core.datatype.impl.type.StringDataType\" />\n";
        str += "          <value>Do something</value>\n";
        str += "        </parameter>\n";
        str += "        <parameter name=\"Priority\" >\n";
        str += "          <type name=\"org.drools.core.process.core.datatype.impl.type.StringDataType\" />\n";
        str += "        </parameter>\n";
        str += "        <parameter name=\"Comment\" >\n";
        str += "          <type name=\"org.drools.core.process.core.datatype.impl.type.StringDataType\" />\n";
        str += "        </parameter>\n";
        str += "        <parameter name=\"Attachment\" >\n";
        str += "          <type name=\"org.drools.core.process.core.datatype.impl.type.ObjectDataType\" className=\"java.lang.Object\" />\n";
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

        KieSession ksession = getKieSessionFromResource(ResourceFactory.newByteArrayResource(str.getBytes())
                                                                       .setSourcePath("src/main/resources/rule.rf")
                                                                       .setResourceType(ResourceType.DRF));

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
        inXml = "<complete-work-item id='" + workItem.getId() + "' />";
        setExec( ksession );

        String outXml = template.requestBody( "direct:exec",
                                              inXml,
                                              String.class );

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

        inXml = "";
        inXml += "<complete-work-item id='" + workItem.getId() + "' >";
        inXml += "    <result identifier='Result'>";
        inXml += "        <string>SomeOtherString</string>";
        inXml += "    </result>";
        inXml += "</complete-work-item>";

        outXml = template.requestBody( "direct:exec",
                                       inXml,
                                       String.class );

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
        str += "        <type name=\"org.drools.core.process.core.datatype.impl.type.StringDataType\" />\n";
        str += "        <value>John Doe</value>\n";
        str += "      </variable>\n";
        str += "      <variable name=\"Person\" >\n";
        str += "        <type name=\"org.drools.core.process.core.datatype.impl.type.ObjectDataType\" className=\"org.kie.camel.testdomain.Person\" />\n";
        str += "      </variable>\n";
        str += "      <variable name=\"MyObject\" >\n";
        str += "        <type name=\"org.drools.core.process.core.datatype.impl.type.StringDataType\" className=\"java.lang.String\" />\n";
        str += "      </variable>\n";
        str += "      <variable name=\"Number\" >\n";
        str += "        <type name=\"org.drools.core.process.core.datatype.impl.type.IntegerDataType\" />\n";
        str += "      </variable>\n";
        str += "    </variables>\n";
        str += "  </header>\n";
        str += "\n";
        str += "  <nodes>\n";
        str += "    <start id=\"1\" name=\"Start\" />\n";
        str += "    <workItem id=\"2\" name=\"HumanTask\" >\n";
        str += "      <work name=\"Human Task\" >\n";
        str += "        <parameter name=\"ActorId\" >\n";
        str += "          <type name=\"org.drools.core.process.core.datatype.impl.type.StringDataType\" />\n";
        str += "          <value>#{UserName}</value>\n";
        str += "        </parameter>\n";
        str += "        <parameter name=\"Content\" >\n";
        str += "          <type name=\"org.drools.core.process.core.datatype.impl.type.StringDataType\" />\n";
        str += "          <value>#{Person.name}</value>\n";
        str += "        </parameter>\n";
        str += "        <parameter name=\"TaskName\" >\n";
        str += "          <type name=\"org.drools.core.process.core.datatype.impl.type.StringDataType\" />\n";
        str += "          <value>Do something</value>\n";
        str += "        </parameter>\n";
        str += "        <parameter name=\"Priority\" >\n";
        str += "          <type name=\"org.drools.core.process.core.datatype.impl.type.StringDataType\" />\n";
        str += "        </parameter>\n";
        str += "        <parameter name=\"Comment\" >\n";
        str += "          <type name=\"org.drools.core.process.core.datatype.impl.type.StringDataType\" />\n";
        str += "        </parameter>\n";
        str += "        <parameter name=\"Attachment\" >\n";
        str += "          <type name=\"org.drools.core.process.core.datatype.impl.type.ObjectDataType\" className=\"java.lang.Object\" />\n";
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

        KieSession ksession = getKieSessionFromResource(ResourceFactory.newByteArrayResource(str.getBytes())
                                                                       .setSourcePath("src/main/resources/rule.rf")
                                                                       .setResourceType(ResourceType.DRF));

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

        String inXml = "<abort-work-item id='" + workItem.getId() + "' />";
        setExec( ksession );

        String outXml = template.requestBody( "direct:exec",
                                              inXml,
                                              String.class );

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

        KieSession ksession = getKieSession( ResourceFactory.newByteArrayResource( str.getBytes() ) );
        setExec( ksession );

        String outXml = template.requestBody( "direct:exec",
                                              inXml,
                                              String.class );

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
        expectedXml += "  <fact-handle identifier=\"outStilton\" external-form=\"" + factHandle.toExternalForm() + "\" /> \n";
        expectedXml += "</execution-results>\n";

        assertXMLEqual( expectedXml,
                        outXml );

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

        KieSession ksession = getKieSession( ResourceFactory.newByteArrayResource(str.getBytes()) );
        setExec( ksession );

        String outXml = template.requestBody( "direct:exec",
                                              inXml,
                                              String.class );
        ExecutionResults result = template.requestBody( "direct:unmarshal",
                                                        outXml,
                                                        ExecutionResults.class );

        outXml = template.requestBody( "direct:exec",
                                       "<batch-execution><query out-identifier='matchingthings' name='results'/></batch-execution>",
                                       String.class );

        //we have not fired the rules yet
        assertFalse( outXml.indexOf( "<price>30</price>" ) > -1 );

        //lets send a command to execute them then
        inXml = "";
        inXml += "<batch-execution>";
        inXml += "  <fire-all-rules max='100'/>";
        inXml += "</batch-execution>";
        template.requestBody( "direct:exec",
                              inXml,
                              String.class );
        //ksession.fireAllRules();

        //ok lets try that again...
        outXml = template.requestBody( "direct:exec",
                                       "<batch-execution><query out-identifier='matchingthings' name='results'/></batch-execution>",
                                       String.class );
        assertTrue( outXml.indexOf( "<price>30</price>" ) > -1 );
    }

    @Test
    public void testExecutionNodeLookup() throws Exception {
        String str = "";
        str += "package org.kie.camel.testdomain \n"
                + "declare Cheese1\n"
                + "   type : String\n"
                + "   price : int\n"
                + "   oldPrice : int\n"
                + "end \n";
        
        str += "rule rule1 \n";
        str += "  when \n";
        str += "    $c : Cheese1() \n";
        str += " \n";
        str += "  then \n";
        str += "    $c.setPrice( $c.getPrice() + 5 ); \n";
        str += "end\n";
        //System.out.println("STR = "+str);
        String str2 = "";
        str2 += "package org.kie.camel.testdomain \n"
                + "declare Cheese2\n"
                + "   type : String\n"
                + "   price : int\n"
                + "   oldPrice : int\n"
                + "end \n";
        str2 += "rule rule2 \n";
        str2 += "  when \n";
        str2 += "    $c : Cheese2() \n";
        str2 += " \n";
        str2 += "  then \n";
        str2 += "    $c.setPrice( $c.getPrice() + 10 ); \n";
        str2 += "end\n";
        //System.out.println("STR2 = "+str2);
        String inXml = "";
        inXml += "<batch-execution lookup=\"ksession1\" >";
        inXml += "  <insert out-identifier='outStilton'>";
        inXml += "    <org.kie.camel.testdomain.Cheese1>";
        inXml += "      <type>stilton</type>";
        inXml += "      <price>25</price>";
        inXml += "      <oldPrice>0</oldPrice>";
        inXml += "    </org.kie.camel.testdomain.Cheese1>";
        inXml += "  </insert>";
        inXml += "  <fire-all-rules />";
        inXml += "</batch-execution>";
        
        String inXml2 = "";
        inXml2 += "<batch-execution lookup=\"ksession2\" >";
        inXml2 += "  <insert out-identifier='outStilton'>";
        inXml2 += "    <org.kie.camel.testdomain.Cheese2>";
        inXml2 += "      <type>stilton</type>";
        inXml2 += "      <price>25</price>";
        inXml2 += "      <oldPrice>0</oldPrice>";
        inXml2 += "    </org.kie.camel.testdomain.Cheese2>";
        inXml2 += "  </insert>";
        inXml2 += "  <fire-all-rules />";
        inXml2 += "</batch-execution>";

        KieSession ksession = getKieSession( ResourceFactory.newByteArrayResource( str.getBytes() ) );
        setExec( ksession );
        
        KieSession ksession2 = getKieSession( ResourceFactory.newByteArrayResource( str2.getBytes() ) );
        
        setExec2( ksession2 );

        String outXml = template.requestBody( "direct:execWithLookup",
                                              inXml,
                                              String.class );
        ClassLoader originalClassLoader = Thread.currentThread().getContextClassLoader();
        
        Thread.currentThread().setContextClassLoader( getClassLoader(ksession) );
        ExecutionResults result = template.requestBody( "direct:unmarshal",
                                                        outXml,
                                                        ExecutionResults.class );
        org.kie.api.definition.type.FactType fT = ksession.getKieBase().getFactType("org.kie.camel.testdomain","Cheese1");
        
        int price = (Integer)fT.get(result.getValue( "outStilton" ), "price");
        assertEquals( 30, 
                      price );

        FactHandle factHandle = (FactHandle) result.getFactHandle( "outStilton" );
//        stilton = (Cheese) ksession.getObject( factHandle );
//        assertEquals( 30,
//                      stilton.getPrice() );
        
        String outXml2 = template.requestBody( "direct:execWithLookup",
                                              inXml2,
                                              String.class );
        
        
        Thread.currentThread().setContextClassLoader( getClassLoader(ksession2) );
        ExecutionResults result2 = template.requestBody( "direct:unmarshal",
                                                        outXml2,
                                                        ExecutionResults.class );
        
        org.kie.api.definition.type.FactType fT2 = ksession2.getKieBase().getFactType("org.kie.camel.testdomain","Cheese2");
        
        int price2 = (Integer)fT2.get(result2.getValue( "outStilton" ), "price");
        assertEquals( 35, price2 );
        
//        Cheese2 stilton2 = (Cheese2) result2.getValue( "outStilton" );
//        assertEquals( 35,
//                      stilton2.getPrice() );
//
          factHandle = (FactHandle) result2.getFactHandle( "outStilton" );
//        stilton2 = (Cheese2) ksession2.getObject( factHandle );
//        assertEquals( 35,
//                      stilton2.getPrice() );
        
        
        Thread.currentThread().setContextClassLoader( originalClassLoader );
        
        String expectedXml = "";
        expectedXml += "<execution-results>\n";
        expectedXml += "  <result identifier=\"outStilton\">\n";
        expectedXml += "    <org.kie.camel.testdomain.Cheese1>\n";
        expectedXml += "      <type>stilton</type>\n";
        expectedXml += "      <oldPrice>0</oldPrice>\n";
        expectedXml += "      <price>30</price>\n";
        expectedXml += "    </org.kie.camel.testdomain.Cheese1>\n";
        expectedXml += "  </result>\n";
        expectedXml += "  <fact-handle identifier=\"outStilton\" external-form=\"" + ((InternalFactHandle) result.getFactHandle( "outStilton" )).toExternalForm() + "\" /> \n";
        expectedXml += "</execution-results>\n";
        
        String expectedXml2 = "";
        expectedXml2 += "<execution-results>\n";
        expectedXml2 += "  <result identifier=\"outStilton\">\n";
        expectedXml2 += "    <org.kie.camel.testdomain.Cheese2>\n";
        expectedXml2 += "      <type>stilton</type>\n";
        expectedXml2 += "      <oldPrice>0</oldPrice>\n";
        expectedXml2 += "      <price>35</price>\n";
        expectedXml2 += "    </org.kie.camel.testdomain.Cheese2>\n";
        expectedXml2 += "  </result>\n";
        expectedXml2 += "  <fact-handle identifier=\"outStilton\" external-form=\"" + ((InternalFactHandle) result2.getFactHandle( "outStilton" )).toExternalForm() + "\" /> \n";
        expectedXml2 += "</execution-results>\n";

        assertXMLEqual( expectedXml,
                        outXml );
        
        assertXMLEqual( expectedXml2,
                        outXml2 );
    }

    private StatelessKieSession getStatelessKieSession(Resource resource) {
        return getStatelessKieSessionFromResource(resource.setSourcePath("src/main/resources/rule.drl")
                                                          .setResourceType(ResourceType.DRL));
    }

    private StatelessKieSession getStatelessKieSessionFromResource(Resource resource) {
        KieServices ks = KieServices.Factory.get();
        KieFileSystem kfs = ks.newKieFileSystem();

        kfs.write(resource);

        KieBuilder kieBuilder = ks.newKieBuilder( kfs ).buildAll();

        List<Message> errors = kieBuilder.getResults().getMessages(Message.Level.ERROR);
        if (!errors.isEmpty()) {
            fail("" + errors);
        }

        return ks.newKieContainer(ks.getRepository().getDefaultReleaseId()).newStatelessKieSession();
    }

    private KieSession getKieSession(Resource resource) {
        return getKieSessionFromResource(resource.setSourcePath("src/main/resources/rule.drl")
                                                 .setResourceType(ResourceType.DRL));
    }

    private KieSession getKieSessionFromResource(Resource resource) {
        KieServices ks = KieServices.Factory.get();
        KieFileSystem kfs = ks.newKieFileSystem();

        kfs.write(resource);

        KieBuilder kieBuilder = ks.newKieBuilder( kfs ).buildAll();

        List<Message> errors = kieBuilder.getResults().getMessages(Message.Level.ERROR);
        if (!errors.isEmpty()) {
            fail("" + errors);
        }

        return ks.newKieContainer(ks.getRepository().getDefaultReleaseId()).newKieSession();
    }

    public ClassLoader getClassLoader(CommandExecutor exec) {
        ClassLoader cl = null;

        if ( exec instanceof StatefulKnowledgeSessionImpl ) {
            cl = ((ReteooRuleBase) ((StatefulKnowledgeSessionImpl) exec).getRuleBase()).getRootClassLoader();
        } else if ( exec instanceof StatelessKnowledgeSessionImpl ) {
            cl = ((ReteooRuleBase) ((StatelessKnowledgeSessionImpl) exec).getRuleBase()).getRootClassLoader();
        } else if ( exec instanceof CommandBasedStatefulKnowledgeSession ) {
            cl = ((ReteooRuleBase) ((KnowledgeBaseImpl) ((CommandBasedStatefulKnowledgeSession) exec).getKieBase()).getRuleBase()).getRootClassLoader();
        }

        return cl;
    }

}
