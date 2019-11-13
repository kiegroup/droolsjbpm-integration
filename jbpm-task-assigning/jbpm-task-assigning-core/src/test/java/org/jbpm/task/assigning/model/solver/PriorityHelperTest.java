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

import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PriorityHelperTest {

    @Test
    public void isHighLevel() {
        Stream.of(0, 1, 2).forEach(level -> assertTrue(PriorityHelper.isHighLevel(level)));
    }

    @Test
    public void isMediumLevel() {
        Stream.of(3, 4, 5, 6).forEach(level -> assertTrue(PriorityHelper.isMediumLevel(level)));
    }

    @Test
    public void isLowLevel() {
        Stream.of(7, 8, 9, 10).forEach(level -> assertTrue(PriorityHelper.isLowLevel(level)));
    }

    @Test
    public void calculateWeightedPenaltySuccessful() {
        for (int priority = 0; priority < 11; priority++) {
            calculateWeightedPenaltySuccess(priority);
        }
    }

    private void calculateWeightedPenaltySuccess(int priority) {
        int endTime = 1234;
        int expectedValue = -(11 - priority) * endTime;
        assertEquals(expectedValue, PriorityHelper.calculateWeightedPenalty(priority, endTime));
    }

    @Test
    public void calculateWeightedPenaltyFailure() {
        String expectedMessage = "Task priority %s is out of range. " +
                "A valid priority value must be between 0 (inclusive) " +
                " and 10 (inclusive)";
        Stream.of(-2, -1, 11, 12).forEach(priority -> {
            Assertions.assertThatThrownBy(() -> {
                PriorityHelper.calculateWeightedPenalty(priority, 1234);
            }).hasMessage(String.format(expectedMessage, priority));
        });
    }
}
