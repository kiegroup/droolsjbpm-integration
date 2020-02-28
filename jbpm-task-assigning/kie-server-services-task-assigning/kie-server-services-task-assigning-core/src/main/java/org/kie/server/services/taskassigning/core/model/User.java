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

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("TaUser")
public class User extends TaskOrUser implements OrganizationalEntity {

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
