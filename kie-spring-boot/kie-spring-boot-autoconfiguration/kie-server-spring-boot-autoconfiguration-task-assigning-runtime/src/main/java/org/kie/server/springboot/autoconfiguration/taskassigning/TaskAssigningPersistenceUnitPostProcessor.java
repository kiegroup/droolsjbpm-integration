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

package org.kie.server.springboot.autoconfiguration.taskassigning;

import org.jbpm.springboot.persistence.JBPMPersistenceUnitPostProcessor;
import org.kie.server.services.taskassigning.runtime.persistence.PlanningTaskImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.jpa.persistenceunit.MutablePersistenceUnitInfo;

public class TaskAssigningPersistenceUnitPostProcessor implements JBPMPersistenceUnitPostProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskAssigningPersistenceUnitPostProcessor.class);

    @Override
    public void postProcessPersistenceUnitInfo(MutablePersistenceUnitInfo pui) {
        LOGGER.debug("Adding the task assigning entities to the current jBPM persistent unit: {}.", pui.getPersistenceUnitName());
        pui.getManagedClassNames().add(PlanningTaskImpl.class.getName());
        LOGGER.debug("Task assigning entities where successfully added.");
    }
}

