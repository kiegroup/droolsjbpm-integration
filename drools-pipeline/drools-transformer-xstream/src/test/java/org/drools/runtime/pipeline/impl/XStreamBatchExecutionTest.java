package org.drools.runtime.pipeline.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.custommonkey.xmlunit.XMLTestCase;
import org.drools.Cheese;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.definition.KnowledgePackage;
import org.drools.io.Resource;
import org.drools.io.ResourceFactory;
import org.drools.runtime.BatchExecutionResult;
import org.drools.runtime.StatelessKnowledgeSession;
import org.drools.runtime.help.BatchExecutionHelper;
import org.drools.runtime.pipeline.Action;
import org.drools.runtime.pipeline.KnowledgeRuntimeCommand;
import org.drools.runtime.pipeline.Pipeline;
import org.drools.runtime.pipeline.PipelineFactory;
import org.drools.runtime.pipeline.ResultHandler;
import org.drools.runtime.pipeline.Transformer;

public class XStreamBatchExecutionTest extends XMLTestCase {

    public void testInsertObject() throws Exception {
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
        inXml += "</batch-execution>";
        
        StatelessKnowledgeSession ksession = getSession2( ResourceFactory.newByteArrayResource( str.getBytes() ) );
        ResultHandlerImpl resultHandler = new ResultHandlerImpl();        
        getPipeline(ksession).insert( inXml, resultHandler );        
        String outXml = ( String ) resultHandler.getObject();
        
        String expectedXml = "";
        expectedXml += "<batch-execution-results>\n";
        expectedXml += "  <result identifier=\"outStilton\">\n";
        expectedXml += "    <org.drools.Cheese>\n";
        expectedXml += "      <type>stilton</type>\n";
        expectedXml += "      <oldPrice>0</oldPrice>\n";        
        expectedXml += "      <price>30</price>\n";
        expectedXml += "    </org.drools.Cheese>\n";
        expectedXml += "  </result>\n";
        expectedXml += "</batch-execution-results>\n";
        
        assertXMLEqual(expectedXml, outXml );
        
        BatchExecutionResult result = ( BatchExecutionResult ) BatchExecutionHelper.newXStreamMarshaller().fromXML( outXml );
        Cheese stilton = ( Cheese ) result.getValue( "outStilton" );
        assertEquals( 30,
                      stilton.getPrice() );        
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
        getPipeline(ksession).insert( inXml, resultHandler );        
        String outXml = ( String ) resultHandler.getObject();
        
        String expectedXml = "";
        expectedXml += "<batch-execution-results>\n";
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
        expectedXml += "</batch-execution-results>\n";
        
        assertXMLEqual( expectedXml, outXml );
        
        BatchExecutionResult result = ( BatchExecutionResult ) BatchExecutionHelper.newXStreamMarshaller().fromXML( outXml );
        List list = ( List ) result.getValue( "list" );
        Cheese stilton25 = new Cheese( "stilton", 30);
        Cheese stilton30 = new Cheese( "stilton", 35);
        
        Set expectedList = new HashSet();
        expectedList.add( stilton25 );
        expectedList.add( stilton30 );
        
        assertEquals( expectedList, new HashSet( list ));       
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
        getPipeline(ksession).insert( inXml, resultHandler );
        
        String outXml = ( String ) resultHandler.getObject();
        
        String expectedXml = "";
        expectedXml += "<batch-execution-results>\n";
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
        expectedXml += "</batch-execution-results>\n";
        
        assertXMLEqual(expectedXml, outXml ); 
        
        BatchExecutionResult result = ( BatchExecutionResult ) BatchExecutionHelper.newXStreamMarshaller().fromXML( outXml );
        Cheese stilton = new Cheese( "stilton", 30 );
        
        assertNull( result.getValue( "list1" ) );
        
        List list2 = ( List ) result.getValue( "list2" );
        assertEquals( 1, list2.size() );
        assertEquals( stilton, list2.get( 0 ) );
        
          
        
        List list3 = ( List ) result.getValue( "outList3" );
        assertEquals( 1, list3.size() );
        assertEquals( stilton, list3.get( 0 ) );          
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
        getPipeline(ksession).insert( inXml, resultHandler );        
        String outXml = ( String ) resultHandler.getObject();
        
        String expectedXml = "";
        expectedXml += "<batch-execution-results>\n";
        expectedXml += "  <result identifier=\"out-list\">\n";
        expectedXml += "    <list>\n";
        expectedXml += "      <org.drools.Cheese>\n";
        expectedXml += "        <type>stilton</type>\n";      
        expectedXml += "        <price>25</price>\n";
        expectedXml += "        <oldPrice>0</oldPrice>\n";          
        expectedXml += "      </org.drools.Cheese>\n";
        expectedXml += "    </list>\n";
        expectedXml += "  </result>\n";        
        expectedXml += "</batch-execution-results>\n";
        
        assertXMLEqual(expectedXml, outXml );      
    }    
    
