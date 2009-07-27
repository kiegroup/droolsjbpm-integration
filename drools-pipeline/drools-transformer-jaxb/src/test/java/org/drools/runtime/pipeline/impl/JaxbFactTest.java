package org.drools.runtime.pipeline.impl;

import java.io.InputStreamReader;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

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
import org.drools.runtime.pipeline.KnowledgeRuntimeCommand;
import org.drools.runtime.pipeline.Pipeline;
import org.drools.runtime.pipeline.PipelineFactory;
import org.drools.runtime.pipeline.ResultHandler;
import org.drools.runtime.pipeline.Transformer;
import org.drools.runtime.rule.FactHandle;
import org.drools.util.StringUtils;

import com.sun.tools.xjc.Language;
import com.sun.tools.xjc.Options;

public class JaxbFactTest extends TestCase {
    public void testFact() throws Exception {
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

        assertFalse( kbuilder.hasErrors() );

        assertFalse( kbuilder.hasErrors() );

        StatefulKnowledgeSession ksession = kbase.newStatefulKnowledgeSession();

        Action executeResultHandler = PipelineFactory.newExecuteResultHandler();

        KnowledgeRuntimeCommand insertStage = PipelineFactory.newStatefulKnowledgeSessionInsert();
        insertStage.setReceiver( executeResultHandler );

        JAXBContext jaxbCtx = KnowledgeBuilderHelper.newJAXBContext( classNames,
                                                                     kbase );
        Unmarshaller unmarshaller = jaxbCtx.createUnmarshaller();

        Transformer transformer = PipelineFactory.newJaxbFromXmlTransformer( jaxbCtx );
        transformer.setReceiver( insertStage );

        Pipeline pipeline = PipelineFactory.newStatefulKnowledgeSessionPipeline( ksession );
        pipeline.setReceiver( transformer );

        String xml = StringUtils.readFileAsString( new InputStreamReader( getClass().getResourceAsStream( "order.xml" ) ) );

        ResultHandlerImpl resultHandler = new ResultHandlerImpl();
        pipeline.insert( xml,
                         resultHandler );

        FactHandle factHandle = (FactHandle) ((Map) resultHandler.getObject()).keySet().iterator().next();
        assertNotNull( factHandle );

        // now round trip that fact
        Action executeResult = PipelineFactory.newExecuteResultHandler();

        Action assignAsResult = PipelineFactory.newAssignObjectAsResult();
        assignAsResult.setReceiver( executeResult );

        //transformer = PipelineFactory.newXStreamToXmlTransformer( xstream );
        Marshaller marshaller = jaxbCtx.createMarshaller();
        transformer = PipelineFactory.newJaxbToXmlTransformer( jaxbCtx );
        transformer.setReceiver( assignAsResult );

        KnowledgeRuntimeCommand getObject = PipelineFactory.newStatefulKnowledgeSessionGetObject();
        getObject.setReceiver( transformer );

        pipeline = PipelineFactory.newStatefulKnowledgeSessionPipeline( ksession );
        pipeline.setReceiver( getObject );

        resultHandler = new ResultHandlerImpl();
        pipeline.insert( factHandle,
                         resultHandler );

        assertEqualsIgnoreWhitespace( xml,
                                      (String) resultHandler.getObject() );
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
