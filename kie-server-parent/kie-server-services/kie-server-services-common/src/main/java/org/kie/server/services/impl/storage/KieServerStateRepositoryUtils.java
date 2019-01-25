/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.services.impl.storage;

import java.util.Properties;

import org.kie.server.api.model.KieServerConfig;
import org.kie.server.api.model.KieServerConfigItem;

public class KieServerStateRepositoryUtils {

    private KieServerStateRepositoryUtils() {
        throw new IllegalStateException("Utility class");
      }

    public static void populateWithSystemProperties(KieServerConfig config) {
        // populate the config state with system properties that are valid to kie server
        populateWithProperties(config, System.getProperties());
    }

    public static void populateWithProperties(KieServerConfig config, Properties properties) {
        // populate the config state with properties that are valid to kie server
        for (String property : properties.stringPropertyNames()) {

            if (property.startsWith("org.kie.server") || property.startsWith("org.kie.executor")) {
                KieServerConfigItem configItem = new KieServerConfigItem(property, properties.getProperty(property), String.class.getName());
                config.addConfigItem(configItem);
            }
        }
    }

}
