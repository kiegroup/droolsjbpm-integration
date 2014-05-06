package org.kie.services.remote.rest.async;

import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;

import org.jbpm.executor.cdi.*;
import org.jbpm.kie.services.api.Kjar;
import org.jbpm.kie.services.impl.KModuleDeploymentUnit;
import org.kie.internal.deployment.DeploymentService;
import org.kie.internal.executor.api.Command;
import org.kie.internal.executor.api.CommandContext;
import org.kie.internal.executor.api.ExecutionResults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeploymentCmd implements Command {

    private final static Logger logger = LoggerFactory.getLogger(DeploymentCmd.class);

    @Override
    public ExecutionResults execute(CommandContext ctx) throws Exception {
        BeanManager beanManager = CDIUtils.lookUpBeanManager(ctx);

        DeploymentService deploymentService = CDIUtils.createBean(DeploymentService.class, beanManager, new AnnotationLiteral<Kjar>(){});
        KModuleDeploymentUnit deploymentUnit = (KModuleDeploymentUnit) ctx.getData("DeploymentUnit");
        JobType type = (JobType) ctx.getData("JobType");

        boolean success = false;
        switch (type) {
            case DEPLOY:
                try {
                    deploymentService.deploy(deploymentUnit);
                    logger.debug("Deployment unit [" + deploymentUnit.getIdentifier() + "] deployed");
                    success = true;
                } catch (Exception e) {
                    logger.error("Unable to deploy [" + deploymentUnit.getIdentifier() + "]", e);
                }
                break;
            case UNDEPLOY:
                try {
                    deploymentService.undeploy(deploymentUnit);
                    logger.debug("Deployment unit [" + deploymentUnit.getIdentifier() + "] undeployed");
                    success = true;
                } catch (Exception e) {
                    logger.error("Unable to undeploy [" + deploymentUnit.getIdentifier() + "]", e);
                }
                break;
            default:
                logger.error("Unknown " + JobType.class.getSimpleName() + " type (" + type.toString() + "), not taking any action");
        }
        ExecutionResults results = new ExecutionResults();
        results.setData("Result", success);
        return results;
    }
}
