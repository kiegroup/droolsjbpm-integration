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
import javax.ws.rs.core.Response;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Histogram;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.integrationtests.category.RESTOnly;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.integrationtests.shared.basetests.RestTextOnlyBaseIntegrationTest;

@Category(RESTOnly.class)
public class RestPrometheusIntegrationTest extends RestTextOnlyBaseIntegrationTest {

    private static final ReleaseId RELEASE_ID_1 = new ReleaseId("org.kie.server.testing", "container-prometheus-tests1", "2.1.0.GA");

    @Test
    public void testPrometheusEndpoint() throws InterruptedException {
        System.setProperty(KieServerConstants.KIE_PROMETHEUS_SERVER_EXT_DISABLED, "false");
        KieContainerResource resource = new KieContainerResource("container", RELEASE_ID_1);

        recordMetric();

        Response response = null;
        try {
            WebTarget clientRequest = newRequest(TestConfig.getKieServerHttpUrl() + "/prometheus/");
            response = clientRequest.request(getMediaType()).get();

            Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());

            String res = response.readEntity(String.class);
            String[] split = res.split("\\n");
            Assert.assertEquals(10, split.length);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Unexpected exception creating container: " + resource.getContainerId() + " with release-id " + resource.getReleaseId(),
                    e);
        } finally {
            if (response != null) {
                response.close();
            }
        }
    }

    public void recordMetric() {

        CollectorRegistry registry = CollectorRegistry.defaultRegistry;

        logger.info("Recording metrics: " + registry.hashCode());

        Histogram histogram = Histogram.build().name("dmn_evaluate_decision_nanosecond" + System.nanoTime())
                .help("DMN Evaluation Time")
                .labelNames("decision_name")
                .buckets(HALF_SECOND_NANO, toNano(1), toNano(2), toNano(3), toNano(4))
                .register();

        int amt = 123456789;
        histogram.labels("prova")
                .observe(amt);

        logger.info("inserted = " + amt);
    }

    /**
     * Number of nanoseconds in a second.
     */
    public static final long NANOSECONDS_PER_SECOND = 1_000_000_000;
    public static final long HALF_SECOND_NANO = 500_000_000;

    public static long toNano(long second) {
        return second * NANOSECONDS_PER_SECOND;
    }
}
