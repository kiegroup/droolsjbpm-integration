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

package org.kie.server.services.taskassigning.planning;

import java.util.Map;

import org.junit.Test;
import org.kie.server.services.taskassigning.planning.test.model.UserSystemService1;
import org.kie.server.services.taskassigning.planning.test.model.UserSystemService2;
import org.kie.server.services.taskassigning.user.system.api.UserSystemService;
import org.kie.server.services.taskassigning.user.system.simple.SimpleUserSystemService;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class UserSystemServiceLoaderTest {

    @Test
    public void loadServices() {
        Map<String, UserSystemService> result = UserSystemServiceLoader.loadServices(getClass().getClassLoader());
        assertEquals(3, result.size());
        assertContains(result, UserSystemService1.NAME, UserSystemService1.class);
        assertContains(result, UserSystemService2.NAME, UserSystemService2.class);
        assertContains(result, SimpleUserSystemService.NAME, SimpleUserSystemService.class);
    }

    private <T> void assertContains(Map<String, UserSystemService> map, String name, Class<T> serviceType) {
        assertTrue(map.containsKey(name));
        assertEquals(serviceType.getName(), map.get(name).getClass().getName());
    }
}
