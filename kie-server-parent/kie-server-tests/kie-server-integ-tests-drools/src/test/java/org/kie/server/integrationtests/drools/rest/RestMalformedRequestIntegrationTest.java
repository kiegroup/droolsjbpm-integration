package org.kie.server.integrationtests.drools.rest;

import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.ClientResponseFailure;
import org.jboss.resteasy.util.GenericType;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.integrationtests.category.RESTOnly;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.integrationtests.shared.RestOnlyBaseIntegrationTest;

import javax.ws.rs.core.Response;

@Category(RESTOnly.class)
public class RestMalformedRequestIntegrationTest extends RestOnlyBaseIntegrationTest {

    @Test
    public void testCreateContainerNonExistingGAV2() throws Exception {
        KieContainerResource resource = new KieContainerResource("no-gav2-container",
                new ReleaseId("foo", "bar", "0.0.0"));

        ClientResponse<ServiceResponse<KieContainerResource>> response = null;
        try {
            ClientRequest clientRequest = newRequest(TestConfig.getHttpUrl() + "/containers/" + resource.getContainerId());
            response = clientRequest.body(
                    getMediaType(), resource).put(
                    new GenericType<ServiceResponse<KieContainerResource>>() {
                    });
            Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        } catch (Exception e) {
            throw new ClientResponseFailure(
                    "Unexpected exception creating container: " + resource.getContainerId() + " with release-id " + resource.getReleaseId(),
                    e, response);
        }
    }

    @Test
    public void testCreateContainerEmptyBody() throws Exception {
        ClientResponse<ServiceResponse<KieContainerResource>> response = null;
        try {
            ClientRequest clientRequest = newRequest(TestConfig.getHttpUrl() + "/containers/empty-body-container");
            response = clientRequest.body(
                    getMediaType(), "").put(
                    new GenericType<ServiceResponse<KieContainerResource>>() {
                    });
            Assert.assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        } catch (Exception e) {
            throw new ClientResponseFailure("Unexpected exception on empty body", e, response);
        }
    }

}
