package org.kie.remote.services.rest;

import java.util.List;
import java.util.Map;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.jbpm.kie.services.api.Kjar;
import org.jbpm.kie.services.impl.KModuleDeploymentService;
import org.jbpm.kie.services.impl.KModuleDeploymentUnit;
import org.kie.internal.deployment.DeployedUnit;
import org.kie.internal.deployment.DeploymentUnit.RuntimeStrategy;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentJobResult;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentUnit;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentUnit.JaxbDeploymentStatus;
import org.kie.remote.services.exception.DeploymentNotFoundException;
import org.kie.remote.services.exception.KieRemoteServicesInternalError;
import org.kie.remote.services.rest.async.AsyncDeploymentJobExecutor;
import org.kie.remote.services.rest.exception.RestOperationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger logger = LoggerFactory.getLogger(DeploymentResource.class);
    
    /* REST information */
    @Context
    private HttpHeaders headers;
    
    @Context
    private UriInfo uriInfo;
    
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
    
    protected KModuleDeploymentUnit createDeploymentUnit(String deploymentId) { 
        String [] gavKK = deploymentId.split(":");
        KModuleDeploymentUnit deployUnit = new KModuleDeploymentUnit(gavKK[0], gavKK[1], gavKK[2]);
        if( gavKK.length > 3 ) { 
            deployUnit.setKbaseName(gavKK[3]);
        }
        if( gavKK.length > 4 ) { 
            deployUnit.setKsessionName(gavKK[4]);
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
        String [] gavKK;
        if( deployedUnit == null ) { 
            logger.debug("No deployment unit found, getting status");
            JaxbDeploymentStatus status = jobExecutor.getStatus(deploymentId);
            switch(status) { 
            case NONEXISTENT:
                gavKK = deploymentId.split(":");
                jaxbDepUnit = new JaxbDeploymentUnit(gavKK[0], gavKK[1], gavKK[2]);
                jaxbDepUnit.setStatus(status);
                logger.info("Deployment " + deploymentId + " does not exist.");
                return createCorrectVariant(jaxbDepUnit, headers, Status.NOT_FOUND);
            case DEPLOYING:
            case UNDEPLOYING:
            case DEPLOY_FAILED:
            case UNDEPLOY_FAILED: 
            case UNDEPLOYED:
                gavKK = deploymentId.split(":");
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
            default: 
                throw new KieRemoteServicesInternalError("Unknown deployment status (" + status.toString() + "), contact the developers.");
            }
        } else { 
            jaxbDepUnit = extractDeploymentUnit(deployedUnit);
            jaxbDepUnit.setStatus(JaxbDeploymentStatus.DEPLOYED);
        }
        
        logger.debug("Returning deployment unit information for " + deploymentId);
        return createCorrectVariant(jaxbDepUnit, headers);
    }
    
    private JaxbDeploymentUnit extractDeploymentUnit(DeployedUnit deployedUnit) { 
        KModuleDeploymentUnit depUnit = (KModuleDeploymentUnit) deployedUnit.getDeploymentUnit();
        return convertKModuleDepUnitToJaxbDepUnit(depUnit);
    }
    
    @POST
    @Path("/deploy")
    public Response deploy() { 
        Map<String, List<String>> params = getRequestParams(uriInfo);
        String oper = getRelativePath(uriInfo);
        String strategy = getStringParam("strategy", false, params, oper);

        KModuleDeploymentUnit deploymentUnit = createDeploymentUnit(deploymentId);
       
        if( strategy != null ) { 
            strategy = strategy.toUpperCase();
            RuntimeStrategy runtimeStrategy;
            try { 
                runtimeStrategy = RuntimeStrategy.valueOf(strategy);
            } catch( IllegalArgumentException iae ) { 
                throw RestOperationException.badRequest("Runtime strategy '" + strategy + "' does not exist.");
            }
            deploymentUnit.setStrategy(runtimeStrategy);
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
        JaxbDeploymentJobResult jobResult; 
        if( deployedUnit != null ) { 
            KModuleDeploymentUnit deploymentUnit = (KModuleDeploymentUnit) deployedUnit.getDeploymentUnit();

            jobResult = jobExecutor.submitUndeployJob(deploymentService, deploymentUnit);
            jobResult.getDeploymentUnit().setStatus(JaxbDeploymentStatus.UNDEPLOYING);
        } else { 
            String [] gavKK = deploymentId.split(":");
            JaxbDeploymentUnit depUnit;
            switch( gavKK.length ) { 
            case 3:
                depUnit = new JaxbDeploymentUnit(gavKK[0], gavKK[1], gavKK[2]);
                break;
            case 4:
                depUnit = new JaxbDeploymentUnit(gavKK[0], gavKK[1], gavKK[2], gavKK[3], null);
                break;
            case 5:
                depUnit = new JaxbDeploymentUnit(gavKK[0], gavKK[1], gavKK[2], gavKK[3], gavKK[4]);
                break;
            default:
                throw new IllegalStateException("Invalid deployment id: " + deploymentId);
            }
            jobResult = new JaxbDeploymentJobResult("Deployment unit has already been undeployed.", true, depUnit, "UNDEPLOY" );
        }
        return createCorrectVariant(jobResult, headers, Status.ACCEPTED);
    }
}
