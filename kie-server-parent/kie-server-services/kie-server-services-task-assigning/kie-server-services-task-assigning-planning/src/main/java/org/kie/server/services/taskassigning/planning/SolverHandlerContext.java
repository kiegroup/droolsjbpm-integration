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
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SolverHandlerContext {

    private long changeSetIds;
    private long currentChangeSetId;
    private long lastProcessedChangeSetId = -1;
    private LocalDateTime previousQueryTime;
    private Map<Long, LocalDateTime> taskChangeTimes = new HashMap<>();

    private Deque<LocalDateTime> queryTimes = new LinkedList<>();
    private int queryTimesSize;
    private long queryMinimumDistance;

    public SolverHandlerContext(int queryTimesSize, long queryMinimumDistance) {
        this.queryTimesSize = queryTimesSize;
        this.queryMinimumDistance = queryMinimumDistance;
    }

    public long getCurrentChangeSetId() {
        return currentChangeSetId;
    }

    public void setCurrentChangeSetId(long currentChangeSetId) {
        this.currentChangeSetId = currentChangeSetId;
    }

    public long nextChangeSetId() {
        return ++changeSetIds;
    }

    public boolean isProcessedChangeSet(long changeSetId) {
        return changeSetId <= lastProcessedChangeSetId;
    }

    public void setProcessedChangeSet(long changeSetId) {
        this.lastProcessedChangeSetId = changeSetId;
    }

    public void clearProcessedChangeSet() {
        lastProcessedChangeSetId = -1;
    }

    /**
     * Registers the time of the last change processed for a task.
     * @param taskId identifier of the task to register.
     * @param changeTime the task change time to register.
     */
    public void setTaskChangeTime(long taskId, LocalDateTime changeTime) {
        taskChangeTimes.put(taskId, changeTime);
    }

    /**
     * Indicates if a change has already been processed for a given task.
     * @param taskId identifier of the task to query.
     * @param changeTime the task change time to query.
     * @return true if the change has already been processed, false in any other case.
     */
    public boolean isProcessedTaskChange(long taskId, LocalDateTime changeTime) {
        return changeTime.equals(taskChangeTimes.get(taskId));
    }

    /**
     * Removes all the registered task change times that occurred strictly before a given time.
     * @param untilLocalDateTime the time for filtering the changes to remove.
     */
    public void clearTaskChangeTimes(LocalDateTime untilLocalDateTime) {
        if (untilLocalDateTime != null) {
            final List<Long> removableChanges = taskChangeTimes.entrySet().stream()
                    .filter(entry -> untilLocalDateTime.compareTo(entry.getValue()) > 0)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
            removableChanges.forEach(removableChange -> taskChangeTimes.remove(removableChange));
        }
    }

    /**
     * Removes all the registered task time changes.
     */
    public void clearTaskChangeTimes() {
        taskChangeTimes.clear();
    }

    /**
     * Gets the previously executed query time.
     * @return the previously executed query time
     */
    public LocalDateTime getPreviousQueryTime() {
        return previousQueryTime;
    }

    /**
     * Sets the previously executed query time.
     * @param previousQueryTime the query time to set.
     */
    public void setPreviousQueryTime(LocalDateTime previousQueryTime) {
        this.previousQueryTime = previousQueryTime;
    }

    /**
     * Resets the registered query times by using a start value.
     * @param startValue the start value for filling the initial query times.
     */
    public void resetQueryTimes(LocalDateTime startValue) {
        queryTimes.clear();
        for (int i = 0; i < queryTimesSize; i++) {
            queryTimes.add(startValue);
        }
    }

    /**
     * Retrieves and removes the first available query time to process.
     * @return the next available query time.
     */
    public LocalDateTime pollNextQueryTime() {
        return queryTimes.pollFirst();
    }

    /**
     * Retrieves and removes the last available query time.
     * @return the last available query time.
     */
    public LocalDateTime pollLastQueryTime() {
        return queryTimes.pollLast();
    }

    public LocalDateTime peekLastQueryTime() {
        return queryTimes.peekLast();
    }

    /**
     * Adds a new value to the available query times.
     * @param nextQueryTime the value to add.
     */
    public void addNextQueryTime(LocalDateTime nextQueryTime) {
        queryTimes.add(nextQueryTime);
    }

    /**
     * Indicates if two query times has the minimum configured separation.
     * @param lastQueryTime the previous query time for the calculation.
     * @param nextQueryTime the next query time for the calculation.
     * @return true if the distance between the query times is at least the expected one, false in any other case.
     */
    public boolean hasMinimalDistance(LocalDateTime lastQueryTime, LocalDateTime nextQueryTime) {
        if (lastQueryTime == null || nextQueryTime == null) {
            return true;
        }
        long lastQueryTimeMillis = lastQueryTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        long nextQueryTimeMillis = nextQueryTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return nextQueryTimeMillis - lastQueryTimeMillis > queryMinimumDistance;
    }
}
