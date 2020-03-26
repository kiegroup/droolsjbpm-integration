/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.controller.rest;

import java.util.stream.IntStream;

import org.junit.Before;
import org.junit.Test;
import org.kie.server.controller.api.model.runtime.ServerInstanceKey;
import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.kie.server.controller.api.storage.KieServerTemplateStorage;
import org.kie.server.controller.impl.storage.InMemoryKieServerTemplateStorage;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.kie.server.controller.rest.ControllerUtils.marshal;

public class ConcurrentModificationExceptionTest {

    private KieServerTemplateStorage templateStorage = InMemoryKieServerTemplateStorage.getInstance();
    private static final String serverTemplateId = "id";

    @Before
    public void setUp() {
        ServerTemplate serverTemplate = new ServerTemplate(serverTemplateId, "name");
        IntStream.range(0, 1000).boxed().map(i -> Integer.toString(i)).map(s -> new ServerInstanceKey(s, s, s, ""))
                .forEach(instance -> serverTemplate.addServerInstance(instance));
        templateStorage.store(serverTemplate);
    }

    private void disconnect() {
        ServerTemplate serverTemplate = templateStorage.load(serverTemplateId);

        try {
            for (ServerInstanceKey instanceKey : serverTemplate.getServerInstanceKeys()) {
                serverTemplate.deleteServerInstance(instanceKey.getServerInstanceId());
            }
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testConcurrency() {
        final ServerTemplate serverTemplate = templateStorage.load(serverTemplateId);
        marshal("application/xml", serverTemplate);

        Thread disconnect = new Thread(() -> disconnect());
        disconnect.start();

        String response = marshal("application/xml", serverTemplate);
        assertNotNull(response);
    }
}