package org.kie.server.services.rest;

import java.util.List;

import javax.ws.rs.Path;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.kie.server.api.commands.CommandScript;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieScannerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.services.api.KieServer;
import org.kie.server.services.impl.KieServerImpl;

@Path("/server")
public class KieServerRestImpl implements KieServer {

    private KieServerImpl server;

    public KieServerRestImpl() {
        // for now, if no server impl is passed as parameter, create one
        this.server = new KieServerImpl();
    }

    public KieServerRestImpl(KieServerImpl server) {
        this.server = server;
    }

    public KieServerImpl getServer() {
        return server;
    }

    public void setServer(KieServerImpl server) {
        this.server = server;
    }

    @Override
    public Response getInfo() {
        return Response.ok(server.getInfo()).build();
    }

    @Override
    public Response execute(CommandScript command) {
        return Response.ok(new GenericEntity<List<ServiceResponse<? extends Object>>>(server.executeScript(command)) {
        }).build();
    }

    @Override
    public Response listContainers() {
        return Response.ok(server.listContainers()).build();
    }

    @Override
    public Response createContainer(String id, KieContainerResource container) {
        ServiceResponse<KieContainerResource> response = server.createContainer(id, container);
        if( response.getType() == ServiceResponse.ResponseType.SUCCESS ) {
            return Response.status(Status.CREATED).entity(response).build();
        }
        return Response.status(Status.BAD_REQUEST).entity(response).build();
    }

    @Override
    public Response getContainerInfo(String id) {
        return Response.ok(server.getContainerInfo(id)).build();
    }

    @Override
    public Response disposeContainer(String id) {
        return Response.ok(server.disposeContainer(id)).build();
    }

    @Override
    public Response execute(String id, String cmdPayload) {
        return Response.ok(server.callContainer(id, cmdPayload)).build();
    }

    @Override
    public Response getScannerInfo(String id) {
        return Response.ok(server.getScannerInfo(id)).build();
    }

    @Override
    public Response updateScanner(String id, KieScannerResource resource) {
        return Response.ok(server.updateScanner(id, resource)).build();
    };

    @Override
    public Response getReleaseId(String id) {
        return Response.ok(server.getContainerReleaseId(id)).build();
    }

    @Override
    public Response updateReleaseId(String id, ReleaseId releaseId) {
        return Response.ok(server.updateContainerReleaseId(id, releaseId)).build();
    }

}
