/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.processmigration.model.exceptions;

import org.kie.processmigration.model.ProcessRef;

public class ProcessDefinitionNotFoundException extends InvalidMigrationException {

    private static final long serialVersionUID = 8119544485261592740L;

    private final String kieServerID;
    private final ProcessRef processRef;

    public ProcessDefinitionNotFoundException(String kieServerID, ProcessRef processRef) {
        super(kieServerID);
        this.kieServerID = kieServerID;
        this.processRef = processRef;
    }

    @Override
    public String getMessage() {
        return String.format("Cound not find ContainerID: %s and Process Definition: %s Running in KieServer: %s",
                             this.processRef.getContainerId(),
                             this.processRef.getProcessId(),
                             this.kieServerID);
    }
}