    public void testQuery() throws Exception {
        String str = "";
        str += "package org.drools.test  \n";
        str += "import org.drools.Cheese \n";
        str += "query cheeses \n";
        str += "    stilton : Cheese(type == 'stilton') \n";
        str += "    cheddar : Cheese(type == 'cheddar', price == stilton.price) \n";
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
        inXml += "</batch-execution>";

        StatelessKnowledgeSession ksession = getSession2( ResourceFactory.newByteArrayResource( str.getBytes() ) );
        ResultHandlerImpl resultHandler = new ResultHandlerImpl();        
        getPipeline(ksession).insert( inXml, resultHandler );        
        String outXml = ( String ) resultHandler.getObject();
        
        String expectedXml = "";
        expectedXml +="<batch-execution-results>\n";
        expectedXml +="  <result identifier='cheeses'>\n";
        expectedXml +="    <query-results>\n";
        expectedXml +="      <identifiers>\n";
        expectedXml +="        <identifier>stilton</identifier>\n";
        expectedXml +="        <identifier>cheddar</identifier>\n";
        expectedXml +="      </identifiers>\n";
        expectedXml +="      <row>\n";
        expectedXml +="        <org.drools.Cheese>\n";
        expectedXml +="          <type>stilton</type>\n";
        expectedXml +="          <price>2</price>\n";
        expectedXml +="          <oldPrice>0</oldPrice>\n";
        expectedXml +="        </org.drools.Cheese>\n";
        expectedXml +="        <org.drools.Cheese>\n";
        expectedXml +="          <type>cheddar</type>\n";
        expectedXml +="          <price>2</price>\n";
        expectedXml +="          <oldPrice>0</oldPrice>\n";
        expectedXml +="        </org.drools.Cheese>\n";
        expectedXml +="      </row>\n";
        expectedXml +="      <row>\n";
        expectedXml +="        <org.drools.Cheese>\n";
        expectedXml +="          <type>stilton</type>\n";
        expectedXml +="          <price>1</price>\n";
        expectedXml +="          <oldPrice>0</oldPrice>\n";
        expectedXml +="        </org.drools.Cheese>\n";
        expectedXml +="        <org.drools.Cheese>\n";
        expectedXml +="          <type>cheddar</type>\n";
        expectedXml +="          <price>1</price>\n";
        expectedXml +="          <oldPrice>0</oldPrice>\n";
        expectedXml +="        </org.drools.Cheese>\n";
        expectedXml +="      </row>\n";
        expectedXml +="    </query-results>\n";
        expectedXml +="  </result>\n";
        expectedXml +="</batch-execution-results>\n";
        
        assertXMLEqual(expectedXml, outXml ); 
        
        BatchExecutionResult batchResult = ( BatchExecutionResult ) BatchExecutionHelper.newXStreamMarshaller().fromXML( outXml );
        
        Cheese stilton1 = new Cheese( "stilton", 1);
        Cheese cheddar1 = new Cheese( "cheddar", 1);
        Cheese stilton2 = new Cheese( "stilton", 2);
        Cheese cheddar2 = new Cheese( "cheddar", 2);  
        
        Set set = new HashSet();
        List list = new ArrayList();
        list.add(stilton1);
        list.add(cheddar1);
        set.add( list );
        
        list = new ArrayList();
        list.add(stilton2);
        list.add(cheddar2);
        set.add( list );
        
        org.drools.runtime.rule.QueryResults results = ( org.drools.runtime.rule.QueryResults) batchResult.getValue( "cheeses" );
        assertEquals( 2, results.size() );        
        assertEquals( 2, results.getIdentifiers().length );
        Set newSet = new HashSet();
        for ( org.drools.runtime.rule.QueryResultsRow result : results ) {
            list = new ArrayList();
            list.add( result.get( "stilton" ) );
            list.add( result.get( "cheddar" ));
            newSet.add( list );
        }
        assertEquals( set, newSet );  
    }       
    
    private Pipeline getPipeline(StatelessKnowledgeSession ksession) {
        Action executeResultHandler = PipelineFactory.newExecuteResultHandler();
        
        Action assignResult = PipelineFactory.newAssignObjectAsResult();
        assignResult.setReceiver( executeResultHandler );
        
        Transformer outTransformer = PipelineFactory.newXStreamToXmlTransformer( BatchExecutionHelper.newXStreamMarshaller() );
        outTransformer.setReceiver( assignResult );

        KnowledgeRuntimeCommand batchExecution = PipelineFactory.newBatchExecutor();
        batchExecution.setReceiver( outTransformer );


        Transformer inTransformer = PipelineFactory.newXStreamFromXmlTransformer( BatchExecutionHelper.newXStreamMarshaller() );
        inTransformer.setReceiver( batchExecution );

        Pipeline pipeline = PipelineFactory.newStatelessKnowledgeSessionPipeline( ksession );
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
    
    private StatelessKnowledgeSession getSession2(String fileName) throws Exception {
        return getSession2( ResourceFactory.newClassPathResource( fileName, getClass() ) );
    }
        
    private StatelessKnowledgeSession getSession2(Resource resource) throws Exception {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add( resource, ResourceType.DRL );
        
        if (kbuilder.hasErrors() ) {
            System.out.println( kbuilder.getErrors() );
        }
        
        assertFalse( kbuilder.hasErrors() );
        Collection<KnowledgePackage> pkgs = kbuilder.getKnowledgePackages();

        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        
       
        kbase.addKnowledgePackages( pkgs );
        StatelessKnowledgeSession session = kbase.newStatelessKnowledgeSession();

        return session;
    }       

}
