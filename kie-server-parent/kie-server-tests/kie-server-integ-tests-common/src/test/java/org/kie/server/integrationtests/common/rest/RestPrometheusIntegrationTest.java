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

package org.kie.server.integrationtests.common.rest;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.integrationtests.category.RESTOnly;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.integrationtests.shared.basetests.RestOnlyBaseIntegrationTest;

@Category(RESTOnly.class)
public class RestPrometheusIntegrationTest extends RestOnlyBaseIntegrationTest {

    @Test
    public void testPrometheusEndpoint() throws Exception {
        KieContainerResource resource = new KieContainerResource("no-gav2-container",
                new ReleaseId("foo", "bar", "0.0.0"));

        Response response = null;
        try {
            WebTarget clientRequest = newRequest(TestConfig.getKieServerHttpUrl() + "/pippo");
            System.out.println("+++++++++++++++ PROMETHEUS TEST");
            response = clientRequest.request(MediaType.TEXT_PLAIN_TYPE).get();

            System.out.println(response);

            Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        } catch (Exception e) {
            throw new RuntimeException(
                    "Unexpected exception creating container: " + resource.getContainerId() + " with release-id " + resource.getReleaseId(),
                    e);
        } finally {
            if(response != null) {
                response.close();
            }
        }
    }

}
