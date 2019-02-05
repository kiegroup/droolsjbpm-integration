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

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "case-milestone")
public class CaseMilestone {

    @XmlElement(name = "milestone-name")
    private String name;
    @XmlElement(name = "milestone-id")
    private String identifier;
    @XmlElement(name = "milestone-achieved")
    private boolean achieved;
    @XmlElement(name = "milestone-achieved-at")
    private Date achievedAt;
    @XmlElement(name = "milestone-status")
    private String status;

    public CaseMilestone() {

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public boolean isAchieved() {
        return achieved;
    }

    public void setAchieved(boolean achieved) {
        this.achieved = achieved;
    }

    public Date getAchievedAt() {
        return achievedAt;
    }

    public void setAchievedAt(Date achievedAt) {
        this.achievedAt = achievedAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private CaseMilestone milestone = new CaseMilestone();

        public CaseMilestone build() {
            return milestone;
        }

        public Builder id(String id) {
            milestone.setIdentifier(id);
            return this;
        }

        public Builder name(String name) {
            milestone.setName(name);
            return this;
        }

        public Builder achieved(boolean achieved) {
            milestone.setAchieved(achieved);
            return this;
        }

        public Builder achievedAt(Date achievedAt) {
            milestone.setAchievedAt(achievedAt);
            return this;
        }

        public Builder status(String status) {
            milestone.setStatus(status);
            return this;
        }
    }

    @Override
    public String toString() {
        return "CaseMilestone{" +
                "name='" + name + '\'' +
                ", achieved=" + achieved +
                ", achievedAt=" + achievedAt +
                '}';
    }
}
