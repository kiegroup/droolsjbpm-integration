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
        this( baseURI, null, null, MediaType.APPLICATION_XML_TYPE );
    }

    public KieServicesClient(String baseURI, MediaType mediaType) {
        this( baseURI, null, null, mediaType );
    }

    public KieServicesClient(String baseURI, String username, String password) {
        this( baseURI, username, password, MediaType.APPLICATION_XML_TYPE );
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
        KieRemoteHttpRequest httpRequest = newRequest(baseURI).get();
        KieRemoteHttpResponse response = httpRequest.response();
        try {
            if (response.code() == Response.Status.OK.getStatusCode()) {
                return serializationProvider.deserialize(response.body(), ServiceResponse.class);
            }
            // TODO print some useful info like response body here (e.g. when the server returns 500 with HTML content)
            throw new KieServicesClientException("Unexpected HTTP response code: " + response.code());
        } catch (SerializationException e) {
            throw new KieServicesClientException("Error while serializing data received from server!", e);
        }
    }

    public ServiceResponse<KieContainerResourceList> listContainers() {
        KieRemoteHttpRequest httpRequest = newRequest(baseURI + "/containers").get();
        KieRemoteHttpResponse response = httpRequest.response();
        try {
            if (response.code() == Response.Status.OK.getStatusCode()) {
                return serializationProvider.deserialize(response.body(), ServiceResponse.class);
            }
            // TODO print some useful info like response body here (e.g. when the server returns 500 with HTML content)
            throw new KieServicesClientException("Unexpected response code: " + response.code());
        } catch (SerializationException e) {
            logger.debug("Received body: " + response.body());
            throw new KieServicesClientException("Error while serializing data received from server!", e);
        }
    }

    public ServiceResponse<KieContainerResource> createContainer(String id, KieContainerResource resource) {
        KieRemoteHttpRequest httpRequest = newRequest(baseURI + "/containers/" + id).body(serializationProvider.serialize(resource)).put();
        KieRemoteHttpResponse response = httpRequest.response();
        int responseCode = response.code();
        try {
            if (responseCode == Response.Status.CREATED.getStatusCode()) {
                return serializationProvider.deserialize(response.body(), ServiceResponse.class);
            } else if (responseCode == Response.Status.BAD_REQUEST.getStatusCode()) {
                return serializationProvider.deserialize(response.body(), ServiceResponse.class);
            }
            // TODO print some useful info like response body here (e.g. when the server returns 500 with HTML content)
            throw new KieServicesClientException("Unexpected HTTP response code: " + response.code());
        } catch (SerializationException e) {
            throw new KieServicesClientException("Error while serializing data received from server!", e);
        }
    }

    public ServiceResponse<KieContainerResource> getContainerInfo(String id) {
        KieRemoteHttpRequest httpRequest = newRequest(baseURI + "/containers/" + id).get();
        KieRemoteHttpResponse response = httpRequest.response();
        try {
            if (response.code() == Response.Status.OK.getStatusCode()) {
                return serializationProvider.deserialize(response.body(), ServiceResponse.class);
            }
            // TODO print some useful info like response body here (e.g. when the server returns 500 with HTML content)
            throw new KieServicesClientException("Unexpected response code: " + response.code());
        } catch (SerializationException e) {
            throw new KieServicesClientException("Error while serializing data received from server!", e);
        }
    }

    public ServiceResponse<Void> disposeContainer(String id) {
        KieRemoteHttpRequest httpRequest = newRequest(baseURI + "/containers/" + id).delete();
        KieRemoteHttpResponse response = httpRequest.response();
        try {
            if (response.code() == Response.Status.OK.getStatusCode()) {
                return serializationProvider.deserialize(response.body(), ServiceResponse.class);
            }
            // TODO print some useful info like response body here (e.g. when the server returns 500 with HTML content)
            throw new KieServicesClientException("Unexpected response code: " + response.code());
        } catch (SerializationException e) {
            throw new KieServicesClientException("Error while serializing data received from server!", e);
        }
    }

    public ServiceResponse<String> executeCommands(String id, String payload) {
        KieRemoteHttpRequest httpRequest = newRequest(baseURI + "/containers/" + id).body(payload).post();
        KieRemoteHttpResponse response = httpRequest.response();
        try {
            if (response.code() == Response.Status.OK.getStatusCode()) {
                return serializationProvider.deserialize(response.body(), ServiceResponse.class);
            }
            // TODO print some useful info like response body here (e.g. when the server returns 500 with HTML content)
            throw new KieServicesClientException("Unexpected response code: " + response.code());
        } catch (SerializationException e) {
            throw new KieServicesClientException("Error while serializing data received from server!", e);
        }
    }

    public List<ServiceResponse<? extends Object>> executeScript(CommandScript script) {
        KieRemoteHttpRequest httpRequest = newRequest(baseURI).body(serializationProvider.serialize(script)).post();
        KieRemoteHttpResponse response = httpRequest.response();
        try {
            if (response.code() == Response.Status.OK.getStatusCode()) {
                return (List<ServiceResponse<? extends Object>>) serializationProvider.deserialize(response.body(), List.class);
            }
            // TODO print some useful info like response body here (e.g. when the server returns 500 with HTML content)
            throw new KieServicesClientException("Unexpected response code: " + response.code());
        } catch (SerializationException e) {
            logger.debug("Data received from server: " + response.body());
            throw new KieServicesClientException("Error while serializing data received from server!", e);
        }
    }

    public ServiceResponse<KieScannerResource> getScannerInfo(String id) {
        KieRemoteHttpRequest httpRequest = newRequest(baseURI + "/containers/" + id + "/scanner");
        KieRemoteHttpResponse response = httpRequest.get().response();
        try {
            if (response.code() == Response.Status.OK.getStatusCode()) {
                return serializationProvider.deserialize(response.body(), ServiceResponse.class);
            }
            // TODO print some useful info like response body here (e.g. when the server returns 500 with HTML content)
            throw new KieServicesClientException("Unexpected response code: " + response.code());
        } catch (SerializationException e) {
            throw new KieServicesClientException("Error while serializing data received from server!", e);
        }
    }
    
    public ServiceResponse<KieScannerResource> updateScanner( String id, KieScannerResource resource ) {
        KieRemoteHttpRequest httpRequest = newRequest(baseURI + "/containers/" + id + "/scanner")
                .body(serializationProvider.serialize(resource)).post();
        KieRemoteHttpResponse response = httpRequest.response();
        try {
            if (response.code() == Response.Status.OK.getStatusCode()) {
                return serializationProvider.deserialize(response.body(), ServiceResponse.class);
            }
            // TODO print some useful info like response body here (e.g. when the server returns 500 with HTML content)
            throw new KieServicesClientException("Unexpected response code: " + response.code());
        } catch (SerializationException e) {
            throw new KieServicesClientException("Error while serializing data received from server!", e);
        }
    }

    public ServiceResponse<ReleaseId> updateReleaseId(String id, ReleaseId releaseId) {
        KieRemoteHttpRequest httpRequest = newRequest(baseURI + "/containers/" + id + "/release-id")
                .body(serializationProvider.serialize(releaseId)).post();
        KieRemoteHttpResponse response = httpRequest.response();
        try {
            if (response.code() == Response.Status.OK.getStatusCode()) {
                return serializationProvider.deserialize(response.body(), ServiceResponse.class);
            }
            // TODO print some useful info like response body here (e.g. when the server returns 500 with HTML content)
            throw new KieServicesClientException("Unexpected response code: " + response.code());
        } catch (SerializationException e) {
            throw new KieServicesClientException("Error while serializing data received from server!", e);
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
    
}
