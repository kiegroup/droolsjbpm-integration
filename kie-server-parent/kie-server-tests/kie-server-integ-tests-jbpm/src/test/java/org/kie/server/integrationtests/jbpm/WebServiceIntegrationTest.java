/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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
import static org.kie.api.runtime.process.ProcessInstance.STATE_COMPLETED;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.kie.server.api.model.instance.ProcessInstance;
import org.kie.server.integrationtests.category.JEEOnly;
import org.kie.server.integrationtests.category.WildflyOnly;
import org.kie.server.integrationtests.config.TestConfig;

@Category({JEEOnly.class, WildflyOnly.class})
public class WebServiceIntegrationTest extends WebServiceBase {

    protected static final String PROCESS_ID_WS = "org.specialtripsagency.specialtripsagencyprocess";

    @Test
    public void testCallWebServiceFromProcess() {
        Map<String, Object> params = new HashMap<>();
        params.put("serviceUrl", TestConfig.getWebServiceHttpURL());
        Long pid = processClient.startProcess(WS_CONTAINER_ID, PROCESS_ID_WS, params);

        assertThat(pid).isNotNull();
        ProcessInstance pi = queryClient.findProcessInstanceById(pid);
        assertThat(pi.getState()).isEqualTo(STATE_COMPLETED);

    }
}
