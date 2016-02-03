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

package org.kie.server.controller.api.model.spec;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.kie.server.api.model.KieScannerStatus;
import org.kie.server.controller.api.model.spec.RuleConfig;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "rule-config")
public class RuleConfig extends ContainerConfig {

    @XmlElement(name = "poll-interval")
    private Long pollInterval;
    @XmlElement(name = "scanner-status")
    private KieScannerStatus scannerStatus;

    public RuleConfig() {
    }

    public RuleConfig(Long pollInterval, KieScannerStatus scannerStatus) {
        this.pollInterval = pollInterval;
        this.scannerStatus = scannerStatus;
    }

    public Long getPollInterval() {
        return pollInterval;
    }

    public KieScannerStatus getScannerStatus() {
        return scannerStatus;
    }

    public void setPollInterval(Long pollInterval) {
        this.pollInterval = pollInterval;
    }

    public void setScannerStatus(KieScannerStatus scannerStatus) {
        this.scannerStatus = scannerStatus;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RuleConfig that = (RuleConfig) o;

        if (pollInterval != null ? !pollInterval.equals(that.pollInterval) : that.pollInterval != null) {
            return false;
        }
        if (scannerStatus != that.scannerStatus) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = pollInterval != null ? pollInterval.hashCode() : 0;
        result = 31 * result + (scannerStatus != null ? scannerStatus.hashCode() : 0);
        return result;
    }
}
