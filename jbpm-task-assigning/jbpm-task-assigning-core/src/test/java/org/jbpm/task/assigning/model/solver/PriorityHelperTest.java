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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PriorityHelperTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void isHighLevelTest() {
        Stream.of(0, 1, 2).forEach(level -> assertTrue(PriorityHelper.isHighLevel(level)));
    }

    @Test
    public void isMediumLevelTest() {
        Stream.of(3, 4, 5, 6).forEach(level -> assertTrue(PriorityHelper.isMediumLevel(level)));
    }

    @Test
    public void isLowLevelTest() {
        Stream.of(7, 8, 9, 10).forEach(level -> assertTrue(PriorityHelper.isLowLevel(level)));
    }

    @Test
    public void calculateWeightedPenaltySuccessfulTest() {
        for (int priority = 0; priority < 11; priority++) {
            calculateWeightedPenaltySuccessTest(priority);
        }
    }

    private void calculateWeightedPenaltySuccessTest(int priority) {
        int endTime = 1234;
        int expectedValue = -(11 - priority) * endTime;
        assertEquals(expectedValue, PriorityHelper.calculateWeightedPenalty(priority, endTime));
    }

    @Test
    public void calculateWeightedPenaltyFailureTest() {
        String expectedMessage = "Task priority %s is out of range. " +
                "A valid priority value must be between 0 (inclusive) " +
                " and 10 (inclusive)";
        Stream.of(-2, -1, 11, 12).forEach(priority -> {
            expectedException.expectMessage(String.format(expectedMessage, priority));
            PriorityHelper.calculateWeightedPenalty(priority, 1234);
        });
    }
}
