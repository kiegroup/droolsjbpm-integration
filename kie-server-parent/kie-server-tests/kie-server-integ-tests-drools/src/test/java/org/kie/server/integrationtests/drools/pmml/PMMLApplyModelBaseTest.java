package org.kie.server.integrationtests.drools.pmml;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.kie.api.KieServices;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.RuleServicesClient;
import org.kie.server.integrationtests.shared.basetests.RestJmsSharedBaseIntegrationTest;

import static org.kie.api.pmml.PMMLConstants.KIE_PMML_IMPLEMENTATION;
import static org.kie.api.pmml.PMMLConstants.LEGACY;

public abstract class PMMLApplyModelBaseTest extends RestJmsSharedBaseIntegrationTest {

    protected RuleServicesClient ruleClient;

    @BeforeClass
    public static void setupFactory() throws Exception {
        System.setProperty(KIE_PMML_IMPLEMENTATION.getName(), LEGACY.getName());
        commandsFactory = KieServices.Factory.get().getCommands();
    }

    @AfterClass
    public static void clearProperty() {
        System.clearProperty(KIE_PMML_IMPLEMENTATION.getName());
    }

    @Override
    protected void setupClients(KieServicesClient kieServicesClient) {
        this.ruleClient = kieServicesClient.getServicesClient(RuleServicesClient.class);
    }
}
