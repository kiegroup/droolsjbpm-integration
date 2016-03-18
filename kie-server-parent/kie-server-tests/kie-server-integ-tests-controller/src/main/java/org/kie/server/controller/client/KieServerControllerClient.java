package org.kie.server.controller.client;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jboss.resteasy.client.ClientExecutor;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.core.executors.ApacheHttpClient4Executor;
import org.jboss.resteasy.spi.ReaderException;
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
import org.kie.server.controller.client.exception.UnexpectedResponseCodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KieServerControllerClient {

    private static Logger logger = LoggerFactory.getLogger(KieServerControllerClient.class);

    private ClientExecutor executor;
    private String controllerBaseUrl;
    private MarshallingFormat format = MarshallingFormat.JAXB;
    private CloseableHttpClient httpClient;
    protected Marshaller marshaller;

    public KieServerControllerClient( String controllerBaseUrl, String login, String password) {
        URL url;
        try {
            url = new URL(controllerBaseUrl);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Malformed controller URL was specified: '" + controllerBaseUrl + "'!", e);
        }

        this.controllerBaseUrl = controllerBaseUrl;
        HttpClientBuilder httpClientBuilder = HttpClientBuilder.create();
        if (login != null) {
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(
                    new AuthScope(url.getHost(), url.getPort()),
                    new UsernamePasswordCredentials(login, password));
            httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
        }
        this.httpClient = httpClientBuilder.build();
        this.executor = new ApacheHttpClient4Executor(httpClient);


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
        ClientRequest clientRequest = new ClientRequest(uri, executor);
        ClientResponse<T> response;

        try {
            response = clientRequest.accept(getMediaType(format)).get(resultType);
        } catch (Exception e) {
            throw createExceptionForUnexpectedFailure(clientRequest, e);
        }

        if ( response.getStatus() == Response.Status.OK.getStatusCode() ) {
            return deserialize(response, resultType);
        } else {
            throw createExceptionForUnexpectedResponseCode( clientRequest, response );
        }
    }

    private void makeDeleteRequest(String uri) {
        ClientRequest clientRequest = new ClientRequest(uri, executor);
        ClientResponse<?> response;

        try {
            response = clientRequest.accept(getMediaType(format)).delete();
            response.releaseConnection();
        } catch (Exception e) {
            throw createExceptionForUnexpectedFailure(clientRequest, e);
        }

        if ( response.getStatus() != Response.Status.NO_CONTENT.getStatusCode() ) {
            throw createExceptionForUnexpectedResponseCode( clientRequest, response );
        }
    }

    private <T> T makePutRequestAndCreateCustomResponse(String uri, Object bodyObject, Class<T> resultType) {
        ClientRequest clientRequest = new ClientRequest(uri, executor);
        ClientResponse<T> response;

        try {
            response = clientRequest.accept(getMediaType(format))
                    .body(getMediaType(format), serialize(bodyObject)).put(resultType);
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
        ClientRequest clientRequest = new ClientRequest(uri, executor);
        ClientResponse<T> response;

        try {
            response = clientRequest.accept(getMediaType(format))
                    .body(getMediaType(format), bodyObject).post(resultType);

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
            ClientRequest request,
            ClientResponse<?> response) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("Unexpected HTTP response code when requesting URI '");
        stringBuffer.append(getClientRequestUri(request));
        stringBuffer.append("'! Response code: ");
        stringBuffer.append(response.getStatus());
        try {
            String responseEntity = response.getEntity(String.class);
            stringBuffer.append(" Response message: ");
            stringBuffer.append(responseEntity);
        } catch (ReaderException e) {
            response.releaseConnection();
            // Exception while reading response - most probably empty response and closed input stream
        }

        logger.debug( stringBuffer.toString());
        return new UnexpectedResponseCodeException(response.getStatus(), stringBuffer.toString());
    }

    private RuntimeException createExceptionForUnexpectedFailure(
            ClientRequest request, Exception e) {
        String summaryMessage = "Unexpected exception when requesting URI '" + getClientRequestUri(request) + "'!";
        logger.debug( summaryMessage);
        return new RuntimeException(summaryMessage, e);
    }

    private String getClientRequestUri(ClientRequest clientRequest) {
        String uri;
        try {
            uri = clientRequest.getUri();
        } catch (Exception e) {
            throw new RuntimeException("Malformed client URL was specified!", e);
        }
        return uri;
    }

    public void close() {
        try {
            executor.close();
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

    protected <T> T deserialize(ClientResponse<T> response, Class<T> type) {
        try {
            if(type == null) {
                return null;
            }
            String content = response.getEntity(String.class);
            logger.debug("About to deserialize content: \n '{}' \n into type: '{}'", content, type);
            if (content == null || content.isEmpty()) {
                return null;
            }

            return marshaller.unmarshall( content, type );
        } catch ( MarshallingException e ) {
            throw new RuntimeException( "Error while deserializing data received from server!", e );
        } finally {
            response.releaseConnection();
        }
    }
}
