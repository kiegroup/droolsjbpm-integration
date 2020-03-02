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

import java.util.HashSet;
import java.util.Set;

public class ImmutableUser extends User {

    private ImmutableUser() {
        //required by the FieldSolutionCloner
    }

    ImmutableUser(long id, String entityId) {
        super(id, entityId);
        super.setGroups(new HashSet<>());
        super.setTypedLabels(new HashSet<>());
    }

    @Override
    public void setEntityId(String entityId) {
        throwImmutableException();
    }

    @Override
    public void setGroups(Set<Group> groups) {
        throwImmutableException();
    }

    @Override
    public void setTypedLabels(Set<TypedLabel> typedLabels) {
        throwImmutableException();
    }

    @Override
    public void setId(Long id) {
        throwImmutableException();
    }

    private void throwImmutableException() {
        throw new UnsupportedOperationException("PLANNING_USER: " + getEntityId() + " object can not be modified.");
    }
}
