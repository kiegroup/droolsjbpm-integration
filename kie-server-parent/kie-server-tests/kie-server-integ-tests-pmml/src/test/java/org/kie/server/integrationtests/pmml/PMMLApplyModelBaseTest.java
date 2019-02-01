package org.kie.server.integrationtests.pmml;

import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.RuleServicesClient;
import org.kie.server.integrationtests.shared.basetests.RestJmsSharedBaseIntegrationTest;


public abstract class PMMLApplyModelBaseTest extends RestJmsSharedBaseIntegrationTest {

    protected RuleServicesClient rulesClient;

    protected RuleServicesClient createRuleServicesClient() throws Exception {
        KieServicesConfiguration cfg = createKieServicesRestConfiguration();
        this.rulesClient = createDefaultClient(cfg, MarshallingFormat.JAXB).getServicesClient(RuleServicesClient.class);
        return this.rulesClient;
    }
}
