/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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
package org.kie.server.services.jbpm.kafka;

import org.jbpm.services.api.model.SignalDesc;

class SignalInfo {

    private final String deploymentId;
    private final SignalDesc signalDesc;

    public SignalInfo(String deploymentId, SignalDesc signalDesc) {
        this.deploymentId = deploymentId;
        this.signalDesc = signalDesc;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public SignalDesc getSignalDesc() {
        return signalDesc;
    }

    @Override
    public String toString() {
        return "SignalInfo [deploymentId=" + deploymentId + ", signalDesc=" + signalDesc + "]";
    }
}
