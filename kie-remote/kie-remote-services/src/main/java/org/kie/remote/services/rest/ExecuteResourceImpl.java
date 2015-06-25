package org.kie.remote.services.rest;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.kie.api.command.Command;
import org.kie.internal.identity.IdentityProvider;
import org.kie.remote.services.cdi.ProcessRequestBean;
import org.kie.remote.services.jaxb.JaxbCommandsRequest;
import org.kie.remote.services.jaxb.JaxbCommandsResponse;
import org.kie.remote.services.util.ExecuteCommandUtil;

@Path("/execute")
@RequestScoped
public class ExecuteResourceImpl {

    @Inject
    protected ProcessRequestBean processRequestBean;
    
    @Inject
    protected IdentityProvider identityProvider;
    
    /**
     * The "/execute" method is an "internal" method that is used by the kie-remote-client classes
     * </p>
     * It is not meant to be used "externally".
     *  
     * @param cmdsRequest The {@link JaxbCommandsRequest} containing the {@link Command} and other necessary info.
     * @return A {@link JaxbCommandsResponse} with the result from the {@link Command}
     */
    @POST
    @Consumes(MediaType.APPLICATION_XML)
    @Produces(MediaType.APPLICATION_XML)
    public JaxbCommandsResponse execute(JaxbCommandsRequest cmdsRequest) {
        return ExecuteCommandUtil.restProcessJaxbCommandsRequest(
                cmdsRequest, 
                identityProvider, 
                processRequestBean);
    } 
    

}
