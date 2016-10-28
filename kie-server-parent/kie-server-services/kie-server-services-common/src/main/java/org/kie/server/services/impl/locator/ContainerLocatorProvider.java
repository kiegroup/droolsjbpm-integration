/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.services.impl.locator;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import org.kie.server.api.KieServerConstants;
import org.kie.server.services.api.ContainerLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Responsible for discovering and providing container locator.
 */
public class ContainerLocatorProvider {

    private static final Logger logger = LoggerFactory.getLogger(ContainerLocatorProvider.class);

    private static final ServiceLoader<ContainerLocator> locators = ServiceLoader.load(ContainerLocator.class);
    private static ContainerLocatorProvider INSTANCE = new ContainerLocatorProvider();

    private Map<String, ContainerLocator> foundLocators = new HashMap<>();
    private String locatorName = System.getProperty(KieServerConstants.KIE_SERVER_CONTAINER_LOCATOR, LatestContainerLocator.class.getSimpleName());

    private ContainerLocatorProvider() {

        locators.forEach( l -> {
                    foundLocators.put(l.getClass().getSimpleName(), l);
                    logger.info("Discovered '{}' container locator and registered under '{}'", l, l.getClass().getSimpleName());
                }
        );
        foundLocators.put(LatestContainerLocator.class.getSimpleName(), LatestContainerLocator.get());
    }

    public ContainerLocator getLocator() {
        ContainerLocator containerLocator = foundLocators.get(locatorName);

        if (containerLocator == null) {
            throw new IllegalStateException("No container locator found under name " + locatorName);
        }

        return containerLocator;
    }

    public static ContainerLocatorProvider get() {
        return INSTANCE;
    }
}
