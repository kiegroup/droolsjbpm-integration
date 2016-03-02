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

package org.kie.server.controller.api.model.events;

import org.kie.server.api.model.ReleaseId;
import org.kie.server.controller.api.model.spec.ContainerSpecKey;
import org.kie.server.controller.api.model.spec.RuleConfig;

public class RuleConfigUpdated {

    private ContainerSpecKey containerSpecKey;
    private RuleConfig ruleConfig;
    private ReleaseId releasedId;

    public ContainerSpecKey getContainerSpecKey() {
        return containerSpecKey;
    }

    public RuleConfig getRuleConfig() {
        return ruleConfig;
    }

    public ReleaseId getReleasedId() {
        return releasedId;
    }

    public void setContainerSpecKey(ContainerSpecKey containerSpecKey) {
        this.containerSpecKey = containerSpecKey;
    }

    public void setRuleConfig(RuleConfig ruleConfig) {
        this.ruleConfig = ruleConfig;
    }

    public void setReleasedId(ReleaseId releasedId) {
        this.releasedId = releasedId;
    }

    @Override
    public String toString() {
        return "RuleConfigUpdated{" +
                "containerSpecKey=" + containerSpecKey +
                ", ruleConfig=" + ruleConfig +
                ", releasedId=" + releasedId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        RuleConfigUpdated that = (RuleConfigUpdated) o;

        if (containerSpecKey != null ? !containerSpecKey.equals(that.containerSpecKey) : that.containerSpecKey != null) {
            return false;
        }
        if (releasedId != null ? !releasedId.equals(that.releasedId) : that.releasedId != null) {
            return false;
        }
        if (ruleConfig != null ? !ruleConfig.equals(that.ruleConfig) : that.ruleConfig != null) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = containerSpecKey != null ? containerSpecKey.hashCode() : 0;
        result = 31 * result + (ruleConfig != null ? ruleConfig.hashCode() : 0);
        result = 31 * result + (releasedId != null ? releasedId.hashCode() : 0);
        return result;
    }
}
