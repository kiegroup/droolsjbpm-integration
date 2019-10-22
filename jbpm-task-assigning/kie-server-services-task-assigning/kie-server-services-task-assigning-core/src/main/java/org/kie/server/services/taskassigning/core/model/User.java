/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
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
import java.util.function.Predicate;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("TaUser")
public class User extends TaskOrUser implements OrganizationalEntity {

    private static class ImmutableUser extends User {

        private ImmutableUser() {
            //required by the FieldSolutionCloner
        }

        private ImmutableUser(long id, String entityId) {
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

    /**
     * System property for configuring the PLANNING_USER entityId.
     */
    private static final String PLANNING_USER_ID_PROPERTY = "org.jbpm.task.assigning.model.planningUserId";

    private static final String PLANNING_USER_ID = System.getProperty(PLANNING_USER_ID_PROPERTY, "planning_user");

    /**
     * Planning user is defined user for avoid breaking hard constraints. When no user is found that met the task required
     * potential owners set, or the required skills set, etc, the PLANNING_USER is assigned.
     */
    public static final User PLANNING_USER = new ImmutableUser(PLANNING_USER_ID.hashCode(), PLANNING_USER_ID);

    public static final Predicate<String> IS_PLANNING_USER = entityId -> PLANNING_USER.getEntityId().equals(entityId);

    private String entityId;
    private Set<Group> groups = new HashSet<>();
    private Set<TypedLabel> typedLabels = new HashSet<>();

    public User() {
    }

    public User(long id, String entityId) {
        super(id);
        this.entityId = entityId;
    }

    @Override
    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public Set<Group> getGroups() {
        return groups;
    }

    public void setGroups(Set<Group> groups) {
        this.groups = groups;
    }

    public Set<TypedLabel> getTypedLabels() {
        return typedLabels;
    }

    public void setTypedLabels(Set<TypedLabel> typedLabels) {
        this.typedLabels = typedLabels;
    }

    @Override
    public User getUser() {
        return this;
    }

    @Override
    public boolean isUser() {
        return true;
    }

    @Override
    public Integer getEndTimeInMinutes() {
        return 0;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", entityId='" + entityId + '\'' +
                ", groups=" + groups +
                ", typedLabels=" + typedLabels +
                '}';
    }
}
