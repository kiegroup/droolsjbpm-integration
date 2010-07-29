package org.drools.camel.component;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import javax.naming.Context;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.builder.RouteBuilder;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jettison.badgerfish.BadgerFishDOMDocumentParser;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.examples.RecursiveElementNameAndTextQualifier;
import org.drools.ChangeCollector;
import org.drools.Cheese;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.DirectoryLookupFactoryService;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.common.InternalFactHandle;
import org.drools.definition.KnowledgePackage;
import org.drools.grid.ExecutionNode;
import org.drools.grid.local.LocalConnection;
import org.drools.io.Resource;
import org.drools.io.ResourceFactory;
import org.drools.rule.builder.dialect.java.AbstractJavaRuleBuilder;
import org.drools.runtime.CommandExecutor;
import org.drools.runtime.ExecutionResults;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.StatelessKnowledgeSession;
import org.drools.runtime.rule.FactHandle;
import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mvel2.integration.impl.MapVariableResolverFactory;
import org.mvel2.templates.SimpleTemplateRegistry;
import org.mvel2.templates.TemplateCompiler;
import org.mvel2.templates.TemplateRegistry;
import org.mvel2.templates.TemplateRuntime;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class JSonBatchExecutionTest  extends BatchTest {//extends ContextTestSupport {
    
    public JSonBatchExecutionTest() {
        super();
        this.dataformat = "json";
        //copyToDataFormat = "jaxb";
    }
        
    
    public void assertXMLEqual(String expectedXml,
                                String resultXml) {
        try {            
            ObjectMapper mapper = new ObjectMapper();
            JsonNode tree1 = mapper.readTree(expectedXml);
            JsonNode tree2 = mapper.readTree(resultXml);
            assertTrue( "Expected:" + expectedXml + "\nwas:" + resultXml, tree1.equals(tree2) );   
        } catch ( Exception e ) {
            throw new RuntimeException( "XML Assertion failure",
                                        e );
        }
    }

    @BeforeClass
    public static void beforeClass() throws Exception {
        //new PrintWriter(new BufferedWriter( new FileWriter( "jaxb.mvt", false )));
    }



//

//    public void testVsmPipeline() throws Exception {
//        String str = "";
//        str += "package org.drools \n";
//        str += "import org.drools.Cheese \n";
//        str += "rule rule1 \n";
//        str += "  when \n";
//        str += "    $c : Cheese() \n";
//        str += " \n";
//        str += "  then \n";
//        str += "    $c.setPrice( $c.getPrice() + 5 ); \n";
//        str += "end\n";
//
//        String inXml = "";
//        inXml += "{\"batch-execution\":{\"lookup\":\"ksession1\", \"commands\":[";
//        inXml += "{\"insert\":{\"object\":{\"org.drools.Cheese\":{\"type\":\"stilton\",\"price\":25,\"oldPrice\":0}}, \"out-identifier\":\"outStilton\" } }";
//        inXml += ", {\"fire-all-rules\":\"\"}";
//        inXml += "]}}";        
//        inXml = roundTripFromXml( inXml );        
//        
//        LocalConnection connection = new LocalConnection();
//        ExecutionNode node = connection.getExecutionNode(null);
//
//        StatefulKnowledgeSession ksession = getExecutionNodeSessionStateful(node, ResourceFactory.newByteArrayResource( str.getBytes() ) );
//
//        node.get(DirectoryLookupFactoryService.class).register("ksession1", ksession);
//
//        XStreamResolverStrategy xstreamStrategy = new XStreamResolverStrategy() {
//            public XStream lookup(String name) {
//                return BatchExecutionHelper.newJSonMarshaller();
//            }
//        };
//
//        ResultHandlerImpl resultHandler = new ResultHandlerImpl();
//        getPipelineSessionStateful(node, xstreamStrategy).insert(inXml, resultHandler);
//        String outXml = (String) resultHandler.getObject();
//
//        ExecutionResults result = (ExecutionResults) BatchExecutionHelper.newJSonMarshaller().fromXML( outXml );
//        Cheese stilton = (Cheese) result.getValue( "outStilton" );
//        assertEquals( 30,
//                      stilton.getPrice() );
//
//        FactHandle factHandle = (FactHandle) result.getFactHandle( "outStilton" );
//        stilton = (Cheese) ksession.getObject( factHandle );
//        assertEquals( 30,
//                      stilton.getPrice() );
//
////        String expectedXml = "";
////        expectedXml += "<execution-results>\n";
////        expectedXml += "  <result identifier=\"outStilton\">\n";
////        expectedXml += "    <org.drools.Cheese>\n";
////        expectedXml += "      <type>stilton</type>\n";
////        expectedXml += "      <oldPrice>0</oldPrice>\n";
////        expectedXml += "      <price>30</price>\n";
////        expectedXml += "    </org.drools.Cheese>\n";
////        expectedXml += "  </result>\n";
////        expectedXml += "  <fact-handle identifier=\"outStilton\" externalForm=\"" + ((InternalFactHandle) result.getFactHandle( "outStilton" )).toExternalForm() + "\" /> \n";
////        expectedXml += "</execution-results>\n";
////
////        assertXMLEqual( expectedXml,
////                        outXml );
//    }

//    private StatefulKnowledgeSession getExecutionNodeSessionStateful(ExecutionNode node, Resource resource) throws Exception {
//        KnowledgeBuilder kbuilder = node.get(KnowledgeBuilderFactoryService.class).newKnowledgeBuilder();
//        kbuilder.add( resource,
//                      ResourceType.DRL );
//
//        if ( kbuilder.hasErrors() ) {
//            System.out.println( kbuilder.getErrors() );
//        }
//
//        assertFalse( kbuilder.hasErrors() );
//        Collection<KnowledgePackage> pkgs = kbuilder.getKnowledgePackages();
//
//        KnowledgeBase kbase = node.get(KnowledgeBaseFactoryService.class).newKnowledgeBase();
//
//        kbase.addKnowledgePackages( pkgs );
//        StatefulKnowledgeSession session = kbase.newStatefulKnowledgeSession();
//
//        return session;
//    }

}
