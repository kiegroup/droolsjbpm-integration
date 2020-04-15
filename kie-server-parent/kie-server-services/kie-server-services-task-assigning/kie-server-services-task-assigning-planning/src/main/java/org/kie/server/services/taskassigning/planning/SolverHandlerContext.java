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
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class SolverHandlerContext {

    private long changeSetIds;
    private long currentChangeSetId;
    private long lastProcessedChangeSetId = -1;
    private LocalDateTime previousQueryTime;
    private LocalDateTime nextQueryTime;
    private Map<Long, LocalDateTime> taskChangeTimes = new HashMap<>();
    private Duration queryShift;

    public SolverHandlerContext(Duration queryShift) {
        this.queryShift = queryShift;
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
            taskChangeTimes.entrySet().stream()
                    .filter(entry -> untilLocalDateTime.compareTo(entry.getValue()) > 0)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList())
                    .forEach(removableChange -> taskChangeTimes.remove(removableChange));
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
     * Gets the next query time to use.
     * @return the query time.
     */
    public LocalDateTime getNextQueryTime() {
        return nextQueryTime;
    }

    /**
     * Sets the next query time to use.
     * @param nextQueryTime the query time to set.
     */
    public void setNextQueryTime(LocalDateTime nextQueryTime) {
        this.nextQueryTime = nextQueryTime;
    }

    /**
     * Shifts a queryTime with the context configured queryShift.
     * @param queryTime a query time to shift.
     * @return the shifted query time or null if a null value is provided.
     */
    public LocalDateTime shiftQueryTime(LocalDateTime queryTime) {
        return queryTime != null ? queryTime.minus(queryShift.toMillis(), ChronoUnit.MILLIS) : null;
    }
}
