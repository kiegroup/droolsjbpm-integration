package org.kie.server.integrationtests.controller.client;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.marshalling.MarshallingException;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.controller.api.model.KieServerInstance;
import org.kie.server.controller.api.model.KieServerInstanceInfo;
import org.kie.server.controller.api.model.KieServerInstanceList;
import org.kie.server.controller.api.model.KieServerSetup;
import org.kie.server.controller.api.model.KieServerStatus;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.integrationtests.controller.client.exception.UnexpectedResponseCodeException;
import org.kie.server.integrationtests.shared.filter.Authenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KieServerControllerClient {

    private static Logger logger = LoggerFactory.getLogger(KieServerControllerClient.class);

    private String controllerBaseUrl;
    private MarshallingFormat format = MarshallingFormat.JAXB;
    private Client httpClient;
    protected Marshaller marshaller;

    public KieServerControllerClient( String controllerBaseUrl, String login, String password) {

        this.controllerBaseUrl = controllerBaseUrl;
        httpClient = new ResteasyClientBuilder()
                .establishConnectionTimeout(10, TimeUnit.SECONDS)
                .socketTimeout(10, TimeUnit.SECONDS)
                .build();
        if (login != null) {
            httpClient.register(new Authenticator(TestConfig.getUsername(), TestConfig.getPassword()));
        }
    }

    public KieServerInstance getKieServerInstance(String kieServerInstanceId) {
        return makeGetRequestAndCreateCustomResponse(controllerBaseUrl + "/admin/server/" + kieServerInstanceId, KieServerInstance.class);
    }

    public KieServerInstance createKieServerInstance(KieServerInfo kieServerInfo) {
        return makePutRequestAndCreateCustomResponse(controllerBaseUrl + "/admin/server/" + kieServerInfo.getServerId(), kieServerInfo, KieServerInstance.class);
    }

    public void deleteKieServerInstance(String kieServerInstanceId) {
        makeDeleteRequest(controllerBaseUrl + "/admin/server/" + kieServerInstanceId);
    }

    public KieServerInstanceList listKieServerInstances() {
        return makeGetRequestAndCreateCustomResponse(controllerBaseUrl + "/admin/servers", KieServerInstanceList.class);
    }

    public KieContainerResource getContainerInfo(String kieServerInstanceId, String containerId) {
        return makeGetRequestAndCreateCustomResponse(controllerBaseUrl + "/admin/server/" + kieServerInstanceId + "/containers/" + containerId, KieContainerResource.class);
    }

    public KieContainerResource createContainer(String kieServerInstanceId, String containerId, KieContainerResource container) {
        return makePutRequestAndCreateCustomResponse(controllerBaseUrl + "/admin/server/" + kieServerInstanceId + "/containers/" + containerId, container, KieContainerResource.class);
    }

    public void disposeContainer(String kieServerInstanceId, String containerId) {
        makeDeleteRequest(controllerBaseUrl + "/admin/server/" + kieServerInstanceId + "/containers/" + containerId);
    }

    public void startContainer(String kieServerInstanceId, String containerId) {
        makePostRequestAndCreateCustomResponse(controllerBaseUrl + "/admin/server/" + kieServerInstanceId + "/containers/" + containerId + "/status/started", "", null);
    }

    public void stopContainer(String kieServerInstanceId, String containerId) {
        makePostRequestAndCreateCustomResponse(controllerBaseUrl + "/admin/server/" + kieServerInstanceId + "/containers/" + containerId + "/status/stopped", "", null);
    }

    private <T> T makeGetRequestAndCreateCustomResponse(String uri, Class<T> resultType) {
        WebTarget clientRequest = httpClient.target(uri);
        Response response;

        response = clientRequest.request(getMediaType(format)).get();

        if ( response.getStatus() == Response.Status.OK.getStatusCode() ) {
            return deserialize(response, resultType);
        } else {
            throw createExceptionForUnexpectedResponseCode( clientRequest, response );
        }
    }

    private void makeDeleteRequest(String uri) {
        WebTarget clientRequest = httpClient.target(uri);
        Response response;

        try {
            response = clientRequest.request(getMediaType(format)).delete();
            response.close();
        } catch (Exception e) {
            throw createExceptionForUnexpectedFailure(clientRequest, e);
        }

        if ( response.getStatus() != Response.Status.NO_CONTENT.getStatusCode() ) {
            throw createExceptionForUnexpectedResponseCode( clientRequest, response );
        }
    }

    private <T> T makePutRequestAndCreateCustomResponse(String uri, Object bodyObject, Class<T> resultType) {
        WebTarget clientRequest = httpClient.target(uri);
        Response response;

        try {
            Entity<String> requestEntity = Entity.entity(serialize(bodyObject), getMediaType(format));
            response = clientRequest.request(getMediaType(format)).put(requestEntity);
        } catch (Exception e) {
            throw createExceptionForUnexpectedFailure(clientRequest, e);
        }

        if ( response.getStatus() == Response.Status.CREATED.getStatusCode() ) {
            return deserialize(response, resultType);
        } else {
            throw createExceptionForUnexpectedResponseCode( clientRequest, response );
        }
    }

    private <T> T makePostRequestAndCreateCustomResponse(String uri, Object bodyObject, Class<T> resultType) {
        WebTarget clientRequest = httpClient.target(uri);
        Response response;

        try {
            Entity<String> requestEntity = Entity.entity(serialize(bodyObject), getMediaType(format));
            response = clientRequest.request(getMediaType(format)).post(requestEntity);

        } catch (Exception e) {
            throw createExceptionForUnexpectedFailure(clientRequest, e);
        }

        if ( response.getStatus() == Response.Status.CREATED.getStatusCode() ||
              response.getStatus() == Response.Status.OK.getStatusCode() ) {
            return deserialize(response, resultType);
        } else {
            throw createExceptionForUnexpectedResponseCode( clientRequest, response );
        }
    }

    private RuntimeException createExceptionForUnexpectedResponseCode(
            WebTarget request,
            Response response) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("Unexpected HTTP response code when requesting URI '");
        stringBuffer.append(getClientRequestUri(request));
        stringBuffer.append("'! Response code: ");
        stringBuffer.append(response.getStatus());
        try {
            String responseEntity = response.readEntity(String.class);
            stringBuffer.append(" Response message: ");
            stringBuffer.append(responseEntity);
        } catch (IllegalStateException e) {
            response.close();
            // Exception while reading response - most probably empty response and closed input stream
        }

        logger.debug( stringBuffer.toString());
        return new UnexpectedResponseCodeException(response.getStatus(), stringBuffer.toString());
    }

    private RuntimeException createExceptionForUnexpectedFailure(
            WebTarget request, Exception e) {
        String summaryMessage = "Unexpected exception when requesting URI '" + getClientRequestUri(request) + "'!";
        logger.debug( summaryMessage);
        return new RuntimeException(summaryMessage, e);
    }

    private String getClientRequestUri(WebTarget clientRequest) {
        String uri;
        try {
            uri = clientRequest.getUri().toString();
        } catch (Exception e) {
            throw new RuntimeException("Malformed client URL was specified!", e);
        }
        return uri;
    }

    public void close() {
        try {
            httpClient.close();
        } catch (Exception e) {
            logger.error("Exception thrown while closing resources!", e);
        }
    }

    public MarshallingFormat getMarshallingFormat() {
        return format;
    }

    public void setMarshallingFormat(MarshallingFormat format) {
        this.format = format;
        Set<Class<?>> controllerClasses = new HashSet<Class<?>>();
        controllerClasses.add(KieServerInstance.class);
        controllerClasses.add(KieServerInstanceList.class);
        controllerClasses.add(KieServerInstanceInfo.class);
        controllerClasses.add(KieServerSetup.class);
        controllerClasses.add(KieServerStatus.class);

        switch (format) {
            case JAXB:
                this.marshaller = MarshallerFactory.getMarshaller(controllerClasses, format, KieServerControllerClient.class.getClassLoader());
                break;
            case JSON:
                this.marshaller = MarshallerFactory.getMarshaller(format, KieServerControllerClient.class.getClassLoader());
                break;

            default:
                this.marshaller = MarshallerFactory.getMarshaller(controllerClasses, format, KieServerControllerClient.class.getClassLoader());
                break;
        }

    }

    private String getMediaType( MarshallingFormat format ) {
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
            throw new RuntimeException( "Error while serializing request data!", e );
        }
    }

    protected <T> T deserialize(Response response, Class<T> type) {
        try {
            if(type == null) {
                return null;
            }
            String content = response.readEntity(String.class);
            logger.debug("About to deserialize content: \n '{}' \n into type: '{}'", content, type);
            if (content == null || content.isEmpty()) {
                return null;
            }

            return marshaller.unmarshall( content, type );
        } catch ( MarshallingException e ) {
            throw new RuntimeException( "Error while deserializing data received from server!", e );
        } finally {
            response.close();
        }
    }
}
