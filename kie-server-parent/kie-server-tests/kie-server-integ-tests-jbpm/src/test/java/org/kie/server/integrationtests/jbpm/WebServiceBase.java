/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.integrationtests.jbpm;

import org.junit.BeforeClass;
import org.junit.experimental.categories.Category;
import org.kie.api.KieServices;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.integrationtests.category.JEEOnly;
import org.kie.server.integrationtests.shared.KieServerDeployer;

@Category({JEEOnly.class})
public class WebServiceBase extends JbpmKieServerBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "webservice-project", "1.0.0.Final");
    protected static final String WS_CONTAINER_ID = "webservice-project";
    
    @BeforeClass
    public static void buildAndDeployArtifacts() {

        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/webservice-project");

        kieContainer = KieServices.Factory.get().newKieContainer(releaseId);

        createContainer(WS_CONTAINER_ID, releaseId);
    }

}
