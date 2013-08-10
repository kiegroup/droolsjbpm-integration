package org.kie.services.remote.setup;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.jboss.as.arquillian.api.ServerSetupTask;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.as.controller.client.helpers.standalone.DeploymentPlan;
import org.jboss.as.controller.client.helpers.standalone.ServerDeploymentActionResult;
import org.jboss.as.controller.client.helpers.standalone.ServerDeploymentManager;
import org.jboss.as.controller.client.helpers.standalone.ServerDeploymentPlanResult;
import org.jboss.as.controller.client.helpers.standalone.ServerUpdateActionResult.Result;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AbstractServerSetupTask {

    protected static Properties arquillianLaunchProperties = getArquillianLaunchProperties();

    private static Logger logger = LoggerFactory.getLogger(AbstractServerSetupTask.class);

    protected static Properties getArquillianLaunchProperties() {
        Properties properties = new Properties();
        try {
            InputStream arquillianLaunchFile = AbstractServerSetupTask.class.getResourceAsStream("/arquillian.launch");
            properties.load(arquillianLaunchFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return properties;
    }

    public void deployResource(ManagementClient managementClient, String containerId, String resource, String msg) throws Exception {
        URL resourceUrl = this.getClass().getResource(resource);

        URL[] urls = { resourceUrl };

        for (URL url : urls) {
            ServerDeploymentManager manager = ServerDeploymentManager.Factory.create(managementClient.getControllerClient());
            DeploymentPlan plan = manager.newDeploymentPlan().add(url).andDeploy().build();

            ServerDeploymentActionResult actionResult = executePlanAndGetResult(manager, plan);
            if (actionResult != null) {
                if (actionResult.getDeploymentException() != null) {
                    throw new RuntimeException(actionResult.getDeploymentException());
                } 
                logger.info(msg + " [status: " + actionResult.getResult() + "]");
            }
        }
    }

    public void undeployResource(ManagementClient managementClient, String containerId, String resource, String msg) throws Exception {
        ServerDeploymentManager manager = ServerDeploymentManager.Factory.create(managementClient.getControllerClient());
        DeploymentPlan undeployPlan = manager.newDeploymentPlan().undeploy(resource).andRemoveUndeployed().build();

        ServerDeploymentActionResult actionResult = executePlanAndGetResult(manager, undeployPlan);

        if( actionResult != null ) { 
            Result undeployResult = actionResult.getResult();
            logger.info(msg + " [status: " + undeployResult + "]");
        }
    }

    private ServerDeploymentActionResult executePlanAndGetResult(ServerDeploymentManager manager, DeploymentPlan plan) throws Exception {
        Future<ServerDeploymentPlanResult> future = manager.execute(plan);
        ServerDeploymentPlanResult planResult = future.get(10, TimeUnit.SECONDS);
        ServerDeploymentActionResult actionResult = planResult.getDeploymentActionResult(plan.getId());
        return actionResult;
    }
}
