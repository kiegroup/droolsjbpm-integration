package org.kie.remote.services.rest;

import static org.kie.remote.services.rest.async.cmd.DeploymentCmd.DEPLOYMENT_UNIT;
import static org.kie.remote.services.rest.async.cmd.DeploymentCmd.JOB_ID;
import static org.kie.remote.services.rest.async.cmd.DeploymentCmd.JOB_TYPE;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.jbpm.kie.services.impl.KModuleDeploymentService;
import org.jbpm.kie.services.impl.KModuleDeploymentUnit;
import org.jbpm.runtime.manager.impl.deploy.DeploymentDescriptorImpl;
import org.jbpm.services.api.DefinitionService;
import org.jbpm.services.api.DeploymentService;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.model.DeployedUnit;
import org.jbpm.services.api.model.ProcessDefinition;
import org.jbpm.services.cdi.Kjar;
import org.kie.internal.executor.api.CommandContext;
import org.kie.internal.executor.api.ExecutorService;
import org.kie.internal.query.QueryContext;
import org.kie.internal.runtime.conf.DeploymentDescriptor;
import org.kie.internal.runtime.conf.MergeMode;
import org.kie.internal.runtime.conf.RuntimeStrategy;
import org.kie.remote.services.cdi.DeploymentInfoBean;
import org.kie.remote.services.rest.async.JobResultManager;
import org.kie.remote.services.rest.async.cmd.DeploymentCmd;
import org.kie.remote.services.rest.async.cmd.JobType;
import org.kie.remote.services.rest.exception.KieRemoteRestOperationException;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentDescriptor;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentJobResult;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentUnit;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentUnit.JaxbDeploymentStatus;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentUnitList;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessDefinition;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessDefinitionList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Singleton
public class DeployResourceBase extends ResourceBase {

    private static final Logger logger = LoggerFactory.getLogger(DeployResourceBase.class);
    
    /* KIE information and processing */
   
    @Inject
    @Kjar
    private KModuleDeploymentService deploymentService;

    @Inject
    private DeploymentInfoBean deploymentInfoBean;
   
    @Inject
    private RuntimeDataService runtimeDataService;
 
    @Inject
    private DefinitionService bpmn2DataService;
    
    /* Async */
    
    @Inject
    private ExecutorService jobExecutor;
    
    private final AtomicLong jobIdGen = new AtomicLong(0);
    
    @Inject
    private JobResultManager jobResultMgr;
    
    // Helper methods ------------------------------------------------------------------------------------------------------------
    
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
   
    /**
     * Create a {@link KModuleDeploymentUnit} instance using the given information
     * @param deploymentId The deployment id
     * @param descriptor The optional {@link JaxbDeploymentDescriptor} instance with additional information
     * @return The {@link KModuleDeploymentUnit} instance
     */
    // pkg scope for tests
    static KModuleDeploymentUnit createDeploymentUnit(String deploymentId, JaxbDeploymentDescriptor descriptor) {
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
        depDescImpl.setClasses(jaxbDepDesc.getClasses());
        
        return depDescImpl;
    }
  
    // Deployment methods ---------------------------------------------------------------------------------------------------------
    
