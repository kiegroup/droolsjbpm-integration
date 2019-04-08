/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.api.model.cases;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "case-role-assignment")
public class CaseRoleAssignment {

    @XmlElement(name = "name")
    private String name;
    @XmlElement(name = "users")
    private List<String> users;
    @XmlElement(name = "groups")
    private List<String> groups;

    public CaseRoleAssignment() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }

    public List<String> getGroups() {
        return groups;
    }

    public void setGroups(List<String> groups) {
        this.groups = groups;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private CaseRoleAssignment adHocFragment = new CaseRoleAssignment();

        public CaseRoleAssignment build() {
            return adHocFragment;
        }

        public Builder users(List<String> users) {
            adHocFragment.setUsers(users);
            return this;
        }

        public Builder groups(List<String> groups) {
            adHocFragment.setGroups(groups);
            return this;
        }

        public Builder name(String name) {
            adHocFragment.setName(name);
            return this;
        }
    }

    @Override
    public String toString() {
        return "CaseRoleAssignment{" +
                "name='" + name + '\'' +
                ", users=" + users +
                ", groups=" + groups +
                '}';
    }
}
