/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "job-request-instance")
public class JobRequestInstance {

    @XmlElement(name="job-command")
    private String command;

    @XmlElement(name="scheduled-date")
    private Date scheduledDate;

    @XmlElement(name="request-data")
    private Map<String, Object> data;

    public JobRequestInstance() {
    }

    public static Builder builder() {
        return new Builder();
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Date getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(Date scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public Map<String, Object> getData() {
        if (this.data == null) {
            this.data = new HashMap<String, Object>();
        }
        return this.data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "JobRequestInstance{" +
                "command='" + command + '\'' +
                ", scheduledDate=" + scheduledDate +
                ", data=" + data +
                '}';
    }

    public static class Builder {

        private JobRequestInstance jobRequestInstance = new JobRequestInstance();

        public JobRequestInstance build() {
            return jobRequestInstance;
        }


        public Builder command(String command) {
            jobRequestInstance.setCommand(command);
            return this;
        }

        public Builder scheduledDate(Date date) {
            jobRequestInstance.setScheduledDate(date == null ? date : new Date(date.getTime()));
            return this;
        }

        public Builder data(Map<String, Object> data) {
            jobRequestInstance.setData(data);
            return this;
        }
    }
}
