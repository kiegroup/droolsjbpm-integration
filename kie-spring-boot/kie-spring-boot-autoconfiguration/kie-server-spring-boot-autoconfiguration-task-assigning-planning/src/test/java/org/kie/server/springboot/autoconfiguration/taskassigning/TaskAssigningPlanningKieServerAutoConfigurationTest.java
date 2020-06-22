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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.server.services.taskassigning.planning.TaskAssigningPlanningKieServerExtension;
import org.springframework.context.event.ContextClosedEvent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.kie.server.api.KieServerConstants.KIE_TASK_ASSIGNING_PLANNING_EXT_DISABLED;
import static org.kie.server.services.taskassigning.core.model.ModelConstants.PLANNING_USER_ID_PROPERTY;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_KEY_STORE_PROCESS_RUNTIME_ALIAS;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_KEY_STORE_PROCESS_RUNTIME_PWD;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_PROCESS_RUNTIME_PWD;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_PROCESS_RUNTIME_TARGET_USER;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_PROCESS_RUNTIME_TIMEOUT;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_PROCESS_RUNTIME_URL;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_PROCESS_RUNTIME_USER;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_PUBLISH_WINDOW_SIZE;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_RUNTIME_DELEGATE_PAGE_SIZE;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_SOLVER_CONFIG_RESOURCE;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_SOLVER_CONTAINER_ARTIFACT_ID;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_SOLVER_CONTAINER_GROUP_ID;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_SOLVER_CONTAINER_ID;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_SOLVER_CONTAINER_VERSION;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_SOLVER_MOVE_THREAD_BUFFER_SIZE;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_SOLVER_MOVE_THREAD_COUNT;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_SOLVER_THREAD_FACTORY_CLASS;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_SYNC_INTERVAL;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_SYNC_QUERIES_SHIFT;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_USERS_SYNC_INTERVAL;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_USER_SYSTEM_CONTAINER_ARTIFACT_ID;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_USER_SYSTEM_CONTAINER_GROUP_ID;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_USER_SYSTEM_CONTAINER_ID;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_USER_SYSTEM_CONTAINER_VERSION;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_USER_SYSTEM_NAME;
import static org.kie.server.services.taskassigning.user.system.simple.SimpleUserSystemService.AFFINITIES_FILE;
import static org.kie.server.services.taskassigning.user.system.simple.SimpleUserSystemService.SKILLS_FILE;
import static org.kie.server.services.taskassigning.user.system.simple.SimpleUserSystemService.USERS_FILE;
import static org.kie.server.springboot.autoconfiguration.taskassigning.TaskAssigningPlanningPropertiesTest.newTaskAssigningProperties;
import static org.mockito.Mockito.mock;

public class TaskAssigningPlanningKieServerAutoConfigurationTest {

    private TaskAssigningPlanningKieServerAutoConfiguration configuration;
    private TaskAssigningPlanningProperties properties;

    private static final String[] TASK_ASSIGNING_PROPERTIES = new String[]{
            KIE_TASK_ASSIGNING_PLANNING_EXT_DISABLED,
            TASK_ASSIGNING_PROCESS_RUNTIME_URL,
            TASK_ASSIGNING_PROCESS_RUNTIME_USER,
            TASK_ASSIGNING_PROCESS_RUNTIME_PWD,
            TASK_ASSIGNING_KEY_STORE_PROCESS_RUNTIME_ALIAS,
            TASK_ASSIGNING_KEY_STORE_PROCESS_RUNTIME_PWD,
            TASK_ASSIGNING_PROCESS_RUNTIME_TIMEOUT,
            TASK_ASSIGNING_PROCESS_RUNTIME_TARGET_USER,
            TASK_ASSIGNING_PUBLISH_WINDOW_SIZE,
            PLANNING_USER_ID_PROPERTY,
            TASK_ASSIGNING_SYNC_INTERVAL,
            TASK_ASSIGNING_SYNC_QUERIES_SHIFT,
            TASK_ASSIGNING_USERS_SYNC_INTERVAL,
            TASK_ASSIGNING_SOLVER_CONFIG_RESOURCE,
            TASK_ASSIGNING_SOLVER_MOVE_THREAD_COUNT,
            TASK_ASSIGNING_SOLVER_MOVE_THREAD_BUFFER_SIZE,
            TASK_ASSIGNING_SOLVER_THREAD_FACTORY_CLASS,
            TASK_ASSIGNING_SOLVER_CONTAINER_ID,
            TASK_ASSIGNING_SOLVER_CONTAINER_GROUP_ID,
            TASK_ASSIGNING_SOLVER_CONTAINER_ARTIFACT_ID,
            TASK_ASSIGNING_SOLVER_CONTAINER_VERSION,
            TASK_ASSIGNING_USER_SYSTEM_NAME,
            TASK_ASSIGNING_USER_SYSTEM_CONTAINER_ID,
            TASK_ASSIGNING_USER_SYSTEM_CONTAINER_GROUP_ID,
            TASK_ASSIGNING_USER_SYSTEM_CONTAINER_ARTIFACT_ID,
            TASK_ASSIGNING_USER_SYSTEM_CONTAINER_VERSION,
            USERS_FILE,
            SKILLS_FILE,
            AFFINITIES_FILE,
            TASK_ASSIGNING_RUNTIME_DELEGATE_PAGE_SIZE};

    @Before
    public void setUp() {
        this.properties = newTaskAssigningProperties();
        this.configuration = new TaskAssigningPlanningKieServerAutoConfiguration(properties);
    }

    @After
    public void cleanUp() {
        clearSystemProperties(TASK_ASSIGNING_PROPERTIES);
    }

