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

package org.kie.server.services.taskassigning.core.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.function.Predicate;

public class ModelConstants {

    private ModelConstants() {
    }

    /**
     * System property for configuring the PLANNING_USER entityId.
     */
    public static final String PLANNING_USER_ID_PROPERTY = "org.kie.server.services.taskassigning.core.model.planningUserId";

    public static final String PLANNING_USER_ID = System.getProperty(PLANNING_USER_ID_PROPERTY, "planninguser");

    /**
     * Planning user is defined user for avoid breaking hard constraints. When no user is found that met the task required
     * potential owners set, or the required skills set, etc, the PLANNING_USER is assigned.
     */
    public static final User PLANNING_USER = new ImmutableUser(PLANNING_USER_ID.hashCode(), PLANNING_USER_ID, true);

    public static final Predicate<String> IS_PLANNING_USER = entityId -> PLANNING_USER.getEntityId().equals(entityId);

    /**
     * This task was introduced for dealing with situations where the solution ends up with no tasks. e.g. there is a
     * solution with tasks A and B, and a user completes both tasks in the jBPM runtime. When the completion events
     * are processed both tasks are removed from the solution with the proper problem fact changes. The solution remains
     * thus with no tasks and an exception is thrown.
     * Since the only potential owner for the dummy task is the PLANNING_USER this task won't affect the score dramatically.
     */
    public static final Task DUMMY_TASK = new ImmutableTask(-1,
                                                            -1,
                                                            "dummy-process",
                                                            "dummy-container",
                                                            "dummy-task",
                                                            10,
                                                            Collections.emptyMap(),
                                                            false,
                                                            Collections.unmodifiableSet(new HashSet<>(Collections.singletonList(PLANNING_USER))),
                                                            Collections.emptyMap());

    /**
     * This task was introduced for dealing with situations where all tasks are pinned and avoid falling into
     * https://issues.jboss.org/browse/PLANNER-241. Will be removed when issue is fixed.
     */
    public static final Task DUMMY_TASK_PLANNER_241 = new ImmutableTask(-2,
                                                                        -1,
                                                                        "dummy-process",
                                                                        "dummy-container",
                                                                        "dummy-task-planner-241",
                                                                        10,
                                                                        Collections.emptyMap(),
                                                                        false,
                                                                        Collections.unmodifiableSet(new HashSet<>(Collections.singletonList(PLANNING_USER))),
                                                                        Collections.emptyMap());

    public static final Predicate<Task> IS_NOT_DUMMY = task -> !DUMMY_TASK.getId().equals(task.getId()) && !DUMMY_TASK_PLANNER_241.getId().equals(task.getId());
}
