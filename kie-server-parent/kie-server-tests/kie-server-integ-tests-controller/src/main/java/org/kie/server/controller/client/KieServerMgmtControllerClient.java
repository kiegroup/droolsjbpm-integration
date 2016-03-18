package org.kie.server.controller.client;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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
import org.kie.server.controller.api.model.KieServerInstance;
import org.kie.server.controller.api.model.KieServerInstanceInfo;
import org.kie.server.controller.api.model.KieServerInstanceList;
import org.kie.server.controller.api.model.KieServerSetup;
import org.kie.server.controller.api.model.KieServerStatus;
import org.kie.server.controller.api.model.runtime.Container;
import org.kie.server.controller.api.model.runtime.ContainerKey;
import org.kie.server.controller.api.model.runtime.ServerInstance;
import org.kie.server.controller.api.model.runtime.ServerInstanceKey;
import org.kie.server.controller.api.model.spec.Capability;
import org.kie.server.controller.api.model.spec.ContainerConfig;
import org.kie.server.controller.api.model.spec.ContainerSpec;
import org.kie.server.controller.api.model.spec.ContainerSpecKey;
import org.kie.server.controller.api.model.spec.ContainerSpecList;
import org.kie.server.controller.api.model.spec.ProcessConfig;
import org.kie.server.controller.api.model.spec.RuleConfig;
import org.kie.server.controller.api.model.spec.ServerConfig;
import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.kie.server.controller.api.model.spec.ServerTemplateKey;
import org.kie.server.controller.api.model.spec.ServerTemplateList;
import org.kie.server.controller.client.exception.UnexpectedResponseCodeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KieServerMgmtControllerClient {

    private static Logger logger = LoggerFactory.getLogger(KieServerMgmtControllerClient.class);

    private static final String MANAGEMENT_LAST_URI_PART = "/management/servers";
    private static final String CONTAINERS_LAST_URI_PART = "/containers";

    private static final String MANAGEMENT_URI_PART = MANAGEMENT_LAST_URI_PART + "/";
    private static final String CONTAINERS_URI_PART = CONTAINERS_LAST_URI_PART + "/";

    private static final String STARTED_STATUS_URI_PART = "/status/started";
    private static final String STOPPED_STATUS_URI_PART = "/status/stopped";
    private static final String CONFIG_URI_PART = "/config/";

    private ClientExecutor executor;
    private String controllerBaseUrl;
    private MarshallingFormat format = MarshallingFormat.JAXB;
    private CloseableHttpClient httpClient;
    protected Marshaller marshaller;

    public KieServerMgmtControllerClient(String controllerBaseUrl, String login, String password) {
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

    public ServerTemplate getServerTemplate(String serverTemplateId) {
        return makeGetRequestAndCreateCustomResponse(controllerBaseUrl + MANAGEMENT_URI_PART + serverTemplateId, ServerTemplate.class);
    }

    public void saveContainerSpec(String serverTemplateId, ContainerSpec containerSpec ) {
        makePutRequestAndCreateCustomResponse(controllerBaseUrl + MANAGEMENT_URI_PART + serverTemplateId + CONTAINERS_URI_PART +containerSpec.getId(), containerSpec, Object.class);
    }

    public void saveServerTemplate(ServerTemplate serverTemplate) {
        makePutRequestAndCreateCustomResponse(controllerBaseUrl + MANAGEMENT_URI_PART + serverTemplate.getId(), serverTemplate, Object.class);
    }

    public void deleteServerTemplate(String serverTemplateId) {
        makeDeleteRequest(controllerBaseUrl + MANAGEMENT_URI_PART + serverTemplateId);
    }

    public ContainerSpec getContainerInfo(String serverTemplateId, String containerId) {
        return makeGetRequestAndCreateCustomResponse(controllerBaseUrl + MANAGEMENT_URI_PART + serverTemplateId + CONTAINERS_URI_PART + containerId, ContainerSpec.class);
    }

    public void deleteContainerSpec(String serverTemplateId, String containerId) {
        makeDeleteRequest(controllerBaseUrl + MANAGEMENT_URI_PART + serverTemplateId + CONTAINERS_URI_PART + containerId);
    }

    public Collection<ServerTemplate> listServerTemplates() {
        ServerTemplateList serverTemplateList = makeGetRequestAndCreateCustomResponse(controllerBaseUrl + MANAGEMENT_LAST_URI_PART, ServerTemplateList.class);
        if (serverTemplateList != null && serverTemplateList.getServerTemplates() != null) {
            return Arrays.asList(serverTemplateList.getServerTemplates());
        }

        return Collections.emptyList();
    }

    public Collection<ContainerSpec> listContainerSpec(String serverTemplateId) {
        ContainerSpecList containerSpecList = makeGetRequestAndCreateCustomResponse(controllerBaseUrl + MANAGEMENT_URI_PART + serverTemplateId + CONTAINERS_LAST_URI_PART, ContainerSpecList.class);
        if (containerSpecList != null && containerSpecList.getContainerSpecs() != null) {
            return Arrays.asList(containerSpecList.getContainerSpecs());
        }

        return Collections.emptyList();
    }

    public void startContainer(String serverTemplateId, String containerId) {
        makePostRequestAndCreateCustomResponse(controllerBaseUrl + MANAGEMENT_URI_PART + serverTemplateId + CONTAINERS_URI_PART + containerId + STARTED_STATUS_URI_PART, "", null);
    }

    public void stopContainer(String serverTemplateId, String containerId) {
        makePostRequestAndCreateCustomResponse(controllerBaseUrl + MANAGEMENT_URI_PART + serverTemplateId + CONTAINERS_URI_PART + containerId + STOPPED_STATUS_URI_PART, "", null);
    }

    public void updateContainerConfig(String serverTemplateId, String containerId, Capability capability, ContainerConfig config) {
        makePostRequestAndCreateCustomResponse(controllerBaseUrl + MANAGEMENT_URI_PART + serverTemplateId + CONTAINERS_URI_PART + containerId + CONFIG_URI_PART + capability.toString(), config, Object.class);
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
                    .body(getMediaType(format), serialize(bodyObject)).post(resultType);

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

        controllerClasses.add(ServerInstance.class);
        controllerClasses.add(ServerInstanceKey.class);
        controllerClasses.add(ServerTemplate.class);
        controllerClasses.add(ServerTemplateKey.class);
        controllerClasses.add(ServerConfig.class);
        controllerClasses.add(RuleConfig.class);
        controllerClasses.add(ProcessConfig.class);
        controllerClasses.add(ContainerSpec.class);
        controllerClasses.add(ContainerSpecKey.class);
        controllerClasses.add(Container.class);
        controllerClasses.add(ContainerKey.class);
        controllerClasses.add(ServerTemplateList.class);
        controllerClasses.add(ContainerSpecList.class);

        Set<Class<?>> minimalControllerClasses = new HashSet<Class<?>>();
        minimalControllerClasses.add(RuleConfig.class);
        minimalControllerClasses.add(ProcessConfig.class);

        switch ( format ) {
            case JAXB:
                this.marshaller = MarshallerFactory.getMarshaller(controllerClasses, format, KieServerMgmtControllerClient.class.getClassLoader());
                break;
            case JSON:
                this.marshaller = MarshallerFactory.getMarshaller(minimalControllerClasses, format, KieServerMgmtControllerClient.class.getClassLoader());
                break;
            default:
                this.marshaller = MarshallerFactory.getMarshaller(controllerClasses, format, KieServerMgmtControllerClient.class.getClassLoader());
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

            T result = marshaller.unmarshall( content, type );
            return result;
        } catch ( MarshallingException e ) {
            throw new RuntimeException( "Error while deserializing data received from server!", e );
        } finally {
            response.releaseConnection();
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Class<T> castClass(Class<?> aClass) {
        return (Class<T>)aClass;
    }
}
