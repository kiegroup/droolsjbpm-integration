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

import java.util.List;
import java.util.Set;

import org.junit.Assert;
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
import org.kie.server.integrationtests.shared.RestJmsSharedBaseIntegrationTest;

public class KieServerContainerCRUDIntegrationTest extends RestJmsSharedBaseIntegrationTest {

    private static ReleaseId releaseId1 = new ReleaseId("org.kie.server.testing", "container-crud-tests1", "2.1.0.GA");
    private static ReleaseId releaseId2 = new ReleaseId("org.kie.server.testing", "container-crud-tests1", "2.1.1.GA");

    @BeforeClass
    public static void initialize() throws Exception {
        createAndDeployKJar(releaseId1);
        createAndDeployKJar(releaseId2);
    }

    @Test
    @Category(Smoke.class)
    public void testCreateContainer() throws Exception {
        ServiceResponse<KieContainerResource> reply = client.createContainer("kie1", new KieContainerResource("kie1",
                                                                                                              releaseId1));
        Assert.assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());
    }

    @Test
    public void testCreateContainerNonExistingGAV() throws Exception {
        ServiceResponse<KieContainerResource> reply = client.createContainer("bad-gav",
                                                                             new KieContainerResource("bad-gav",
                                                                                                      new ReleaseId(
                                                                                                              "foo",
                                                                                                              "bar",
                                                                                                              "0.0.0")));
        Assert.assertEquals(ServiceResponse.ResponseType.FAILURE, reply.getType());
    }

    @Test
    public void testCreateContainerAfterFailure() throws Exception {
        // non-existing ID to simulate failure
        KieContainerResource resource = new KieContainerResource("kie1", new ReleaseId("non-existing", "non-existing",
                                                                                       "0.0.0"));
        ServiceResponse<KieContainerResource> reply = client.createContainer(resource.getContainerId(), resource);

        Assert.assertEquals(ServiceResponse.ResponseType.FAILURE, reply.getType());

        // now try to re-create the container with a valid release ID
        resource.setReleaseId(releaseId1);
        reply = client.createContainer(resource.getContainerId(), resource);

        Assert.assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());
    }

    @Test
    @Category(Smoke.class)
    public void testGetContainerInfo() throws Exception {
        client.createContainer("container-info", new KieContainerResource("container-info", releaseId1));
        ServiceResponse<KieContainerResource> reply = client.getContainerInfo("container-info");
        Assert.assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());

        KieContainerResource info = reply.getResult();
        Assert.assertEquals(KieContainerStatus.STARTED, info.getStatus());
    }

    @Test
    public void testGetContainerInfoNonExisting() throws Exception {
        ServiceResponse<KieContainerResource> reply = client.getContainerInfo("non-existing-container");
        logger.info(reply.getMsg());
        Assert.assertEquals(ServiceResponse.ResponseType.FAILURE, reply.getType());
    }

    @Test
    public void testListContainers() throws Exception {
        client.createContainer("list-containers-c1", new KieContainerResource("list-containers-c1", releaseId1));
        client.createContainer("list-containers-c2", new KieContainerResource("list-containers-c2", releaseId1));
        ServiceResponse<KieContainerResourceList> reply = client.listContainers();
        Assert.assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());
        List<KieContainerResource> containers = reply.getResult().getContainers();
        Assert.assertEquals("Number of listed containers!", 2, containers.size());
        assertContainsContainer(containers, "list-containers-c1");
        assertContainsContainer(containers, "list-containers-c2");
    }

    @Test
    public void testUpdateReleaseIdForExistingContainer() throws Exception {
        client.createContainer("update-releaseId", new KieContainerResource("update-releaseId", releaseId1));

        ServiceResponse<ReleaseId> reply = client.updateReleaseId("update-releaseId", releaseId2);
        Assert.assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());
        Assert.assertEquals(releaseId2, reply.getResult());

        ServiceResponse<Void> disposeReply = client.disposeContainer("update-releaseId");
        Assert.assertEquals(ServiceResponse.ResponseType.SUCCESS, disposeReply.getType());
    }

    @Test
    public void testDisposeContainer() throws Exception {
        client.createContainer("dispose-container", new KieContainerResource("dispose-container", releaseId1));
        ServiceResponse<Void> reply = client.disposeContainer("dispose-container");
        Assert.assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());
        // verify the container is no longer returned when calling listContainers()
        ServiceResponse<KieContainerResourceList> listReply = client.listContainers();
        Assert.assertEquals(ServiceResponse.ResponseType.SUCCESS, listReply.getType());
        List<KieContainerResource> containers = listReply.getResult().getContainers();
        assertNullOrEmpty("No containers returned!", containers);
    }

    @Test
    public void testDisposeNonExistingContainer() throws Exception {
        ServiceResponse<Void> reply = client.disposeContainer("non-existing-container");
        Assert.assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());
    }

    @Test
    public void testUpdateReleaseIdForNotExistingContainer() throws Exception {

        ServiceResponse<ReleaseId> reply = client.updateReleaseId("update-releaseId", releaseId2);
        Assert.assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());
        Assert.assertEquals(releaseId2, reply.getResult());

        ServiceResponse<KieContainerResourceList> replyList = client.listContainers();
        Assert.assertEquals(ServiceResponse.ResponseType.SUCCESS, replyList.getType());
        List<KieContainerResource> containers = replyList.getResult().getContainers();
        Assert.assertEquals("Number of listed containers!", 1, containers.size());
        assertContainsContainer(containers, "update-releaseId");

        ServiceResponse<Void> disposeReply = client.disposeContainer("update-releaseId");
        Assert.assertEquals(ServiceResponse.ResponseType.SUCCESS, disposeReply.getType());

    }

    @Test
    public void testUpdateReleaseIdForExistingContainerAndCheckServerState() throws Exception {
        client.createContainer("update-releaseId", new KieContainerResource("update-releaseId", releaseId1));

        ServiceResponse<ReleaseId> reply = client.updateReleaseId("update-releaseId", releaseId2);
        Assert.assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());
        Assert.assertEquals(releaseId2, reply.getResult());

        ServiceResponse<KieServerStateInfo> currentServerStateReply = client.getServerState();
        Assert.assertEquals(ServiceResponse.ResponseType.SUCCESS, currentServerStateReply.getType());

        KieServerStateInfo currentServerState = currentServerStateReply.getResult();
        Assert.assertNotNull(currentServerState);

        Set<KieContainerResource> containers = currentServerState.getContainers();
        Assert.assertEquals(1, containers.size());

        KieContainerResource container = containers.iterator().next();
        Assert.assertNotNull(container);

        Assert.assertEquals(releaseId2, container.getReleaseId());
        Assert.assertEquals(releaseId2, container.getResolvedReleaseId());

        ServiceResponse<Void> disposeReply = client.disposeContainer("update-releaseId");
        Assert.assertEquals(ServiceResponse.ResponseType.SUCCESS, disposeReply.getType());
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
