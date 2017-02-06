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

package org.kie.server.api.model.cases;

import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "case-file")
public class CaseFile {

    @XmlElement(name="case-data")
    private Map<String, Object> data = new HashMap<>();

    @XmlElement(name="case-user-assignments")
    private Map<String, String> userAssignments = new HashMap<>();

    @XmlElement(name="case-group-assignments")
    private Map<String, String> groupAssignments = new HashMap<>();

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public Map<String, String> getUserAssignments() {
        return userAssignments;
    }

    public void setUserAssignments(Map<String, String> userAssignments) {
        this.userAssignments = userAssignments;
    }

    public Map<String, String> getGroupAssignments() {
        return groupAssignments;
    }

    public void setGroupAssignments(Map<String, String> groupAssignments) {
        this.groupAssignments = groupAssignments;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private CaseFile caseFile = new CaseFile();

        public CaseFile build() {
            return caseFile;
        }

        public Builder data(Map<String, Object> data) {
            caseFile.setData(data);
            return this;
        }

        public Builder userAssignments(Map<String, String> data) {
            caseFile.setUserAssignments(data);
            return this;
        }

        public Builder groupAssignments(Map<String, String> data) {
            caseFile.setGroupAssignments(data);
            return this;
        }

        public Builder addUserAssignments(String role, String user) {
            caseFile.getUserAssignments().put(role, user);
            return this;
        }

        public Builder addGroupAssignments(String role, String group) {
            caseFile.getGroupAssignments().put(role, group);
            return this;
        }
    }

    @Override
    public String toString() {
        return "CaseFile{" +
                "data=" + data +
                ", userAssignments=" + userAssignments +
                ", groupAssignments=" + groupAssignments +
                '}';
    }
}
