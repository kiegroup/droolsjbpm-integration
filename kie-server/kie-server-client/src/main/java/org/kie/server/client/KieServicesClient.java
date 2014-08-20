package org.kie.server.client;

import java.util.List;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.ClientResponseFailure;
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
    
    public KieServicesClient(String baseURI) {
        this.baseURI = baseURI;
    }

    public ServiceResponse<KieServerInfo> getServerInfo() throws ClientResponseFailure {
        ClientResponse<ServiceResponse<KieServerInfo>> response = null;
        try {
            ClientRequest clientRequest = new ClientRequest(baseURI);
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

    public ServiceResponse<KieContainerResourceList> listContainers() throws ClientResponseFailure {
        ClientResponse<ServiceResponse<KieContainerResourceList>> response = null;
        try {
            ClientRequest clientRequest = new ClientRequest(baseURI+"/containers");
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
            ClientRequest clientRequest = new ClientRequest(baseURI+"/containers/"+id);
            response = clientRequest.body(MediaType.APPLICATION_XML_TYPE, resource).put(new GenericType<ServiceResponse<KieContainerResource>>(){});
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
            ClientRequest clientRequest = new ClientRequest(baseURI+"/containers/"+id);
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
            ClientRequest clientRequest = new ClientRequest(baseURI+"/containers/"+id);
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
            ClientRequest clientRequest = new ClientRequest(baseURI+"/containers/"+id);
            response = clientRequest.body(MediaType.APPLICATION_XML_TYPE, payload).post(new GenericType<ServiceResponse<String>>(){});
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
            ClientRequest clientRequest = new ClientRequest(baseURI);
            response = clientRequest.body(MediaType.APPLICATION_XML_TYPE, script).post(new GenericType<List<ServiceResponse<? extends Object>>>() {});
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
            ClientRequest clientRequest = new ClientRequest(baseURI+"/containers/"+id+"/scanner");
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
            ClientRequest clientRequest = new ClientRequest(baseURI+"/containers/"+id+"/scanner");
            response = clientRequest.body(MediaType.APPLICATION_XML_TYPE, resource).post(new GenericType<ServiceResponse<KieScannerResource>>(){});
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
            ClientRequest clientRequest = new ClientRequest(baseURI+"/containers/"+id+"/release-id");
            response = clientRequest.body(MediaType.APPLICATION_XML_TYPE, releaseId).post(new GenericType<ServiceResponse<ReleaseId>>(){});
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
