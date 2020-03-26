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

package org.kie.server.api.model.taskassigning;

public enum TaskInputVariablesReadMode {
    /**
     * Don't load the task variables.
     */
    DONT_READ,
    /**
     * Load the task variables for all the returned tasks.
     */
    READ_FOR_ALL,
    /**
     * Optimization, read the task variables only for the tasks that aren't in a sink status and hasn't an associated planning task.
     * This option is intended for internal use.
     */
    READ_FOR_ACTIVE_TASKS_WITH_NO_PLANNING_ENTITY
}
