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

package org.kie.server.services.taskassigning.core.util;

import java.util.Map;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class NamedServiceLoaderTest {

    @Test
    public void loadServices() {
        Map<String, ExampleService> result = NamedServiceLoader.loadServices(ExampleService.class, ExampleService::getName, getClass().getClassLoader());
        assertThat(result.size()).isEqualTo(2);
        assertContains(result, ExampleService1.NAME, ExampleService1.class);
        assertContains(result, ExampleService2.NAME, ExampleService2.class);
    }

    private <T> void assertContains(Map<String, ExampleService> map, String name, Class<T> serviceType) {
        assertThat(map.containsKey(name)).isTrue();
        assertThat(serviceType.getName()).isEqualTo(map.get(name).getClass().getName());
    }
}
