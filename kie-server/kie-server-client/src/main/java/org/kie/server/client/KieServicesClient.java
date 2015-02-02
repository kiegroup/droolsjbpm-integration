package org.kie.server.client;

import org.kie.remote.common.rest.KieRemoteHttpRequest;
import org.kie.remote.common.rest.KieRemoteHttpResponse;
import org.kie.server.api.commands.CommandScript;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.marshalling.MarshallingException;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerResourceList;
import org.kie.server.api.model.KieScannerResource;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.ServiceResponsesList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class KieServicesClient {
    private static      Logger logger                         = LoggerFactory.getLogger( KieServicesClient.class );
    public static final long   DEFAULT_REQUEST_TIMEOUT_MILLIS = 30000;

    private final String     baseURI;
    private final String     username;
    private final String     password;
    private final MediaType  mediaType;
    private final long       requestTimeoutMillis;
    private final Marshaller marshaller;

    public KieServicesClient(String baseURI) {
        this( baseURI, null, null, MediaType.APPLICATION_XML_TYPE );
    }

    public KieServicesClient(String baseURI, MediaType mediaType) {
        this( baseURI, null, null, mediaType );
    }

    public KieServicesClient(String baseURI, String username, String password) {
        this( baseURI, username, password, MediaType.APPLICATION_XML_TYPE );
    }

    public KieServicesClient(String baseURI, String username, String password, MediaType mediaType) {
        this( baseURI, username, password, mediaType, DEFAULT_REQUEST_TIMEOUT_MILLIS );
    }

    public KieServicesClient(String baseURI, String username, String password, MediaType mediaType, long requestTimeoutMillis) {
        this.baseURI = baseURI;
        this.username = username;
        this.password = password;
        this.mediaType = mediaType;
        this.requestTimeoutMillis = requestTimeoutMillis;
        if (MediaType.APPLICATION_XML_TYPE.equals(mediaType)) {
            marshaller = MarshallerFactory.getMarshaller( MarshallingFormat.JAXB, getClass().getClassLoader() );
        } else if (MediaType.APPLICATION_JSON_TYPE.equals(mediaType)) {
            marshaller = MarshallerFactory.getMarshaller( MarshallingFormat.JSON, getClass().getClassLoader() );
        } else {
            throw new RuntimeException("Unsupported media type '" + mediaType + "' specified!");
        }
    }

    public ServiceResponse<KieServerInfo> getServerInfo() {
        return makeHttpGetRequestAndCreateServiceResponse(baseURI, KieServerInfo.class);
    }

    public ServiceResponse<KieContainerResourceList> listContainers() {
        return makeHttpGetRequestAndCreateServiceResponse(baseURI + "/containers", KieContainerResourceList.class);
    }

    public ServiceResponse<KieContainerResource> createContainer(String id, KieContainerResource resource) {
        return makeHttpPutRequestAndCreateServiceResponse(baseURI + "/containers/" + id, resource, KieContainerResource.class);
    }

    public ServiceResponse<KieContainerResource> getContainerInfo(String id) {
        return makeHttpGetRequestAndCreateServiceResponse(baseURI + "/containers/" + id, KieContainerResource.class);
    }

    public ServiceResponse<Void> disposeContainer(String id) {
        return makeHttpDeleteRequestAndCreateServiceResponse(baseURI + "/containers/" + id, Void.class);
    }

    public ServiceResponse<String> executeCommands(String id, String payload) {
        return makeHttpPostRequestAndCreateServiceResponse(baseURI + "/containers/" + id, payload, String.class);
    }

    public ServiceResponsesList executeScript(CommandScript script) {
        return makeHttpPostRequestAndCreateCustomResult(baseURI, script, ServiceResponsesList.class);
    }

    public ServiceResponse<KieScannerResource> getScannerInfo(String id) {
        return makeHttpGetRequestAndCreateServiceResponse(baseURI + "/containers/" + id + "/scanner", KieScannerResource.class);
    }

    public ServiceResponse<KieScannerResource> updateScanner(String id, KieScannerResource resource) {
        return makeHttpPostRequestAndCreateServiceResponse(baseURI + "/containers/" + id + "/scanner", resource,
                KieScannerResource.class);
    }

    public ServiceResponse<ReleaseId> updateReleaseId(String id, ReleaseId releaseId) {
        return makeHttpPostRequestAndCreateServiceResponse(baseURI + "/containers/" + id + "/release-id", releaseId,
                ReleaseId.class);
    }

    @SuppressWarnings("unchecked")
    private <T> ServiceResponse<T> makeHttpGetRequestAndCreateServiceResponse(String uri, Class<T> resultType) {
        KieRemoteHttpRequest request = newRequest(uri).get();
        KieRemoteHttpResponse response = request.response();

        if (response.code() == Response.Status.OK.getStatusCode()) {
            ServiceResponse serviceResponse = deserialize(response.body(), ServiceResponse.class);
            checkResultType(serviceResponse, resultType);
            return serviceResponse;
        } else {
            throw createExceptionForUnexpectedResponseCode(request, response);
        }
    }

    private <T> ServiceResponse<T> makeHttpPostRequestAndCreateServiceResponse(String uri, Object bodyObject,
            Class<T> resultType) {
        return makeHttpPostRequestAndCreateServiceResponse(uri, serialize(bodyObject), resultType);
    }

    @SuppressWarnings("unchecked")
    private <T> ServiceResponse<T> makeHttpPostRequestAndCreateServiceResponse(String uri, String body, Class<T> resultType) {
        KieRemoteHttpRequest request = newRequest(uri).body(body).post();
        KieRemoteHttpResponse response = request.response();

        if (response.code() == Response.Status.OK.getStatusCode()) {
            ServiceResponse serviceResponse = deserialize(response.body(), ServiceResponse.class);
            checkResultType(serviceResponse, resultType);
            return serviceResponse;
        } else {
            throw createExceptionForUnexpectedResponseCode(request, response);
        }
    }

    private <T> T makeHttpPostRequestAndCreateCustomResult(String uri, Object bodyObject, Class<T> resultType) {
        return makeHttpPostRequestAndCreateCustomResult(uri, serialize(bodyObject), resultType);
    }

    private <T> T makeHttpPostRequestAndCreateCustomResult(String uri, String body, Class<T> resultType) {
        KieRemoteHttpRequest request = newRequest(uri).body(body).post();
        KieRemoteHttpResponse response = request.response();

        if (response.code() == Response.Status.OK.getStatusCode()) {
            return deserialize(response.body(), resultType);
        } else {
            throw createExceptionForUnexpectedResponseCode(request, response);
        }
    }

    private <T> ServiceResponse<T> makeHttpPutRequestAndCreateServiceResponse(String uri, Object bodyObject,
            Class<T> resultType) {
        return makeHttpPutRequestAndCreateServiceResponse(uri, serialize(bodyObject), resultType);
    }

    @SuppressWarnings("unchecked")
    private <T> ServiceResponse<T> makeHttpPutRequestAndCreateServiceResponse(String uri, String body, Class<T> resultType) {
        KieRemoteHttpRequest request = newRequest(uri).body(body).put();
        KieRemoteHttpResponse response = request.response();

        if (response.code() == Response.Status.CREATED.getStatusCode() ||
                response.code() == Response.Status.BAD_REQUEST.getStatusCode()) {
            ServiceResponse serviceResponse = deserialize(response.body(), ServiceResponse.class);
            checkResultType(serviceResponse, resultType);
            return serviceResponse;
        } else {
            throw createExceptionForUnexpectedResponseCode(request, response);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> ServiceResponse<T> makeHttpDeleteRequestAndCreateServiceResponse(String uri, Class<T> resultType) {
        KieRemoteHttpRequest request = newRequest(uri).delete();
        KieRemoteHttpResponse response = request.response();

        if (response.code() == Response.Status.OK.getStatusCode()) {
            ServiceResponse serviceResponse = deserialize(response.body(), ServiceResponse.class);
            checkResultType(serviceResponse, resultType);
            return serviceResponse;
        } else {
            throw createExceptionForUnexpectedResponseCode(request, response);
        }
    }

    private KieRemoteHttpRequest newRequest(String uri) {
        KieRemoteHttpRequest httpRequest =
                KieRemoteHttpRequest.newRequest(uri).followRedirects(true).timeout(requestTimeoutMillis);
        httpRequest.accept(mediaType.toString());
        if (username != null && password != null) {
            httpRequest.basicAuthorization(username, password);
        }
        return httpRequest;
    }

    private String serialize(Object object) {
        try {
            return marshaller.marshall( object );
        } catch (MarshallingException e) {
            throw new KieServicesClientException("Error while serializing request data!", e);
        }
    }

    private <T> T deserialize(String content, Class<T> type) {
        try {
            return marshaller.unmarshall(content, type);
        } catch (MarshallingException e) {
            throw new KieServicesClientException("Error while deserializing data received from server!", e);
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
        if (actualResult != null && !expectedResultType.isInstance(actualResult)) {
            throw new KieServicesClientException("Error while creating service response! The actual result type " +
                    serviceResponse.getResult().getClass() + " does not match the expected type " + expectedResultType + "!");
        }
    }

    private RuntimeException createExceptionForUnexpectedResponseCode(KieRemoteHttpRequest request,
            KieRemoteHttpResponse response) {
        String summaryMessage = "Unexpected HTTP response code when requesting URI '" + request.getUri() + "'! Error code: " +
                response.code() + ", message: " + response.message();
        logger.debug(summaryMessage + ", response body: " + response.body());
        return new KieServicesClientException(summaryMessage);
    }

}
