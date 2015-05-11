package org.kie.server.integrationtests.jbpm;

import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.client.KieServicesException;

import static org.junit.Assert.*;

public class ProcessServiceIntegrationTest extends JbpmKieServerBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "definition-project",
            "1.0.0.Final");


    @BeforeClass
    public static void buildAndDeployArtifacts() {

        buildAndDeployCommonMavenParent();
        buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/definition-project").getFile());
    }


    @Test
    public void testStartAndAbortProcess() {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));
        Long result = client.startProcess("definition-project", "definition-project.evaluation", null);

        assertNotNull(result);
        assertTrue(result.longValue() > 0);

        client.abortProcessInstance("definition-project", result);

    }

    @Test(expected = KieServicesException.class)
    public void testStartNotExistingProcess() {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));
        client.startProcess("definition-project", "not-existing", null);
    }

    @Test(expected = KieServicesException.class)
    public void testAbortExistingProcess() {
        assertSuccess(client.createContainer("definition-project", new KieContainerResource("definition-project", releaseId)));
        client.abortProcessInstance("definition-project", 9999l);
    }
}
