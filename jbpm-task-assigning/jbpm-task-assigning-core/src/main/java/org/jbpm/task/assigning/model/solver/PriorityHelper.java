/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.task.assigning.model.solver;

import org.jbpm.task.assigning.TaskAssigningRuntimeException;

/**
 * Helper class for manging priority calculations for tasks coming from the jBPM runtime.
 * By convention jBPM tasks priorities goes from  [0 (high),... 5 (medium),... 10 (low)]
 */
public class PriorityHelper {

    private PriorityHelper() {
    }

    public static boolean isHighLevel(int priority) {
        return priority == 0 || priority == 1 || priority == 2;
    }

    public static boolean isMediumLevel(int priority) {
        return priority == 3 || priority == 4 || priority == 5 || priority == 6;
    }

    public static boolean isLowLevel(int priority) {
        return priority == 7 || priority == 8 || priority == 9 || priority == 10;
    }

    public static int calculateWeightedPenalty(int priority, int endTime) {
        assertValue(priority);
        return -(11 - priority) * endTime;
    }

    private static void assertValue(int priority) {
        if (priority < 0 || priority > 10) {
            throw new IllegalArgumentException(String.format("Task priority %s is out of range. " +
                                                                     "A valid priority value must be between 0 (inclusive) " +
                                                                     " and 10 (inclusive)", priority));
        }
    }
}
