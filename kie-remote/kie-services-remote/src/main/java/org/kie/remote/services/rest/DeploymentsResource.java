package org.kie.remote.services.rest;

import static org.kie.remote.services.rest.DeploymentResource.convertKModuleDepUnitToJaxbDepUnit;

import java.util.Collection;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.jbpm.kie.services.api.Kjar;
import org.jbpm.kie.services.impl.KModuleDeploymentService;
import org.jbpm.kie.services.impl.KModuleDeploymentUnit;
import org.kie.internal.deployment.DeployedUnit;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentUnit;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentUnit.JaxbDeploymentStatus;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentUnitList;
import org.kie.remote.services.cdi.DeploymentInfoBean;

@Path("/deployment")
@RequestScoped
public class DeploymentsResource extends ResourceBase {

    @Context
    private HttpHeaders headers;
    
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
            if( deployedUnit != null ) { 
                JaxbDeploymentUnit jaxbDepUnit = convertKModuleDepUnitToJaxbDepUnit((KModuleDeploymentUnit) deployedUnit.getDeploymentUnit());
                jaxbDepUnit.setStatus(JaxbDeploymentStatus.DEPLOYED);
                depUnitList.add(jaxbDepUnit);
            }
        }
        
        return createCorrectVariant(new JaxbDeploymentUnitList(depUnitList), headers);
    }
    
}
