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
import java.time.format.DateTimeParseException;
import java.util.function.Predicate;

import static java.util.Objects.isNull;
import static org.apache.commons.lang3.StringUtils.trimToNull;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_IMPROVE_SOLUTION_ON_BACKGROUND_DURATION;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_PROCESS_RUNTIME_TARGET_USER;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_PUBLISH_WINDOW_SIZE;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_SYNC_INTERVAL;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_SYNC_QUERIES_SHIFT;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_USERS_SYNC_INTERVAL;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_WAIT_FOR_IMPROVED_SOLUTION_DURATION;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_INIT_DELAY;
import static org.kie.server.services.taskassigning.planning.TaskAssigningPlanningKieServerExtensionMessages.PARAMETER_MUST_HAVE_A_GREATER_OR_EQUAL_TO_ZERO_DURATION_VALUE_ERROR;
import static org.kie.server.services.taskassigning.planning.TaskAssigningPlanningKieServerExtensionMessages.PARAMETER_MUST_HAVE_A_GREATER_THAN_ZERO_DURATION_VALUE_ERROR;
import static org.kie.server.services.taskassigning.planning.TaskAssigningPlanningKieServerExtensionMessages.PARAMETER_MUST_HAVE_A_GREATER_THAN_ZERO_INTEGER_VALUE_ERROR;
import static org.kie.server.services.taskassigning.planning.TaskAssigningPlanningKieServerExtensionMessages.PARAMETER_MUST_HAVE_A_NON_EMPTY_STRING_VALUE_ERROR;
import static org.kie.server.services.taskassigning.planning.TaskAssigningPlanningKieServerExtensionMessages.PARAMETER_MUST_BE_ZERO_OR_GREATER_THAN_ZERO_LONG_VALUE_ERROR;

public class TaskAssigningPlanningKieServerExtensionHelper {

    static final int DEFAULT_PUBLISH_WINDOW_SIZE = 2;
    static final String DEFAULT_SYNC_INTERVAL = "PT2S";
    static final String DEFAULT_SYNC_QUERIES_SHIFT = "PT10M";
    static final String DEFAULT_USERS_SYNC_INTERVAL = "PT2H";
    static final String DEFAULT_WAIT_FOR_IMPROVED_SOLUTION_DURATION = "PT0S";
    static final String DEFAULT_IMPROVE_SOLUTION_ON_BACKGROUND_DURATION = "PT1M";
    static final long DEFAULT_INIT_DELAY = 0L;

    private static final String CAUSE = ", cause :";
	
    private TaskAssigningPlanningKieServerExtensionHelper() {
    }

