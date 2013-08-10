package org.kie.services.remote.setup;

import org.jboss.as.arquillian.api.ServerSetupTask;
import org.jboss.as.arquillian.container.ManagementClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JmsQueueServerSetupTask extends AbstractServerSetupTask implements ServerSetupTask {


    private static Logger logger = LoggerFactory.getLogger(JmsQueueServerSetupTask.class);

    public static final String HORNETQ_JMS_XML = "/hornetq-jms.xml";


    @Override
    public void setup(ManagementClient managementClient, String containerId) throws Exception {
        deployResource(managementClient, containerId, HORNETQ_JMS_XML, "Deploying jms Queues");
    }

    @Override
    public void tearDown(ManagementClient managementClient, String containerId) throws Exception {
        undeployResource(managementClient, containerId, HORNETQ_JMS_XML, "Undeployed jms queues");
    }

}
