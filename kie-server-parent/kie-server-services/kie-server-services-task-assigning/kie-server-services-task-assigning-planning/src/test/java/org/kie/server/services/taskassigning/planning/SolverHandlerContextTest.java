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

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class SolverHandlerContextTest {

    private static final long TASK_ID1 = 1;
    private static final long TASK_ID2 = 2;
    private static final long TASK_ID3 = 3;
    private static final long TASK_ID4 = 4;
    private static final int QUERY_TIMES_SIZE = 2;
    private static final long QUERY_MINIM_DISTANCE = 2000;

    private SolverHandlerContext context;

    @Before
    public void setUp() {
        context = new SolverHandlerContext(QUERY_TIMES_SIZE, QUERY_MINIM_DISTANCE);
    }

    @Test
    public void currentChangeSetId() {
        context.setCurrentChangeSetId(2);
        assertThat(2).isEqualTo(context.getCurrentChangeSetId());
    }

    @Test
    public void nextChangeSetId() {
        for (int i = 1; i < 10; i++) {
            assertThat(i).isEqualTo(context.nextChangeSetId());
        }
    }

    @Test
    public void isProcessedChangeSet() {
        context.setProcessedChangeSet(5);
        for (int i = 0; i <= 5; i++) {
            assertThat(context.isProcessedChangeSet(i)).isTrue();
        }
    }

    @Test
    public void clearProcessedChangeSet() {
        context.setProcessedChangeSet(5);
        assertThat(context.isProcessedChangeSet(5)).isTrue();
        context.clearProcessedChangeSet();
        assertThat(context.isProcessedChangeSet(5)).isFalse();
    }

    @Test
    public void getPreviousQueryTime() {
        LocalDateTime queryTime = LocalDateTime.now();
        context.setPreviousQueryTime(queryTime);
        assertThat(queryTime).isEqualTo(context.getPreviousQueryTime());
    }

    @Test
    public void setTaskChangeTime() {
        LocalDateTime taskChangeTime = LocalDateTime.now();
        context.setTaskChangeTime(TASK_ID1, taskChangeTime);
        assertThat(context.isProcessedTaskChange(TASK_ID1, taskChangeTime)).isTrue();
    }

    @Test
    public void isProcessedTaskChangeWithOtherChange() {
        LocalDateTime otherChangeTime = LocalDateTime.now();
        LocalDateTime taskChangeTime = otherChangeTime.plusMinutes(1);
        context.setTaskChangeTime(TASK_ID1, otherChangeTime);
        assertThat(context.isProcessedTaskChange(TASK_ID1, taskChangeTime)).isFalse();
    }

    @Test
    public void isProcessedTaskChangeWithNoChange() {
        LocalDateTime taskChangeTime = LocalDateTime.now();
        assertThat(context.isProcessedTaskChange(TASK_ID1, taskChangeTime)).isFalse();
    }

    @Test
    public void clearTaskChangeTimes() {
        List<Pair<Long, LocalDateTime>> taskChanges = Arrays.asList(Pair.of(TASK_ID1, LocalDateTime.now()),
                                                                    Pair.of(TASK_ID2, LocalDateTime.now()),
                                                                    Pair.of(TASK_ID3, LocalDateTime.now()),
                                                                    Pair.of(TASK_ID4, LocalDateTime.now()));
        taskChanges.forEach(taskChange -> context.setTaskChangeTime(taskChange.getLeft(), taskChange.getRight()));
        taskChanges.forEach(taskChange -> assertThat(context.isProcessedTaskChange(taskChange.getLeft(), taskChange.getRight())).isTrue());
        context.clearTaskChangeTimes();
        taskChanges.forEach(taskChange -> assertThat(context.isProcessedTaskChange(taskChange.getLeft(), taskChange.getRight())).isFalse());
    }

    @Test
    public void clearTaskChangeTimesWithFilter() {
        LocalDateTime changeTime = LocalDateTime.now();
        List<Pair<Long, LocalDateTime>> taskChanges = Arrays.asList(Pair.of(TASK_ID1, changeTime.plusMinutes(1)),
                                                                    Pair.of(TASK_ID2, changeTime.plusMinutes(2)),
                                                                    Pair.of(TASK_ID3, changeTime.plusMinutes(3)),
                                                                    Pair.of(TASK_ID4, changeTime.plusMinutes(4)));
        taskChanges.forEach(taskChange -> context.setTaskChangeTime(taskChange.getLeft(), taskChange.getRight()));
        taskChanges.forEach(taskChange -> assertThat(context.isProcessedTaskChange(taskChange.getLeft(), taskChange.getRight())).isTrue());

        int filterIndex = 2;
        context.clearTaskChangeTimes(taskChanges.get(filterIndex).getRight());
        for (int i = 0; i < filterIndex - 1; i++) {
            assertThat(context.isProcessedTaskChange(taskChanges.get(i).getLeft(), taskChanges.get(i).getRight())).isFalse();
        }
        for (int i = filterIndex; i < taskChanges.size(); i++) {
            assertThat(context.isProcessedTaskChange(taskChanges.get(i).getLeft(), taskChanges.get(i).getRight())).isTrue();
        }
    }

    @Test
    public void resetQueryTimes() {
        LocalDateTime value = LocalDateTime.now();
        context.resetQueryTimes(value);
        for (int i = 0; i < QUERY_TIMES_SIZE; i++) {
            assertThat(context.pollNextQueryTime()).isEqualTo(value);
        }
        assertThat(context.pollNextQueryTime()).isNull();
        assertThat(context.pollLastQueryTime()).isNull();
    }

    @Test
    public void pollNextQueryTime() {
        LocalDateTime value = LocalDateTime.now();
        LocalDateTime nextQueryTime = LocalDateTime.now();
        context.resetQueryTimes(value);
        context.addNextQueryTime(nextQueryTime);
        for (int i = 0; i < QUERY_TIMES_SIZE; i++) {
            assertThat(context.pollNextQueryTime()).isEqualTo(value);
        }
        assertThat(context.pollNextQueryTime()).isEqualTo(nextQueryTime);
    }

    @Test
    public void pollLastQueryTime() {
        LocalDateTime nextQueryTime = LocalDateTime.now();
        context.addNextQueryTime(nextQueryTime);
        assertThat(context.pollLastQueryTime()).isEqualTo(nextQueryTime);
    }

    @Test
    public void peekLastQueryTime() {
        LocalDateTime value1 = LocalDateTime.now();
        LocalDateTime value2 = LocalDateTime.now();
        context.addNextQueryTime(value1);
        context.addNextQueryTime(value2);
        assertThat(context.peekLastQueryTime()).isEqualTo(value2);
        assertThat(context.pollLastQueryTime()).isEqualTo(value2);
        assertThat(context.peekLastQueryTime()).isEqualTo(value1);
        assertThat(context.pollLastQueryTime()).isEqualTo(value1);
    }

    @Test
    public void hasMinimalDistanceTrue() {
        LocalDateTime previousQueryTime = LocalDateTime.now();
        LocalDateTime nextQueryTime = previousQueryTime.plus(QUERY_MINIM_DISTANCE + 1, ChronoUnit.MILLIS);
        assertThat(context.hasMinimalDistance(previousQueryTime, nextQueryTime)).isTrue();
        assertThat(context.hasMinimalDistance(null, nextQueryTime)).isTrue();
        assertThat(context.hasMinimalDistance(null, null)).isTrue();
    }

    @Test
    public void hasMinimalDistanceFalse() {
        LocalDateTime previousQueryTime = LocalDateTime.now();
        LocalDateTime nextQueryTime = previousQueryTime.plus(QUERY_MINIM_DISTANCE, ChronoUnit.MILLIS);
        assertThat(context.hasMinimalDistance(previousQueryTime, nextQueryTime)).isFalse();
        nextQueryTime = previousQueryTime.plus(QUERY_MINIM_DISTANCE - 1, ChronoUnit.MILLIS);
        assertThat(context.hasMinimalDistance(previousQueryTime, nextQueryTime)).isFalse();
    }
}