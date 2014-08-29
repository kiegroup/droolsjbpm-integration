package org.kie.remote.services.rest.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/deployment")
public interface DeploymentsResource {

    @GET
    // TODO: docs pagination
    public Response listDeployments();

    @GET
    @Path("/processes")
    // DOCS: (+ pagination)
    public Response listProcessDefinitions();

}