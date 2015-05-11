package org.kie.server.client.impl;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.kie.remote.common.rest.KieRemoteHttpRequest;
import org.kie.remote.common.rest.KieRemoteHttpResponse;
import org.kie.server.api.commands.CallContainerCommand;
import org.kie.server.api.commands.CommandScript;
import org.kie.server.api.commands.CreateContainerCommand;
import org.kie.server.api.commands.DisposeContainerCommand;
import org.kie.server.api.commands.GetContainerInfoCommand;
import org.kie.server.api.commands.GetScannerInfoCommand;
import org.kie.server.api.commands.GetServerInfoCommand;
import org.kie.server.api.commands.ListContainersCommand;
import org.kie.server.api.commands.RegisterServerControllerCommand;
import org.kie.server.api.commands.UpdateReleaseIdCommand;
import org.kie.server.api.commands.UpdateScannerCommand;
import org.kie.server.api.jms.JMSConstants;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.marshalling.MarshallingException;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerResourceList;
import org.kie.server.api.model.KieScannerResource;
import org.kie.server.api.model.KieServerCommand;
import org.kie.server.api.model.KieServerConfig;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.ServiceResponsesList;
import org.kie.server.api.model.definition.AssociatedEntitiesDefinition;
import org.kie.server.api.model.definition.ProcessDefinition;
import org.kie.server.api.model.definition.ServiceTasksDefinition;
import org.kie.server.api.model.definition.SubProcessesDefinition;
import org.kie.server.api.model.definition.TaskInputsDefinition;
import org.kie.server.api.model.definition.TaskOutputsDefinition;
import org.kie.server.api.model.definition.UserTaskDefinitionList;
import org.kie.server.api.model.definition.VariablesDefinition;
import org.kie.server.api.model.type.JaxbLong;
import org.kie.server.api.model.type.JaxbMap;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        this.marshaller = MarshallerFactory.getMarshaller( config.getExtraJaxbClasses(), config.getMarshallingFormat(), cl );
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

    /*
     * jBPM part START
     * Process definition related operations
     */

    @Override
    public ProcessDefinition getProcessDefinition(String containerId, String processId) {
        if( config.isRest() ) {
            return makeHttpGetRequestAndCreateCustomResponse(
                    baseURI + "/containers/" + containerId + "/process/definition/" + processId,
                    ProcessDefinition.class);
        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public SubProcessesDefinition getReusableSubProcessDefinitions(String containerId, String processId) {
        if( config.isRest() ) {
            return makeHttpGetRequestAndCreateCustomResponse(
                    baseURI + "/containers/" + containerId + "/process/definition/" + processId + "/subprocesses",
                    SubProcessesDefinition.class);

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public VariablesDefinition getProcessVariableDefinitions(String containerId, String processId) {
        if( config.isRest() ) {
            return makeHttpGetRequestAndCreateCustomResponse(
                    baseURI + "/containers/" + containerId + "/process/definition/" + processId + "/variables",
                    VariablesDefinition.class);

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public ServiceTasksDefinition getServiceTaskDefinitions(String containerId, String processId) {
        if( config.isRest() ) {
            return makeHttpGetRequestAndCreateCustomResponse(
                    baseURI + "/containers/" + containerId + "/process/definition/" + processId + "/tasks/service",
                    ServiceTasksDefinition.class);

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public AssociatedEntitiesDefinition getAssociatedEntityDefinitions(String containerId, String processId) {
        if( config.isRest() ) {
            return makeHttpGetRequestAndCreateCustomResponse(
                    baseURI + "/containers/" + containerId + "/process/definition/" + processId + "/entities",
                    AssociatedEntitiesDefinition.class);

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public UserTaskDefinitionList getUserTaskDefinitions(String containerId, String processId) {
        if( config.isRest() ) {
            return makeHttpGetRequestAndCreateCustomResponse(
                    baseURI + "/containers/" + containerId + "/process/definition/" + processId + "/tasks/user",
                    UserTaskDefinitionList.class);

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public TaskInputsDefinition getUserTaskInputDefinitions(String containerId, String processId, String taskName) {
        if( config.isRest() ) {
            return makeHttpGetRequestAndCreateCustomResponse(
                    baseURI + "/containers/" + containerId + "/process/definition/" + processId + "/tasks/user/" + taskName + "/inputs",
                    TaskInputsDefinition.class);

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public TaskOutputsDefinition getUserTaskOutputDefinitions(String containerId, String processId, String taskName) {
        if( config.isRest() ) {
            return makeHttpGetRequestAndCreateCustomResponse(
                    baseURI + "/containers/" + containerId + "/process/definition/" + processId + "/tasks/user/" + taskName + "/outputs",
                    TaskOutputsDefinition.class);

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public Long startProcess(String containerId, String processId, Map<String, Object> variables) {
        if( config.isRest() ) {
            JaxbLong result = makeHttpPutRequestAndCreateCustomResponse(
                    baseURI + "/containers/" + containerId + "/process/" + processId, new JaxbMap(variables),
                    JaxbLong.class);

            if (result == null) {
                return null;
            }

            return result.getValue();

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public void abortProcessInstance(String containerId, Long processInstanceId) {
        if( config.isRest() ) {
            makeHttpDeleteRequestAndCreateCustomResponse(
                    baseURI + "/containers/" + containerId + "/process/instance/" + processInstanceId,
                    null);

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public Object getProcessInstanceVariable(String containerId, Long processInstanceId, String variableName) {
        return getProcessInstanceVariable(containerId, processInstanceId, variableName, Object.class);
    }

    @Override
    public <T> T getProcessInstanceVariable(String containerId, Long processInstanceId, String variableName, Class<T> type) {
        if( config.isRest() ) {
            return makeHttpGetRequestAndCreateCustomResponse(
                    baseURI + "/containers/" + containerId + "/process/instance/" + processInstanceId + "/variable/" + variableName,
                    type);

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }

    @Override
    public Map<String, Object> getProcessInstanceVariables(String containerId, Long processInstanceId) {
        if( config.isRest() ) {
            JaxbMap variables = makeHttpGetRequestAndCreateCustomResponse(
                    baseURI + "/containers/" + containerId + "/process/instance/" + processInstanceId + "/variables",
                    JaxbMap.class);

            if (variables != null && variables.getEntries() != null) {
                return variables.unwrap();
            }

            return Collections.emptyMap();

        } else {
            throw new UnsupportedOperationException("Not yet supported");
        }
    }


    /*
     * jBPM part END
     *
     */

    @SuppressWarnings("unchecked")
    private <T> ServiceResponse<T> makeHttpGetRequestAndCreateServiceResponse(String uri, Class<T> resultType) {
        logger.debug("About to send GET request to '{}'", uri);
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

    private <T> T makeHttpGetRequestAndCreateCustomResponse(String uri, Class<T> resultType) {
        logger.debug("About to send GET request to '{}'", uri);
        KieRemoteHttpRequest request = newRequest( uri ).get();
        KieRemoteHttpResponse response = request.response();

        if ( response.code() == Response.Status.OK.getStatusCode() ) {
            return deserialize( response.body(), resultType );
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
        logger.debug("About to send POST request to '{}' with payload '{}'", uri, body);
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
        logger.debug("About to send POST request to '{}' with payload '{}'", uri, body);
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
        logger.debug("About to send PUT request to '{}' with payload '{}'", uri, body);
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

    private <T> T makeHttpPutRequestAndCreateCustomResponse(
            String uri, Object bodyObject,
            Class<T> resultType) {
        return makeHttpPutRequestAndCreateCustomResponse(uri, serialize(bodyObject), resultType);
    }

    @SuppressWarnings("unchecked")
    private <T> T makeHttpPutRequestAndCreateCustomResponse(String uri, String body, Class<T> resultType) {
        logger.debug("About to send PUT request to '{}' with payload '{}'", uri, body);
        KieRemoteHttpRequest request = newRequest( uri ).body( body ).put();
        KieRemoteHttpResponse response = request.response();

        if ( response.code() == Response.Status.CREATED.getStatusCode() ||
                response.code() == Response.Status.BAD_REQUEST.getStatusCode() ) {
            T serviceResponse = deserialize( response.body(), resultType );

            return serviceResponse;
        } else {
            throw createExceptionForUnexpectedResponseCode( request, response );
        }
    }

    @SuppressWarnings("unchecked")
    private <T> ServiceResponse<T> makeHttpDeleteRequestAndCreateServiceResponse(String uri, Class<T> resultType) {
        logger.debug("About to send DELETE request to '{}' ", uri);
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

    @SuppressWarnings("unchecked")
    private <T> T makeHttpDeleteRequestAndCreateCustomResponse(String uri, Class<T> resultType) {
        logger.debug("About to send DELETE request to '{}' ", uri);
        KieRemoteHttpRequest request = newRequest( uri ).delete();
        KieRemoteHttpResponse response = request.response();

        if ( response.code() == Response.Status.OK.getStatusCode() ||
                response.code() == Response.Status.NO_CONTENT.getStatusCode()) {
            if (resultType == null) {
                return null;
            }

            return deserialize( response.body(), resultType );
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
                marshaller = MarshallerFactory.getMarshaller( config.getExtraJaxbClasses(), config.getMarshallingFormat(), CommandScript.class.getClassLoader() );
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
        if (object == null) {
            return null;
        }

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
