/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.server.client.impl;

import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.kie.server.common.rest.KieServerHttpRequest;
import org.kie.server.common.rest.KieServerHttpRequestException;
import org.kie.server.common.rest.KieServerHttpResponse;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.commands.CommandScript;
import org.kie.server.api.jms.JMSConstants;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.marshalling.MarshallingException;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.ServiceResponsesList;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesException;
import org.kie.server.client.balancer.LoadBalancer;
import org.kie.server.client.jms.ResponseHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.server.api.rest.RestURI.*;

public abstract class AbstractKieServicesClientImpl {

    private static Logger logger = LoggerFactory.getLogger(AbstractKieServicesClientImpl.class);

    protected static final Boolean BYPASS_AUTH_USER = Boolean.parseBoolean(System.getProperty(KieServerConstants.CFG_BYPASS_AUTH_USER, "false"));

    protected LoadBalancer loadBalancer;
    protected final KieServicesConfiguration config;
    protected final Marshaller marshaller;
    protected ClassLoader classLoader;

    protected KieServicesClientImpl owner;

    // used by JMS to handle response via different interaction patterns
    private ResponseHandler responseHandler;

    public AbstractKieServicesClientImpl(KieServicesConfiguration config) {
        this.config = config.clone();
        this.loadBalancer = config.getLoadBalancer() == null ? LoadBalancer.getDefault(config.getServerUrl()) : config.getLoadBalancer();
        this.classLoader = Thread.currentThread().getContextClassLoader() != null ? Thread.currentThread().getContextClassLoader() : CommandScript.class.getClassLoader();
        this.marshaller = MarshallerFactory.getMarshaller(config.getExtraClasses(), config.getMarshallingFormat(), classLoader);
        this.responseHandler = config.getResponseHandler();
    }

    public AbstractKieServicesClientImpl(KieServicesConfiguration config, ClassLoader classLoader) {
        this.config = config.clone();
        this.loadBalancer = config.getLoadBalancer() == null ? LoadBalancer.getDefault(config.getServerUrl()) : config.getLoadBalancer();
        this.classLoader = classLoader;
        this.marshaller = MarshallerFactory.getMarshaller( config.getExtraClasses(), config.getMarshallingFormat(), classLoader );
        this.responseHandler = config.getResponseHandler();
    }

