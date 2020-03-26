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

package org.kie.server.services.taskassigning.runtime.persistence;

import java.net.URL;

import javax.persistence.spi.PersistenceUnitInfo;

import org.kie.server.services.jbpm.jpa.PersistenceUnitExtensionsLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.server.api.KieServerConstants.KIE_TASK_ASSIGNING_RUNTIME_EXT_DISABLED;

public class TaskAssigningPersistenceUnitExtensionsLoader implements PersistenceUnitExtensionsLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskAssigningPersistenceUnitExtensionsLoader.class);

    public TaskAssigningPersistenceUnitExtensionsLoader() {
        // SPI constructor
    }

    @Override
    public boolean isEnabled() {
        return "false".equals(System.getProperty(KIE_TASK_ASSIGNING_RUNTIME_EXT_DISABLED));
    }

    @Override
    public void loadExtensions(PersistenceUnitInfo info) {
        LOGGER.debug("Adding the task assigning entities to the current jBPM persistent unit info.");
        final String classResource = "/" + PlanningTaskImpl.class.getName().replaceAll("[.]", "/") + ".class";
        final URL classURL = this.getClass().getResource(classResource);
        if (classURL != null) {
            info.getManagedClassNames().add(PlanningTaskImpl.class.getName());
            final String classJarLocation = classURL.toExternalForm().split("!")[0].replace(classResource, "");
            try {
                info.getJarFileUrls().add(new URL(classJarLocation));
            } catch (Exception e) {
                // in case setting URL to jar file location only fails, fallback to complete URL
                info.getJarFileUrls().add(classURL);
            }
            LOGGER.debug("Task assigning entities where successfully added.");
        } else {
            LOGGER.error("Unexpected error, it was not possible to get resource for: {}", classResource);
        }
    }
}

