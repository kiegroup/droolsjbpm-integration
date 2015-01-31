package org.kie.server.services.rest;

import java.util.List;

import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.Variant;

import org.kie.remote.common.rest.RestEasy960Util;
import org.kie.server.api.commands.CommandScript;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieScannerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.ServiceResponsesList;
import org.kie.server.services.api.KieServer;
import org.kie.server.services.impl.KieServerImpl;
import org.kie.server.services.impl.KieServerLocator;

import static org.kie.remote.common.rest.RestEasy960Util.*;

@Path("/server")
public class KieServerRestImpl implements KieServer {

    private KieServerImpl server;

    public KieServerRestImpl() {
        // for now, if no server impl is passed as parameter, create one
        this.server = KieServerLocator.getInstance();
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
    public Response getInfo(HttpHeaders headers) {
        return createCorrectVariant(server.getInfo(), headers);
    }

    @Override
    public Response execute(HttpHeaders headers, CommandScript command) {
        return createCorrectVariant(new GenericEntity<ServiceResponsesList>(server.executeScript(command)) {
        }, headers);
    }

    @Override
    public Response listContainers(HttpHeaders headers) {
        return createCorrectVariant(server.listContainers(), headers);
    }

    @Override
    public Response createContainer(HttpHeaders headers, String id, KieContainerResource container) {
        ServiceResponse<KieContainerResource> response = server.createContainer(id, container);
        if( response.getType() == ServiceResponse.ResponseType.SUCCESS ) {
            return createCorrectVariant(response, headers, Status.CREATED);
        }
        return createCorrectVariant(response, headers, Status.BAD_REQUEST);
    }

    @Override
    public Response getContainerInfo(HttpHeaders headers, String id) {
        return createCorrectVariant(server.getContainerInfo(id), headers);
    }

    @Override
    public Response disposeContainer(HttpHeaders headers, String id) {
        return createCorrectVariant(server.disposeContainer(id), headers);
    }

    @Override
    public Response execute(HttpHeaders headers, String id, String cmdPayload) {
        return createCorrectVariant(server.callContainer(id, cmdPayload), headers);
    }

    @Override
    public Response getScannerInfo(HttpHeaders headers, String id) {
        return createCorrectVariant(server.getScannerInfo(id), headers);
    }

    @Override
    public Response updateScanner(HttpHeaders headers, String id, KieScannerResource resource) {
        return createCorrectVariant(server.updateScanner(id, resource), headers);
    };

    @Override
    public Response getReleaseId(HttpHeaders headers, String id) {
        return createCorrectVariant(server.getContainerReleaseId(id), headers);
    }

    @Override
    public Response updateReleaseId(HttpHeaders headers, String id, ReleaseId releaseId) {
        return createCorrectVariant(server.updateContainerReleaseId(id, releaseId), headers);
    }

    protected static Response createCorrectVariant(Object responseObj, HttpHeaders headers) {
        return createCorrectVariant(responseObj, headers, null);
    }

    protected static Response createCorrectVariant(Object responseObj, HttpHeaders headers, javax.ws.rs.core.Response.Status status) {
        Response.ResponseBuilder responseBuilder = null;
        Variant v = getVariant(headers);
        if( v == null ) {
            v = defaultVariant;
        }
        if( status != null ) {
            responseBuilder = Response.status(status).entity(responseObj).variant(v);
        } else {
            responseBuilder = Response.ok(responseObj, v);
        }
        return responseBuilder.build();
    }

}
