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

public class ProcessNotFoundException extends InvalidMigrationException {

    private static final long serialVersionUID = 8119544485261592740L;

    public ProcessNotFoundException(String containerId) {
        super(containerId);
    }

    @Override
    public String getMessage() {
        return String.format("Missing Process with ContainerID: %s", super.getMessage());
    }
}
