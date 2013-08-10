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

public class DatasourceServerSetupTask extends AbstractServerSetupTask implements ServerSetupTask {


    private static Logger logger = LoggerFactory.getLogger(DatasourceServerSetupTask.class);

    public static final String JBPM_DS_XML = "/jbpm-ds.xml";


    @Override
    public void setup(ManagementClient managementClient, String containerId) throws Exception {
        deployResource(managementClient, containerId, JBPM_DS_XML, "Deploying jbpm datasource");
    }

    @Override
    public void tearDown(ManagementClient managementClient, String containerId) throws Exception {
        undeployResource(managementClient, containerId, JBPM_DS_XML, "Undeployed jbpm datasource");
    }

}
