package org.kie.camel.component;


import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationConfig;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.drools.core.runtime.help.impl.XStreamJSon;
import org.drools.core.runtime.help.impl.XStreamJSon.JSonQueryResultsConverter;
import org.junit.BeforeClass;

import java.io.StringWriter;

public class JSonBatchExecutionTest extends BatchTest {

    public JSonBatchExecutionTest() {
        super();
        this.dataformat = "json";
    }

    public void assertXMLEqual(String expectedXml,
                                String resultXml) {
        try {
            ObjectMapper mapper = new ObjectMapper();

            JsonNode expectedTree = mapper.readTree( expectedXml );
            JsonNode resultTree = mapper.readTree( resultXml );

            assertEquals("Expected:" + expectedXml + "\nwas:" + resultXml,
                    expectedTree, resultTree);

        } catch ( Exception e ) {
            throw new RuntimeException( "XML Assertion failure",
                                        e );
        }
    }

    @BeforeClass
    public static void beforeClass() throws Exception {
        XStreamJSon.SORT_MAPS = true; // allows us to test results easier
        //new PrintWriter(new BufferedWriter( new FileWriter( "jaxb.mvt", false )));
    }

    //

    //    public void testVsmPipeline() throws Exception {
    //        String str = "";
    //        str += "package org.drools \n";
    //        str += "import org.drools.camel.testdomain.Cheese \n";
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
    //        inXml += "{\"insert\":{\"object\":{\"org.drools.camel.testdomain.Cheese\":{\"type\":\"stilton\",\"price\":25,\"oldPrice\":0}}, \"out-identifier\":\"outStilton\" } }";
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
    ////        expectedXml += "    <org.drools.camel.testdomain.Cheese>\n";
    ////        expectedXml += "      <type>stilton</type>\n";
    ////        expectedXml += "      <oldPrice>0</oldPrice>\n";
    ////        expectedXml += "      <price>30</price>\n";
    ////        expectedXml += "    </org.drools.camel.testdomain.Cheese>\n";
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
