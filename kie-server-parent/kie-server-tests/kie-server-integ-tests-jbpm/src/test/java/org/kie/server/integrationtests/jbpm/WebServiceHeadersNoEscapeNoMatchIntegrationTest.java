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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.kie.server.integrationtests.category.JEEOnly;
import org.kie.server.integrationtests.config.TestConfig;

@Category({JEEOnly.class})
public class WebServiceHeadersNoEscapeNoMatchIntegrationTest extends WebServiceBase {

    @Test
    public void testCallWebServiceHeadersNoEscapeNoMatch() {
        Map<String, Object> params = new HashMap<>();
        params.put("serviceUrl", TestConfig.getWebServiceHttpURL());
        params.put("coupon", "España");
        params.put("ns_coupon", "http://acme-travel.com");
        
        Map<String, Object> outputParams = processClient.computeProcessOutcome(WS_CONTAINER_ID, "org.specialtripsagency.travelAgencyHeadersProcess", params);
        assertEquals("525", outputParams.get("ratePerPerson"));
    }
}
