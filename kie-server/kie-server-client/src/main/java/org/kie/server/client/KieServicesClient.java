package org.kie.server.client;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ContextResolver;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.map.ObjectMapper;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.ClientResponseFailure;
import org.jboss.resteasy.client.core.executors.ApacheHttpClient4Executor;
import org.jboss.resteasy.spi.ResteasyProviderFactory;
import org.jboss.resteasy.util.GenericType;
import org.kie.server.api.commands.CommandScript;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerResourceList;
import org.kie.server.api.model.KieScannerResource;
import org.kie.server.api.model.KieServerInfo;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;


public class KieServicesClient {
    
    private final String baseURI;
    private final String username;
    private final String password;
    private final MediaType mediaType;

    public KieServicesClient(String baseURI) {
        this( baseURI, null, null, MediaType.APPLICATION_XML_TYPE );
    }

    public KieServicesClient(String baseURI, MediaType mediaType) {
        this( baseURI, null, null, mediaType );
    }

    public KieServicesClient(String baseURI, String username, String password) {
        this( baseURI, username, password, MediaType.APPLICATION_XML_TYPE );
    }

    static {
        ResteasyProviderFactory factory = ResteasyProviderFactory.getInstance();
        ContextResolver<ObjectMapper> contextResolver = new JacksonConfig();
        factory.addContextResolver(contextResolver);
    }

    public KieServicesClient(String baseURI, String username, String password, MediaType mediaType) {
        this.baseURI = baseURI;
        this.username = username;
        this.password = password;
        this.mediaType = mediaType;
    }

    public ServiceResponse<KieServerInfo> getServerInfo() throws ClientResponseFailure {
        ClientResponse<ServiceResponse<KieServerInfo>> response = null;
        try {
            ClientRequest clientRequest = newRequest(baseURI);
            response = clientRequest.get(new GenericType<ServiceResponse<KieServerInfo>>(){});
            if( response.getStatus() == Response.Status.OK.getStatusCode() ) {
                return response.getEntity();
            }
            throw new ClientResponseFailure("Unexpected response code: "+response.getStatus(), response );
        } catch (ClientResponseFailure e) {
            throw e;
        } catch (Exception e) {
            throw new ClientResponseFailure("Unexpected exception retrieving server info.", e, response );
        }
    }

    private ClientRequest newRequest(String uri) {
        URI uriObject;
        try {
            uriObject = new URI(uri);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Malformed URI was specified: '" + uri + "'!", e);
        }
        if (username == null || password == null) {
            return new ClientRequest(uri).accept(mediaType);
        } else {
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(
                    new AuthScope(uriObject.getHost(), uriObject.getPort()),
                    new UsernamePasswordCredentials(username, password)
            );

            DefaultHttpClient client = new DefaultHttpClient();
            client.setCredentialsProvider(credentialsProvider);
            ApacheHttpClient4Executor executor = new ApacheHttpClient4Executor(client);
            return new ClientRequest(uri, executor).accept(mediaType);
        }
    }

    public ServiceResponse<KieContainerResourceList> listContainers() throws ClientResponseFailure {
        ClientResponse<ServiceResponse<KieContainerResourceList>> response = null;
        try {
            ClientRequest clientRequest = newRequest(baseURI + "/containers");
            response = clientRequest.get(new GenericType<ServiceResponse<KieContainerResourceList>>(){});
            if( response.getStatus() == Response.Status.OK.getStatusCode() ) {
                return response.getEntity();
            }
            throw new ClientResponseFailure("Unexpected response code: "+response.getStatus(), response );
        } catch (ClientResponseFailure e) {
            throw e;
        } catch (Exception e) {
            throw new ClientResponseFailure("Unexpected exception retrieving list of containers.", e, response );
        }
    }

    public ServiceResponse<KieContainerResource> createContainer(String id, KieContainerResource resource) throws ClientResponseFailure {
        ClientResponse<ServiceResponse<KieContainerResource>> response = null;
        try {
            ClientRequest clientRequest = newRequest(baseURI + "/containers/" + id);
            response = clientRequest.body(mediaType, resource).put(new GenericType<ServiceResponse<KieContainerResource>>(){});
            if( response.getStatus() == Response.Status.CREATED.getStatusCode() ) {
                return response.getEntity();
            } else if( response.getStatus() == Response.Status.BAD_REQUEST.getStatusCode() ) {
                return response.getEntity();
            }
            throw new ClientResponseFailure("Unexpected response code: "+response.getStatus(), response );
        } catch (ClientResponseFailure e) {
            throw e;
        } catch (Exception e) {
            throw new ClientResponseFailure("Unexpected exception creating container: "+id+" with release-id "+resource.getReleaseId(), e, response );
        }
    }

