/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.services.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import org.kie.server.api.KieServerConstants;
import org.kie.server.services.api.StartupStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StartupStrategyProvider {

    private static final Logger logger = LoggerFactory.getLogger(StartupStrategyProvider.class);

    private static final ServiceLoader<StartupStrategy> strategies = ServiceLoader.load(StartupStrategy.class);
    private static StartupStrategyProvider INSTANCE = new StartupStrategyProvider();

    private Map<String, StartupStrategy> foundStrategies = new HashMap<>();
    private String strategyName = System.getProperty(KieServerConstants.KIE_SERVER_STARTUP_STRATEGY, ControllerBasedStartupStrategy.class.getSimpleName());

    private StartupStrategyProvider() {

        strategies.forEach( s -> {
                    foundStrategies.put(s.getClass().getSimpleName(), s);
                    logger.debug("Discovered '{}' startup strategy and registered under '{}'", s, s.getClass().getSimpleName());
                }
        );
        foundStrategies.put(ControllerBasedStartupStrategy.class.getSimpleName(), new ControllerBasedStartupStrategy());
    }

    public StartupStrategy getStrategy() {
        StartupStrategy strategy = foundStrategies.get(strategyName);

        if (strategy == null) {
            throw new IllegalStateException("No startup strategy found under name " + strategyName);
        }

        return strategy;
    }

    public static StartupStrategyProvider get() {
        return INSTANCE;
    }
    
    public static void clear() {
        INSTANCE = new StartupStrategyProvider();
    }
}
