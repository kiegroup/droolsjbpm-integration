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

package org.kie.server.api.model;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.thoughtworks.xstream.annotations.XStreamAlias;

@XmlRootElement(name = "kie-scanner")
@XStreamAlias( "kie-scanner" )
public class KieScannerResource {

    @XStreamAlias( "status" )
    private KieScannerStatus status;
    @XStreamAlias( "poll-interval" )
    private Long             pollInterval;

    public KieScannerResource() {
    }

    public KieScannerResource(KieScannerStatus status) {
        this(status, null);
    }

    public KieScannerResource(KieScannerStatus status, Long interval) {
        this.status = status;
        this.pollInterval = interval;
    }

    @XmlAttribute(name = "status")
    public KieScannerStatus getStatus() {
        return status;
    }

    public void setStatus(KieScannerStatus status) {
        this.status = status;
    }

    @XmlAttribute(name = "poll-interval")
    public Long getPollInterval() {
        return pollInterval;
    }

    public void setPollInterval(Long pollInterval) {
        this.pollInterval = pollInterval;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((pollInterval == null) ? 0 : pollInterval.hashCode());
        result = prime * result + ((status == null) ? 0 : status.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        KieScannerResource other = (KieScannerResource) obj;
        if (pollInterval == null) {
            if (other.pollInterval != null)
                return false;
        } else if (!pollInterval.equals(other.pollInterval))
            return false;
        if (status != other.status)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "KieScannerResource [status=" + status + ", pollInterval=" + pollInterval + "]";
    }

}