    /**
     * Initializes the URL that will be used for web service access
     *
     * @param url URL of the server instance
     * @return An URL that can be used for the web services
     */
    protected String initializeURI(URL url, String servicePrefix) {
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

    public void setOwner(KieServicesClientImpl owner) {
        this.owner = owner;
    }

    public LoadBalancer getLoadBalancer() {
        return loadBalancer;
    }

    public ResponseHandler getResponseHandler() {
        return responseHandler;
    }

    public void setResponseHandler(ResponseHandler responseHandler) {
        if (config.getTransport() == KieServicesConfiguration.Transport.REST) {
            throw new UnsupportedOperationException("Response handlers can only be configured for JMS client");
        }

        this.responseHandler = responseHandler;
    }

    protected void throwExceptionOnFailure(ServiceResponse<?> serviceResponse) {
        if (serviceResponse != null && ServiceResponse.ResponseType.FAILURE.equals(serviceResponse.getType())){
            throw new KieServicesException(serviceResponse.getMsg());
        }
    }

    protected boolean shouldReturnWithNullResponse(ServiceResponse<?> serviceResponse) {
        if (serviceResponse != null && ServiceResponse.ResponseType.NO_RESPONSE.equals(serviceResponse.getType())){
            logger.debug("Returning null as the response type is NO_RESPONSE");
            return true;
        }

        return false;
    }

    protected void sendTaskOperation(String containerId, Long taskId, String operation, String queryString) {
        Map<String, Object> valuesMap = new HashMap<String, Object>();
        valuesMap.put(CONTAINER_ID, containerId);
        valuesMap.put(TASK_INSTANCE_ID, taskId);

        makeHttpPutRequestAndCreateCustomResponse(
                build(loadBalancer.getUrl(), operation, valuesMap) + queryString, null, String.class, getHeaders(null));
    }

    @SuppressWarnings("unchecked")
    protected <T> ServiceResponse<T> makeHttpGetRequestAndCreateServiceResponse(String uri, Class<T> resultType) {

        logger.debug("About to send GET request to '{}'", uri);
        KieServerHttpRequest request = invoke(uri, new RemoteHttpOperation(){
            @Override
            public KieServerHttpRequest doOperation(String url) {
                return newRequest(url).get();
            }
        });
        KieServerHttpResponse response = request.response();

        owner.setConversationId(response.header(KieServerConstants.KIE_CONVERSATION_ID_TYPE_HEADER));
        if ( response.code() == Response.Status.OK.getStatusCode() ) {
            ServiceResponse serviceResponse = deserialize( response.body(), ServiceResponse.class );
            checkResultType( serviceResponse, resultType );
            return serviceResponse;
        } else {
            throw createExceptionForUnexpectedResponseCode( request, response );
        }
    }

    protected <T> T makeHttpGetRequestAndCreateCustomResponse(String uri, Class<T> resultType) {
        logger.debug("About to send GET request to '{}'", uri);
        KieServerHttpRequest request = invoke(uri, new RemoteHttpOperation() {
            @Override
            public KieServerHttpRequest doOperation(String url) {
                return newRequest(url).get();
            }
        });
        KieServerHttpResponse response = request.response();

        owner.setConversationId(response.header(KieServerConstants.KIE_CONVERSATION_ID_TYPE_HEADER));
        if ( response.code() == Response.Status.OK.getStatusCode() ) {

            return deserialize(response.body(), resultType);

        } else {
            throw createExceptionForUnexpectedResponseCode( request, response );
        }
    }

    protected String makeHttpGetRequestAndCreateRawResponse(String uri) {
        logger.debug("About to send GET request to '{}'", uri);
        KieServerHttpRequest request = invoke(uri, new RemoteHttpOperation() {
            @Override
            public KieServerHttpRequest doOperation(String url) {
                return newRequest(url).get();
            }
        });
        KieServerHttpResponse response = request.response();

        owner.setConversationId(response.header(KieServerConstants.KIE_CONVERSATION_ID_TYPE_HEADER));

        if ( response.code() == Response.Status.OK.getStatusCode() ) {

            return response.body();

        } else {
            throw createExceptionForUnexpectedResponseCode( request, response );
        }
    }

    protected String makeHttpGetRequestAndCreateRawResponse(String uri, Map<String, String> headers) {
        logger.debug("About to send GET request to '{}'", uri);
        KieServerHttpRequest request = invoke(uri, new RemoteHttpOperation(){
            @Override
            public KieServerHttpRequest doOperation(String url) {
                return newRequest( uri ).headers(headers).get();
            }
        });
        KieServerHttpResponse response = request.response();

        owner.setConversationId(response.header(KieServerConstants.KIE_CONVERSATION_ID_TYPE_HEADER));

        if ( response.code() == Response.Status.OK.getStatusCode() ) {

            return response.body();

        } else {
            throw createExceptionForUnexpectedResponseCode( request, response );
        }
    }

    protected <T> ServiceResponse<T> makeHttpPostRequestAndCreateServiceResponse(
            String uri, Object bodyObject,
            Class<T> resultType) {
        return makeHttpPostRequestAndCreateServiceResponse( uri, serialize( bodyObject ), resultType );
    }

    protected <T> ServiceResponse<T> makeHttpPostRequestAndCreateServiceResponse(
            String uri, Object bodyObject,
            Class<T> resultType, Map<String, String> headers) {
        return makeHttpPostRequestAndCreateServiceResponse( uri, serialize( bodyObject ), resultType, headers );
    }

    protected <T> ServiceResponse<T> makeHttpPostRequestAndCreateServiceResponse(String uri, String body, Class<T> resultType) {
        return  makeHttpPostRequestAndCreateServiceResponse( uri, body, resultType, new HashMap<String, String>() );
    }

    @SuppressWarnings("unchecked")
    protected <T> ServiceResponse<T> makeHttpPostRequestAndCreateServiceResponse(String uri, String body, Class<T> resultType, Map<String, String> headers) {
        logger.debug("About to send POST request to '{}' with payload '{}'", uri, body);
        KieServerHttpRequest request = invoke(uri, new RemoteHttpOperation(){
            @Override
            public KieServerHttpRequest doOperation(String url) {
                return newRequest( uri ).headers(headers).body(body).post();
            }
        });

        KieServerHttpResponse response = request.response();

        owner.setConversationId(response.header(KieServerConstants.KIE_CONVERSATION_ID_TYPE_HEADER));

        if ( response.code() == Response.Status.OK.getStatusCode() ) {
            ServiceResponse serviceResponse = deserialize( response.body(), ServiceResponse.class );
            checkResultType( serviceResponse, resultType );
            return serviceResponse;
        } else {
            throw createExceptionForUnexpectedResponseCode( request, response );
        }
    }


    protected <T> T makeHttpPostRequestAndCreateCustomResponse(String uri, Object bodyObject, Class<T> resultType, Map<String, String> headers) {
        return makeHttpPostRequestAndCreateCustomResponse(uri, serialize(bodyObject), resultType, headers);
    }

    protected <T> T makeHttpPostRequestAndCreateCustomResponse(String uri, Object bodyObject, Class<T> resultType) {
        return makeHttpPostRequestAndCreateCustomResponse(uri, serialize(bodyObject), resultType, new HashMap<String, String>() );
    }

    protected <T> T makeHttpPostRequestAndCreateCustomResponse(String uri, String body, Class<T> resultType, Map<String, String> headers) {
        logger.debug("About to send POST request to '{}' with payload '{}'", uri, body);
        KieServerHttpRequest request = invoke(uri, new RemoteHttpOperation(){
            @Override
            public KieServerHttpRequest doOperation(String url) {
                return newRequest(uri ).headers(headers).body(body).post();
            }
        });

        KieServerHttpResponse response = request.response();

        owner.setConversationId(response.header(KieServerConstants.KIE_CONVERSATION_ID_TYPE_HEADER));

        if ( response.code() == Response.Status.OK.getStatusCode()
                || response.code() == Response.Status.CREATED.getStatusCode()) {
            return deserialize( response.body(), resultType );
        } else {
            throw createExceptionForUnexpectedResponseCode( request, response );
        }
    }

    protected <T> ServiceResponse<T> makeHttpPutRequestAndCreateServiceResponse(
            String uri, Object bodyObject,
            Class<T> resultType) {
        return makeHttpPutRequestAndCreateServiceResponse( uri, serialize( bodyObject ), resultType );
    }

    @SuppressWarnings("unchecked")
    protected <T> ServiceResponse<T> makeHttpPutRequestAndCreateServiceResponse(String uri, String body, Class<T> resultType) {
        logger.debug("About to send PUT request to '{}' with payload '{}'", uri, body);
        KieServerHttpRequest request = invoke(uri, new RemoteHttpOperation(){
            @Override
            public KieServerHttpRequest doOperation(String url) {
                return newRequest(uri).body(body).put();
            }
        });

        KieServerHttpResponse response = request.response();

        owner.setConversationId(response.header(KieServerConstants.KIE_CONVERSATION_ID_TYPE_HEADER));

        if ( response.code() == Response.Status.CREATED.getStatusCode() ||
                response.code() == Response.Status.BAD_REQUEST.getStatusCode() ) {
            ServiceResponse serviceResponse = deserialize( response.body(), ServiceResponse.class );
            checkResultType( serviceResponse, resultType );
            return serviceResponse;
        } else {
            throw createExceptionForUnexpectedResponseCode( request, response );
        }
    }

    protected <T> T makeHttpPutRequestAndCreateCustomResponse(
            String uri, Object bodyObject,
            Class<T> resultType, Map<String, String> headers) {
        return makeHttpPutRequestAndCreateCustomResponse(uri, serialize(bodyObject), resultType, headers);
    }

    @SuppressWarnings("unchecked")
    protected <T> T makeHttpPutRequestAndCreateCustomResponse(String uri, String body, Class<T> resultType, Map<String, String> headers) {
        logger.debug("About to send PUT request to '{}' with payload '{}'", uri, body);
        KieServerHttpRequest request = invoke(uri, new RemoteHttpOperation(){
            @Override
            public KieServerHttpRequest doOperation(String url) {
                return newRequest( uri ).headers(headers).body(body).put();
            }
        });

        KieServerHttpResponse response = request.response();

        owner.setConversationId(response.header(KieServerConstants.KIE_CONVERSATION_ID_TYPE_HEADER));

        if ( response.code() == Response.Status.CREATED.getStatusCode() ||
                response.code() == Response.Status.BAD_REQUEST.getStatusCode() ) {
            T serviceResponse = deserialize( response.body(), resultType );

            return serviceResponse;
        } else {
            throw createExceptionForUnexpectedResponseCode( request, response );
        }
    }

    @SuppressWarnings("unchecked")
    protected <T> ServiceResponse<T> makeHttpDeleteRequestAndCreateServiceResponse(String uri, Class<T> resultType) {
        logger.debug("About to send DELETE request to '{}' ", uri);
        KieServerHttpRequest request = invoke(uri, new RemoteHttpOperation(){
            @Override
            public KieServerHttpRequest doOperation(String url) {
                return newRequest( uri ).delete();
            }
        });

        KieServerHttpResponse response = request.response();

        owner.setConversationId(response.header(KieServerConstants.KIE_CONVERSATION_ID_TYPE_HEADER));

        if ( response.code() == Response.Status.OK.getStatusCode() ) {
            ServiceResponse serviceResponse = deserialize( response.body(), ServiceResponse.class );
            checkResultType( serviceResponse, resultType );
            return serviceResponse;
        } else {
            throw createExceptionForUnexpectedResponseCode( request, response );
        }
    }

    @SuppressWarnings("unchecked")
    protected <T> T makeHttpDeleteRequestAndCreateCustomResponse(String uri, Class<T> resultType) {
        logger.debug("About to send DELETE request to '{}' ", uri);
        KieServerHttpRequest request = invoke(uri, new RemoteHttpOperation(){
            @Override
            public KieServerHttpRequest doOperation(String url) {
                return newRequest( uri ).delete();
            }
        });

        KieServerHttpResponse response = request.response();

        owner.setConversationId(response.header(KieServerConstants.KIE_CONVERSATION_ID_TYPE_HEADER));

        if ( response.code() == Response.Status.OK.getStatusCode() ||
                response.code() == Response.Status.NO_CONTENT.getStatusCode() ) {
            if (resultType == null) {
                return null;
            }

            return deserialize( response.body(), resultType );
        } else {
            throw createExceptionForUnexpectedResponseCode( request, response );
        }
    }

    protected KieServerHttpRequest newRequest(String uri) {
        KieServerHttpRequest httpRequest =
                KieServerHttpRequest.newRequest( uri ).followRedirects( true ).timeout( config.getTimeout() );
        httpRequest.accept( getMediaType( config.getMarshallingFormat() ) );
        httpRequest.header(KieServerConstants.KIE_CONTENT_TYPE_HEADER, config.getMarshallingFormat().toString());

        if (config.getHeaders() != null) {
            for (Map.Entry<String, String> header : config.getHeaders().entrySet()) {
                httpRequest.header(header.getKey(), header.getValue());
                logger.debug("Adding additional header {} value {}", header.getKey(), header.getValue());
            }
        }
        // apply authorization
        if (config.getCredentialsProvider() != null) {
            String authorization = config.getCredentialsProvider().getAuthorization();
            // add authorization only when it's not empty to allow anonymous requests
            if (authorization != null && !authorization.isEmpty()) {
                httpRequest.header(config.getCredentialsProvider().getHeaderName(), authorization);
            }
        }
        // apply conversationId
        if (owner.getConversationId() != null) {
            httpRequest.header(KieServerConstants.KIE_CONVERSATION_ID_TYPE_HEADER, owner.getConversationId());
        }

        return httpRequest;
    }

    /**
     * Method to communicate with the backend via JMS.
     *
     * @param command The {@link org.kie.api.command.Command} object to be executed.
     * @return The result of the {@link org.kie.api.command.Command} object execution.
     */
    protected ServiceResponsesList executeJmsCommand( CommandScript command ) {
        return executeJmsCommand(command, null);
    }

    protected ServiceResponsesList executeJmsCommand( CommandScript command, String classType ) {
        return executeJmsCommand(command, classType, null, null);
    }

    protected ServiceResponsesList executeJmsCommand( CommandScript command, String classType, String targetCapability ) {
        return executeJmsCommand(command, classType, targetCapability, null);
    }

    protected ServiceResponsesList executeJmsCommand( CommandScript command, String classType, String targetCapability, String containerId ) {
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

            try {
                if( config.getPassword() != null ) {
                    connection = factory.createConnection(config.getUserName(), config.getPassword());
                } else {
                    connection = factory.createConnection();
                }
                session = connection.createSession(config.isJmsTransactional(), Session.AUTO_ACKNOWLEDGE);
                producer = session.createProducer(sendQueue);

                connection.start();
            } catch( JMSException jmse ) {
                throw new KieServicesException("Unable to setup a JMS connection.", jmse);
            }

            // Create msg
            TextMessage textMsg;
            Marshaller marshaller;
            try {

                // serialize request
                marshaller = MarshallerFactory.getMarshaller( config.getExtraClasses(), config.getMarshallingFormat(), classLoader );
                String xmlStr = marshaller.marshall( command );
                logger.debug("Message content to be sent '{}'", xmlStr);
                textMsg = session.createTextMessage(xmlStr);

                // set properties
                // 1. corr id
                textMsg.setJMSCorrelationID(corrId);
                // 2. serialization info
                textMsg.setIntProperty( JMSConstants.SERIALIZATION_FORMAT_PROPERTY_NAME, config.getMarshallingFormat().getId() );
                textMsg.setIntProperty( JMSConstants.INTERACTION_PATTERN_PROPERTY_NAME, responseHandler.getInteractionPattern() );
                if (classType != null) {
                    textMsg.setStringProperty(JMSConstants.CLASS_TYPE_PROPERTY_NAME, classType);
                }

                if (targetCapability != null) {
                    textMsg.setStringProperty(JMSConstants.TARGET_CAPABILITY_PROPERTY_NAME, targetCapability);
                }
                textMsg.setStringProperty(JMSConstants.USER_PROPERTY_NAME, config.getUserName());
                textMsg.setStringProperty(JMSConstants.PASSWRD_PROPERTY_NAME, config.getPassword());

                if (containerId != null) {
                    textMsg.setStringProperty(JMSConstants.CONTAINER_ID_PROPERTY_NAME, containerId);
                }

                if (owner.getConversationId() != null) {
                    textMsg.setStringProperty(JMSConstants.CONVERSATION_ID_PROPERTY_NAME, owner.getConversationId());
                }

                if (config.getHeaders() != null) {
                    for (Map.Entry<String, String> header : config.getHeaders().entrySet()) {
                        logger.debug("Adding additional property {} value {}", header.getKey(), header.getValue());
                        textMsg.setStringProperty(header.getKey(), header.getValue());
                    }
                }

                // send
                producer.send(textMsg);
            } catch( JMSException jmse ) {
                throw new KieServicesException("Unable to send a JMS message.", jmse);
            }

            // receive
            cmdResponse = responseHandler.handleResponse(selector, connection, session, responseQueue, config, marshaller, owner);

            return cmdResponse;
        } finally {
            responseHandler.dispose(connection, session);
        }
    }


