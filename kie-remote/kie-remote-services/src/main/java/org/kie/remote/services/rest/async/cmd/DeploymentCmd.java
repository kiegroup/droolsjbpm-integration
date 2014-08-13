package org.kie.remote.services.rest.async.cmd;

import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;

import org.apache.deltaspike.core.api.provider.BeanManagerProvider;
import org.jbpm.executor.cdi.CDIUtils;
import org.jbpm.kie.services.impl.KModuleDeploymentUnit;
import org.jbpm.services.api.DeploymentService;
import org.jbpm.services.cdi.Kjar;
import org.kie.internal.executor.api.Command;
import org.kie.internal.executor.api.CommandContext;
import org.kie.internal.executor.api.ExecutionResults;
import org.kie.remote.services.rest.async.JobResultManager;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentJobResult;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentUnit.JaxbDeploymentStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This command is executed by the jbpm-executor asynchronously.
 * </p>
 * It contains the logic to deploy or undeploy a deployment 
 */
public class DeploymentCmd implements Command {

    private final static Logger logger = LoggerFactory.getLogger(DeploymentCmd.class);

    public final static String DEPLOYMENT_UNIT = "DeploymentUnit";
    public final static String JOB_TYPE = "JobType";
    public final static String JOB_ID = "JobId";
    
    @Override
    public ExecutionResults execute(CommandContext ctx) throws Exception {
        DeploymentService deploymentService = getDeploymentService(ctx);
        
        KModuleDeploymentUnit deploymentUnit = (KModuleDeploymentUnit) ctx.getData(DEPLOYMENT_UNIT);
        JobType jobType = (JobType) ctx.getData(JOB_TYPE);
        String deploymentId = deploymentUnit.getIdentifier();

        JobResultManager jobResultMgr = getJobManager(ctx);
        String jobId = (String) ctx.getData(JOB_ID);
        JaxbDeploymentJobResult jobResult = jobResultMgr.getJob(jobId);
        boolean success = false;
        switch (jobType) {
            case DEPLOY:
                try {
                    jobResult.getDeploymentUnit().setStatus(JaxbDeploymentStatus.DEPLOYING);
                    deploymentService.deploy(deploymentUnit);
                    jobResult.getDeploymentUnit().setStatus(JaxbDeploymentStatus.DEPLOYED);
                    jobResult.setSuccess(true);
                    logger.debug("Deployment unit [{}] deployed", deploymentId);
                    success = true;
                } catch (Exception e) {
                    jobResult.getDeploymentUnit().setStatus(JaxbDeploymentStatus.DEPLOY_FAILED);
                    jobResult.setSuccess(false);
                    logger.error("Unable to deploy [{}]", deploymentId, e);
                }
                break;
            case UNDEPLOY:
                try {
                    jobResult.getDeploymentUnit().setStatus(JaxbDeploymentStatus.UNDEPLOYING);
                    deploymentService.undeploy(deploymentUnit);
                    jobResult.getDeploymentUnit().setStatus(JaxbDeploymentStatus.UNDEPLOYED);
                    logger.debug("Deployment unit [{}] undeployed", deploymentId);
                    jobResult.setSuccess(false);
                    success = true;
                } catch (Exception e) {
                    jobResult.getDeploymentUnit().setStatus(JaxbDeploymentStatus.UNDEPLOY_FAILED);
                    jobResult.setSuccess(false);
                    logger.error("Unable to undeploy [{}]", deploymentId, e);
                }
                break;
            default:
                logger.error("Unknown " + JobType.class.getSimpleName() + " type (" + jobType.toString() + "), not taking any action");
        }
        ExecutionResults results = new ExecutionResults();
        results.setData("Result", success);
        return results;
    }
    
    @SuppressWarnings("serial")
    private DeploymentService getDeploymentService(CommandContext ctx) throws Exception {
        BeanManager beanManager = getBeanManager();
        return CDIUtils.createBean(DeploymentService.class, beanManager, new AnnotationLiteral<Kjar>(){});
    }
   
    private JobResultManager getJobManager(CommandContext ctx) throws Exception {
        BeanManager beanManager = getBeanManager();
        return CDIUtils.createBean(JobResultManager.class, beanManager);
    }
   
    private BeanManager getBeanManager() { 
        return BeanManagerProvider.getInstance().getBeanManager();
    } 
        
}
