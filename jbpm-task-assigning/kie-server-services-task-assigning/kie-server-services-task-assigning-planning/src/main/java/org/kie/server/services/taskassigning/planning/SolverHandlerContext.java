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

public class SolverHandlerContext {

    private long changeSetIds;
    private long currentChangeSetId;
    private long lastProcessedChangeSetId = -1;
    private LocalDateTime lastModificationDate;

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

    public LocalDateTime getLastModificationDate() {
        return lastModificationDate;
    }

    public void setLastModificationDate(LocalDateTime lastModificationDate) {
        this.lastModificationDate = lastModificationDate;
    }
}
