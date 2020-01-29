/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.server.integrationtests.controller;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.runners.Parameterized;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.controller.client.KieServerControllerClientFactory;
import org.kie.server.controller.client.exception.KieServerControllerClientException;
import org.kie.server.integrationtests.config.TestConfig;

import static org.assertj.core.api.Assertions.assertThat;

public class WebSocketKieControllerRuntimeManagementIntegrationTest extends KieControllerRuntimeManagementIntegrationTest<KieServerControllerClientException> {

    @Override
    protected void assertNotFoundException(KieServerControllerClientException e) {
        assertThat(e.getMessage()).isNotNull();
    }

    @Override
    protected void assertBadRequestException(KieServerControllerClientException e) {
        assertThat(e.getMessage()).isNotNull();
    }

    @Parameterized.Parameters(name = "{index}: {0} {1}")
    public static Collection<Object[]> data() {
        KieServicesConfiguration restConfiguration = createKieServicesRestConfiguration();

        return Arrays.asList(new Object[][]{{MarshallingFormat.JSON, restConfiguration}});
    }

    @Before
    @Override
    public void createControllerClient() {
        if (TestConfig.isLocalServer()) {
            controllerClient = KieServerControllerClientFactory.newWebSocketClient(TestConfig.getControllerWebSocketManagementUrl(),
                                                                                   null,
                                                                                   (String) null);
        } else {
            controllerClient = KieServerControllerClientFactory.newWebSocketClient(TestConfig.getControllerWebSocketManagementUrl(),
                                                                                   TestConfig.getUsername(),
                                                                                   TestConfig.getPassword());
        }
    }
}