    @Test
    public void taskAssigningPlanningServerExtension() {
        List<Pair<String, String>> previousValues = buildPreviousValues(TASK_ASSIGNING_PROPERTIES);
        setSystemSystemProperties(previousValues);

        TaskAssigningPlanningKieServerExtension extension = (TaskAssigningPlanningKieServerExtension) configuration.taskAssigningPlanningServerExtension();
        assertTrue(extension.isActive());

        assertSystemProperty(TASK_ASSIGNING_PROCESS_RUNTIME_URL, properties.getProcessRuntime().getUrl());
        assertSystemProperty(TASK_ASSIGNING_PROCESS_RUNTIME_USER, properties.getProcessRuntime().getUser());
        assertSystemProperty(TASK_ASSIGNING_PROCESS_RUNTIME_PWD, properties.getProcessRuntime().getPwd());
        assertSystemProperty(TASK_ASSIGNING_KEY_STORE_PROCESS_RUNTIME_ALIAS, properties.getProcessRuntime().getKey().getAlias());
        assertSystemProperty(TASK_ASSIGNING_KEY_STORE_PROCESS_RUNTIME_PWD, properties.getProcessRuntime().getKey().getPwd());
        assertSystemProperty(TASK_ASSIGNING_PROCESS_RUNTIME_TIMEOUT, Integer.toString(properties.getProcessRuntime().getTimeout()));
        assertSystemProperty(TASK_ASSIGNING_PROCESS_RUNTIME_TARGET_USER, properties.getProcessRuntime().getTargetUser());
        assertSystemProperty(TASK_ASSIGNING_PUBLISH_WINDOW_SIZE, Integer.toString(properties.getPublishWindowSize()));
        assertSystemProperty(PLANNING_USER_ID_PROPERTY, properties.getCore().getModel().getPlanningUserId());

        assertSystemProperty(TASK_ASSIGNING_SYNC_INTERVAL, properties.getSolutionSyncInterval());
        assertSystemProperty(TASK_ASSIGNING_SYNC_QUERIES_SHIFT, properties.getSolutionSyncQueriesShift());
        assertSystemProperty(TASK_ASSIGNING_USERS_SYNC_INTERVAL, properties.getUsersSyncInterval());

        assertSystemProperty(TASK_ASSIGNING_SOLVER_CONFIG_RESOURCE, properties.getSolver().getConfigResource());
        assertSystemProperty(TASK_ASSIGNING_SOLVER_MOVE_THREAD_COUNT, properties.getSolver().getMoveThreadCount());
        assertSystemProperty(TASK_ASSIGNING_SOLVER_MOVE_THREAD_BUFFER_SIZE, Integer.toString(properties.getSolver().getMoveThreadBufferSize()));
        assertSystemProperty(TASK_ASSIGNING_SOLVER_THREAD_FACTORY_CLASS, properties.getSolver().getThreadFactoryClass());

        assertSystemProperty(TASK_ASSIGNING_SOLVER_CONTAINER_ID, properties.getSolver().getContainer().getId());
        assertSystemProperty(TASK_ASSIGNING_SOLVER_CONTAINER_GROUP_ID, properties.getSolver().getContainer().getGroupId());
        assertSystemProperty(TASK_ASSIGNING_SOLVER_CONTAINER_ARTIFACT_ID, properties.getSolver().getContainer().getArtifactId());
        assertSystemProperty(TASK_ASSIGNING_SOLVER_CONTAINER_VERSION, properties.getSolver().getContainer().getVersion());

        assertSystemProperty(TASK_ASSIGNING_USER_SYSTEM_NAME, properties.getUserSystem().getName());
        assertSystemProperty(TASK_ASSIGNING_USER_SYSTEM_CONTAINER_ID, properties.getUserSystem().getContainer().getId());
        assertSystemProperty(TASK_ASSIGNING_USER_SYSTEM_CONTAINER_GROUP_ID, properties.getUserSystem().getContainer().getGroupId());
        assertSystemProperty(TASK_ASSIGNING_USER_SYSTEM_CONTAINER_ARTIFACT_ID, properties.getUserSystem().getContainer().getArtifactId());
        assertSystemProperty(TASK_ASSIGNING_USER_SYSTEM_CONTAINER_VERSION, properties.getUserSystem().getContainer().getVersion());

        assertSystemProperty(USERS_FILE, properties.getUserSystem().getSimple().getUsers());
        assertSystemProperty(SKILLS_FILE, properties.getUserSystem().getSimple().getSkills());
        assertSystemProperty(AFFINITIES_FILE, properties.getUserSystem().getSimple().getAffinities());

        assertSystemProperty(TASK_ASSIGNING_RUNTIME_DELEGATE_PAGE_SIZE, Integer.toString(properties.getRuntimeDelegate().getPageSize()));

        configuration.handleContextRefreshEvent(mock(ContextClosedEvent.class));

        previousValues.forEach(previousValue -> assertSystemProperty(previousValue.getKey(), previousValue.getValue()));
    }

    private void assertSystemProperty(String propertyName, String value) {
        assertEquals(value, System.getProperty(propertyName));
    }

    private void clearSystemProperties(String... propertyNames) {
        Arrays.stream(propertyNames).forEach(System::clearProperty);
    }

    private void setSystemSystemProperties(List<Pair<String, String>> propertyValues) {
        propertyValues.forEach(propertyValue -> System.setProperty(propertyValue.getKey(), propertyValue.getValue()));
    }

    private List<Pair<String, String>> buildPreviousValues(String... propertyNames) {
        return Arrays.stream(propertyNames)
                .map(propertyName -> Pair.of(propertyName, propertyName + "_GENERATED_VALUE"))
                .collect(Collectors.toList());
    }
}
