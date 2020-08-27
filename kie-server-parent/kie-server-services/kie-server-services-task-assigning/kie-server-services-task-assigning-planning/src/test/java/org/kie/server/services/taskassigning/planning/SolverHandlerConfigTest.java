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

import java.time.Duration;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SolverHandlerConfigTest {

    public static final String TARGET_USER = "TARGET_USER";
    public static final int PUBLISH_WINDOW_SIZE = 3;
    public static final Duration SYNC_INTERVAL = Duration.parse("PT3S");
    public static final Duration SYNC_QUERIES_SHIFT = Duration.parse("PT11M");
    public static final Duration USERS_SYNC_INTERVAL = Duration.parse("PT3H");
    public static final Duration WAIT_FOR_IMPROVED_SOLUTION_DURATION = Duration.parse("PT1S");
    public static final Duration IMPROVE_SOLUTION_ON_BACKGROUND_DURATION = Duration.parse("PT1M");

    protected SolverHandlerConfig config;

    @Before
    public void setUp() {
        config = new SolverHandlerConfig(TARGET_USER,
                                         PUBLISH_WINDOW_SIZE,
                                         SYNC_INTERVAL,
                                         SYNC_QUERIES_SHIFT,
                                         USERS_SYNC_INTERVAL,
                                         WAIT_FOR_IMPROVED_SOLUTION_DURATION,
                                         IMPROVE_SOLUTION_ON_BACKGROUND_DURATION);
    }

    @Test
    public void getTargetUserId() {
        assertThat(config.getTargetUserId()).isEqualTo(TARGET_USER);
    }

    @Test
    public void getPublishWindowSize() {
        assertThat(config.getPublishWindowSize()).isEqualTo(PUBLISH_WINDOW_SIZE);
    }

    @Test
    public void getSyncInterval() {
        assertThat(config.getSyncInterval()).isEqualTo(SYNC_INTERVAL);
    }

    @Test
    public void getSyncQueriesShift() {
        assertThat(config.getSyncQueriesShift()).isEqualTo(SYNC_QUERIES_SHIFT);
    }

    @Test
    public void getUsersSyncInterval() {
        assertThat(config.getUsersSyncInterval()).isEqualTo(USERS_SYNC_INTERVAL);
    }

    @Test
    public void getWaitForImprovedSolutionDuration() {
        assertThat(config.getWaitForImprovedSolutionDuration()).isEqualTo(WAIT_FOR_IMPROVED_SOLUTION_DURATION);
    }

    @Test
    public void getImproveSolutionOnBackgroundDuration() {
        assertThat(config.getImproveSolutionOnBackgroundDuration()).isEqualTo(IMPROVE_SOLUTION_ON_BACKGROUND_DURATION);
    }
}
