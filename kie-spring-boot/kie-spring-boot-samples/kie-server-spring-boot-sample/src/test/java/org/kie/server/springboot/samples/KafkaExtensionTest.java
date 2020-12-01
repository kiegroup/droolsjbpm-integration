/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.springboot.samples;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.server.services.api.KieServer;
import org.kie.server.services.api.KieServerExtension;
import org.kie.server.services.impl.KieServerImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {KieServerApplication.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@TestPropertySource(locations = "classpath:application-kafka.properties")
public class KafkaExtensionTest {

    private static final String KAFKA = "Kafka";
    
    @Autowired
    private KieServer kieServer;

    @Test
    public void kafkaExtensionStarted() {
        Map<String, KieServerExtension> extensions = ((KieServerImpl) kieServer).getServerExtensions().stream()
                .collect(Collectors.toMap(KieServerExtension::getExtensionName, Function.identity()));
        assertExtensionInitialized(extensions, KAFKA);
    }

    private void assertExtensionInitialized(Map<String, KieServerExtension> extensions, String extensionName) {
        KieServerExtension extension = extensions.get(extensionName);
        assertNotNull("Extension " + extensionName + " was not found in current server", extension);
        assertTrue("Extension " + extensionName + " is expected to be active", extension.isActive());
        assertTrue("Extension " + extensionName + " is expected to be initialized", extension.isInitialized());
    }
}
