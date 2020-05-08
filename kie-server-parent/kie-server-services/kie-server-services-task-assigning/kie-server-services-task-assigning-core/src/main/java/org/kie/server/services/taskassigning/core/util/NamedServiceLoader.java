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

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Facilitates the SPI loading of service implementations that can be identified by name.
 */
public class NamedServiceLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(NamedServiceLoader.class);

    private NamedServiceLoader() {
    }

    /**
     * Loads all the implementations for a given service and store them by name.
     * @param serviceType service type for loading.
     * @param serviceName a function that returns the name for a given service instance.
     * @param cl class loader instance for doing the service loading.
     * @param <S> the class of the given service.
     * @return a map with all loaded service implementations indexed by name.
     */
    public static <S> Map<String, S> loadServices(Class<S> serviceType, Function<S, String> serviceName, ClassLoader cl) {
        final Map<String, S> result = new HashMap<>();
        final ServiceLoader<S> availableServices = ServiceLoader.load(serviceType, cl);
        for (S service : availableServices) {
            Object existed = result.putIfAbsent(serviceName.apply(service), service);
            if (existed == null) {
                LOGGER.debug("Service {} was added to the result", serviceName.apply(service));
            } else {
                LOGGER.warn("A service instance with name {} was already added to the result, this another instance will be discarded {}.", serviceName.apply(service), service);
            }
        }
        return result;
    }
}