    protected String getMediaType( MarshallingFormat format ) {
        switch ( format ) {
            case JAXB: return MediaType.APPLICATION_XML;
            case JSON: return MediaType.APPLICATION_JSON;
            default: return MediaType.APPLICATION_XML;
        }
    }

    protected String serialize(Object object) {
        if (object == null) {
            return "";
        }

        try {
            return marshaller.marshall( object );
        } catch ( MarshallingException e ) {
            throw new KieServicesException( "Error while serializing request data!", e );
        }
    }

    protected <T> T deserialize(String content, Class<T> type) {
        logger.debug("About to deserialize content: \n '{}' \n into type: '{}'", content, type);
        if (content == null || content.isEmpty()) {
            return null;
        }
        try {
            return marshaller.unmarshall(content, type);
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
    protected void checkResultType(ServiceResponse<?> serviceResponse, Class<?> expectedResultType) {
        Object actualResult = serviceResponse.getResult();
        if ( actualResult != null && !expectedResultType.isInstance( actualResult ) ) {
            throw new KieServicesException(
                    "Error while creating service response! The actual result type " +
                            serviceResponse.getResult().getClass() + " does not match the expected type " + expectedResultType + "!" );
        }
    }

    protected RuntimeException createExceptionForUnexpectedResponseCode(
            KieServerHttpRequest request,
            KieServerHttpResponse response) {
        String summaryMessage = "Unexpected HTTP response code when requesting URI '" + request.getUri() + "'! Error code: " +
                response.code() + ", message: " + response.body();
        logger.debug( summaryMessage + ", response body: " + getMessage(response) );
        return new KieServicesException( summaryMessage );
    }

    protected String getMessage(KieServerHttpResponse response) {
        try {
            String body = response.body();
            if (body != null && !body.isEmpty()) {
                return body;
            }
        } catch (Exception e) {
            logger.debug("Error when getting both of the response {}", e.getMessage());
        }
        return response.message();
    }

    protected String buildQueryString(String paramName, List<?> items) {
        StringBuilder builder = new StringBuilder("?");
        for (Object o : items) {
            builder.append(paramName).append("=").append(o).append("&");
        }
        builder.deleteCharAt(builder.length()-1);

        return builder.toString();
    }

    protected Map<String, String> getHeaders(Object object) {
        Map<String, String> headers = new HashMap<String, String>();
        if (object != null) {
            headers.put(KieServerConstants.CLASS_TYPE_HEADER, object.getClass().getName());
        }

        return headers;
    }

    protected String getUserQueryStr(String userId) {
        if (BYPASS_AUTH_USER) {
            return "?user=" + userId;
        }

        return "";
    }

    protected String getUserAndPagingQueryString(String userId, Integer page, Integer pageSize) {
        StringBuilder queryString = new StringBuilder(getUserQueryStr(userId));
        if (queryString.length() == 0) {
            queryString.append("?");
        } else {
            queryString.append("&");
        }
        queryString.append("page=" + page).append("&pageSize=" + pageSize);

        return queryString.toString();
    }

    protected String getUserAndAdditionalParam(String userId, String name, String value) {
        StringBuilder queryString = new StringBuilder(getUserQueryStr(userId));
        if (queryString.length() == 0) {
            queryString.append("?");
        } else {
            queryString.append("&");
        }
        queryString.append(name).append("=").append(value);

        return queryString.toString();
    }

    protected String getUserAndAdditionalParams(String userId, String name, List<?> values) {
        StringBuilder queryString = new StringBuilder(getUserQueryStr(userId));
        if (values != null) {
            if (queryString.length() == 0) {
                queryString.append("?");
            } else {
                queryString.append("&");
            }
            for (Object value : values) {
                queryString.append(name).append("=").append(value).append("&");
            }
            queryString.deleteCharAt(queryString.length() - 1);
        }
        return queryString.toString();
    }

    protected String getPagingQueryString(String inQueryString, Integer page, Integer pageSize) {
        StringBuilder queryString = new StringBuilder(inQueryString);
        if (queryString.length() == 0) {
            queryString.append("?");
        } else {
            queryString.append("&");
        }
        queryString.append("page=" + page).append("&pageSize=" + pageSize);

        return queryString.toString();
    }

    protected String getSortingQueryString(String inQueryString, String sort, boolean sortOrder) {
        StringBuilder queryString = new StringBuilder(inQueryString);
        if (queryString.length() == 0) {
            queryString.append("?");
        } else {
            queryString.append("&");
        }
        queryString.append("sort=" + sort).append("&sortOrder=" + sortOrder);

        return queryString.toString();
    }

    protected String getAdditionalParams(String inQueryString, String name, List<?> values) {
        StringBuilder queryString = new StringBuilder(inQueryString);

        if (values != null) {
            if (queryString.length() == 0) {
                queryString.append("?");
            } else {
                queryString.append("&");
            }
            for (Object value : values) {
                queryString.append(name).append("=").append(value).append("&");
            }
            queryString.deleteCharAt(queryString.length() - 1);
        }
        return queryString.toString();
    }


    protected Map<?, ?> safeMap(Map<?, ?> map) {
        if (map == null) {

            return new HashMap<Object, Object>();
        }
        return new HashMap<Object, Object>(map);
    }

    protected List<?> safeList(List<?> list) {
        if (list == null) {

            return new ArrayList<Object>();
        }
        return new ArrayList<Object>(list);
    }

    /*
 * override of the regular method to allow backward compatibility for string based result of ServiceResponse
 */
    protected <T> ServiceResponse<T> makeBackwardCompatibleHttpPostRequestAndCreateServiceResponse(String uri, Object body, Class<T> resultType, Map<String, String> headers) {
        logger.debug("About to send POST request to '{}' with payload '{}'", uri, body);
        KieServerHttpRequest request = newRequest( uri ).headers(headers).body(serialize( body )).post();
        KieServerHttpResponse response = request.response();

        owner.setConversationId(response.header(KieServerConstants.KIE_CONVERSATION_ID_TYPE_HEADER));

        if ( response.code() == Response.Status.OK.getStatusCode() ) {
            ServiceResponse serviceResponse = deserialize( response.body(), ServiceResponse.class );
            // serialize it back to string to make it backward compatible
            serviceResponse.setResult(serialize(serviceResponse.getResult()));
            checkResultType(serviceResponse, resultType);
            return serviceResponse;
        } else {
            throw createExceptionForUnexpectedResponseCode( request, response );
        }
    }

    protected <T> ServiceResponse<T> makeBackwardCompatibleHttpPostRequestAndCreateServiceResponse(String uri, String body, Class<T> resultType) {
        logger.debug("About to send POST request to '{}' with payload '{}'", uri, body);
        KieServerHttpRequest request = newRequest( uri ).body( body ).post();
        KieServerHttpResponse response = request.response();

        owner.setConversationId(response.header(KieServerConstants.KIE_CONVERSATION_ID_TYPE_HEADER));

        if ( response.code() == Response.Status.OK.getStatusCode() ) {
            ServiceResponse serviceResponse = deserialize( response.body(), ServiceResponse.class );
            // serialize it back to string to make it backward compatible
            serviceResponse.setResult(serialize(serviceResponse.getResult()));
            checkResultType(serviceResponse, resultType);
            return serviceResponse;
        } else {
            throw createExceptionForUnexpectedResponseCode( request, response );
        }
    }

    public String getConversationId() {
        return owner.getConversationId();
    }

    protected KieServerHttpRequest invoke(String url, RemoteHttpOperation operation) {
        String nextUrl = null;
        do {
            try {
                return operation.doOperation(url);
            } catch (KieServerHttpRequestException e) {
                if (e.getCause() instanceof ConnectException) {
                    logger.debug("Marking endpoint '{}' as failed due to {}", url, e.getCause().getMessage());
                    loadBalancer.markAsFailed(url);
                    nextUrl = loadBalancer.getUrl();
                    url = nextUrl;
                    logger.debug("Selecting next endpoint from load balancer - '{}'", url);
                } else {
                    throw e;
                }
            }
        } while (nextUrl != null);

        throw new KieServerHttpRequestException("Unable to invoke operation " + operation);
    }

    private abstract class RemoteHttpOperation {

        public abstract KieServerHttpRequest doOperation(String url);
    }
}
