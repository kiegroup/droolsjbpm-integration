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

import org.junit.Test;
import org.kie.server.services.taskassigning.runtime.persistence.PlanningTaskImpl;
import org.springframework.orm.jpa.persistenceunit.MutablePersistenceUnitInfo;

import static org.junit.Assert.assertTrue;

public class TaskAssigningPersistenceUnitPostProcessorTest {

    @Test
    public void postProcessPersistenceUnitInfo() {
        MutablePersistenceUnitInfo persistenceUnitInfo = new MutablePersistenceUnitInfo();
        TaskAssigningPersistenceUnitPostProcessor processor = new TaskAssigningPersistenceUnitPostProcessor();
        processor.postProcessPersistenceUnitInfo(persistenceUnitInfo);
        assertTrue("Class: " + PlanningTaskImpl.class.getName() + " is expected to have been added as managed class.",
                   persistenceUnitInfo.getManagedClassNames().contains(PlanningTaskImpl.class.getName()));
    }
}
