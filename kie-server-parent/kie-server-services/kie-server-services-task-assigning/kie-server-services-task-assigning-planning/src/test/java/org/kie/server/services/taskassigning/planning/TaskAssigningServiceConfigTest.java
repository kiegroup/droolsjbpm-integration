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

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.kie.server.services.taskassigning.planning.SolverHandlerConfigTest.IMPROVE_SOLUTION_ON_BACKGROUND_DURATION;
import static org.kie.server.services.taskassigning.planning.SolverHandlerConfigTest.PUBLISH_WINDOW_SIZE;
import static org.kie.server.services.taskassigning.planning.SolverHandlerConfigTest.SYNC_INTERVAL;
import static org.kie.server.services.taskassigning.planning.SolverHandlerConfigTest.SYNC_QUERIES_SHIFT;
import static org.kie.server.services.taskassigning.planning.SolverHandlerConfigTest.TARGET_USER;
import static org.kie.server.services.taskassigning.planning.SolverHandlerConfigTest.USERS_SYNC_INTERVAL;
import static org.kie.server.services.taskassigning.planning.SolverHandlerConfigTest.WAIT_FOR_IMPROVED_SOLUTION_DURATION;

public class TaskAssigningServiceConfigTest {

    @Test
    public void getSolverHandlerConfig() {
        TaskAssigningServiceConfig serviceConfig = new TaskAssigningServiceConfig(TARGET_USER,
                                                                                  PUBLISH_WINDOW_SIZE,
                                                                                  SYNC_INTERVAL,
                                                                                  SYNC_QUERIES_SHIFT,
                                                                                  USERS_SYNC_INTERVAL,
                                                                                  WAIT_FOR_IMPROVED_SOLUTION_DURATION,
                                                                                  IMPROVE_SOLUTION_ON_BACKGROUND_DURATION);

        SolverHandlerConfig handlerConfig = serviceConfig.getSolverHandlerConfig();
        assertThat(handlerConfig).isNotNull();
        assertThat(handlerConfig.getTargetUserId()).isEqualTo(TARGET_USER);
        assertThat(handlerConfig.getPublishWindowSize()).isEqualTo(PUBLISH_WINDOW_SIZE);
        assertThat(handlerConfig.getSyncInterval()).isEqualTo(SYNC_INTERVAL);
        assertThat(handlerConfig.getSyncQueriesShift()).isEqualTo(SYNC_QUERIES_SHIFT);
        assertThat(handlerConfig.getUsersSyncInterval()).isEqualTo(USERS_SYNC_INTERVAL);
        assertThat(handlerConfig.getWaitForImprovedSolutionDuration()).isEqualTo(WAIT_FOR_IMPROVED_SOLUTION_DURATION);
        assertThat(handlerConfig.getImproveSolutionOnBackgroundDuration()).isEqualTo(IMPROVE_SOLUTION_ON_BACKGROUND_DURATION);
    }
}