    public ServiceResponse<KieContainerResource> getContainerInfo(String id) throws ClientResponseFailure {
        ClientResponse<ServiceResponse<KieContainerResource>> response = null;
        try {
            ClientRequest clientRequest = newRequest(baseURI + "/containers/" + id);
            response = clientRequest.get(new GenericType<ServiceResponse<KieContainerResource>>(){});
            if( response.getStatus() == Response.Status.OK.getStatusCode() ) {
                return response.getEntity();
            }
            throw new ClientResponseFailure("Unexpected response code: "+response.getStatus(), response );
        } catch (ClientResponseFailure e) {
            throw e;
        } catch (Exception e) {
            throw new ClientResponseFailure("Unexpected exception retrieving container info.", e, response );
        }
    }

    public ServiceResponse<Void> disposeContainer(String id) throws ClientResponseFailure {
        ClientResponse<ServiceResponse<Void>> response = null;
        try {
            ClientRequest clientRequest = newRequest(baseURI + "/containers/" + id);
            response = clientRequest.delete(new GenericType<ServiceResponse<Void>>(){});
            if( response.getStatus() == Response.Status.OK.getStatusCode() ) {
                return response.getEntity();
            }
            throw new ClientResponseFailure("Unexpected response code: "+response.getStatus(), response );
        } catch (ClientResponseFailure e) {
            throw e;
        } catch (Exception e) {
            throw new ClientResponseFailure("Unexpected exception disposing container: "+id, e, response );
        }
    }

    public ServiceResponse<String> executeCommands(String id, String payload) throws ClientResponseFailure {
        ClientResponse<ServiceResponse<String>> response = null;
        try {
            ClientRequest clientRequest = newRequest(baseURI + "/containers/" + id);
            response = clientRequest.body(mediaType, payload).post(new GenericType<ServiceResponse<String>>(){});
            if( response.getStatus() == Response.Status.OK.getStatusCode() ) {
                return response.getEntity();
            }
            throw new ClientResponseFailure("Unexpected response code: "+response.getStatus(), response );
        } catch (ClientResponseFailure e) {
            throw e;
        } catch (Exception e) {
            throw new ClientResponseFailure("Unexpected exception executing commands on container "+id, e, response );
        }
    }

    public List<ServiceResponse<? extends Object>> executeScript(CommandScript script) throws ClientResponseFailure {
        ClientResponse<List<ServiceResponse<? extends Object>>> response = null;
        try {
            ClientRequest clientRequest = newRequest(baseURI);
            response = clientRequest.body(mediaType, script).post(new GenericType<List<ServiceResponse<? extends Object>>>() {});
            if( response.getStatus() == Response.Status.OK.getStatusCode() ) {
                return response.getEntity();
            }
            throw new ClientResponseFailure("Unexpected response code: "+response.getStatus(), response );
        } catch (ClientResponseFailure e) {
            throw e;
        } catch (Exception e) {
            throw new ClientResponseFailure("Unexpected exception retrieving server info.", e, response );
        }
    }
    
    public ServiceResponse<KieScannerResource> getScannerInfo( String id ) {
        ClientResponse<ServiceResponse<KieScannerResource>> response = null;
        try {
            ClientRequest clientRequest = newRequest(baseURI + "/containers/" + id + "/scanner");
            response = clientRequest.get(new GenericType<ServiceResponse<KieScannerResource>>(){});
            if( response.getStatus() == Response.Status.OK.getStatusCode() ) {
                return response.getEntity();
            }
            throw new ClientResponseFailure("Unexpected response code: "+response.getStatus(), response );
        } catch (ClientResponseFailure e) {
            throw e;
        } catch (Exception e) {
            throw new ClientResponseFailure("Unexpected exception retrieving scanner info for container '"+id+"'.", e, response );
        }
    }
    
    public ServiceResponse<KieScannerResource> updateScanner( String id, KieScannerResource resource ) {
        ClientResponse<ServiceResponse<KieScannerResource>> response = null;
        try {
            ClientRequest clientRequest = newRequest(baseURI + "/containers/" + id + "/scanner");
            response = clientRequest.body(mediaType, resource).post(new GenericType<ServiceResponse<KieScannerResource>>(){});
            if( response.getStatus() == Response.Status.OK.getStatusCode() ) {
                return response.getEntity();
            }
            throw new ClientResponseFailure("Unexpected response code: "+response.getStatus(), response );
        } catch (ClientResponseFailure e) {
            throw e;
        } catch (Exception e) {
            throw new ClientResponseFailure("Unexpected exception scanner for container '"+id+"'.", e, response );
        }
    }

    public ServiceResponse<ReleaseId> updateReleaseId(String id, ReleaseId releaseId) {
        ClientResponse<ServiceResponse<ReleaseId>> response = null;
        try {
            ClientRequest clientRequest = newRequest(baseURI + "/containers/" + id + "/release-id");
            response = clientRequest.body(mediaType, releaseId).post(new GenericType<ServiceResponse<ReleaseId>>(){});
            if( response.getStatus() == Response.Status.OK.getStatusCode() ) {
                return response.getEntity();
            }
            throw new ClientResponseFailure("Unexpected response code: "+response.getStatus(), response );
        } catch (ClientResponseFailure e) {
            throw e;
        } catch (Exception e) {
            throw new ClientResponseFailure("Unexpected exception updating releaseId for container '"+id+"'.", e, response );
        }
    }
    
}
