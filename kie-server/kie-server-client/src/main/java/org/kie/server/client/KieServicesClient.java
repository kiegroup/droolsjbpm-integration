package org.kie.server.client;

import org.kie.remote.common.rest.KieRemoteHttpRequest;
import org.kie.remote.common.rest.KieRemoteHttpResponse;
import org.kie.server.api.commands.CommandScript;
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
import java.util.List;

public class KieServicesClient {
    private static Logger logger = LoggerFactory.getLogger(KieServicesClient.class);
    public static final long DEFAULT_REQUEST_TIMEOUT_MILLIS = 30000;


    private final String baseURI;
    private final String username;
    private final String password;
    private final MediaType mediaType;
    private final long requestTimeoutMillis;
    private final SerializationProvider serializationProvider;

    public KieServicesClient(String baseURI) {
        this(baseURI, null, null, MediaType.APPLICATION_XML_TYPE);
    }

    public KieServicesClient(String baseURI, MediaType mediaType) {
        this(baseURI, null, null, mediaType);
    }

    public KieServicesClient(String baseURI, String username, String password) {
        this(baseURI, username, password, MediaType.APPLICATION_XML_TYPE);
    }

    public KieServicesClient(String baseURI, String username, String password, MediaType mediaType) {
        this(baseURI, username, password, mediaType, DEFAULT_REQUEST_TIMEOUT_MILLIS);
    }

    public KieServicesClient(String baseURI, String username, String password, MediaType mediaType, long requestTimeoutMillis) {
        this.baseURI = baseURI;
        this.username = username;
        this.password = password;
        this.mediaType = mediaType;
        this.requestTimeoutMillis = requestTimeoutMillis;
        if (MediaType.APPLICATION_XML_TYPE.equals(mediaType)) {
            serializationProvider = new JaxbSerializationProvider();
        } else if (MediaType.APPLICATION_JSON_TYPE.equals(mediaType)) {
            serializationProvider = new JsonSerializationProvider();
        } else {
            throw new RuntimeException("Unsupported media type '" + mediaType + "' specified!");
        }
    }

    public ServiceResponse<KieServerInfo> getServerInfo() {
        return makeHttpGetRequestAndProcessResponse(baseURI, ServiceResponse.class);
    }

    public ServiceResponse<KieContainerResourceList> listContainers() {
        return makeHttpGetRequestAndProcessResponse(baseURI + "/containers", ServiceResponse.class);
    }

    public ServiceResponse<KieContainerResource> createContainer(String id, KieContainerResource resource) {
        return makeHttpPutRequestAndProcessResponse(baseURI + "/containers/" + id, resource, ServiceResponse.class);
    }

    public ServiceResponse<KieContainerResource> getContainerInfo(String id) {
        return makeHttpGetRequestAndProcessResponse(baseURI + "/containers/" + id, ServiceResponse.class);
    }

    public ServiceResponse<Void> disposeContainer(String id) {
        return makeHttpDeleteRequestAndProcessResponse(baseURI + "/containers/" + id, ServiceResponse.class);
    }

    public ServiceResponse<String> executeCommands(String id, String payload) {
        return makeHttpPostRequestAndProcessResponse(baseURI + "/containers/" + id, payload, ServiceResponse.class);
    }

    public ServiceResponsesList executeScript(CommandScript script) {
        return makeHttpPostRequestAndProcessResponse(baseURI, script, ServiceResponsesList.class);
    }

    public ServiceResponse<KieScannerResource> getScannerInfo(String id) {
        return makeHttpGetRequestAndProcessResponse(baseURI + "/containers/" + id + "/scanner", ServiceResponse.class);
    }

    public ServiceResponse<KieScannerResource> updateScanner(String id, KieScannerResource resource) {
        return makeHttpPostRequestAndProcessResponse(baseURI + "/containers/" + id + "/scanner", resource, ServiceResponse.class);
    }

