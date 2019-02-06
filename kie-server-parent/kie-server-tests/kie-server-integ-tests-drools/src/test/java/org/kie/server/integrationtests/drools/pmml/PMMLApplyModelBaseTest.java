package org.kie.server.integrationtests.drools.pmml;

import org.junit.BeforeClass;
import org.kie.api.KieServices;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.RuleServicesClient;
import org.kie.server.integrationtests.shared.basetests.RestJmsSharedBaseIntegrationTest;


public abstract class PMMLApplyModelBaseTest extends RestJmsSharedBaseIntegrationTest {

    protected RuleServicesClient ruleClient;

    @BeforeClass
    public static void setupFactory() throws Exception {
        commandsFactory = KieServices.Factory.get().getCommands();
    }

    @Override
    protected void setupClients(KieServicesClient kieServicesClient) {
        this.ruleClient = kieServicesClient.getServicesClient(RuleServicesClient.class);
    }
}
