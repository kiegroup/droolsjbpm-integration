/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "case-file-data-item")
public class CaseFileDataItem {

    @XmlElement(name = "case-id")
    private String caseId;
    @XmlElement(name = "name")
    private String name;
    @XmlElement(name = "value")
    private String value;
    @XmlElement(name = "type")
    private String type;
    @XmlElement(name = "last-modified-by")
    private String lastModifiedBy;
    @XmlElement(name = "last-modified")
    private Date lastModified;

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public Date getLastModified() {
        return lastModified;
    }

    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private CaseFileDataItem caseFileDataItem = new CaseFileDataItem();

        public CaseFileDataItem build() {
            return caseFileDataItem;
        }

        public Builder caseId(String caseId) {
            caseFileDataItem.setCaseId(caseId);
            return this;
        }

        public Builder name(String name) {
            caseFileDataItem.setName(name);
            return this;
        }

        public Builder value(String value) {
            caseFileDataItem.setValue(value);
            return this;
        }

        public Builder type(String type) {
            caseFileDataItem.setType(type);
            return this;
        }

        public Builder lastModifiedBy(String lastModifiedBy) {
            caseFileDataItem.setLastModifiedBy(lastModifiedBy);
            return this;
        }

        public Builder lastModified(Date lastModified) {
            caseFileDataItem.setLastModified(lastModified);
            return this;
        }
    }

    @Override
    public String toString() {
        return "CaseFileDataItem{" +
                "caseId='" + caseId + '\'' +
                ", name='" + name + '\'' +
                ", value='" + value + '\'' +
                ", type='" + type + '\'' +
                ", lastModifiedBy='" + lastModifiedBy + '\'' +
                ", lastModified=" + lastModified +
                '}';
    }
}