    public static TaskAssigningServiceConfig readAndValidateTaskAssigningServiceConfig() throws TaskAssigningValidationException {
        String targetUserId;
        int publishWindowSize;
        Duration syncInterval;
        Duration syncQueriesShift;
        Duration usersSyncInterval;
        Duration waitForImprovedSolutionDuration;
        Duration improveSolutionOnBackgroundDuration;
        long initDelay;

        targetUserId = trimToNull(System.getProperty(TASK_ASSIGNING_PROCESS_RUNTIME_TARGET_USER));
        if (isNull(targetUserId)) {
            throw new TaskAssigningValidationException(String.format(PARAMETER_MUST_HAVE_A_NON_EMPTY_STRING_VALUE_ERROR,
                                                                     TASK_ASSIGNING_PROCESS_RUNTIME_TARGET_USER));
        }
        publishWindowSize = parseAndValidateGreaterThanZeroInteger(System.getProperty(TASK_ASSIGNING_PUBLISH_WINDOW_SIZE, Integer.toString(DEFAULT_PUBLISH_WINDOW_SIZE)),
                                                                   String.format(PARAMETER_MUST_HAVE_A_GREATER_THAN_ZERO_INTEGER_VALUE_ERROR,
                                                                                 TASK_ASSIGNING_PUBLISH_WINDOW_SIZE));

        syncInterval = parseAndValidateGreaterThanZeroDuration(System.getProperty(TASK_ASSIGNING_SYNC_INTERVAL, DEFAULT_SYNC_INTERVAL),
                                                               String.format(PARAMETER_MUST_HAVE_A_GREATER_THAN_ZERO_DURATION_VALUE_ERROR,
                                                                             TASK_ASSIGNING_SYNC_INTERVAL));

        syncQueriesShift = parseAndValidateGreaterThanZeroDuration(System.getProperty(TASK_ASSIGNING_SYNC_QUERIES_SHIFT, DEFAULT_SYNC_QUERIES_SHIFT),
                                                                   String.format(PARAMETER_MUST_HAVE_A_GREATER_THAN_ZERO_DURATION_VALUE_ERROR,
                                                                                 TASK_ASSIGNING_SYNC_QUERIES_SHIFT));

        usersSyncInterval = parseAndValidateGreaterOrEqualThanZeroDuration(System.getProperty(TASK_ASSIGNING_USERS_SYNC_INTERVAL, DEFAULT_USERS_SYNC_INTERVAL),
                                                                           String.format(PARAMETER_MUST_HAVE_A_GREATER_OR_EQUAL_TO_ZERO_DURATION_VALUE_ERROR,
                                                                                         TASK_ASSIGNING_USERS_SYNC_INTERVAL));

        waitForImprovedSolutionDuration = parseAndValidateGreaterOrEqualThanZeroDuration(System.getProperty(TASK_ASSIGNING_WAIT_FOR_IMPROVED_SOLUTION_DURATION, DEFAULT_WAIT_FOR_IMPROVED_SOLUTION_DURATION),
                                                                                         String.format(PARAMETER_MUST_HAVE_A_GREATER_OR_EQUAL_TO_ZERO_DURATION_VALUE_ERROR,
                                                                                                       TASK_ASSIGNING_WAIT_FOR_IMPROVED_SOLUTION_DURATION));

        improveSolutionOnBackgroundDuration = parseAndValidateGreaterOrEqualThanZeroDuration(System.getProperty(TASK_ASSIGNING_IMPROVE_SOLUTION_ON_BACKGROUND_DURATION, DEFAULT_IMPROVE_SOLUTION_ON_BACKGROUND_DURATION),
                                                                                             String.format(PARAMETER_MUST_HAVE_A_GREATER_OR_EQUAL_TO_ZERO_DURATION_VALUE_ERROR,
                                                                                                           TASK_ASSIGNING_IMPROVE_SOLUTION_ON_BACKGROUND_DURATION));

        initDelay = parseAndValidateEqualsOrGreaterThanZeroLong(System.getProperty(TASK_ASSIGNING_INIT_DELAY, Long.toString(DEFAULT_INIT_DELAY)),
                                                                String.format(PARAMETER_MUST_BE_ZERO_OR_GREATER_THAN_ZERO_LONG_VALUE_ERROR,
                                                                              TASK_ASSIGNING_INIT_DELAY));
        
        return new TaskAssigningServiceConfig(targetUserId, publishWindowSize, syncInterval, syncQueriesShift,
                                              usersSyncInterval, waitForImprovedSolutionDuration, improveSolutionOnBackgroundDuration, initDelay);
    }

    private static Duration parseAndValidateGreaterThanZeroDuration(String value, String validationErrorMessage) throws TaskAssigningValidationException {
        return parseAndValidateDuration(value, duration -> duration.toMillis() > 0, validationErrorMessage);
    }

    private static Duration parseAndValidateGreaterOrEqualThanZeroDuration(String value, String validationErrorMessage) throws TaskAssigningValidationException {
        return parseAndValidateDuration(value, duration -> duration.toMillis() >= 0, validationErrorMessage);
    }

    private static Duration parseAndValidateDuration(String value, Predicate<Duration> validation, String validationErrorMessage) throws TaskAssigningValidationException {
        Duration result;
        try {
            result = Duration.parse(value);
        } catch (DateTimeParseException e) {
            String msg = validationErrorMessage + CAUSE + e.toString();
            throw new TaskAssigningValidationException(msg);
        }
        if (validation.negate().test(result)) {
            throw new TaskAssigningValidationException(validationErrorMessage);
        }
        return result;
    }

    private static int parseAndValidateGreaterThanZeroInteger(String value, String validationErrorMessage) throws TaskAssigningValidationException {
        int result;
        try {
            result = Integer.parseInt(value);
        } catch (NumberFormatException e) {
            String msg = validationErrorMessage + CAUSE + e.toString();
            throw new TaskAssigningValidationException(msg);
        }
        if (result <= 0) {
            throw new TaskAssigningValidationException(validationErrorMessage);
        }
        return result;
    }
    
    private static long parseAndValidateEqualsOrGreaterThanZeroLong(String value, String validationErrorMessage) throws TaskAssigningValidationException {
    	long result;
        try {
            result = Long.parseLong(value);
        } catch (NumberFormatException e) {
            String msg = validationErrorMessage + CAUSE + e.toString();
            throw new TaskAssigningValidationException(msg);
        }
        if (result < 0) {
            throw new TaskAssigningValidationException(validationErrorMessage);
        }
        return result;
	}
}
