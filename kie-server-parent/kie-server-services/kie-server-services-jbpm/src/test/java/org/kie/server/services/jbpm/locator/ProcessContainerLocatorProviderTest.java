/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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
package org.kie.server.services.jbpm.locator;

import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;
import org.kie.server.api.KieServerConstants;
import org.kie.server.services.api.ContainerLocator;

import static org.hamcrest.CoreMatchers.instanceOf;

public class ProcessContainerLocatorProviderTest {

    @Before
    public void setUp() {
        System.clearProperty(KieServerConstants.KIE_SERVER_PROCESS_INSTANCE_CONTAINER_LOCATOR);
    }

    @Test
    public void testShouldGetDefaultLocator() {
        ContainerLocator containerLocator = ProcessContainerLocatorProvider.get().getLocator(1L);

        MatcherAssert.assertThat(containerLocator, instanceOf(ByProcessInstanceIdContainerLocator.class));
    }

    @Test
    public void testShouldReturnLocatorSetInProperty() {
        System.setProperty(KieServerConstants.KIE_SERVER_PROCESS_INSTANCE_CONTAINER_LOCATOR,
            ByContextMappingInfoContainerLocator.class.getSimpleName());

        ContainerLocator containerLocator = ProcessContainerLocatorProvider.get().getLocator(1L);

        MatcherAssert.assertThat(containerLocator, instanceOf(ByContextMappingInfoContainerLocator.class));
    }

    @Test
    public void testShouldReturnLocatorFromServices() {
        System.setProperty(KieServerConstants.KIE_SERVER_PROCESS_INSTANCE_CONTAINER_LOCATOR,
            "ContainerLocatorFactoryMock");

        ContainerLocator containerLocator = ProcessContainerLocatorProvider.get().getLocator(1L);

        MatcherAssert.assertThat(containerLocator, instanceOf(ContainerLocatorFactoryMock.ContainerLocatorMock.class));
    }

    @Test(expected = IllegalStateException.class)
    public void testLocatorNotExists() {
        System.setProperty(KieServerConstants.KIE_SERVER_PROCESS_INSTANCE_CONTAINER_LOCATOR, "NotExistsContainerLocator");

        ProcessContainerLocatorProvider.get().getLocator(1L);
    }
}
