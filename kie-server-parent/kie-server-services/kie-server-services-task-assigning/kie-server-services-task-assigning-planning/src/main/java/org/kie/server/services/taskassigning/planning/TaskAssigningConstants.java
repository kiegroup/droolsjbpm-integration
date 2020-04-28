/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

public class TaskAssigningConstants {

    private TaskAssigningConstants() {
    }

    /**
     * Property for configuring the rest endpoint url of the kie-server with the jBPM runtime.
     */
    public static final String JBPM_TASK_ASSIGNING_PROCESS_RUNTIME_URL = "org.kie.server.taskAssigning.processRuntime.url";

    /**
     * Property for configuring the user for connecting with the with the jBPM runtime.
     */
    public static final String JBPM_TASK_ASSIGNING_PROCESS_RUNTIME_USER = "org.kie.server.taskAssigning.processRuntime.user";

    /**
     * Property for configuring the user password for connecting with the jBPM runtime.
     */
    public static final String JBPM_TASK_ASSIGNING_PROCESS_RUNTIME_PWD = "org.kie.server.taskAssigning.processRuntime.pwd";

    /**
     * Property for configuring the alias of the user password for connecting with the jBPM runtime in cases where the
     * kie-server key store is used.
     */
    public static final String JBPM_TASK_ASSIGNING_KEY_STORE_PROCESS_RUNTIME_ALIAS = "kie.keystore.key.taskAssigning.processRuntime.alias";

    /**
     * Property for configuring the password for getting the key stored under the alias name
     * JBPM_TASK_ASSIGNING_KEY_STORE_PROCESS_RUNTIME_ALIAS in cases where the kie-server key store is used.
     */
    public static final String JBPM_TASK_ASSIGNING_KEY_STORE_PROCESS_RUNTIME_PWD = "kie.keystore.key.taskAssigning.processRuntime.pwd";

    /**
     * Property for configuring the connection timeout when interacting with the jBPM runtime.
     */
    public static final String JBPM_TASK_ASSIGNING_PROCESS_RUNTIME_TIMEOUT = "org.kie.server.taskAssigning.processRuntime.timeout";

    /**
     * Property for configuring a user identifier for using as the "on behalf of" user when interacting with the jBPM runtime.
     */
    public static final String JBPM_TASK_ASSIGNING_PROCESS_RUNTIME_TARGET_USER = "org.kie.server.taskAssigning.processRuntime.targetUser";

    /**
     * Property for configuring the size of the tasks publish window.
     */
    public static final String JBPM_TASK_ASSIGNING_PUBLISH_WINDOW_SIZE = "org.kie.server.taskAssigning.publishWindowSize";

    /**
     * Property for configuring the solution synchronization period. This value is "normally" a short interval in
     * milliseconds.
     * The formats accepted are based on the ISO-8601 duration format {@code PnDTnHnMn.nS} with days considered to
     * be exactly 24 hours.
     * <p>
     * e.g.
     * "PT1.500S"  configures 1500 milliseconds.
     * "PT0.500S"  configures 500 milliseconds.
     * "PT3S"      configures 3000 milliseconds.
     */
    public static final String JBPM_TASK_ASSIGNING_SYNC_INTERVAL = "org.kie.server.taskAssigning.solutionSyncInterval";

    /**
     * Property for configuring the safety minimal query distance in milliseconds.
     */
    public static final String JBPM_TASK_ASSIGNING_SYNC_QUERIES_SHIFT = "org.kie.server.taskAssigning.solutionSyncQueriesShift";

    /**
     * Property for configuring the users information synchronization period.
     * The formats accepted are based on the ISO-8601 duration format {@code PnDTnHnMn.nS} with days considered to
     * be exactly 24 hours.
     * <p>
     * e.g.
     * "PT1H30M"   configures 1 hour 30 minutes.
     */
    public static final String JBPM_TASK_ASSIGNING_USERS_SYNC_INTERVAL = "org.kie.server.taskAssigning.usersSyncInterval";

    /**
     * Property for configuring the resource with the solver configuration.
     */
    public static final String TASK_ASSIGNING_SOLVER_CONFIG_RESOURCE = "org.kie.server.taskAssigning.solver.configResource";

    public static final String TASK_ASSIGNING_SOLVER_CONTAINER_ID = "org.kie.server.taskAssigning.solver.container.id";

    public static final String TASK_ASSIGNING_SOLVER_CONTAINER_GROUP_ID = "org.kie.server.taskAssigning.solver.container.groupId";

    public static final String TASK_ASSIGNING_SOLVER_CONTAINER_ARTIFACT_ID = "org.kie.server.taskAssigning.solver.container.artifactId";

    public static final String TASK_ASSIGNING_SOLVER_CONTAINER_VERSION = "org.kie.server.taskAssigning.solver.container.version";

    public static final String TASK_ASSIGNING_USER_SYSTEM_NAME = "org.kie.server.taskAssigning.userSystem.name";

    public static final String TASK_ASSIGNING_USER_SYSTEM_CONTAINER_ID = "org.kie.server.taskAssigning.userSystem.container.id";

    public static final String TASK_ASSIGNING_USER_SYSTEM_CONTAINER_GROUP_ID = "org.kie.server.taskAssigning.userSystem.container.groupId";

    public static final String TASK_ASSIGNING_USER_SYSTEM_CONTAINER_ARTIFACT_ID = "org.kie.server.taskAssigning.userSystem.container.artifactId";

    public static final String TASK_ASSIGNING_USER_SYSTEM_CONTAINER_VERSION = "org.kie.server.taskAssigning.userSystem.container.version";

    /**
     * Property for configuring the pageSize for the tasks queries that are used for populating/updating the solver.
     */
    public static final String TASK_ASSIGNING_RUNTIME_DELEGATE_PAGE_SIZE = "org.kie.server.taskAssigning.runtimeDelegate.pageSize";
}
