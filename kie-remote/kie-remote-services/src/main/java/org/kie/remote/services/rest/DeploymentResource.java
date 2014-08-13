package org.kie.remote.services.rest;

import static org.kie.remote.services.rest.async.cmd.DeploymentCmd.DEPLOYMENT_UNIT;
import static org.kie.remote.services.rest.async.cmd.DeploymentCmd.JOB_ID;
import static org.kie.remote.services.rest.async.cmd.DeploymentCmd.JOB_TYPE;

import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jbpm.kie.services.impl.KModuleDeploymentService;
import org.jbpm.kie.services.impl.KModuleDeploymentUnit;
import org.jbpm.runtime.manager.impl.deploy.DeploymentDescriptorImpl;
import org.jbpm.services.api.DefinitionService;
import org.jbpm.services.api.DeploymentService;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.model.DeployedUnit;
import org.jbpm.services.api.model.DeploymentUnit;
import org.jbpm.services.cdi.Kjar;
import org.kie.internal.executor.api.CommandContext;
import org.kie.internal.executor.api.ExecutorService;
import org.kie.internal.runtime.conf.DeploymentDescriptor;
import org.kie.internal.runtime.conf.MergeMode;
import org.kie.internal.runtime.conf.RuntimeStrategy;
import org.kie.remote.common.exception.RestOperationException;
import org.kie.remote.services.rest.async.JobResultManager;
import org.kie.remote.services.rest.async.cmd.DeploymentCmd;
import org.kie.remote.services.rest.async.cmd.JobType;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentDescriptor;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentJobResult;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentUnit;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentUnit.JaxbDeploymentStatus;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessDefinitionList;
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
    
    @PathParam("deploymentId")
    private String deploymentId;
    
    /* KIE information and processing */
   
    @Inject
    @Kjar
    private KModuleDeploymentService deploymentService;
  
    @Inject
    private RuntimeDataService runtimeDataService;
    
    @Inject
    private DefinitionService bpmn2DataService;
    
    /* Async */
    
    @Inject
    private ExecutorService jobExecutor;
    
    @Inject
    private JobResultManager jobResultMgr;
    
    private final AtomicLong jobIdGen = new AtomicLong(0);
   
    // Helper methods ------------------------------------------------------------------------------------------------------------
   
    /**
     * Create a {@link KModuleDeploymentUnit} instance using the given information
     * @param deploymentId The deployment id
     * @param descriptor The optional {@link JaxbDeploymentDescriptor} instance with additional information
     * @return The {@link KModuleDeploymentUnit} instance
     */
    protected KModuleDeploymentUnit createDeploymentUnit(String deploymentId, JaxbDeploymentDescriptor descriptor) {
        String [] gavKK = deploymentId.split(":");
        KModuleDeploymentUnit deployUnit = new KModuleDeploymentUnit(gavKK[0], gavKK[1], gavKK[2]);
        if( gavKK.length > 3 ) { 
            deployUnit.setKbaseName(gavKK[3]);
        }
        if( gavKK.length > 4 ) { 
            deployUnit.setKsessionName(gavKK[4]);
        }
        if (descriptor != null) {
            DeploymentDescriptor realDepDesc = convertToDeploymentDescriptor(descriptor);
            deployUnit.setDeploymentDescriptor(realDepDesc);
        }
        return deployUnit;
    }

    /**
     * Convert the received {@link JaxbDeploymentDescriptor} instance to a {@link DeploymentDescriptor} instance
     * that the {@link DeploymentService} can process.
     * @param jaxbDepDesc The received {@link JaxbDeploymentDescriptor} instance
     * @return A {@link DeploymentDescriptor} instance
     */
    private static DeploymentDescriptor convertToDeploymentDescriptor( JaxbDeploymentDescriptor jaxbDepDesc ) { 
        DeploymentDescriptorImpl depDescImpl = new DeploymentDescriptorImpl(jaxbDepDesc.getPersistenceUnit());
       
        depDescImpl.setAuditPersistenceUnit(jaxbDepDesc.getAuditPersistenceUnit());
        depDescImpl.setAuditMode(jaxbDepDesc.getAuditMode());
        depDescImpl.setPersistenceMode(jaxbDepDesc.getPersistenceMode());
        depDescImpl.setRuntimeStrategy(jaxbDepDesc.getRuntimeStrategy());
        depDescImpl.setMarshallingStrategies(jaxbDepDesc.getMarshallingStrategies());
        depDescImpl.setEventListeners(jaxbDepDesc.getEventListeners());
        depDescImpl.setTaskEventListeners(jaxbDepDesc.getTaskEventListeners());
        depDescImpl.setGlobals(jaxbDepDesc.getGlobals());
        depDescImpl.setWorkItemHandlers(jaxbDepDesc.getWorkItemHandlers());
        depDescImpl.setEnvironmentEntries(jaxbDepDesc.getEnvironmentEntries()); 
        depDescImpl.setConfiguration(jaxbDepDesc.getConfiguration()); 
        depDescImpl.setRequiredRoles(jaxbDepDesc.getRequiredRoles());
        
        return depDescImpl;
    }
   
    /**
     * Convert the {@link KModuleDeploymentUnit} instance from the {@link DeploymentService} 
     * to a {@link JaxbDeploymentUnit} usable by the REST service.
     * @param kDepUnit The {@link KModuleDeploymentUnit} instance
     * @return A {@link JaxbDeploymentUnit} instance
     */
    static JaxbDeploymentUnit convertKModuleDepUnitToJaxbDepUnit(KModuleDeploymentUnit kDepUnit ) { 
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

    /**
     * Retrieve the status of the {@link DeploymentUnit} specified in the URL.
     * @return A {@link JaxbDeploymentUnit} instance
     */
    @GET
    public Response getConfig() { 
        JaxbDeploymentUnit jaxbDepUnit = determineStatus(true);
        logger.debug("Returning deployment unit information for " + deploymentId);
        return createCorrectVariant(jaxbDepUnit, headers);
    }

    /**
     * Determines the status of a deployment
     * @param checkDeploymentService Whether or not to use the {@link DeploymentService} when checking the status
     * @return A {@link JaxbDeploymentUnit} representing the status
     */
    // pkg scope for tests
    JaxbDeploymentUnit determineStatus(boolean checkDeploymentService) { 
        
        JaxbDeploymentUnit jaxbDepUnit;
        if( checkDeploymentService ) { 
            DeployedUnit deployedUnit = deploymentService.getDeployedUnit(deploymentId);

            // Deployed
            if( deployedUnit != null ) {
                KModuleDeploymentUnit depUnit = (KModuleDeploymentUnit) deployedUnit.getDeploymentUnit();
                jaxbDepUnit = convertKModuleDepUnitToJaxbDepUnit(depUnit);
                jaxbDepUnit.setStatus(JaxbDeploymentStatus.DEPLOYED);
                return jaxbDepUnit;
            } 
        }
        
        // Most recent job? 
        JaxbDeploymentJobResult jobResult = jobResultMgr.getMostRecentJob(deploymentId);
        if( jobResult != null ) { 
            jaxbDepUnit = jobResult.getDeploymentUnit();
            return jaxbDepUnit;
        }
        
        // Nonexistent? 
        String [] gavKK = deploymentId.split(":");
        switch( gavKK.length ) { 
        case 3:
            jaxbDepUnit = new JaxbDeploymentUnit(gavKK[0], gavKK[1], gavKK[2]);
            break;
        case 4:
            jaxbDepUnit = new JaxbDeploymentUnit(gavKK[0], gavKK[1], gavKK[2], gavKK[3], null);
            break;
        case 5:
            jaxbDepUnit = new JaxbDeploymentUnit(gavKK[0], gavKK[1], gavKK[2], gavKK[3], gavKK[4]);
            break;
        default:
            throw RestOperationException.notFound("Invalid deployment id: " + deploymentId);
        }
        jaxbDepUnit.setStatus(JaxbDeploymentStatus.NONEXISTENT);
        return jaxbDepUnit;
    }
    
    /**
     * Queues a request to deploy the given deployment unit. If the deployment already exist, this
     * operation will fail. 
     * @param deployDescriptor An optional {@link DeploymentDescriptor} instance specifying additional information about how
     *                         the deployment unit should be deployed.
     * @return A {@link JaxbDeploymentJobResult} instance with the initial status of the job
     */
    @POST
    @Path("/deploy")
    public Response deploy(JaxbDeploymentDescriptor deployDescriptor) {
        DeployedUnit deployedUnit = deploymentService.getDeployedUnit(deploymentId);
        JaxbDeploymentJobResult jobResult; 
        if( deployedUnit != null ) { 
            // If the deployment unit already exists, request can not be completed..
            KModuleDeploymentUnit kDepUnit = (KModuleDeploymentUnit) deployedUnit.getDeploymentUnit();
            JaxbDeploymentUnit jaxbDepUnit = convertKModuleDepUnitToJaxbDepUnit(kDepUnit);
            jobResult = new JaxbDeploymentJobResult(
                    null, 
                    "The deployment already exists and must be first undeployed!", 
                   jaxbDepUnit, 
                   JobType.DEPLOY.toString());
            jobResult.setSuccess(false);
        } else { 
            // parse request/options and schedule deployment
            Map<String, String []> params = getRequestParams();
            String oper = getRelativePath();
            String strategy = getStringParam("strategy", false, params, oper);
            String mergeMode = getStringParam("mergemode", false, params, oper);

            KModuleDeploymentUnit deploymentUnit = createDeploymentUnit(deploymentId, deployDescriptor);

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
            if (mergeMode != null) {
                mergeMode = mergeMode.toUpperCase();
                MergeMode mode;
                try {
                    mode = MergeMode.valueOf(mergeMode);
                }  catch( IllegalArgumentException iae ) {
                    throw RestOperationException.badRequest("Merge mode '" + mergeMode + "' does not exist.");
                }
                deploymentUnit.setMergeMode(mode);
            }

            jobResult = scheduleDeploymentJobRequest(JobType.DEPLOY, deploymentUnit);
        }

        return createCorrectVariant(jobResult, headers, Status.ACCEPTED);
    }
   
    /**
     * Queues a request to undeploy the deployment unit specified in the URL
     * @return A {@link JaxbDeploymentJobResult} instance with the initial status of the job
     */
    @POST
    @Path("/undeploy")
    public Response undeploy() { 
        DeployedUnit deployedUnit = deploymentService.getDeployedUnit(deploymentId);
        JaxbDeploymentJobResult jobResult; 
        if( deployedUnit != null ) { 
            KModuleDeploymentUnit deploymentUnit = (KModuleDeploymentUnit) deployedUnit.getDeploymentUnit();
            jobResult = scheduleDeploymentJobRequest(JobType.UNDEPLOY, deploymentUnit);
        } else { 
            JaxbDeploymentUnit depUnit = determineStatus(false);
           
            String explanation;
            switch( depUnit.getStatus()) { 
            case ACCEPTED: // deployment service (above) has not found it, so it must be still deploying
            case DEPLOYED: // minor race condition between the deployment service and this code
            case DEPLOYING: // obvious.. 
                explanation = "The deployment can not be undeployed because the initial deployment has not yet fully completed.";
                break;
            case DEPLOY_FAILED: 
                explanation = "The deployment can not be undeployed because the initial deployment failed.";
                break;
            case NONEXISTENT:
            case UNDEPLOYED:
            case UNDEPLOYING:
                explanation = "The deployment can not be undeployed because it has already been undeployed (or is currently being undeployed)";
                break;
            case UNDEPLOY_FAILED: // from the last request
                explanation = "The last undeployment failed, but the deployment unit is no longer present (and can not be undeployed, thus). "
                        + "There is probably a very high load on this server. Turning on debugging may provide insight.";
                       logger.debug("Stack trace:", new Throwable()); 
                break;
            default: 
                throw new IllegalStateException("Unknown deployment unit status: " + depUnit.getStatus());
            }
            jobResult = new JaxbDeploymentJobResult(null, explanation, depUnit, JobType.UNDEPLOY.toString() );
            jobResult.setSuccess(false);
        }
        return createCorrectVariant(jobResult, headers, Status.ACCEPTED);
    }
   
    /**
     * Schedules a deploy or undeploy job with the jbpm-executor for execution.
     * @param jobType The type of job: deploy or undeploy
     * @param deploymentUnit The deployment unit that should be acted upon
     * @return The initial status of the job in a {@link JaxbDeploymentJobResult} instance
     */
    private JaxbDeploymentJobResult scheduleDeploymentJobRequest(JobType jobType, KModuleDeploymentUnit deploymentUnit) { 
        CommandContext ctx = new CommandContext();
        ctx.setData(DEPLOYMENT_UNIT,  deploymentUnit);
        ctx.setData(JOB_TYPE, jobType);
        ctx.setData("businessKey", deploymentId);
        ctx.setData("retries", 0);
       
        String jobTypeLower = jobType.toString().toLowerCase();
       
        String jobId = "" + System.currentTimeMillis() + "-" + jobIdGen.incrementAndGet();
        ctx.setData(JOB_ID, jobId);
        JaxbDeploymentJobResult jobResult = new JaxbDeploymentJobResult(
                jobId,
                jobTypeLower + " job accepted.", 
                convertKModuleDepUnitToJaxbDepUnit(deploymentUnit), jobType.toString());
        jobResult.getDeploymentUnit().setStatus(JaxbDeploymentStatus.ACCEPTED);

        logger.debug( "{} job [{}] for deployment '{}' created.", jobType.toString(), jobId, deploymentUnit.getIdentifier());
        jobResultMgr.putJob(jobResult.getJobId(), jobResult, jobType);
        Long executorJobId;
        try { 
            executorJobId = jobExecutor.scheduleRequest(DeploymentCmd.class.getName(), ctx);
            jobResult.setIdentifier(executorJobId);
            jobResult.setSuccess(true);
        } catch( Exception e ) { 
            String msg = "Unable to " + jobType.toString().toLowerCase() 
                    + " deployment '" + deploymentId + "': "
                    + e.getClass().getSimpleName() + " thrown [" + e.getMessage() + "]";
            logger.error( msg, e );
            jobResult.setExplanation(msg);
            jobResult.setSuccess(false);
        } 
                
        return jobResult;
    }
    
    @GET
    @Path("/processes")
    // DOCS: (+ pagination)
    public Response listProcessDefinitions() { 
        String oper = getRelativePath();
        Map<String, String[]> params = getRequestParams();
        int [] pageInfo = getPageNumAndPageSize(params, oper);
        int maxNumResults = getMaxNumResultsNeeded(pageInfo); 
        
        JaxbProcessDefinitionList jaxbProcDefList  = new JaxbProcessDefinitionList();
        fillProcessDefinitionList(deploymentId, pageInfo, maxNumResults, runtimeDataService, bpmn2DataService, 
                jaxbProcDefList.getProcessDefinitionList());
        JaxbProcessDefinitionList resultList 
            = paginateAndCreateResult(pageInfo, jaxbProcDefList.getProcessDefinitionList(), new JaxbProcessDefinitionList());
        return createCorrectVariant(resultList, headers);
    }
}
