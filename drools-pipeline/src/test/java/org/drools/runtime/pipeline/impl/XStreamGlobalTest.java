package org.drools.runtime.pipeline.impl;

import junit.framework.TestCase;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLAssert;
import org.custommonkey.xmlunit.XMLUnit;
import org.custommonkey.xmlunit.examples.RecursiveElementNameAndTextQualifier;
import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.builder.KnowledgeBuilder;
import org.drools.builder.KnowledgeBuilderFactory;
import org.drools.builder.ResourceType;
import org.drools.io.ResourceFactory;
import org.drools.runtime.StatefulKnowledgeSession;
import org.drools.runtime.pipeline.Action;
import org.drools.runtime.pipeline.KnowledgeRuntimeCommand;
import org.drools.runtime.pipeline.Pipeline;
import org.drools.runtime.pipeline.PipelineFactory;
import org.drools.runtime.pipeline.Transformer;
import org.drools.runtime.pipeline.impl.XStreamStatefulSessionTest.ResultHandlerImpl;

import com.thoughtworks.xstream.XStream;

public class XStreamGlobalTest extends TestCase {
    
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
    
    
    public void testGlobal() throws Exception {
        String xml = "";
        xml += "<list>\n";
        xml += "  <example.OrderItem>\n";
        xml += "    <price>8.9</price>\n";        
        xml += "    <productId>111</productId>\n";
        xml += "    <quantity>2</quantity>\n";        
        xml += "  </example.OrderItem>\n";
        xml += "</list>";

        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();

        kbuilder.add( ResourceFactory.newClassPathResource( "test_XStreamDirectRoot.drl",
                                                            XStreamStatefulSessionTest.class ),
                      ResourceType.DRL );

        assertFalse( kbuilder.hasErrors() );

        assertFalse( kbuilder.hasErrors() );

        KnowledgeBase kbase = KnowledgeBaseFactory.newKnowledgeBase();
        kbase.addKnowledgePackages( kbuilder.getKnowledgePackages() );

        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();

        KnowledgeRuntimeCommand setGlobalStage = PipelineFactory.newStatefulKnowledgeSessionSetGlobal( "list" );

        XStream xstream = new XStream();
        Transformer transformer = PipelineFactory.newXStreamFromXmlTransformer( xstream );
        transformer.setReceiver( setGlobalStage );

        Pipeline pipeline = PipelineFactory.newStatefulKnowledgeSessionPipeline( ksession );
        pipeline.setReceiver( transformer );

        ResultHandlerImpl resultHandler = new ResultHandlerImpl();
        pipeline.insert( xml,
                         resultHandler );


        // now round trip that global
        Action executeResult = PipelineFactory.newExecuteResultHandler();
        
        Action assignAsResult = PipelineFactory.newAssignObjectAsResult();
        assignAsResult.setReceiver( executeResult );
        
        transformer = PipelineFactory.newXStreamToXmlTransformer( xstream );
        transformer.setReceiver( assignAsResult );
        
        KnowledgeRuntimeCommand getGlobalStage = PipelineFactory.newStatefulKnowledgeSessionGetGlobal( );        
        getGlobalStage.setReceiver( transformer );

        pipeline = PipelineFactory.newStatefulKnowledgeSessionPipeline( ksession );
        pipeline.setReceiver( getGlobalStage );
        
        resultHandler = new ResultHandlerImpl();
        pipeline.insert( "list", resultHandler );  
        
        assertXMLEqual( xml, (String) resultHandler.getObject() );
    }
    
    private static void assertEqualsIgnoreWhitespace(final String expected,
                                                     final String actual) {
               final String cleanExpected = expected.replaceAll( "\\s+",
                                                                 "" );
               final String cleanActual = actual.replaceAll( "\\s+",
                                                             "" );
               assertEquals( cleanExpected,
                             cleanActual );
           }
}
