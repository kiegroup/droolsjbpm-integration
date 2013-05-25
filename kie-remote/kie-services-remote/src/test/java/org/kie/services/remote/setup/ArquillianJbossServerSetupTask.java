package org.kie.services.remote.setup;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.jboss.as.arquillian.api.ServerSetupTask;
import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.as.controller.client.helpers.standalone.DeploymentPlan;
import org.jboss.as.controller.client.helpers.standalone.ServerDeploymentActionResult;
import org.jboss.as.controller.client.helpers.standalone.ServerDeploymentManager;
import org.jboss.as.controller.client.helpers.standalone.ServerDeploymentPlanResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ArquillianJbossServerSetupTask implements ServerSetupTask {

    protected static Properties arquillianLaunchProperties = getArquillianLaunchProperties();

    private static Logger logger = LoggerFactory.getLogger(ArquillianJbossServerSetupTask.class);

    public static final String HORNETQ_JMS_XML = "/hornetq-jms.xml";
    public static final String JBPM_DS_XML = "/jbpm-ds.xml";

    protected static Properties getArquillianLaunchProperties() {
        Properties properties = new Properties();
        try {
            InputStream arquillianLaunchFile = ArquillianJbossServerSetupTask.class.getResourceAsStream("/arquillian.launch");
            properties.load(arquillianLaunchFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return properties;
    }

    @Override
    public void setup(ManagementClient managementClient, String containerId) throws Exception {
        logger.info("Deploying JMS Queues");

        URL hornetqJmsXmlUrl = this.getClass().getResource(HORNETQ_JMS_XML);
        URL jbpmDSXmlUrl = this.getClass().getResource(JBPM_DS_XML);

        URL[] urls = { hornetqJmsXmlUrl, jbpmDSXmlUrl };

        for (URL url : urls) {
            ServerDeploymentManager manager = ServerDeploymentManager.Factory.create(managementClient.getControllerClient());
            DeploymentPlan plan = manager.newDeploymentPlan().add(url).andDeploy().build();

            Future<ServerDeploymentPlanResult> future = manager.execute(plan);
            ServerDeploymentPlanResult result = future.get(10, TimeUnit.SECONDS);
            ServerDeploymentActionResult actionResult = result.getDeploymentActionResult(plan.getId());
            if (actionResult != null) {
                if (actionResult.getDeploymentException() != null) {
                    throw new RuntimeException(actionResult.getDeploymentException());
                }
            }
        }
    }

    @Override
    public void tearDown(ManagementClient managementClient, String containerId) throws Exception {
        ServerDeploymentManager manager = ServerDeploymentManager.Factory.create(managementClient.getControllerClient());
        DeploymentPlan undeployPlan = manager.newDeploymentPlan().undeploy(HORNETQ_JMS_XML).undeploy(JBPM_DS_XML)
                .andRemoveUndeployed().build();

        manager.execute(undeployPlan).get();
    }

}
