/*
 * Copyright 2015 JBoss Inc
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

package org.kie.server.api.model.instance;

import java.util.Date;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "variable-instance")
public class VariableInstance {

    @XmlElement(name="name")
    private String variableName;

    @XmlElement(name="old-value")
    private String oldValue;

    @XmlElement(name="value")
    private String value;

    @XmlElement(name="process-instance-id")
    private Long processInstanceId;

    @XmlElement(name="modification-date")
    private Date date;

    public VariableInstance() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getVariableName() {
        return variableName;
    }

    public void setVariableName(String variableName) {
        this.variableName = variableName;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Long getProcessInstanceId() {
        return processInstanceId;
    }

    public void setProcessInstanceId(Long processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public String toString() {
        return "VariableInstance{" +
                "variableName='" + variableName + '\'' +
                ", oldValue='" + oldValue + '\'' +
                ", value='" + value + '\'' +
                ", processInstanceId=" + processInstanceId +
                ", date=" + date +
                '}';
    }

    public static class Builder {

        private VariableInstance variableInstance = new VariableInstance();

        public VariableInstance build() {
            return variableInstance;
        }

        public Builder name(String name) {
            variableInstance.setVariableName(name);
            return this;
        }

        public Builder value(String value) {
            variableInstance.setValue(value);
            return this;
        }

        public Builder oldValue(String oldValue) {
            variableInstance.setOldValue(oldValue);
            return this;
        }

        public Builder processInstanceId(Long processInstanceId) {
            variableInstance.setProcessInstanceId(processInstanceId);
            return this;
        }

        public Builder date(Date date) {
            variableInstance.setDate(date == null ? date : new Date(date.getTime()));
            return this;
        }
    }
}
