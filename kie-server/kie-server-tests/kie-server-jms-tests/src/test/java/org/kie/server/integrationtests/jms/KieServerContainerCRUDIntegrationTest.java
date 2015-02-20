package org.kie.server.integrationtests.jms;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.kie.server.api.model.*;

import java.util.List;

public class KieServerContainerCRUDIntegrationTest
        extends KieServerBaseIntegrationTest {

    private static ReleaseId releaseId1 = new ReleaseId("org.kie.server.testing", "container-crud-tests1", "2.1.0.GA");
    private static ReleaseId releaseId2 = new ReleaseId("org.kie.server.testing", "container-crud-tests1", "2.1.1.GA");

    @BeforeClass
    public static void initialize() throws Exception {
        createAndDeployKJar(releaseId1);
        createAndDeployKJar(releaseId2);
    }

    @Test
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

//    @Test
//    public void testCreateContainerNonExistingGAV2() throws Exception {
//        KieContainerResource resource = new KieContainerResource("no-gav2-container",
//                                                                 new ReleaseId("foo", "bar", "0.0.0"));
//
//        ClientResponse<ServiceResponse<KieContainerResource>> response = null;
//        try {
//            ClientRequest clientRequest = newRequest(BASE_URI + "/containers/" + resource.getContainerId());
//            response = clientRequest.body(
//                    MediaType.APPLICATION_XML_TYPE, resource).put(
//                    new GenericType<ServiceResponse<KieContainerResource>>() {
//                    });
//            Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
//            Assert.assertEquals(ServiceResponse.ResponseType.FAILURE, response.getEntity().getType());
//        } catch (Exception e) {
//            throw new ClientResponseFailure(
//                    "Unexpected exception creating container: " + resource.getContainerId() + " with release-id " + resource.getReleaseId(),
//                    e, response);
//        }
//    }
//
//    @Test
//    public void testCreateContainerEmptyBody() throws Exception {
//        ClientResponse<ServiceResponse<KieContainerResource>> response = null;
//        try {
//            ClientRequest clientRequest = newRequest(BASE_URI + "/containers/empty-body-container");
//            response = clientRequest.body(
//                    MediaType.APPLICATION_XML_TYPE, "").put(
//                    new GenericType<ServiceResponse<KieContainerResource>>() {
//                    });
//            Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
//        } catch (Exception e) {
//            throw new ClientResponseFailure("Unexpected exception on empty body", e, response);
//        }
//    }
//
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
    public void testGetContainerInfo() throws Exception {
        client.createContainer("container-info", new KieContainerResource("container-info", releaseId1));
        ServiceResponse<KieContainerResource> reply = client.getContainerInfo("container-info");
        Assert.assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());

        KieContainerResource info = reply.getResult();
        Assert.assertEquals(KieContainerStatus.STARTED, info.getStatus());
    }

    @Test
    @Ignore
    public void testGetContainerInfoNonExisting() throws Exception {
        ServiceResponse<KieContainerResource> reply = client.getContainerInfo("non-existing-container");
        System.out.println(reply.getMsg());
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
        Assert.assertEquals("No containers returned!", 0, containers.size());
    }

    @Test
    public void testDisposeNonExistingContainer() throws Exception {
        ServiceResponse<Void> reply = client.disposeContainer("non-existing-container");
        Assert.assertEquals(ServiceResponse.ResponseType.SUCCESS, reply.getType());
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
