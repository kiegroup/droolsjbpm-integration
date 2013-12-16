package org.kie.services.remote.rest;

import java.util.List;
import java.util.Map;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jboss.resteasy.spi.BadRequestException;
import org.jboss.resteasy.spi.NotFoundException;
import org.jbpm.kie.services.api.Kjar;
import org.jbpm.kie.services.impl.KModuleDeploymentService;
import org.jbpm.kie.services.impl.KModuleDeploymentUnit;
import org.kie.internal.deployment.DeployedUnit;
import org.kie.internal.deployment.DeploymentUnit.RuntimeStrategy;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentJobResult;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentUnit;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentUnit.JaxbDeploymentStatus;
import org.kie.services.remote.exception.KieRemoteServicesInternalError;
import org.kie.services.remote.rest.async.AsyncDeploymentJobExecutor;

/**
 * If a method in this class is annotated by a @Path annotation, 
 * then the name of the method should match the URL specified in the @Path, 
 * where "_" characters should be used for all "/" characters in the path. 
 * <p>
 * For example: 
 * <pre>
 * @Path("/begin/{varOne: [_a-zA-Z0-9-:\\.]+}/midddle/{varTwo: [a-z]+}")
 * public void begin_varOne_middle_varTwo() { 
 * </pre>
 * 
 * If the method is annotated by the @Path anno, but is the "root", then
 * give it a name that explains it's funtion.
 */
@Path("/deployment/{deploymentId: [\\w\\.-]+(:[\\w\\.-]+){2,2}(:[\\w\\.-]*){0,2}}")
@RequestScoped
public class DeploymentResource extends ResourceBase {

    /* REST information */
    @Context
    private HttpHeaders headers;
    
    @Context
    private HttpServletRequest request;
    
    @Context
    private Request restRequest;

    @PathParam("deploymentId")
    private String deploymentId;
    
    /* KIE resources */
   
    @Inject
    @Kjar
    private KModuleDeploymentService deploymentService;
    
    @Inject
    private AsyncDeploymentJobExecutor jobExecutor;
   
    // Helper methods ------------------------------------------------------------------------------------------------------------
    
    private KModuleDeploymentUnit createDeploymentUnit(String deploymentId) { 
        String [] gavKK = deploymentId.split(":");
        KModuleDeploymentUnit deployUnit = new KModuleDeploymentUnit(gavKK[0], gavKK[1], gavKK[2]);
        if( gavKK.length > 3 ) { 
            deployUnit.setKbaseName(gavKK[3]);
        }
        if( gavKK.length > 4 ) { 
            deployUnit.setKbaseName(gavKK[4]);
        }
        return deployUnit;
    }

    public static JaxbDeploymentUnit convertKModuleDepUnitToJaxbDepUnit(KModuleDeploymentUnit kDepUnit ) { 
        JaxbDeploymentUnit jDepUnit = new JaxbDeploymentUnit(
                kDepUnit.getGroupId(),
                kDepUnit.getArtifactId(),
                kDepUnit.getVersion(),
                kDepUnit.getKbaseName(),
                kDepUnit.getKsessionName());
        jDepUnit.setStrategy(kDepUnit.getStrategy());
        return jDepUnit;
    }

    // REST operations -----------------------------------------------------------------------------------------------------------

    @GET
    @Path("/")
    public Response getConfig() { 
        DeployedUnit deployedUnit = deploymentService.getDeployedUnit(deploymentId);
        JaxbDeploymentUnit jaxbDepUnit;
        if( deployedUnit == null ) { 
            JaxbDeploymentStatus status = jobExecutor.getStatus(deploymentId);
            String [] gavKK = deploymentId.split(":");
            switch(status) { 
            case DEPLOYING:
            case UNDEPLOYING:
            case DEPLOY_FAILED:
            case UNDEPLOY_FAILED: 
            case UNDEPLOYED:
                jaxbDepUnit = new JaxbDeploymentUnit(gavKK[0], gavKK[1], gavKK[2]);
                jaxbDepUnit.setStatus(status);
                break;
            case DEPLOYED:
                deployedUnit = deploymentService.getDeployedUnit(deploymentId);
                if( deployedUnit == null ) { 
                    throw new KieRemoteServicesInternalError("Impossible error: the job executor says that this has been deployed but it can't be located via the deployment service.");
                } else { 
                    jaxbDepUnit = extractDeploymentUnit(deployedUnit);
                    jaxbDepUnit.setStatus(status);
                }
                break;
            case NONEXISTENT:
                throw new NotFoundException("Deployment " + deploymentId + " does not exist");
            default: 
                throw new KieRemoteServicesInternalError("Unknown deployment status (" + status.toString() + "), contact the developers.");
            }
        } else { 
            jaxbDepUnit = extractDeploymentUnit(deployedUnit);
            jaxbDepUnit.setStatus(JaxbDeploymentStatus.DEPLOYED);
        }
        
        return createCorrectVariant(jaxbDepUnit, headers);
    }
    
    private JaxbDeploymentUnit extractDeploymentUnit(DeployedUnit deployedUnit) { 
        KModuleDeploymentUnit depUnit = (KModuleDeploymentUnit) deployedUnit.getDeploymentUnit();
        return convertKModuleDepUnitToJaxbDepUnit(depUnit);
    }
    
    @POST
    @Path("/deploy")
    public Response deploy() { 
        Map<String, List<String>> params = getRequestParams(request);
        String oper = getRelativePath(request);
        String strategy = getStringParam("strategy", false, params, oper);

        KModuleDeploymentUnit deploymentUnit = createDeploymentUnit(deploymentId);
       
        if( strategy != null ) { 
            strategy = strategy.toUpperCase();
            RuntimeStrategy runtimeStrategy;
            try { 
                runtimeStrategy = RuntimeStrategy.valueOf(strategy);
                deploymentUnit.setStrategy(runtimeStrategy);
            } catch( IllegalArgumentException iae ) { 
                throw new BadRequestException("Runtime strategy '" + strategy + "' does not exist.");
            }
        }

        JaxbDeploymentJobResult jobResult;
        jobResult = jobExecutor.submitDeployJob(deploymentService, deploymentUnit);
        jobResult.getDeploymentUnit().setStatus(JaxbDeploymentStatus.DEPLOYING);

        return createCorrectVariant(jobResult, headers, Status.ACCEPTED);
    }
    
    @POST
    @Path("/undeploy")
    public Response undeploy() { 
        DeployedUnit deployedUnit = deploymentService.getDeployedUnit(deploymentId);
        KModuleDeploymentUnit deploymentUnit = (KModuleDeploymentUnit) deployedUnit.getDeploymentUnit();
        
        JaxbDeploymentJobResult jobResult = jobExecutor.submitUndeployJob(deploymentService, deploymentUnit);
        jobResult.getDeploymentUnit().setStatus(JaxbDeploymentStatus.UNDEPLOYING);

        return createCorrectVariant(jobResult, headers, Status.ACCEPTED);
    }
}
