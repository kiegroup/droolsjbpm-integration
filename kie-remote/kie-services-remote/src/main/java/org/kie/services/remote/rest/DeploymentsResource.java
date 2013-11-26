package org.kie.services.remote.rest;

import static org.kie.services.remote.rest.DeploymentResource.*;

import java.util.Collection;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import org.jbpm.kie.services.api.Kjar;
import org.jbpm.kie.services.impl.KModuleDeploymentService;
import org.jbpm.kie.services.impl.KModuleDeploymentUnit;
import org.kie.internal.deployment.DeployedUnit;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentUnit;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentUnit.JaxbDeploymentStatus;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentUnitList;
import org.kie.services.remote.cdi.DeploymentInfoBean;

@Path("/deployment")
@RequestScoped
public class DeploymentsResource extends ResourceBase {

    /* REST information */
    @Context
    private HttpHeaders headers;
    
    @Context
    private HttpServletRequest request;
    
    @Context
    private Request restRequest;

    /* KIE resources */
   
    @Inject
    @Kjar
    private KModuleDeploymentService deploymentService;
   
    @Inject
    private DeploymentInfoBean deploymentInfoBean;
   
    // REST operations -----------------------------------------------------------------------------------------------------------
    
    @GET
    @Path("/")
    public Response listDeployments() { 
        Collection<String> deploymentIds = deploymentInfoBean.getDeploymentIds();
        
        JaxbDeploymentUnitList jaxbDepUnitList = new JaxbDeploymentUnitList();
        List<JaxbDeploymentUnit> depUnitList = jaxbDepUnitList.getDeploymentUnitList();
        for( String deploymentId : deploymentIds ) { 
            DeployedUnit deployedUnit = deploymentService.getDeployedUnit(deploymentId);
            JaxbDeploymentUnit jaxbDepUnit = convertKModuleDepUnitToJaxbDepUnit((KModuleDeploymentUnit) deployedUnit.getDeploymentUnit());
            jaxbDepUnit.setStatus(JaxbDeploymentStatus.DEPLOYED);
            depUnitList.add(jaxbDepUnit);
        }
        
        return createCorrectVariant(depUnitList, headers);
    }
    
}
