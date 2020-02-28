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

package org.kie.server.services.taskassigning.core.model;

import java.util.Map;
import java.util.Set;

public class ImmutableTask extends Task {

    private ImmutableTask() {
        //required by the FieldSolutionCloner.
    }

    ImmutableTask(long id, long processInstanceId, String processId, String containerId, String name,
                  int priority, Map<String, Object> inputData, boolean pinned,
                  Set<OrganizationalEntity> potentialOwners, Set<TypedLabel> typedLabels) {
        super(id, processInstanceId, processId, containerId, name, priority, inputData, pinned, potentialOwners,
              typedLabels);
    }

    @Override
    public void setPinned(boolean pinned) {
        //this task can never be pined
    }

    @Override
    public void setProcessInstanceId(long processInstanceId) {
        throwImmutableException("processInstanceId");
    }

    @Override
    public void setProcessId(String processId) {
        throwImmutableException("processId");
    }

    @Override
    public void setContainerId(String containerId) {
        throwImmutableException("containerId");
    }

    @Override
    public void setName(String name) {
        throwImmutableException("name");
    }

    @Override
    public void setPriority(int priority) {
        throwImmutableException("priority");
    }

    @Override
    public void setInputData(Map<String, Object> inputData) {
        throwImmutableException("inputData");
    }

    @Override
    public void setPotentialOwners(Set<OrganizationalEntity> potentialOwners) {
        throwImmutableException("potentialOwners");
    }

    @Override
    public void setTypedLabels(Set<TypedLabel> typedLabels) {
        throwImmutableException("typedLabels");
    }

    private void throwImmutableException(String filedName) {
        throw new UnsupportedOperationException("Task: " + getName() + " don't accept modifications of field: " + filedName);
    }
}