    public ServiceResponse<ReleaseId> updateReleaseId(String id, ReleaseId releaseId) {
        return makeHttpPostRequestAndProcessResponse(baseURI + "/containers/" + id + "/release-id", releaseId, ServiceResponse.class);
    }

    private <T> T makeHttpGetRequestAndProcessResponse(String uri, Class<T> type) {
        KieRemoteHttpRequest request = newRequest(uri).get();
        KieRemoteHttpResponse response = request.response();
        try {
            if (response.code() == Response.Status.OK.getStatusCode()) {
                return serializationProvider.deserialize(response.body(), type);
            } else {
                handleGenericHttpErrorCodes(request, response);
            }

        } catch (SerializationException e) {
            throw new KieServicesClientException("Error while serializing data received from server!", e);
        }
        throw new KieServicesClientException("Unexpected HTTP response code: " + response.code() + ". Message: " +
                response.message());
    }

    private <T> T makeHttpPostRequestAndProcessResponse(String uri, Object bodyObject, Class<T> type) {
        return makeHttpPostRequestAndProcessResponse(uri, serializationProvider.serialize(bodyObject), type);
    }

    private <T> T makeHttpPostRequestAndProcessResponse(String uri, String body, Class<T> type) {
        KieRemoteHttpRequest request = newRequest(uri).body(body).post();
        KieRemoteHttpResponse response = request.response();
        try {
            if (response.code() == Response.Status.OK.getStatusCode()) {
                return serializationProvider.deserialize(response.body(), type);
            } else {
                handleGenericHttpErrorCodes(request, response);
            }
        } catch (SerializationException e) {
            throw new KieServicesClientException("Error while serializing data received from server!", e);
        }
        throw new KieServicesClientException("Unexpected HTTP response code: " + response.code() + ". Message: " +
                response.message());
    }

    private <T> T makeHttpPutRequestAndProcessResponse(String uri, Object bodyObject, Class<T> type) {
        return makeHttpPutRequestAndProcessResponse(uri, serializationProvider.serialize(bodyObject), type);
    }

    private <T> T makeHttpPutRequestAndProcessResponse(String uri, String body, Class<T> type) {
        KieRemoteHttpRequest request = newRequest(uri).body(body).put();
        KieRemoteHttpResponse response = request.response();
        try {
            if (response.code() == Response.Status.CREATED.getStatusCode()) {
                return serializationProvider.deserialize(response.body(), type);
            } else if (response.code() == Response.Status.BAD_REQUEST.getStatusCode()) {
                return serializationProvider.deserialize(response.body(), type);
            } else {
                handleGenericHttpErrorCodes(request, response);
            }
        } catch (SerializationException e) {
            throw new KieServicesClientException("Error while serializing data received from server!", e);
        }
        throw new KieServicesClientException("Unexpected HTTP response code: " + response.code() + ". Message: " +
                response.message());
    }


    private <T> T makeHttpDeleteRequestAndProcessResponse(String uri, Class<T> type) {
        KieRemoteHttpRequest request = newRequest(uri).delete();
        KieRemoteHttpResponse response = request.response();
        try {
            if (response.code() == Response.Status.OK.getStatusCode()) {
                return serializationProvider.deserialize(response.body(), type);
            } else {
                handleGenericHttpErrorCodes(request, response);
            }
        } catch (SerializationException e) {
            throw new KieServicesClientException("Error while serializing data received from server!", e);
        }
        throw new KieServicesClientException("Unexpected HTTP response code: " + response.code() + ". Message: " +
                response.message());
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

    private void handleGenericHttpErrorCodes(KieRemoteHttpRequest request, KieRemoteHttpResponse response) {
        if (response.code() == Response.Status.NOT_FOUND.getStatusCode()) {
            throw new KieServicesClientException("Requested resource with URI '" + request.getUri() + "' not found! Message: " +
                    response.message());
        } else if (response.code() == Response.Status.INTERNAL_SERVER_ERROR.getStatusCode()) {
            throw new KieServicesClientException("Internal server error! " + response.message() + ". Whole body: " +
                    response.body());
        }
    }

}
