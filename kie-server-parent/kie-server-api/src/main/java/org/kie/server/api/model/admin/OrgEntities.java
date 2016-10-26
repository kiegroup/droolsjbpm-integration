/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.server.api.model.admin;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "org-entities")
public class OrgEntities {

    @XmlElement(name="users")
    private List<String> users;
    @XmlElement(name="groups")
    private List<String> groups;

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

        private OrgEntities orgEntities = new OrgEntities();

        public OrgEntities build() {
            return orgEntities;
        }

        public Builder users(List<String> users) {
            orgEntities.setUsers(new ArrayList<>(users));
            return this;
        }

        public Builder groups(List<String> groups) {
            orgEntities.setGroups(new ArrayList<>(groups));
            return this;
        }
    }

    @Override
    public String toString() {
        return "OrgEntities{" +
                ", users=" + users +
                ", groups=" + groups +
                '}';
    }
}
