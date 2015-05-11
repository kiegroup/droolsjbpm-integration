package org.kie.server.integrationtests.jbpm;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.rules.ExternalResource;
import org.kie.server.integrationtests.shared.RestJmsSharedBaseIntegrationTest;

public class JbpmKieServerBaseIntegrationTest extends RestJmsSharedBaseIntegrationTest {


    @ClassRule
    public static ExternalResource StaticResource = new DBExternalResource();


    @Before
    public void cleanup() {
        cleanupSingletonSessionId();
    }
}
