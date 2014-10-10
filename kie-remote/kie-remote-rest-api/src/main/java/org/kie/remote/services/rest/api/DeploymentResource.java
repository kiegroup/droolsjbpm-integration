package org.kie.remote.services.rest.api;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

import org.kie.internal.runtime.conf.DeploymentDescriptor;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentDescriptor;

@Path("/deployment/{deploymentId: [\\w\\.-]+(:[\\w\\.-]+){2,2}(:[\\w\\.-]*){0,2}}")
public interface DeploymentResource {

    /**
     * Retrieve the status of the {@link DeploymentUnit} specified in the URL.
     * 
     * @return A {@link JaxbDeploymentUnit} instance
     */
    @GET
    public Response getConfig();

    /**
     * Queues a request to deploy the given deployment unit. If the deployment already exist, this
     * operation will fail.
     * 
     * @param deployDescriptor An optional {@link DeploymentDescriptor} instance specifying additional information about how
     * the deployment unit should be deployed.
     * @return A {@link JaxbDeploymentJobResult} instance with the initial status of the job
     */
    @POST
    @Path("/deploy")
    public Response deploy( JaxbDeploymentDescriptor deployDescriptor );

    /**
     * Queues a request to undeploy the deployment unit specified in the URL
     * 
     * @return A {@link JaxbDeploymentJobResult} instance with the initial status of the job
     */
    @POST
    @Path("/undeploy")
    public Response undeploy();

    @GET
    @Path("/processes")
    public Response listProcessDefinitions();

}