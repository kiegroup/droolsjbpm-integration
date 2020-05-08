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

package org.kie.server.services.taskassigning.planning;

import static org.kie.server.services.taskassigning.planning.TaskAssigningPlanningKieServerExtension.EXTENSION_NAME;

public class TaskAssigningPlanningKieServerExtensionMessages {

    private TaskAssigningPlanningKieServerExtensionMessages() {
    }

    static final String PLANNER_EXTENSION_MESSAGE_PREFIX = EXTENSION_NAME + ": %s";

    static final String EXTENSION_WONT_OPERATE_PROPERLY_ERROR_PART = EXTENSION_NAME + " won't operate properly";

    static final String CREATE_CONTAINER_ERROR = "Container creation failed for containerId: %s, error: %s";

    static final String ACTIVATE_CONTAINER_ERROR = "Container activation failed for containerId: %s, error: %s";

    static final String EXTENSION_CONTAINER_NOT_IN_EXPECTED_STATUS_ERROR = "Container %s must be in %s status but is currently %s." +
            " " + EXTENSION_WONT_OPERATE_PROPERLY_ERROR_PART;

    static final String CONTAINER_NOT_ACCESSIBLE_ERROR = "It was not possible get access to containerId: %s" +
            " " + EXTENSION_WONT_OPERATE_PROPERLY_ERROR_PART;

    static final String SOLVER_CONFIGURATION_ERROR = "Planner solver is not properly configured, error: %s";

    static final String PLANNER_SOLVER_NOT_CONFIGURED_ERROR = "No solverConfigResource has been configured for starting the task assigning solver." +
            " " + EXTENSION_WONT_OPERATE_PROPERLY_ERROR_PART;

    static final String PLANNER_SOLVER_INSTANTIATION_CHECK_ERROR = "An error was produced during solver instantiation check." +
            " It was not possible to create a solver for the provided configuration, error: %s." +
            " " + EXTENSION_WONT_OPERATE_PROPERLY_ERROR_PART;

    static final String PLANNER_CONTAINER_NOT_AVAILABLE = "Planner container %s is not available." +
            " " + EXTENSION_WONT_OPERATE_PROPERLY_ERROR_PART;

    static final String PLANNER_SOLVER_SOLUTION_FACTORY_NOT_FOUND = "SolutionFactory %s was not found." +
            " " + EXTENSION_WONT_OPERATE_PROPERLY_ERROR_PART;

    static final String UNDESIRED_EXTENSIONS_RUNNING_ERROR = "It's was detected that the following extensions %s" +
            " are running in current server, but it's not recommended to run them on the same server instance as the " + EXTENSION_NAME + " sever.";

    static final String USER_SYSTEM_NAME_NOT_CONFIGURED_ERROR = "No user system service name has been configured." +
            " " + EXTENSION_WONT_OPERATE_PROPERLY_ERROR_PART + ". Please use the property %s to configure it";

    static final String USER_SYSTEM_CONFIGURATION_ERROR = "User system service is not properly configured, error: %s";

    static final String USER_SYSTEM_CONTAINER_NOT_AVAILABLE = "User system service container %s is not available." +
            " " + EXTENSION_WONT_OPERATE_PROPERLY_ERROR_PART;

    static final String USER_SYSTEM_SERVICE_NOT_FOUND = "User system service %s was not found." +
            " " + EXTENSION_WONT_OPERATE_PROPERLY_ERROR_PART;

    static final String USER_SYSTEM_SERVICE_START_ERROR = "User system service %s initialization failed, error: %s." +
            " " + EXTENSION_WONT_OPERATE_PROPERLY_ERROR_PART;

    static final String REQUIRED_PARAMETERS_FOR_CONTAINER_ARE_MISSING = "Required parameters for container configuration are missing." +
            " containerId: %s, groupId: %s, artifactId: %s, version: %s";

    static final String HEALTH_CHECK_IS_ALIVE_MESSAGE = EXTENSION_NAME + " is alive";

    static String addExtensionMessagePrefix(String msg) {
        return String.format(PLANNER_EXTENSION_MESSAGE_PREFIX, msg);
    }
}
