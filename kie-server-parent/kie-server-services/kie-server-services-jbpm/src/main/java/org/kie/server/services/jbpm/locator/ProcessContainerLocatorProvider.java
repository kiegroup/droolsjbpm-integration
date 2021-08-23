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

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import org.kie.server.api.KieServerConstants;
import org.kie.server.services.api.ContainerLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.server.api.KieServerConstants.KIE_SERVER_PROCESS_INSTANCE_CONTAINER_LOCATOR;

public class ProcessContainerLocatorProvider {
    private static final Logger logger = LoggerFactory.getLogger(ProcessContainerLocatorProvider.class);

    private static final ProcessContainerLocatorProvider INSTANCE = new ProcessContainerLocatorProvider();
    private final Map<String, ContainerLocatorFactory> locators = new HashMap<>();

    private ProcessContainerLocatorProvider() {
        ServiceLoader<ContainerLocatorFactory> containerLocators = ServiceLoader.load(ContainerLocatorFactory.class);
        containerLocators.forEach( l -> {
                locators.put(l.getClass().getSimpleName(), l);
                logger.info("Discovered '{}' container locator factory and registered under '{}'", l, l.getClass().getSimpleName());
            }
        );

        locators.put(ByProcessInstanceIdContainerLocator.class.getSimpleName(), ByProcessInstanceIdContainerLocator.Factory.get());
        locators.put(ByContextMappingInfoContainerLocator.class.getSimpleName(), ByContextMappingInfoContainerLocator.Factory.get());
    }

    public ContainerLocator getLocator(final Number processInstanceId) {
        String processInstanceLocatorName = System.getProperty(KIE_SERVER_PROCESS_INSTANCE_CONTAINER_LOCATOR,
                                                                ByProcessInstanceIdContainerLocator.class.getSimpleName());

        ContainerLocatorFactory containerLocatorFactory = locators.get(processInstanceLocatorName);

        if (containerLocatorFactory == null) {
            throw new IllegalStateException("No container locator factory found under name " + processInstanceLocatorName
                + " please review '" + KIE_SERVER_PROCESS_INSTANCE_CONTAINER_LOCATOR + "' property value");
        }

        logger.info("Container locator factory was found '{}'", containerLocatorFactory.getClass().getSimpleName());
        return containerLocatorFactory.create(processInstanceId);
    }

    public static ProcessContainerLocatorProvider get() {
        return INSTANCE;
    }
}