    /**
     * Determines the status of a deployment
     * @param checkDeploymentService Whether or not to use the {@link DeploymentService} when checking the status
     * @return A {@link JaxbDeploymentUnit} representing the status
     */
    public JaxbDeploymentUnit determineStatus(String deploymentId, boolean checkDeploymentService) { 
        
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
            throw KieRemoteRestOperationException.notFound("Invalid deployment id: " + deploymentId);
        }
        jaxbDepUnit.setStatus(JaxbDeploymentStatus.NONEXISTENT);
        return jaxbDepUnit;
    }

    public JaxbDeploymentJobResult submitDeployJob(String deploymentId, String strategy, String mergeMode, JaxbDeploymentDescriptor deployDescriptor ) { 
        JaxbDeploymentJobResult jobResult;
        DeployedUnit deployedUnit = deploymentService.getDeployedUnit(deploymentId);
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

            KModuleDeploymentUnit deploymentUnit = createDeploymentUnit(deploymentId, deployDescriptor);

            if( strategy != null ) { 
                strategy = strategy.toUpperCase();
                RuntimeStrategy runtimeStrategy;
                try { 
                    runtimeStrategy = RuntimeStrategy.valueOf(strategy);
                } catch( IllegalArgumentException iae ) { 
                    throw KieRemoteRestOperationException.badRequest("Runtime strategy '" + strategy + "' does not exist.");
                }
                deploymentUnit.setStrategy(runtimeStrategy);
            }
            if (mergeMode != null) {
                mergeMode = mergeMode.toUpperCase();
                MergeMode mode;
                try {
                    mode = MergeMode.valueOf(mergeMode);
                }  catch( IllegalArgumentException iae ) {
                    throw KieRemoteRestOperationException.badRequest("Merge mode '" + mergeMode + "' does not exist.");
                }
                deploymentUnit.setMergeMode(mode);
            }

            jobResult = scheduleDeploymentJobRequest(deploymentId, JobType.DEPLOY, deploymentUnit);
        }
        return jobResult;
    }
    
    /**
     * Schedules a deploy or undeploy job with the jbpm-executor for execution.
     * @param jobType The type of job: deploy or undeploy
     * @param deploymentUnit The deployment unit that should be acted upon
     * @return The initial status of the job in a {@link JaxbDeploymentJobResult} instance
     */
    private JaxbDeploymentJobResult scheduleDeploymentJobRequest(String deploymentId, JobType jobType, KModuleDeploymentUnit deploymentUnit) { 
        CommandContext ctx = new CommandContext();
        ctx.setData(DEPLOYMENT_UNIT,  deploymentUnit);
        ctx.setData(JOB_TYPE, jobType);
        ctx.setData("businessKey", deploymentId);
        ctx.setData("retries", 0);
	ctx.setData("owner", ExecutorService.EXECUTOR_ID);
       
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
   
    public JaxbDeploymentJobResult submitUndeployJob(String deploymentId) { 
        DeployedUnit deployedUnit = deploymentService.getDeployedUnit(deploymentId);
        JaxbDeploymentJobResult jobResult; 
        if( deployedUnit != null ) { 
            KModuleDeploymentUnit deploymentUnit = (KModuleDeploymentUnit) deployedUnit.getDeploymentUnit();
            jobResult = scheduleDeploymentJobRequest(deploymentId, JobType.UNDEPLOY, deploymentUnit);
        } else { 
            JaxbDeploymentUnit depUnit = determineStatus(deploymentId, false);
           
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
        return jobResult;
    }

    public JaxbDeploymentUnitList getDeploymentList(int [] pageInfo, int maxNumResults) { 
        List<String> deploymentIds = new ArrayList<String>(deploymentInfoBean.getDeploymentIds());
        Collections.sort(deploymentIds);

        JaxbDeploymentUnitList jaxbDepUnitList = new JaxbDeploymentUnitList();
        List<JaxbDeploymentUnit> depUnitList = jaxbDepUnitList.getDeploymentUnitList();
        for( String deploymentId : deploymentIds ) { 
            DeployedUnit deployedUnit = deploymentService.getDeployedUnit(deploymentId);
            if( deployedUnit != null ) { 
                JaxbDeploymentUnit jaxbDepUnit = convertKModuleDepUnitToJaxbDepUnit((KModuleDeploymentUnit) deployedUnit.getDeploymentUnit());
                jaxbDepUnit.setStatus(JaxbDeploymentStatus.DEPLOYED);
                depUnitList.add(jaxbDepUnit);
                if( depUnitList.size() >= maxNumResults) { 
                    // pagination parameters indicate that no more than current list is needed
                    break;
                }
            }
        }

        JaxbDeploymentUnitList resultList = paginateAndCreateResult(pageInfo, depUnitList, new JaxbDeploymentUnitList());
        return resultList;
    }
    
    public JaxbProcessDefinitionList getProcessDefinitionList(int [] pageInfo, int maxNumResults) { 
        List<String> deploymentIds = new ArrayList<String>(deploymentInfoBean.getDeploymentIds());
        Collections.sort(deploymentIds);
        
        JaxbProcessDefinitionList jaxbProcDefList = new JaxbProcessDefinitionList();
        List<JaxbProcessDefinition> procDefList = jaxbProcDefList.getProcessDefinitionList();
        for( String deploymentId : deploymentIds ) {
            fillProcessDefinitionList(deploymentId, pageInfo, maxNumResults, procDefList);
                
            if( procDefList.size() == maxNumResults) { 
                // pagination parameters indicate that no more than current list is needed
                break;
            }
        }
       
        JaxbProcessDefinitionList resultList = paginateAndCreateResult(pageInfo, procDefList, new JaxbProcessDefinitionList());
        return resultList;
    }
   
    public void fillProcessDefinitionList(String deploymentId, int [] pageInfo, int maxNumResults, List<JaxbProcessDefinition> procDefList) { 
        List<String> processIdList = Collections.EMPTY_LIST;
        try { 
            processIdList = new ArrayList<String>(runtimeDataService.getProcessIds(deploymentId, new QueryContext(pageInfo[0], pageInfo[1])));
            Collections.sort(processIdList);
        } catch( Exception e) { 
            // possibly because the deployment is being modified and not fully un/deployed.. (un/deploy*ing*) 
            logger.debug( "Unable to retrieve process ids for deployment '{}': {}", deploymentId, e.getMessage(), e);
        }
        for( String processId : processIdList ) { 
            ProcessDefinition processAssetDesc;
            try { 
                processAssetDesc = runtimeDataService.getProcessesByDeploymentIdProcessId(deploymentId, processId); 
                if( processAssetDesc == null ) { 
                    logger.error( "No process definition information available for process definition '{}' in deployment '{}'!", 
                            processId, deploymentId); 
                    continue;
                }
            } catch( Exception e ) {
                // possibly because the deployment is being modified and not fully un/deployed.. (un/deploy*ing*) 
                logger.debug( "Unable to retrieve process definition for process '{}' in deployment '{}': {}", 
                        processId, deploymentId, e.getMessage(), e);
                continue; 
            }
            JaxbProcessDefinition jaxbProcDef = convertProcAssetDescToJaxbProcDef(processAssetDesc);
            Map<String, String> variables; 
            try { 
                variables = bpmn2DataService.getProcessVariables(deploymentId, processId);
            } catch( Exception e) { 
                // possibly because the deployment is being modified and not fully un/deployed.. (un/deploy*ing*) 
                logger.debug( "Unable to retrieve process definition data for process '{}' in deployment '{}': {}", 
                        processId, deploymentId, e.getMessage(), e);
                continue; 
            }
            jaxbProcDef.setVariables(variables);
            procDefList.add(jaxbProcDef);

            if( procDefList.size() == maxNumResults) { 
                // pagination parameters indicate that no more than current list is needed
                break;
            }
        }
    }
}
