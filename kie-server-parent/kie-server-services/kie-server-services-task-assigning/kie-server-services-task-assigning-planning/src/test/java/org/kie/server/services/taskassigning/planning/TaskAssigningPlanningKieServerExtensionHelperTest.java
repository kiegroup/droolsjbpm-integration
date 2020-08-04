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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.kie.server.services.taskassigning.planning.SolverHandlerConfigTest.IMPROVE_SOLUTION_ON_BACKGROUND_DURATION;
import static org.kie.server.services.taskassigning.planning.SolverHandlerConfigTest.PUBLISH_WINDOW_SIZE;
import static org.kie.server.services.taskassigning.planning.SolverHandlerConfigTest.SYNC_INTERVAL;
import static org.kie.server.services.taskassigning.planning.SolverHandlerConfigTest.SYNC_QUERIES_SHIFT;
import static org.kie.server.services.taskassigning.planning.SolverHandlerConfigTest.TARGET_USER;
import static org.kie.server.services.taskassigning.planning.SolverHandlerConfigTest.USERS_SYNC_INTERVAL;
import static org.kie.server.services.taskassigning.planning.SolverHandlerConfigTest.WAIT_FOR_IMPROVED_SOLUTION_DURATION;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_IMPROVE_SOLUTION_ON_BACKGROUND_DURATION;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_PROCESS_RUNTIME_TARGET_USER;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_PUBLISH_WINDOW_SIZE;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_SYNC_INTERVAL;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_SYNC_QUERIES_SHIFT;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_USERS_SYNC_INTERVAL;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_WAIT_FOR_IMPROVED_SOLUTION_DURATION;
import static org.kie.server.services.taskassigning.planning.TaskAssigningPlanningKieServerExtensionHelper.DEFAULT_IMPROVE_SOLUTION_ON_BACKGROUND_DURATION;
import static org.kie.server.services.taskassigning.planning.TaskAssigningPlanningKieServerExtensionHelper.DEFAULT_PUBLISH_WINDOW_SIZE;
import static org.kie.server.services.taskassigning.planning.TaskAssigningPlanningKieServerExtensionHelper.DEFAULT_SYNC_INTERVAL;
import static org.kie.server.services.taskassigning.planning.TaskAssigningPlanningKieServerExtensionHelper.DEFAULT_SYNC_QUERIES_SHIFT;
import static org.kie.server.services.taskassigning.planning.TaskAssigningPlanningKieServerExtensionHelper.DEFAULT_USERS_SYNC_INTERVAL;
import static org.kie.server.services.taskassigning.planning.TaskAssigningPlanningKieServerExtensionHelper.DEFAULT_WAIT_FOR_IMPROVED_SOLUTION_DURATION;

@RunWith(Parameterized.class)
public class TaskAssigningPlanningKieServerExtensionHelperTest {

    private static final Duration NEGATIVE_DURATION = Duration.parse("PT-2S");
    private static final String NON_PARSEABLE = "NON_PARSEABLE";

    @Parameter
    public String parameterName;

    @Parameter(1)
    public String parameterFailingValue;

    @Parameterized.Parameters(name = "parameterName = {0}, parameterFailingValue = {1}")
    public static Collection<Object[]> produceFailingCases() {
        List<Object[]> data = new ArrayList<>();
        data.add(new Object[]{TASK_ASSIGNING_PROCESS_RUNTIME_TARGET_USER, null});
        data.add(new Object[]{TASK_ASSIGNING_PROCESS_RUNTIME_TARGET_USER, " "});
        data.add(new Object[]{TASK_ASSIGNING_PUBLISH_WINDOW_SIZE, "-1"});
        data.add(new Object[]{TASK_ASSIGNING_PUBLISH_WINDOW_SIZE, "0"});
        data.add(new Object[]{TASK_ASSIGNING_PUBLISH_WINDOW_SIZE, NON_PARSEABLE});
        data.add(new Object[]{TASK_ASSIGNING_SYNC_INTERVAL, Duration.ZERO.toString()});
        data.add(new Object[]{TASK_ASSIGNING_SYNC_INTERVAL, NEGATIVE_DURATION.toString()});
        data.add(new Object[]{TASK_ASSIGNING_SYNC_INTERVAL, NON_PARSEABLE});
        data.add(new Object[]{TASK_ASSIGNING_SYNC_QUERIES_SHIFT, Duration.ZERO.toString()});
        data.add(new Object[]{TASK_ASSIGNING_SYNC_QUERIES_SHIFT, NEGATIVE_DURATION.toString()});
        data.add(new Object[]{TASK_ASSIGNING_USERS_SYNC_INTERVAL, NEGATIVE_DURATION.toString()});
        data.add(new Object[]{TASK_ASSIGNING_USERS_SYNC_INTERVAL, NON_PARSEABLE});
        data.add(new Object[]{TASK_ASSIGNING_WAIT_FOR_IMPROVED_SOLUTION_DURATION, NEGATIVE_DURATION.toString()});
        data.add(new Object[]{TASK_ASSIGNING_WAIT_FOR_IMPROVED_SOLUTION_DURATION, NON_PARSEABLE});
        data.add(new Object[]{TASK_ASSIGNING_IMPROVE_SOLUTION_ON_BACKGROUND_DURATION, NEGATIVE_DURATION.toString()});
        data.add(new Object[]{TASK_ASSIGNING_IMPROVE_SOLUTION_ON_BACKGROUND_DURATION, NON_PARSEABLE});
        return data;
    }

