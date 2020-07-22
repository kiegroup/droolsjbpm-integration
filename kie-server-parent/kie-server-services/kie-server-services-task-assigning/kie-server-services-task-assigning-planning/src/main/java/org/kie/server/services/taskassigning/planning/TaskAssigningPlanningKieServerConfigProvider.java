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

package org.kie.server.services.taskassigning.planning;

import java.util.Collections;
import java.util.List;

import org.kie.server.api.model.KieServerConfigItem;
import org.kie.server.api.model.KieServerConfigProvider;
import org.kie.server.services.taskassigning.core.model.TaskAssigningSolution;

/**
 * Configures the list of packages that must be ignored but the OptaPlanner classes scanning, SolverServiceBase
 */
public class TaskAssigningPlanningKieServerConfigProvider implements KieServerConfigProvider {

    /**
     * Must be the same as SolverServiceBase.SCAN_EXCLUDED_PACKAGES_CONFIG_ITEM but we don't want dependencies on this
     * module.
     */
    public static final String VALUE_NAME = "OptaPlanner.scanExcludedPackages";

    @Override
    public List<KieServerConfigItem> getItems() {
        return Collections.singletonList(new KieServerConfigItem(VALUE_NAME,
                                                                 TaskAssigningSolution.class.getPackage().getName(),
                                                                 String.class.getName()));
    }
}