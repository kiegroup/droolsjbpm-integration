package org.kie.server.client.impl;

import org.kie.api.command.Command;
import org.kie.remote.common.rest.KieRemoteHttpRequest;
import org.kie.remote.common.rest.KieRemoteHttpResponse;
import org.kie.server.api.commands.*;
import org.kie.server.api.jms.JMSConstants;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.marshalling.MarshallingException;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.*;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class KieServicesClientImpl
        implements KieServicesClient {
    private static Logger logger = LoggerFactory.getLogger( KieServicesClientImpl.class );

    private       String                   baseURI;
    private final KieServicesConfiguration config;
    private final Marshaller               marshaller;

    public KieServicesClientImpl(KieServicesConfiguration config) {
        this.config = config.clone();
        this.baseURI = config.getServerUrl();
        ClassLoader cl = Thread.currentThread().getContextClassLoader() != null ? Thread.currentThread().getContextClassLoader() : CommandScript.class.getClassLoader();
        this.marshaller = MarshallerFactory.getMarshaller( config.getMarshallingFormat(), cl );
    }

    /**
     * Initializes the URL that will be used for web service access
     *
     * @param url URL of the server instance
     * @return An URL that can be used for the web services
     */
    private String initializeURI(URL url, String servicePrefix) {
        if ( url == null ) {
            throw new IllegalArgumentException( "The url may not be empty or null." );
        }
        try {
            url.toURI();
        } catch ( URISyntaxException urise ) {
            throw new IllegalArgumentException(
                    "URL (" + url.toExternalForm() + ") is incorrectly formatted: " + urise.getMessage(), urise );
        }

        String urlString = url.toExternalForm();
        if ( !urlString.endsWith( "/" ) ) {
            urlString += "/";
        }
        urlString += "services/"+servicePrefix + "/server";

        URL serverPlusServicePrefixUrl;
        try {
            serverPlusServicePrefixUrl = new URL( urlString );
        } catch ( MalformedURLException murle ) {
            throw new IllegalArgumentException(
                    "URL (" + url.toExternalForm() + ") is incorrectly formatted: " + murle.getMessage(), murle );
        }

        return urlString;
    }

    @Override
    public ServiceResponse<KieServerInfo> register(String controllerEndpoint, KieServerConfig kieServerConfig) {
        if( config.isRest() ) {
            return makeHttpPostRequestAndCreateServiceResponse( baseURI+"/controller/"+controllerEndpoint, kieServerConfig, KieServerInfo.class );
        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand) new RegisterServerControllerCommand( controllerEndpoint, kieServerConfig ) ) );
            return (ServiceResponse<KieServerInfo>) executeJmsCommand( script ).getResponses().get( 0 );
        }
    }

    @Override
    public ServiceResponse<KieServerInfo> getServerInfo() {
        if( config.isRest() ) {
            return makeHttpGetRequestAndCreateServiceResponse( baseURI, KieServerInfo.class );
        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand) new GetServerInfoCommand() ) );
            return (ServiceResponse<KieServerInfo>) executeJmsCommand( script ).getResponses().get( 0 );
        }
    }

    @Override
    public ServiceResponse<KieContainerResourceList> listContainers() {
        if( config.isRest() ) {
            return makeHttpGetRequestAndCreateServiceResponse( baseURI + "/containers", KieContainerResourceList.class );
        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand) new ListContainersCommand() ) );
            return (ServiceResponse<KieContainerResourceList>) executeJmsCommand( script ).getResponses().get( 0 );
        }
    }

    @Override
    public ServiceResponse<KieContainerResource> createContainer(String id, KieContainerResource resource) {
        if( config.isRest() ) {
            return makeHttpPutRequestAndCreateServiceResponse( baseURI + "/containers/" + id, resource, KieContainerResource.class );
        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand) new CreateContainerCommand( resource ) ) );
            return (ServiceResponse<KieContainerResource>) executeJmsCommand( script ).getResponses().get( 0 );
        }
    }

    @Override
    public ServiceResponse<KieContainerResource> getContainerInfo(String id) {
        if( config.isRest() ) {
            return makeHttpGetRequestAndCreateServiceResponse( baseURI + "/containers/" + id, KieContainerResource.class );
        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand) new GetContainerInfoCommand( id ) ) );
            return (ServiceResponse<KieContainerResource>) executeJmsCommand( script ).getResponses().get( 0 );
        }
    }

    @Override
    public ServiceResponse<Void> disposeContainer(String id) {
        if( config.isRest() ) {
            return makeHttpDeleteRequestAndCreateServiceResponse( baseURI + "/containers/" + id, Void.class );
        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand) new DisposeContainerCommand( id ) ) );
            return (ServiceResponse<Void>) executeJmsCommand( script ).getResponses().get( 0 );
        }
    }

    @Override
    public ServiceResponse<String> executeCommands(String id, String payload) {
        if( config.isRest() ) {
            return makeHttpPostRequestAndCreateServiceResponse( baseURI + "/containers/" + id, payload, String.class );
        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand) new CallContainerCommand( id, payload ) ) );
            return (ServiceResponse<String>) executeJmsCommand( script ).getResponses().get( 0 );
        }
    }

    @Override
    public ServiceResponsesList executeScript(CommandScript script) {
        if( config.isRest() ) {
            return makeHttpPostRequestAndCreateCustomResult( baseURI, script, ServiceResponsesList.class );
        } else {
            return executeJmsCommand( script );
        }
    }

    @Override
    public ServiceResponse<KieScannerResource> getScannerInfo(String id) {
        if( config.isRest() ) {
            return makeHttpGetRequestAndCreateServiceResponse( baseURI + "/containers/" + id + "/scanner", KieScannerResource.class );
        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand) new GetScannerInfoCommand( id ) ) );
            return (ServiceResponse<KieScannerResource>) executeJmsCommand( script ).getResponses().get( 0 );
        }
    }

    @Override
    public ServiceResponse<KieScannerResource> updateScanner(String id, KieScannerResource resource) {
        if( config.isRest() ) {
            return makeHttpPostRequestAndCreateServiceResponse(
                    baseURI + "/containers/" + id + "/scanner", resource,
                    KieScannerResource.class );
        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand) new UpdateScannerCommand( id, resource ) ) );
            return (ServiceResponse<KieScannerResource>) executeJmsCommand( script ).getResponses().get( 0 );
        }
    }

    @Override
    public ServiceResponse<ReleaseId> updateReleaseId(String id, ReleaseId releaseId) {
        if( config.isRest() ) {
            return makeHttpPostRequestAndCreateServiceResponse(
                    baseURI + "/containers/" + id + "/release-id", releaseId,
                    ReleaseId.class );
        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand) new UpdateReleaseIdCommand( id, releaseId ) ) );
            return (ServiceResponse<ReleaseId>) executeJmsCommand( script ).getResponses().get( 0 );
        }
    }

    @SuppressWarnings("unchecked")
    private <T> ServiceResponse<T> makeHttpGetRequestAndCreateServiceResponse(String uri, Class<T> resultType) {
        KieRemoteHttpRequest request = newRequest( uri ).get();
        KieRemoteHttpResponse response = request.response();

        if ( response.code() == Response.Status.OK.getStatusCode() ) {
            ServiceResponse serviceResponse = deserialize( response.body(), ServiceResponse.class );
            checkResultType( serviceResponse, resultType );
            return serviceResponse;
        } else {
            throw createExceptionForUnexpectedResponseCode( request, response );
        }
    }

    private <T> ServiceResponse<T> makeHttpPostRequestAndCreateServiceResponse(
            String uri, Object bodyObject,
            Class<T> resultType) {
        return makeHttpPostRequestAndCreateServiceResponse( uri, serialize( bodyObject ), resultType );
    }

    @SuppressWarnings("unchecked")
    private <T> ServiceResponse<T> makeHttpPostRequestAndCreateServiceResponse(String uri, String body, Class<T> resultType) {
        KieRemoteHttpRequest request = newRequest( uri ).body( body ).post();
        KieRemoteHttpResponse response = request.response();

        if ( response.code() == Response.Status.OK.getStatusCode() ) {
            ServiceResponse serviceResponse = deserialize( response.body(), ServiceResponse.class );
            checkResultType( serviceResponse, resultType );
            return serviceResponse;
        } else {
            throw createExceptionForUnexpectedResponseCode( request, response );
        }
    }

    private <T> T makeHttpPostRequestAndCreateCustomResult(String uri, Object bodyObject, Class<T> resultType) {
        return makeHttpPostRequestAndCreateCustomResult( uri, serialize( bodyObject ), resultType );
    }

    private <T> T makeHttpPostRequestAndCreateCustomResult(String uri, String body, Class<T> resultType) {
        KieRemoteHttpRequest request = newRequest( uri ).body( body ).post();
        KieRemoteHttpResponse response = request.response();

        if ( response.code() == Response.Status.OK.getStatusCode() ) {
            return deserialize( response.body(), resultType );
        } else {
            throw createExceptionForUnexpectedResponseCode( request, response );
        }
    }

    private <T> ServiceResponse<T> makeHttpPutRequestAndCreateServiceResponse(
            String uri, Object bodyObject,
            Class<T> resultType) {
        return makeHttpPutRequestAndCreateServiceResponse( uri, serialize( bodyObject ), resultType );
    }

    @SuppressWarnings("unchecked")
    private <T> ServiceResponse<T> makeHttpPutRequestAndCreateServiceResponse(String uri, String body, Class<T> resultType) {
        KieRemoteHttpRequest request = newRequest( uri ).body( body ).put();
        KieRemoteHttpResponse response = request.response();

        if ( response.code() == Response.Status.CREATED.getStatusCode() ||
             response.code() == Response.Status.BAD_REQUEST.getStatusCode() ) {
            ServiceResponse serviceResponse = deserialize( response.body(), ServiceResponse.class );
            checkResultType( serviceResponse, resultType );
            return serviceResponse;
        } else {
            throw createExceptionForUnexpectedResponseCode( request, response );
        }
    }

    @SuppressWarnings("unchecked")
    private <T> ServiceResponse<T> makeHttpDeleteRequestAndCreateServiceResponse(String uri, Class<T> resultType) {
        KieRemoteHttpRequest request = newRequest( uri ).delete();
        KieRemoteHttpResponse response = request.response();

        if ( response.code() == Response.Status.OK.getStatusCode() ) {
            ServiceResponse serviceResponse = deserialize( response.body(), ServiceResponse.class );
            checkResultType( serviceResponse, resultType );
            return serviceResponse;
        } else {
            throw createExceptionForUnexpectedResponseCode( request, response );
        }
    }

    private KieRemoteHttpRequest newRequest(String uri) {
        KieRemoteHttpRequest httpRequest =
                KieRemoteHttpRequest.newRequest( uri ).followRedirects( true ).timeout( config.getTimeout() );
        httpRequest.accept( getMediaType( config.getMarshallingFormat() ) );
        if ( config.getUserName() != null && config.getPassword() != null ) {
            httpRequest.basicAuthorization( config.getUserName(), config.getPassword() );
        }
        return httpRequest;
    }

    /**
     * Method to communicate with the backend via JMS.
     *
     * @param command The {@link org.kie.api.command.Command} object to be executed.
     * @return The result of the {@link org.kie.api.command.Command} object execution.
     */
    private ServiceResponsesList executeJmsCommand( CommandScript command ) {
        ConnectionFactory factory = config.getConnectionFactory();
        Queue sendQueue = config.getRequestQueue();
        Queue responseQueue = config.getResponseQueue();

        Connection connection = null;
        Session session = null;
        ServiceResponsesList cmdResponse = null;
        String corrId = UUID.randomUUID().toString();
        String selector = "JMSCorrelationID = '" + corrId + "'";
        try {
            // setup
            MessageProducer producer;
            MessageConsumer consumer;
            try {
                if( config.getPassword() != null ) {
                    connection = factory.createConnection(config.getUserName(), config.getPassword());
                } else {
                    connection = factory.createConnection();
                }
                session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
                producer = session.createProducer(sendQueue);
                consumer = session.createConsumer(responseQueue, selector);

                connection.start();
            } catch( JMSException jmse ) {
                throw new KieServicesException("Unable to setup a JMS connection.", jmse);
            }

            // Create msg
            TextMessage textMsg;
            Marshaller marshaller;
            try {

                // serialize request
                marshaller = MarshallerFactory.getMarshaller( config.getMarshallingFormat(), CommandScript.class.getClassLoader() );
                String xmlStr = marshaller.marshall( command );
                textMsg = session.createTextMessage(xmlStr);

                // set properties
                // 1. corr id
                textMsg.setJMSCorrelationID(corrId);
                // 2. serialization info
                textMsg.setIntProperty( JMSConstants.SERIALIZATION_FORMAT_PROPERTY_NAME, config.getMarshallingFormat().getId() );
                // send
                producer.send(textMsg);
            } catch( JMSException jmse ) {
                throw new KieServicesException("Unable to send a JMS message.", jmse);
            }

            // receive
            Message response;
            try {
                response = consumer.receive( config.getTimeout() );
            } catch( JMSException jmse ) {
                jmse.printStackTrace();
                throw new KieServicesException("Unable to receive or retrieve the JMS response.", jmse);
            }

            if( response == null ) {
                logger.warn("Response is empty");
                return null;
            }
            // extract response
            assert response != null: "Response is empty.";
            try {
                String responseStr = ((TextMessage) response).getText();
                cmdResponse = marshaller.unmarshall(responseStr, ServiceResponsesList.class);
                return cmdResponse;
            } catch( JMSException jmse ) {
                throw new KieServicesException("Unable to extract " + ServiceResponsesList.class.getSimpleName()
                                                       + " instance from JMS response.", jmse);
            }
        } finally {
            if( connection != null ) {
                try {
                    connection.close();
                    if( session != null ) {
                        session.close();
                    }
                } catch( JMSException jmse ) {
                    logger.warn("Unable to close connection or session!", jmse);
                }
            }
        }
    }


    private String getMediaType( MarshallingFormat format ) {
        switch ( format ) {
            case JAXB: return MediaType.APPLICATION_XML;
            case JSON: return MediaType.APPLICATION_JSON;
            default: return MediaType.APPLICATION_XML;
        }
    }

    private String serialize(Object object) {
        try {
            return marshaller.marshall( object );
        } catch ( MarshallingException e ) {
            throw new KieServicesException( "Error while serializing request data!", e );
        }
    }

    private <T> T deserialize(String content, Class<T> type) {
        try {
            return marshaller.unmarshall( content, type );
        } catch ( MarshallingException e ) {
            throw new KieServicesException( "Error while deserializing data received from server!", e );
        }
    }

    /**
     * Checks whether the specified {@code ServiceResponse} contains the expected result type. In case the type is different,
     * {@code KieServicesClientException} is thrown. This catches the errors early, before returning the result from the client.
     * Without this check users could experience {@code ClassCastException} when retrieving the result that does not have
     * the expected type.
     */
    private void checkResultType(ServiceResponse<?> serviceResponse, Class<?> expectedResultType) {
        Object actualResult = serviceResponse.getResult();
        if ( actualResult != null && !expectedResultType.isInstance( actualResult ) ) {
            throw new KieServicesException(
                    "Error while creating service response! The actual result type " +
                    serviceResponse.getResult().getClass() + " does not match the expected type " + expectedResultType + "!" );
        }
    }

    private RuntimeException createExceptionForUnexpectedResponseCode(
            KieRemoteHttpRequest request,
            KieRemoteHttpResponse response) {
        String summaryMessage = "Unexpected HTTP response code when requesting URI '" + request.getUri() + "'! Error code: " +
                                response.code() + ", message: " + response.message();
        logger.debug( summaryMessage + ", response body: " + response.body() );
        return new KieServicesException( summaryMessage );
    }

}
