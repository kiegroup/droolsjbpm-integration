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

package org.kie.server.springboot.autoconfiguration.taskassigning;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TaskAssigningPlanningPropertiesTest {

    public static final String PROCESS_RUNTIME_URL = "PROCESS_RUNTIME_URL";

    public static final String PROCESS_RUNTIME_USER = "PROCESS_RUNTIME_USER";

    public static final String PROCESS_RUNTIME_PWD = "PROCESS_RUNTIME_PWD";

    public static final String PROCESS_RUNTIME_KEY_ALIAS = "PROCESS_RUNTIME_KEY_ALIAS";

    public static final String PROCESS_RUNTIME_KEY_PWD = "PROCESS_RUNTIME_KEY_PWD";

    public static final int PROCESS_RUNTIME_TIMEOUT = 12;

    public static final String PROCESS_RUNTIME_TARGET_USER = "PROCESS_RUNTIME_TARGET_USER";

    public static final int PUBLISH_WINDOW_SIZE = 34;

    public static final String SOLUTION_SYNC_INTERVAL = "SYNC_INTERVAL";

    public static final String SOLUTION_SYNC_QUERIES_SHIFT = "SOLUTION_SYNC_QUERIES_SHIFT";

    public static final String USERS_SYNC_INTERVAL = "USERS_SYNC_INTERVAL";

    public static final String SOLVER_CONFIG_RESOURCE = "SOLVER_CONFIG_RESOURCE";

    public static final String SOLVER_MOVE_THREAD_COUNT = "SOLVER_MOVE_THREAD_COUNT";

    public static final int SOLVER_MOVE_THREAD_BUFFER_SIZE = 56;

    public static final String SOLVER_THREAD_FACTORY_CLASS = "SOLVER_THREAD_FACTORY_CLASS";

    public static final String SOLVER_CONTAINER_ID = "SOLVER_CONTAINER_ID";

    public static final String SOLVER_CONTAINER_GROUP_ID = "SOLVER_CONTAINER_GROUP_ID";

    public static final String SOLVER_CONTAINER_ARTIFACT_ID = "SOLVER_CONTAINER_ARTIFACT_ID";

    public static final String SOLVER_CONTAINER_VERSION = "SOLVER_CONTAINER_VERSION";

    public static final String USER_SYSTEM_NAME = "SOLVER_CONTAINER_VERSION";

    public static final String USER_SYSTEM_CONTAINER_ID = "USER_SYSTEM_CONTAINER_ID";

    public static final String USER_SYSTEM_CONTAINER_GROUP_ID = "USER_SYSTEM_CONTAINER_GROUP_ID";

    public static final String USER_SYSTEM_CONTAINER_ARTIFACT_ID = "USER_SYSTEM_CONTAINER_ARTIFACT_ID";

    public static final String USER_SYSTEM_CONTAINER_VERSION = "USER_SYSTEM_CONTAINER_VERSION";

    public static final String PLANNING_USER_ID = "PLANNING_USER_ID";

    public static final String USER_SYSTEM_SIMPLE_USERS = "USER_SYSTEM_SIMPLE_USERS";

    public static final String USER_SYSTEM_SIMPLE_SKILLS = "USER_SYSTEM_SIMPLE_SKILLS";

    public static final String USER_SYSTEM_SIMPLE_AFFINITIES = "USER_SYSTEM_SIMPLE_AFFINITIES";

    public static final int RUNTIME_DELEGATE_PAGE_SIZE = 78;

    public static TaskAssigningPlanningProperties newTaskAssigningProperties() {
        TaskAssigningPlanningProperties properties = new TaskAssigningPlanningProperties();
        properties.getSolver().setConfigResource(SOLVER_CONFIG_RESOURCE);
        properties.getSolver().setMoveThreadCount(SOLVER_MOVE_THREAD_COUNT);
        properties.getSolver().setMoveThreadBufferSize(SOLVER_MOVE_THREAD_BUFFER_SIZE);
        properties.getSolver().setThreadFactoryClass(SOLVER_THREAD_FACTORY_CLASS);

        properties.getSolver().getContainer().setId(SOLVER_CONTAINER_ID);
        properties.getSolver().getContainer().setGroupId(SOLVER_CONTAINER_GROUP_ID);
        properties.getSolver().getContainer().setArtifactId(SOLVER_CONTAINER_ARTIFACT_ID);
        properties.getSolver().getContainer().setVersion(SOLVER_CONTAINER_VERSION);

        properties.getCore().getModel().setPlanningUserId(PLANNING_USER_ID);

        properties.getProcessRuntime().setUrl(PROCESS_RUNTIME_URL);
        properties.getProcessRuntime().setUser(PROCESS_RUNTIME_USER);
        properties.getProcessRuntime().setPwd(PROCESS_RUNTIME_PWD);
        properties.getProcessRuntime().setTimeout(PROCESS_RUNTIME_TIMEOUT);
        properties.getProcessRuntime().setTargetUser(PROCESS_RUNTIME_TARGET_USER);
        properties.getProcessRuntime().getKey().setAlias(PROCESS_RUNTIME_KEY_ALIAS);
        properties.getProcessRuntime().getKey().setPwd(PROCESS_RUNTIME_KEY_PWD);

        properties.setPublishWindowSize(PUBLISH_WINDOW_SIZE);
        properties.setSolutionSyncInterval(SOLUTION_SYNC_INTERVAL);
        properties.setSolutionSyncQueriesShift(SOLUTION_SYNC_QUERIES_SHIFT);
        properties.setUsersSyncInterval(USERS_SYNC_INTERVAL);

        properties.getUserSystem().setName(USER_SYSTEM_NAME);
        properties.getUserSystem().getContainer().setId(USER_SYSTEM_CONTAINER_ID);
        properties.getUserSystem().getContainer().setGroupId(USER_SYSTEM_CONTAINER_GROUP_ID);
        properties.getUserSystem().getContainer().setArtifactId(USER_SYSTEM_CONTAINER_ARTIFACT_ID);
        properties.getUserSystem().getContainer().setVersion(USER_SYSTEM_CONTAINER_VERSION);

        properties.getUserSystem().getSimple().setUsers(USER_SYSTEM_SIMPLE_USERS);
        properties.getUserSystem().getSimple().setSkills(USER_SYSTEM_SIMPLE_SKILLS);
        properties.getUserSystem().getSimple().setAffinities(USER_SYSTEM_SIMPLE_AFFINITIES);

        properties.getRuntimeDelegate().setPageSize(RUNTIME_DELEGATE_PAGE_SIZE);

        return properties;
    }

    @Test
    public void getters() {
        TaskAssigningPlanningProperties properties = newTaskAssigningProperties();

        assertEquals(SOLVER_CONFIG_RESOURCE, properties.getSolver().getConfigResource());
        assertEquals(SOLVER_MOVE_THREAD_COUNT, properties.getSolver().getMoveThreadCount());
        assertEquals(SOLVER_MOVE_THREAD_BUFFER_SIZE, properties.getSolver().getMoveThreadBufferSize());
        assertEquals(SOLVER_THREAD_FACTORY_CLASS, properties.getSolver().getThreadFactoryClass());

        assertEquals(SOLVER_CONTAINER_ID, properties.getSolver().getContainer().getId());
        assertEquals(SOLVER_CONTAINER_GROUP_ID, properties.getSolver().getContainer().getGroupId());
        assertEquals(SOLVER_CONTAINER_ARTIFACT_ID, properties.getSolver().getContainer().getArtifactId());
        assertEquals(SOLVER_CONTAINER_VERSION, properties.getSolver().getContainer().getVersion());

        assertEquals(PLANNING_USER_ID, properties.getCore().getModel().getPlanningUserId());

        assertEquals(PROCESS_RUNTIME_URL, properties.getProcessRuntime().getUrl());
        assertEquals(PROCESS_RUNTIME_USER, properties.getProcessRuntime().getUser());
        assertEquals(PROCESS_RUNTIME_PWD, properties.getProcessRuntime().getPwd());
        assertEquals(PROCESS_RUNTIME_TIMEOUT, properties.getProcessRuntime().getTimeout());
        assertEquals(PROCESS_RUNTIME_TARGET_USER, properties.getProcessRuntime().getTargetUser());
        assertEquals(PROCESS_RUNTIME_KEY_ALIAS, properties.getProcessRuntime().getKey().getAlias());
        assertEquals(PROCESS_RUNTIME_KEY_PWD, properties.getProcessRuntime().getKey().getPwd());

        assertEquals(PUBLISH_WINDOW_SIZE, properties.getPublishWindowSize());
        assertEquals(SOLUTION_SYNC_INTERVAL, properties.getSolutionSyncInterval());
        assertEquals(SOLUTION_SYNC_QUERIES_SHIFT, properties.getSolutionSyncQueriesShift());
        assertEquals(USERS_SYNC_INTERVAL, properties.getUsersSyncInterval());

        assertEquals(USER_SYSTEM_NAME, properties.getUserSystem().getName());
        assertEquals(USER_SYSTEM_CONTAINER_ID, properties.getUserSystem().getContainer().getId());
        assertEquals(USER_SYSTEM_CONTAINER_GROUP_ID, properties.getUserSystem().getContainer().getGroupId());
        assertEquals(USER_SYSTEM_CONTAINER_ARTIFACT_ID, properties.getUserSystem().getContainer().getArtifactId());
        assertEquals(USER_SYSTEM_CONTAINER_VERSION, properties.getUserSystem().getContainer().getVersion());

        assertEquals(USER_SYSTEM_SIMPLE_USERS, properties.getUserSystem().getSimple().getUsers());
        assertEquals(USER_SYSTEM_SIMPLE_SKILLS, properties.getUserSystem().getSimple().getSkills());
        assertEquals(USER_SYSTEM_SIMPLE_AFFINITIES, properties.getUserSystem().getSimple().getAffinities());

        assertEquals(RUNTIME_DELEGATE_PAGE_SIZE, properties.getRuntimeDelegate().getPageSize());
    }
}
