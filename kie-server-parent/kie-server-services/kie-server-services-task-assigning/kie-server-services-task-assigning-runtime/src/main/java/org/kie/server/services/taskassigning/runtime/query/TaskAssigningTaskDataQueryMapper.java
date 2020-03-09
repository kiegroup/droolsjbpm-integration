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

package org.kie.server.services.taskassigning.runtime.query;

import org.kie.server.api.model.taskassigning.TaskAssigningQueries;

public class TaskAssigningTaskDataQueryMapper extends TaskAssigningTaskDataWithPotentialOwnersQueryMapper {

    public static final String NAME = TaskAssigningQueries.TASK_DATA_QUERY_MAPPER;

    public TaskAssigningTaskDataQueryMapper() {
        // Dedicated for ServiceLoader to create instance, use <code>get()</code> method instead
    }

    /**
     * Default access to get instance of the mapper
     */
    public static TaskAssigningTaskDataQueryMapper get() {
        return new TaskAssigningTaskDataQueryMapper();
    }

    @Override
    protected boolean readPotentialOwners() {
        return false;
    }

    @Override
    public String getName() {
        return NAME;
    }
}