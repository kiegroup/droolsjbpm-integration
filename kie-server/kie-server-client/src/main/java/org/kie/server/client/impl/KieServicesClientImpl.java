package org.kie.server.client.impl;

import org.kie.remote.common.rest.KieRemoteHttpRequest;
import org.kie.remote.common.rest.KieRemoteHttpResponse;
import org.kie.server.api.commands.CommandScript;
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

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;

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
    public ServiceResponse<KieServerInfo> getServerInfo() {
        return makeHttpGetRequestAndCreateServiceResponse( baseURI, KieServerInfo.class );
    }

    @Override
    public ServiceResponse<KieContainerResourceList> listContainers() {
        return makeHttpGetRequestAndCreateServiceResponse( baseURI + "/containers", KieContainerResourceList.class );
    }

    @Override
    public ServiceResponse<KieContainerResource> createContainer(String id, KieContainerResource resource) {
        return makeHttpPutRequestAndCreateServiceResponse( baseURI + "/containers/" + id, resource, KieContainerResource.class );
    }

    @Override
    public ServiceResponse<KieContainerResource> getContainerInfo(String id) {
        return makeHttpGetRequestAndCreateServiceResponse( baseURI + "/containers/" + id, KieContainerResource.class );
    }

    @Override
    public ServiceResponse<Void> disposeContainer(String id) {
        return makeHttpDeleteRequestAndCreateServiceResponse( baseURI + "/containers/" + id, Void.class );
    }

    @Override
    public ServiceResponse<String> executeCommands(String id, String payload) {
        return makeHttpPostRequestAndCreateServiceResponse( baseURI + "/containers/" + id, payload, String.class );
    }

    @Override
    public ServiceResponsesList executeScript(CommandScript script) {
        return makeHttpPostRequestAndCreateCustomResult( baseURI, script, ServiceResponsesList.class );
    }

    @Override
    public ServiceResponse<KieScannerResource> getScannerInfo(String id) {
        return makeHttpGetRequestAndCreateServiceResponse( baseURI + "/containers/" + id + "/scanner", KieScannerResource.class );
    }

    @Override
    public ServiceResponse<KieScannerResource> updateScanner(String id, KieScannerResource resource) {
        return makeHttpPostRequestAndCreateServiceResponse(
                baseURI + "/containers/" + id + "/scanner", resource,
                KieScannerResource.class );
    }

    @Override
    public ServiceResponse<ReleaseId> updateReleaseId(String id, ReleaseId releaseId) {
        return makeHttpPostRequestAndCreateServiceResponse(
                baseURI + "/containers/" + id + "/release-id", releaseId,
                ReleaseId.class );
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
