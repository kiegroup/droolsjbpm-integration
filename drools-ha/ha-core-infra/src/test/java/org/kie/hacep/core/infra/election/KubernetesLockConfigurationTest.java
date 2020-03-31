/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.hacep.core.infra.election;

import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import static org.junit.Assert.*;

public class KubernetesLockConfigurationTest {

    @Test
    public void constructorTest(){
        KubernetesLockConfiguration configuration = new KubernetesLockConfiguration("default");
        assertNotNull(configuration);
        assertTrue(configuration.getClusterLabels().isEmpty());
        assertEquals("default-leaders",configuration.getConfigMapName());

        assertNull(configuration.getGroupName());
        configuration.setGroupName("Group test");
        assertEquals("Group test", configuration.getGroupName());

        assertNull(configuration.getPodName());
        configuration.setPodName("Pod test");
        assertEquals("Pod test",configuration.getPodName());

        assertNull(configuration.getKubernetesResourcesNamespace());
        configuration.setKubernetesResourcesNamespace("Resource name Test");
        assertEquals("Resource name Test", configuration.getKubernetesResourcesNamespace());

        assertTrue(configuration.getClusterLabels().isEmpty());
        Map map = new HashMap();
        map.put("key", "value");
        configuration.setClusterLabels(map);
        assertFalse(configuration.getClusterLabels().isEmpty());

        assertTrue(20000 == configuration.getRenewDeadlineMillis());
        configuration.setRenewDeadlineMillis(10000l);
        assertTrue(10000 == configuration.getRenewDeadlineMillis());

        assertTrue(5000 == configuration.getRetryPeriodMillis());
        configuration.setRetryPeriodMillis(3000l);
        assertTrue(3000 == configuration.getRetryPeriodMillis());

        assertTrue(30000 == configuration.getLeaseDurationMillis());
        configuration.setLeaseDurationMillis(20000l);
        assertTrue(20000 == configuration.getLeaseDurationMillis());

        assertTrue(1.2 == configuration.getJitterFactor());
        configuration.setJitterFactor(2.4);
        assertTrue(2.4 == configuration.getJitterFactor());

        assertNotNull(configuration.toString());
    }
}
