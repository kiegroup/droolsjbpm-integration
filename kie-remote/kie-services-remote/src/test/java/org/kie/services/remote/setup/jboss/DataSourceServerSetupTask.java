package org.kie.services.remote.setup.jboss;

import static org.jboss.as.controller.descriptions.ModelDescriptionConstants.*;

import java.util.ArrayList;
import java.util.List;

import org.jboss.as.arquillian.container.ManagementClient;
import org.jboss.as.controller.client.ModelControllerClient;
import org.jboss.as.controller.client.OperationBuilder;
import org.jboss.dmr.ModelNode;
import org.kie.services.remote.setup.jboss.DataSource.DataSourceBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DataSourceServerSetupTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceServerSetupTask.class);
    private static final String SUBSYSTEM_DATASOURCES = "datasources";
    private static final String DATASOURCE = "data-source";

    // Protected configuration methods -----------------------------------------------------
    
    protected static DataSource[] getDataSourceConfigurations(final ManagementClient managementClient, String containerId) {
        DataSourceBuilder builder = new DataSourceBuilder();
        builder = builder.name("jdbc/jbpm-ds")
                .connectionUrl("jdbc:postgresql://localhost:5432/jbpm5")
                .username("jbpm5")
                .password("jbpm5")
                .driver("org.postgresql.Driver");
        
        builder = builder.name("jdbc/jbpm-ds")
                .connectionUrl("jdbc:h2:file:/tmp/arquillian-test")
                .username("sa")
                .password("sasa")
                .driver("org.h2.Driver");
        
        DataSource [] dataSources = new DataSource [1];
        dataSources[0] = builder.build();
        return dataSources;
    }

    // Public methods --------------------------------------------------------
    
    /**
     * Adds a security domain represented by this class to the AS configuration.
     * 
     * @param managementClient
     * @param containerId
     * @throws Exception
     * @see org.jboss.as.arquillian.api.ServerSetupTask#setup(org.jboss.as.arquillian.container.ManagementClient,
     *      java.lang.String)
     */
    public static void setupDataSource(final ManagementClient managementClient, String containerId) throws Exception {
        final DataSource[] dataSourceConfigurations = getDataSourceConfigurations(managementClient, containerId);

        if (dataSourceConfigurations == null) {
            LOGGER.warn("Null DataSourceConfiguration array provided");
            return;
        }

        final List<ModelNode> updates = new ArrayList<ModelNode>();
        
        for (final DataSource config : dataSourceConfigurations) {
            final String name = config.getName();
            LOGGER.info("Adding datasource " + name);
            final ModelNode dsNode = new ModelNode();
            dsNode.get(OP).set(ADD);
            dsNode.get(OP_ADDR).add(SUBSYSTEM, SUBSYSTEM_DATASOURCES);
            dsNode.get(OP_ADDR).add(DATASOURCE, name);

            dsNode.get("connection-url").set(config.getConnectionUrl());
            dsNode.get("jndi-name").set(config.getJndiName());
            dsNode.get("driver-name").set(config.getDriver());
            if (isNotEmpty(config.getUsername())) {
                dsNode.get("user-name").set(config.getUsername());
            }
            if (isNotEmpty(config.getPassword())) {
                dsNode.get("password").set(config.getPassword());
            }
            updates.add(dsNode);
            final ModelNode enableNode = new ModelNode();
            enableNode.get(OP).set(ENABLE);
            enableNode.get(OP_ADDR).add(SUBSYSTEM, SUBSYSTEM_DATASOURCES);
            enableNode.get(OP_ADDR).add(DATASOURCE, name);
            updates.add(enableNode);
        }
        applyUpdates(updates, managementClient.getControllerClient());
    }

    /**
     * Removes the security domain from the AS configuration.
     * 
     * @param managementClient
     * @param containerId
     * @see org.jboss.as.test.integration.security.common.AbstractSecurityDomainSetup#tearDown(org.jboss.as.arquillian.container.ManagementClient,
     *      java.lang.String)
     */
    public static void tearDownDataSource(ManagementClient managementClient, String containerId) throws Exception {
        final DataSource[] dataSourceConfigurations = getDataSourceConfigurations(managementClient, containerId);
        if (dataSourceConfigurations == null) {
            LOGGER.warn("Null DataSourceConfiguration array provided");
            return;
        }
        final List<ModelNode> updates = new ArrayList<ModelNode>();
        for (final DataSource config : dataSourceConfigurations) {
            final String name = config.getName();
            LOGGER.info("Removing datasource " + name);
            final ModelNode op = new ModelNode();
            op.get(OP).set(REMOVE);
            op.get(OP_ADDR).add(SUBSYSTEM, SUBSYSTEM_DATASOURCES);
            op.get(OP_ADDR).add(DATASOURCE, name);

            updates.add(op);
        }

        applyUpdates(updates, managementClient.getControllerClient());
    }

    // Protected helper methods -----------------------------------------------------
    
    protected static void applyUpdates(List<ModelNode> updates, final ModelControllerClient client) throws Exception {
        for (ModelNode update : updates) {
            ModelNode result = client.execute(new OperationBuilder(update).build());
            if (LOGGER.isInfoEnabled()) {
                LOGGER.info("Client update: " + update);
                LOGGER.info("Client update result: " + result);
            }
            if (result.hasDefined("outcome") && "success".equals(result.get("outcome").asString())) {
                LOGGER.debug("Operation succeeded.");
            } else if (result.hasDefined("failure-description")) {
                throw new RuntimeException(result.get("failure-description").toString());
            } else {
                throw new RuntimeException("Operation not successful; outcome = " + result.get("outcome"));
            }
        }
    }

    protected static boolean isNotEmpty(String str) {
        return ! (str == null || str.length() == 0);
    }
    
}