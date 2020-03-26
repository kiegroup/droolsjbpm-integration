/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.services.taskassigning.runtime;

import org.kie.server.services.jbpm.JbpmKieServerExtension;

import static org.kie.server.services.taskassigning.runtime.TaskAssigningRuntimeKieServerExtension.EXTENSION_NAME;
import static org.kie.server.services.taskassigning.runtime.TaskAssigningRuntimeKieServerExtension.TASK_ASSIGNING_QUERY_DEFINITIONS_RESOURCE;

public class TaskAssigningRuntimeKieServerExtensionMessages {

    private TaskAssigningRuntimeKieServerExtensionMessages() {
    }

    static final String MISSING_REQUIRED_JBPM_EXTENSION_ERROR = JbpmKieServerExtension.EXTENSION_NAME + " extension is required for the task assigning api to work.";

    static final String QUERIES_INITIALIZATION_ERROR = "An error was produced during extension queries initialization, error: %s";

    static final String QUERIES_RESOURCE_NOT_FOUND = "Extension queries resource was not found: " + TASK_ASSIGNING_QUERY_DEFINITIONS_RESOURCE;

    static final String HEALTH_CHECK_ERROR = EXTENSION_NAME + " failed due to: %s";

    static final String HEALTH_CHECK_IS_ALIVE_MESSAGE = EXTENSION_NAME + " is alive";
}
