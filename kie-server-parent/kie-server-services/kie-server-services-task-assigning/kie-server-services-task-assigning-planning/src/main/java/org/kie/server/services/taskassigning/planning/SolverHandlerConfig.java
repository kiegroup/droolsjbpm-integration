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

import static org.kie.soup.commons.validation.Preconditions.checkGreaterOrEqualTo;
import static org.kie.soup.commons.validation.Preconditions.checkGreaterThan;

public class SolverHandlerConfig {

    private String targetUserId;
    private int publishWindowSize;
    private Duration syncInterval;
    private Duration syncQueriesShift;
    private Duration usersSyncInterval;
    private Duration waitForImprovedSolutionDuration;
    private Duration improveSolutionOnBackgroundDuration;
    private long initDelay;

    public SolverHandlerConfig(String targetUserId,
                               int publishWindowSize,
                               Duration syncInterval,
                               Duration syncQueriesShift,
                               Duration usersSyncInterval,
                               Duration waitForImprovedSolutionDuration,
                               Duration improveSolutionOnBackgroundDuration,
                               long initDelay) {
        if (targetUserId == null || targetUserId.isEmpty()) {
            throw new IllegalArgumentException("A non empty targetUserId is expected.");
        }
        checkGreaterThan("publishWindowSize", publishWindowSize, 0);
        checkGreaterThan("syncInterval", syncInterval, Duration.ZERO);
        checkGreaterThan("syncQueriesShift", syncQueriesShift, Duration.ZERO);
        checkGreaterOrEqualTo("usersSyncInterval", usersSyncInterval, Duration.ZERO);
        checkGreaterOrEqualTo("waitForImprovedSolutionDuration", waitForImprovedSolutionDuration, Duration.ZERO);
        checkGreaterOrEqualTo("improveSolutionOnBackgroundDuration", improveSolutionOnBackgroundDuration, Duration.ZERO);
        checkGreaterOrEqualTo("initDelay", initDelay, 0L);
        this.targetUserId = targetUserId;
        this.publishWindowSize = publishWindowSize;
        this.syncInterval = syncInterval;
        this.syncQueriesShift = syncQueriesShift;
        this.usersSyncInterval = usersSyncInterval;
        this.waitForImprovedSolutionDuration = waitForImprovedSolutionDuration;
        this.improveSolutionOnBackgroundDuration = improveSolutionOnBackgroundDuration;
        this.initDelay = initDelay;
    }

    public String getTargetUserId() {
        return targetUserId;
    }

    public int getPublishWindowSize() {
        return publishWindowSize;
    }

    public Duration getSyncInterval() {
        return syncInterval;
    }

    public Duration getSyncQueriesShift() {
        return syncQueriesShift;
    }

    public Duration getUsersSyncInterval() {
        return usersSyncInterval;
    }

    public Duration getWaitForImprovedSolutionDuration() {
        return waitForImprovedSolutionDuration;
    }

    public Duration getImproveSolutionOnBackgroundDuration() {
        return improveSolutionOnBackgroundDuration;
    }

    public long getInitDelay() {
        return initDelay;
    }
}
