/*
 * Copyright 2015 - 2017 Red Hat, Inc. and/or its affiliates.
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

import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerResourceList;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieServerStateInfo;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.integrationtests.category.Smoke;
import org.kie.server.integrationtests.shared.KieServerAssert;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.kie.server.integrationtests.shared.basetests.RestJmsSharedBaseIntegrationTest;

import java.util.List;
import java.util.Set;

public class KieServerContainerCRUDIntegrationTest extends RestJmsSharedBaseIntegrationTest {

    private static ReleaseId releaseId1 = new ReleaseId("org.kie.server.testing", "container-crud-tests1", "2.1.0.GA");
    private static ReleaseId releaseId2 = new ReleaseId("org.kie.server.testing", "container-crud-tests1", "2.1.1.GA");

    @BeforeClass
    public static void initialize() throws Exception {
        KieServerDeployer.createAndDeployKJar(releaseId1);
        KieServerDeployer.createAndDeployKJar(releaseId2);
    }

    @Before
    public void setupKieServer() throws Exception {
        disposeAllContainers();
    }

    @Test
    @Category(Smoke.class)
    public void testCreateContainer() throws Exception {
        ServiceResponse<KieContainerResource> reply = client.createContainer("kie1", new KieContainerResource("kie1",
                                                                                                              releaseId1));
        KieServerAssert.assertSuccess(reply);
    }

    @Test
    public void testCreateContainerNonExistingGAV() throws Exception {
        ServiceResponse<KieContainerResource> reply = client.createContainer("bad-gav",
                                                                             new KieContainerResource("bad-gav",
                                                                                                      new ReleaseId(
                                                                                                              "foo",
                                                                                                              "bar",
                                                                                                              "0.0.0")));
        KieServerAssert.assertFailure(reply);

    }

    @Test
    public void testCreateContainerAfterFailure() throws Exception {
        // non-existing ID to simulate failure
        KieContainerResource resource = new KieContainerResource("kie1", new ReleaseId("non-existing", "non-existing",
                                                                                       "0.0.0"));
        ServiceResponse<KieContainerResource> reply = client.createContainer(resource.getContainerId(), resource);

        KieServerAssert.assertFailure(reply);

        // now try to re-create the container with a valid release ID
        resource.setReleaseId(releaseId1);
        reply = client.createContainer(resource.getContainerId(), resource);

        KieServerAssert.assertSuccess(reply);
    }

    @Test
    @Category(Smoke.class)
    public void testGetContainerInfo() throws Exception {
        client.createContainer("container-info", new KieContainerResource("container-info", releaseId1));
        ServiceResponse<KieContainerResource> reply = client.getContainerInfo("container-info");
        KieServerAssert.assertSuccess(reply);

        KieContainerResource info = reply.getResult();
        Assert.assertEquals(KieContainerStatus.STARTED, info.getStatus());
    }

    @Test
    public void testGetContainerInfoNonExisting() throws Exception {
        ServiceResponse<KieContainerResource> reply = client.getContainerInfo("non-existing-container");
        logger.info(reply.getMsg());
        KieServerAssert.assertFailure(reply);
    }

    @Test
    public void testListContainers() throws Exception {
        client.createContainer("list-containers-c1", new KieContainerResource("list-containers-c1", releaseId1));
        client.createContainer("list-containers-c2", new KieContainerResource("list-containers-c2", releaseId1));
        ServiceResponse<KieContainerResourceList> reply = client.listContainers();
        KieServerAssert.assertSuccess(reply);
        List<KieContainerResource> containers = reply.getResult().getContainers();
        Assert.assertEquals("Number of listed containers!", 2, containers.size());
        assertContainsContainer(containers, "list-containers-c1");
        assertContainsContainer(containers, "list-containers-c2");
    }

    @Test
    public void testGetReleaseId() throws Exception {
        String containerId = "get-releaseId";
        client.createContainer(containerId, new KieContainerResource(containerId, releaseId1));

        ServiceResponse<ReleaseId> reply = client.getReleaseId(containerId);
        KieServerAssert.assertSuccess(reply);
        Assert.assertEquals(releaseId1, reply.getResult());

        ServiceResponse<Void> disposeReply = client.disposeContainer(containerId);
        KieServerAssert.assertSuccess(disposeReply);
    }

    @Test
    public void testUpdateReleaseIdForExistingContainer() throws Exception {
        client.createContainer("update-releaseId", new KieContainerResource("update-releaseId", releaseId1));

        ServiceResponse<ReleaseId> reply = client.updateReleaseId("update-releaseId", releaseId2);
        KieServerAssert.assertSuccess(reply);
        Assert.assertEquals(releaseId2, reply.getResult());

        ServiceResponse<Void> disposeReply = client.disposeContainer("update-releaseId");
        KieServerAssert.assertSuccess(disposeReply);
    }

    @Test
    public void testDisposeContainer() throws Exception {
        client.createContainer("dispose-container", new KieContainerResource("dispose-container", releaseId1));
        ServiceResponse<Void> reply = client.disposeContainer("dispose-container");
        KieServerAssert.assertSuccess(reply);
        // verify the container is no longer returned when calling listContainers()
        ServiceResponse<KieContainerResourceList> listReply = client.listContainers();
        KieServerAssert.assertSuccess(listReply);
        List<KieContainerResource> containers = listReply.getResult().getContainers();
        KieServerAssert.assertNullOrEmpty("No containers returned!", containers);
    }

    @Test
    public void testDisposeNonExistingContainer() throws Exception {
        ServiceResponse<Void> reply = client.disposeContainer("non-existing-container");
        KieServerAssert.assertSuccess(reply);
    }

    @Test
    public void testUpdateReleaseIdForNotExistingContainer() throws Exception {
        ServiceResponse<ReleaseId> reply = client.updateReleaseId("update-releaseId", releaseId2);
        KieServerAssert.assertSuccess(reply);
        Assert.assertEquals(releaseId2, reply.getResult());

        ServiceResponse<KieContainerResourceList> replyList = client.listContainers();
        KieServerAssert.assertSuccess(replyList);
        List<KieContainerResource> containers = replyList.getResult().getContainers();
        Assert.assertEquals("Number of listed containers!", 1, containers.size());
        assertContainsContainer(containers, "update-releaseId");

        ServiceResponse<Void> disposeReply = client.disposeContainer("update-releaseId");
        KieServerAssert.assertSuccess(disposeReply);
    }

    @Test
    public void testUpdateReleaseIdForExistingContainerAndCheckServerState() throws Exception {
        client.createContainer("update-releaseId", new KieContainerResource("update-releaseId", releaseId1));

        ServiceResponse<ReleaseId> reply = client.updateReleaseId("update-releaseId", releaseId2);
        KieServerAssert.assertSuccess(reply);
        Assert.assertEquals(releaseId2, reply.getResult());

        ServiceResponse<KieServerStateInfo> currentServerStateReply = client.getServerState();
        KieServerAssert.assertSuccess(currentServerStateReply);

        KieServerStateInfo currentServerState = currentServerStateReply.getResult();
        Assert.assertNotNull(currentServerState);

        Set<KieContainerResource> containers = currentServerState.getContainers();
        Assert.assertEquals(1, containers.size());

        KieContainerResource container = containers.iterator().next();
        Assert.assertNotNull(container);

        Assert.assertEquals(releaseId2, container.getReleaseId());
        Assert.assertEquals(releaseId2, container.getResolvedReleaseId());

        ServiceResponse<Void> disposeReply = client.disposeContainer("update-releaseId");
        KieServerAssert.assertSuccess(disposeReply);
    }

    private void assertContainsContainer(List<KieContainerResource> containers, String expectedContainerId) {
        for (KieContainerResource container : containers) {
            if (container.getContainerId().equals(expectedContainerId)) {
                return;
            }
        }
        Assert.fail(
                "Container list " + containers + " does not contain expected container with id " + expectedContainerId);
    }

}
