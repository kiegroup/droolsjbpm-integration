/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.server.integrationtests.common;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieScannerResource;
import org.kie.server.api.model.KieScannerStatus;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.integrationtests.shared.KieServerAssert;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.kie.server.integrationtests.shared.basetests.RestJmsSharedBaseIntegrationTest;

public class KieServerContainerNonExistingDependency extends RestJmsSharedBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "not-existing-dependency-kjar", "1.0.0");

    @BeforeClass
    public static void initialize() throws Exception {
        // Uncomment in first run, then comment back. 
//        KieServerDeployer.buildAndDeployCommonMavenParent();
//        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/really-not-existing").getFile());
//        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/not-existing-dependency-kjar").getFile());
    }

    @Before
    public void setupKieServer() throws Exception {
        disposeAllContainers();
    }

    @Test
    public void testCreateContainerSpecial() throws Exception {
        ServiceResponse<KieContainerResource> reply = client.createContainer("kie1", new KieContainerResource("kie1", releaseId));
        KieServerAssert.assertSuccess(reply);

        ServiceResponse<KieScannerResource> si = client.updateScanner("kie1", new KieScannerResource(KieScannerStatus.STARTED, 10000l));
        KieServerAssert.assertSuccess(si);
    }
}
