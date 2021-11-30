/*
 * Copyright 2016 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.kie.internal.runtime.conf.DeploymentDescriptor;
import org.kie.internal.runtime.conf.ObjectModel;
import org.kie.internal.runtime.manager.deploy.DeploymentDescriptorImpl;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerResourceFilter;
import org.kie.server.api.model.KieContainerResourceList;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieContainerStatusFilter;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ReleaseIdFilter;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.integrationtests.shared.KieServerAssert;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.kie.server.integrationtests.shared.basetests.KieServerBaseIntegrationTest;
import org.kie.server.integrationtests.shared.basetests.RestJmsSharedBaseIntegrationTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(Parameterized.class)
public class KieServerContainerListFilteringIntegrationTest extends RestJmsSharedBaseIntegrationTest {

    private static ReleaseId releaseId1 = new ReleaseId("org.kie.server.testing", "container-crud-tests1", "2.1.0.GA");
    private static ReleaseId releaseId2 = new ReleaseId("org.kie.server.testing", "container-crud-tests1", "2.1.1.GA");
    private static ReleaseId releaseId3 = new ReleaseId("org.kie.server.testing", "container-crud-tests2", "2.1.2.GA");

    private static final String CONTAINER_ID_1 = "list-containers-releaseId-c1";
    private static final String CONTAINER_ID_2 = "list-containers-releaseId-c2";
    private static final String CONTAINER_ID_3 = "list-containers-releaseId-c3";

    private static final ReleaseIdFilter VERSION_FILTER_1 = new ReleaseIdFilter.Builder().version("2.1.0.GA").build();
    private static final ReleaseIdFilter VERSION_FILTER_2 = new ReleaseIdFilter.Builder().version("3.0.0.GA").build();
    private static final ReleaseIdFilter ARTIFACT_ID_FILTER = new ReleaseIdFilter.Builder().artifactId("container-crud-tests2").build();
    private static final ReleaseIdFilter GROUP_ID_FILTER = new ReleaseIdFilter.Builder().groupId("org.kie.server.testing").build();
    private static final ReleaseIdFilter ARTIFACT_ID_VERSION_FILTER = new ReleaseIdFilter.Builder().artifactId("container-crud-tests1").version("2.1.1.GA").build();
    private static final ReleaseIdFilter GROUPD_ID_ARTIFACT_ID_FILTER = new ReleaseIdFilter.Builder().groupId("org.kie.server.testing").artifactId("container-crud-tests1").build();
    private static final ReleaseIdFilter RELEASE_ID_FILTER_1 = new ReleaseIdFilter("org.kie.server.testing", "container-crud-tests1", "2.1.0.GA");
    private static final ReleaseIdFilter RELEASE_ID_FILTER_2 = new ReleaseIdFilter("org.kie.server.testing", "container-crud-tests2", "2.1.2.GA");

    private static final KieContainerStatusFilter CREATING_DISPOSING_FILTER = new KieContainerStatusFilter(KieContainerStatus.CREATING, KieContainerStatus.DISPOSING);
    private static final KieContainerStatusFilter STARTED_FILTER = new KieContainerStatusFilter(KieContainerStatus.STARTED);

    @BeforeClass
    public static void initialize() throws Exception {
        // we need to disable policies, otherwise it will cause a race condition
        KieServerBaseIntegrationTest.tearDown();

        KieServerBaseIntegrationTest.setupClass("");

        KieServerDeployer.createAndDeployKJar(releaseId1);
        KieServerDeployer.createAndDeployKJar(releaseId2);

        DeploymentDescriptor brokenDescriptor = new DeploymentDescriptorImpl("org.jbpm.domain");
        brokenDescriptor = brokenDescriptor.getBuilder()
                                           .addTaskEventListener(new ObjectModel("mvel", "new org.kie.not.existing.TaskEventListener()", new Object[0]))
                                           .get();

        Map<String, String> content = new HashMap<>();
        content.put("src/main/resources/META-INF/kie-deployment-descriptor.xml", brokenDescriptor.toXml());
        content.put("src/main/resources/script-process.bpmn2", readFile("/script-process.bpmn2"));
        KieServerDeployer.createAndDeployKJar(releaseId3, content);
    }

    @Before
    public void setupKieServer() throws Exception {
        disposeAllContainers();
        client.createContainer(CONTAINER_ID_1, new KieContainerResource(CONTAINER_ID_1, releaseId1));
        client.createContainer(CONTAINER_ID_2, new KieContainerResource(CONTAINER_ID_2, releaseId2));
         //container 3 have status FAILED because is not created kjar
        client.createContainer(CONTAINER_ID_3, new KieContainerResource(CONTAINER_ID_3, releaseId3));
    }

    @Parameterized.Parameters(name = "{0} {1} {2} {3} {4}")
    public static Collection<Object[]> data() {

        Collection<Object[]> testData = new ArrayList<Object[]>(Arrays.asList(new Object[][]
        {
            {VERSION_FILTER_1, KieContainerStatusFilter.ACCEPT_ALL, Arrays.asList(new String[]{CONTAINER_ID_1})},
            {VERSION_FILTER_2, CREATING_DISPOSING_FILTER, Arrays.asList(new String[]{})},
            {ARTIFACT_ID_FILTER, KieContainerStatusFilter.ACCEPT_ALL, Arrays.asList(new String[]{CONTAINER_ID_3})},
            {GROUP_ID_FILTER, KieContainerStatusFilter.ACCEPT_ALL, Arrays.asList(new String[]{CONTAINER_ID_1, CONTAINER_ID_2, CONTAINER_ID_3})},
            {ARTIFACT_ID_VERSION_FILTER, STARTED_FILTER, Arrays.asList(new String[]{CONTAINER_ID_2})},
            {GROUPD_ID_ARTIFACT_ID_FILTER, KieContainerStatusFilter.ACCEPT_ALL, Arrays.asList(new String[]{CONTAINER_ID_1, CONTAINER_ID_2})},
            {RELEASE_ID_FILTER_1, KieContainerStatusFilter.ACCEPT_ALL, Arrays.asList(new String[]{CONTAINER_ID_1})},
            {RELEASE_ID_FILTER_2, STARTED_FILTER, Arrays.asList(new String[]{})},
            {ReleaseIdFilter.ACCEPT_ALL, STARTED_FILTER, Arrays.asList(new String[]{CONTAINER_ID_1, CONTAINER_ID_2})},
            {ReleaseIdFilter.ACCEPT_ALL, KieContainerStatusFilter.ACCEPT_ALL, Arrays.asList(new String[]{CONTAINER_ID_1, CONTAINER_ID_2, CONTAINER_ID_3})}
        }
        ));

        //concat with parent parameters (marshalling, configuration)
        return createParametersCollection(testData);
    }

    private static Collection<Object[]> createParametersCollection(Collection<Object[]> data) {
        Collection<Object[]> toReturn = new ArrayList<Object[]>();

        for (Object[] row : data) {
            for (Object[] settings : RestJmsSharedBaseIntegrationTest.data()) {
                toReturn.add(Stream.of(settings, row).flatMap(Stream::of).toArray(Object[]::new));
            }
        }
        return toReturn;
    }

    //parametrs with ids 0 and 1 are used in parent class
    @Parameterized.Parameter(2)
    public ReleaseIdFilter releaseIdFilter;

    @Parameterized.Parameter(3)
    public KieContainerStatusFilter statusFilter;

    @Parameterized.Parameter(4)
    public List<String> expectedContainersIds;

    @Test
    public void testListContainersWithFiltering() {
        ServiceResponse<KieContainerResourceList> reply = client.listContainers(new KieContainerResourceFilter(releaseIdFilter, statusFilter));
        KieServerAssert.assertSuccess(reply);

        List<KieContainerResource> containers = reply.getResult().getContainers();
        if (expectedContainersIds.isEmpty()) {
            KieServerAssert.assertNullOrEmpty("Should be return empty list", containers);
        } else {
            assertNotNull(containers);
            assertEquals(expectedContainersIds.size(), containers.size());
            checkContainersList(containers);
        }
    }

    private void checkContainersList(List<KieContainerResource> containers) {
        for (KieContainerResource container : containers) {
            assertTrue(isContainerExpected(container));
            checkReleaseIdByFilter(container.getReleaseId());
            checkContainerStatusByFilter(container.getStatus());
        }
    }

    private boolean isContainerExpected(KieContainerResource container) {
        return expectedContainersIds.contains(container.getContainerId());
    }

    private void checkReleaseIdByFilter(ReleaseId releaseId) {
        String version = releaseIdFilter.getVersion();
        String artifactId = releaseIdFilter.getArtifactId();
        String groupId = releaseIdFilter.getGroupId();

        if (version != null) {
            assertEquals(version, releaseId.getVersion());
        }
        if (artifactId != null) {
            assertEquals(artifactId, releaseId.getArtifactId());
        }
        if (groupId != null) {
            assertEquals(groupId, releaseId.getGroupId());
        }
    }

    private void checkContainerStatusByFilter(KieContainerStatus status) {
        if (statusFilter.getAcceptedStatuses().isEmpty()) {
            return;
        }
        for (KieContainerStatus expectedStatus : statusFilter.getAcceptedStatuses()) {
            if (expectedStatus.equals(status)) {
                return;
            }
        }
        Assert.fail("Status " + status + " is not in expected list " + expectedContainersIds);
    }

    private static String readFile(String resourceName) {
        try {
            URI resourceUri = KieServerContainerListFilteringIntegrationTest.class.getResource(resourceName).toURI();
            return new String(Files.readAllBytes(Paths.get(resourceUri)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
