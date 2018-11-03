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

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "process-config")
public class ProcessConfig extends ContainerConfig {

    @XmlElement(name = "runtimeStrategy")
    private String runtimeStrategy;
    @XmlElement(name = "kbase")
    private String kBase;
    @XmlElement(name = "ksession")
    private String kSession;
    @XmlElement(name = "mergeMode")
    private String mergeMode;

    public ProcessConfig() {
    }

    public ProcessConfig(String runtimeStrategy, String kBase, String kSession, String mergeMode) {
        this.runtimeStrategy = runtimeStrategy;
        this.kBase = kBase;
        this.kSession = kSession;
        this.mergeMode = mergeMode;
    }

    public String getRuntimeStrategy() {
        return runtimeStrategy;
    }

    public String getKBase() {
        return kBase;
    }

    public String getKSession() {
        return kSession;
    }

    public String getMergeMode() {
        return mergeMode;
    }

    public void setRuntimeStrategy(String runtimeStrategy) {
        this.runtimeStrategy = runtimeStrategy;
    }

    public void setKBase(String kBase) {
        this.kBase = kBase;
    }

    public void setKSession(String kSession) {
        this.kSession = kSession;
    }

    public void setMergeMode(String mergeMode) {
        this.mergeMode = mergeMode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ProcessConfig that = (ProcessConfig) o;

        if (kBase != null ? !kBase.equals(that.kBase) : that.kBase != null) {
            return false;
        }
        if (kSession != null ? !kSession.equals(that.kSession) : that.kSession != null) {
            return false;
        }
        if (mergeMode != null ? !mergeMode.equals(that.mergeMode) : that.mergeMode != null) {
            return false;
        }
        if (runtimeStrategy != null ? !runtimeStrategy.equals(that.runtimeStrategy) : that.runtimeStrategy != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = runtimeStrategy != null ? runtimeStrategy.hashCode() : 0;
        result = 31 * result + (kBase != null ? kBase.hashCode() : 0);
        result = 31 * result + (kSession != null ? kSession.hashCode() : 0);
        result = 31 * result + (mergeMode != null ? mergeMode.hashCode() : 0);
        return result;
    }
}