    @Test
    public void readAndValidateTaskAssigningServiceConfigFull() throws TaskAssigningValidationException {
        prepareTaskAssigningServiceProperties();
        TaskAssigningServiceConfig serviceConfig = TaskAssigningPlanningKieServerExtensionHelper.readAndValidateTaskAssigningServiceConfig();
        SolverHandlerConfig handlerConfig = serviceConfig.getSolverHandlerConfig();
        assertThat(handlerConfig.getTargetUserId()).isEqualTo(TARGET_USER);
        assertThat(handlerConfig.getPublishWindowSize()).isEqualTo(PUBLISH_WINDOW_SIZE);
        assertThat(handlerConfig.getSyncInterval()).isEqualTo(SYNC_INTERVAL);
        assertThat(handlerConfig.getSyncQueriesShift()).isEqualTo(SYNC_QUERIES_SHIFT);
        assertThat(handlerConfig.getUsersSyncInterval()).isEqualTo(USERS_SYNC_INTERVAL);
        assertThat(handlerConfig.getWaitForImprovedSolutionDuration()).isEqualTo(WAIT_FOR_IMPROVED_SOLUTION_DURATION);
        assertThat(handlerConfig.getImproveSolutionOnBackgroundDuration()).isEqualTo(IMPROVE_SOLUTION_ON_BACKGROUND_DURATION);
    }

    @Test
    public void readAndValidateTaskAssigningServiceConfigDefaults() throws TaskAssigningValidationException {
        System.setProperty(TASK_ASSIGNING_PROCESS_RUNTIME_TARGET_USER, TARGET_USER);
        TaskAssigningServiceConfig serviceConfig = TaskAssigningPlanningKieServerExtensionHelper.readAndValidateTaskAssigningServiceConfig();
        SolverHandlerConfig handlerConfig = serviceConfig.getSolverHandlerConfig();
        assertThat(handlerConfig.getTargetUserId()).isEqualTo(TARGET_USER);
        assertThat(handlerConfig.getPublishWindowSize()).isEqualTo(DEFAULT_PUBLISH_WINDOW_SIZE);
        assertThat(handlerConfig.getSyncInterval()).isEqualTo(Duration.parse(DEFAULT_SYNC_INTERVAL));
        assertThat(handlerConfig.getSyncQueriesShift()).isEqualTo(Duration.parse(DEFAULT_SYNC_QUERIES_SHIFT));
        assertThat(handlerConfig.getUsersSyncInterval()).isEqualTo(Duration.parse(DEFAULT_USERS_SYNC_INTERVAL));
        assertThat(handlerConfig.getWaitForImprovedSolutionDuration()).isEqualTo(Duration.parse(DEFAULT_WAIT_FOR_IMPROVED_SOLUTION_DURATION));
        assertThat(handlerConfig.getImproveSolutionOnBackgroundDuration()).isEqualTo(Duration.parse(DEFAULT_IMPROVE_SOLUTION_ON_BACKGROUND_DURATION));
    }

    @Test
    public void readAndValidateTaskAssigningServiceConfigFailures() {
        prepareTaskAssigningServiceProperties();
        if (parameterFailingValue != null) {
            System.setProperty(parameterName, parameterFailingValue);
        } else {
            System.clearProperty(parameterName);
        }
        assertThatThrownBy(TaskAssigningPlanningKieServerExtensionHelper::readAndValidateTaskAssigningServiceConfig)
                .hasMessageStartingWith("Parameter %s", parameterName);
    }

    @After
    public void cleanUp() {
        clearTaskAssigningServiceProperties();
    }

    public static void prepareTaskAssigningServiceProperties() {
        System.setProperty(TASK_ASSIGNING_PROCESS_RUNTIME_TARGET_USER, TARGET_USER);
        System.setProperty(TASK_ASSIGNING_PUBLISH_WINDOW_SIZE, Integer.toString(PUBLISH_WINDOW_SIZE));
        System.setProperty(TASK_ASSIGNING_SYNC_INTERVAL, SYNC_INTERVAL.toString());
        System.setProperty(TASK_ASSIGNING_SYNC_QUERIES_SHIFT, SYNC_QUERIES_SHIFT.toString());
        System.setProperty(TASK_ASSIGNING_USERS_SYNC_INTERVAL, USERS_SYNC_INTERVAL.toString());
        System.setProperty(TASK_ASSIGNING_WAIT_FOR_IMPROVED_SOLUTION_DURATION, WAIT_FOR_IMPROVED_SOLUTION_DURATION.toString());
        System.setProperty(TASK_ASSIGNING_IMPROVE_SOLUTION_ON_BACKGROUND_DURATION, IMPROVE_SOLUTION_ON_BACKGROUND_DURATION.toString());
    }

    public static void clearTaskAssigningServiceProperties() {
        System.clearProperty(TASK_ASSIGNING_PROCESS_RUNTIME_TARGET_USER);
        System.clearProperty(TASK_ASSIGNING_PUBLISH_WINDOW_SIZE);
        System.clearProperty(TASK_ASSIGNING_SYNC_INTERVAL);
        System.clearProperty(TASK_ASSIGNING_SYNC_QUERIES_SHIFT);
        System.clearProperty(TASK_ASSIGNING_USERS_SYNC_INTERVAL);
        System.clearProperty(TASK_ASSIGNING_WAIT_FOR_IMPROVED_SOLUTION_DURATION);
        System.clearProperty(TASK_ASSIGNING_IMPROVE_SOLUTION_ON_BACKGROUND_DURATION);
    }
}