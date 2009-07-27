package org.drools.runtime.pipeline.impl;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.naming.Context;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import junit.framework.TestCase;

import org.apache.activemq.broker.BrokerService;
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
import org.drools.runtime.pipeline.ResultHandlerFactory;
import org.drools.runtime.pipeline.Service;
import org.drools.runtime.pipeline.Transformer;
import org.drools.runtime.rule.FactHandle;
import org.drools.util.StringUtils;

import com.sun.tools.xjc.Language;
import com.sun.tools.xjc.Options;

public class JaxbSimpleJmsMessengerTest extends TestCase {

    private SimpleProducer simpleProducer;
    private BrokerService  broker;
    private String         destinationName = "dynamicQueues/FOO.BAR";
    private String         url             = "vm://localhost:61616";

    private Properties     props;

    protected void setUp() {
        try {
            this.broker = new BrokerService();
            // configure the broker
            this.broker.setBrokerName( "consumer" );
            this.broker.addConnector( url );
            this.broker.start();

            props = new Properties();
            props.setProperty( Context.INITIAL_CONTEXT_FACTORY,
                               "org.apache.activemq.jndi.ActiveMQInitialContextFactory" );
            props.setProperty( Context.PROVIDER_URL,
                               this.url );

            this.simpleProducer = new SimpleProducer( props,
                                                      this.destinationName );
            this.simpleProducer.start();
        } catch ( Exception e ) {
            throw new RuntimeException( e );
        }
    }
    
    protected void tearDown() throws Exception {
        this.simpleProducer.stop();
        this.broker.stop();
    }

    public void testJmsWithJaxb() throws Exception {
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

        Action resultHandlerStage = PipelineFactory.newExecuteResultHandler();

        KnowledgeRuntimeCommand insertStage = PipelineFactory.newStatefulKnowledgeSessionInsert();
        insertStage.setReceiver( resultHandlerStage );

        JAXBContext jaxbCtx = KnowledgeBuilderHelper.newJAXBContext( classNames,
                                                                     kbase );
        Transformer transformer = PipelineFactory.newJaxbFromXmlTransformer( jaxbCtx );
        transformer.setReceiver( insertStage );

        Action unwrapObjectStage = PipelineFactory.newJmsUnwrapMessageObject();
        unwrapObjectStage.setReceiver( transformer );

        Pipeline pipeline = PipelineFactory.newStatefulKnowledgeSessionPipeline( ksession );
        pipeline.setReceiver( unwrapObjectStage );

        ResultHandleFactoryImpl factory = new ResultHandleFactoryImpl();
        Service feeder = PipelineFactory.newJmsMessenger( pipeline,
                                                          props,
                                                          this.destinationName,
                                                          factory );
        feeder.start();

        String xml = StringUtils.readFileAsString( new InputStreamReader( getClass().getResourceAsStream( "order.xml" ) ) );

        this.simpleProducer.sendObject( xml );

        for ( int i = 0; i < 5; i++ ) {
            // iterate and sleep 5 times, to give these messages time to complete.
            if ( factory.list.size() == 1 ) {
                break;
            }
            Thread.sleep( 5000 );
        }

        FactHandle factHandle = (FactHandle) ((Map) ((ResultHandlerImpl) factory.list.get( 0 )).getObject()).keySet().iterator().next();
        assertNotNull( factHandle );

        assertEquals( 1,
                      factory.list.size() );

        Action executeResult = PipelineFactory.newExecuteResultHandler();

        Action assignAsResult = PipelineFactory.newAssignObjectAsResult();
        assignAsResult.setReceiver( executeResult );

        //transformer = PipelineFactory.newXStreamToXmlTransformer( xstream );
        transformer = PipelineFactory.newJaxbToXmlTransformer( jaxbCtx );
        transformer.setReceiver( assignAsResult );

        KnowledgeRuntimeCommand getObject = PipelineFactory.newStatefulKnowledgeSessionGetObject();
        getObject.setReceiver( transformer );

        pipeline = PipelineFactory.newStatefulKnowledgeSessionPipeline( ksession );
        pipeline.setReceiver( getObject );

        ResultHandlerImpl resultHandler = new ResultHandlerImpl();
        pipeline.insert( factHandle,
                         resultHandler );

        assertEqualsIgnoreWhitespace( xml,
                                      (String) resultHandler.getObject() );
        feeder.stop();

    }

    public static class ResultHandleFactoryImpl
        implements
        ResultHandlerFactory {
        List list = new ArrayList();

        public ResultHandler newResultHandler() {
            ResultHandler handler = new ResultHandlerImpl();
            list.add( handler );
            return handler;
        }

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
