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

import java.util.HashMap;
import java.util.Map;

import org.kie.server.services.api.KieServerExtension;
import org.kie.server.services.impl.KieServerImpl;
import org.kie.server.services.taskassigning.core.model.ModelConstants;
import org.kie.server.services.taskassigning.planning.TaskAssigningPlanningKieServerExtension;
import org.kie.server.services.taskassigning.user.system.simple.SimpleUserSystemService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;
import static org.kie.server.api.KieServerConstants.KIE_TASK_ASSIGNING_PLANNING_EXT_DISABLED;
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

@Configuration
@ConditionalOnClass({KieServerImpl.class})
@EnableConfigurationProperties({TaskAssigningPlanningProperties.class})
public class TaskAssigningPlanningKieServerAutoConfiguration {

    private TaskAssigningPlanningProperties properties;
    private Map<String, String> restorableProperties = new HashMap<>();

    public TaskAssigningPlanningKieServerAutoConfiguration(TaskAssigningPlanningProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnMissingBean(name = "taskAssigningPlanningServerExtension")
    @ConditionalOnProperty(name = "kieserver.taskAssigning.planning.enabled")
    public KieServerExtension taskAssigningPlanningServerExtension() {
        initProperties();
        return new TaskAssigningPlanningKieServerExtension();
    }

    @EventListener
    public void handleContextRefreshEvent(ContextClosedEvent evt) {
        restoreProperties();
    }

    private void initProperties() {
        restorableProperties.put(KIE_TASK_ASSIGNING_PLANNING_EXT_DISABLED, System.getProperty(KIE_TASK_ASSIGNING_PLANNING_EXT_DISABLED));
        System.setProperty(KIE_TASK_ASSIGNING_PLANNING_EXT_DISABLED, Boolean.FALSE.toString());

        setIfConfigured(TASK_ASSIGNING_PROCESS_RUNTIME_URL, properties.getProcessRuntime().getUrl());
        setIfConfigured(TASK_ASSIGNING_PROCESS_RUNTIME_USER, properties.getProcessRuntime().getUser());
        setIfConfigured(TASK_ASSIGNING_PROCESS_RUNTIME_PWD, properties.getProcessRuntime().getPwd());
        if (properties.getProcessRuntime().getTimeout() > 0) {
            setIfConfigured(TASK_ASSIGNING_PROCESS_RUNTIME_TIMEOUT, Integer.toString(properties.getProcessRuntime().getTimeout()));
        }

        setIfConfigured(TASK_ASSIGNING_KEY_STORE_PROCESS_RUNTIME_ALIAS, properties.getProcessRuntime().getKey().getAlias());
        setIfConfigured(TASK_ASSIGNING_KEY_STORE_PROCESS_RUNTIME_PWD, properties.getProcessRuntime().getKey().getPwd());

        setIfConfigured(TASK_ASSIGNING_PROCESS_RUNTIME_TARGET_USER, properties.getProcessRuntime().getTargetUser());
        if (properties.getPublishWindowSize() > 0) {
            setIfConfigured(TASK_ASSIGNING_PUBLISH_WINDOW_SIZE, Integer.toString(properties.getPublishWindowSize()));
        }

        setIfConfigured(ModelConstants.PLANNING_USER_ID_PROPERTY, properties.getCore().getModel().getPlanningUserId());

        setIfConfigured(TASK_ASSIGNING_SYNC_INTERVAL, properties.getSolutionSyncInterval());
        setIfConfigured(TASK_ASSIGNING_SYNC_QUERIES_SHIFT, properties.getSolutionSyncQueriesShift());
        setIfConfigured(TASK_ASSIGNING_USERS_SYNC_INTERVAL, properties.getUsersSyncInterval());

        setIfConfigured(TASK_ASSIGNING_SOLVER_CONFIG_RESOURCE, properties.getSolver().getConfigResource());
        setIfConfigured(TASK_ASSIGNING_SOLVER_MOVE_THREAD_COUNT, properties.getSolver().getMoveThreadCount());
        if (properties.getSolver().getMoveThreadBufferSize() > 1) {
            setIfConfigured(TASK_ASSIGNING_SOLVER_MOVE_THREAD_BUFFER_SIZE, Integer.toString(properties.getSolver().getMoveThreadBufferSize()));
        }
        setIfConfigured(TASK_ASSIGNING_SOLVER_THREAD_FACTORY_CLASS, properties.getSolver().getThreadFactoryClass());

        setIfConfigured(TASK_ASSIGNING_SOLVER_CONTAINER_ID, properties.getSolver().getContainer().getId());
        setIfConfigured(TASK_ASSIGNING_SOLVER_CONTAINER_GROUP_ID, properties.getSolver().getContainer().getGroupId());
        setIfConfigured(TASK_ASSIGNING_SOLVER_CONTAINER_ARTIFACT_ID, properties.getSolver().getContainer().getArtifactId());
        setIfConfigured(TASK_ASSIGNING_SOLVER_CONTAINER_VERSION, properties.getSolver().getContainer().getVersion());

        setIfConfigured(TASK_ASSIGNING_USER_SYSTEM_NAME, properties.getUserSystem().getName());
        setIfConfigured(TASK_ASSIGNING_USER_SYSTEM_CONTAINER_ID, properties.getUserSystem().getContainer().getId());
        setIfConfigured(TASK_ASSIGNING_USER_SYSTEM_CONTAINER_GROUP_ID, properties.getUserSystem().getContainer().getGroupId());
        setIfConfigured(TASK_ASSIGNING_USER_SYSTEM_CONTAINER_ARTIFACT_ID, properties.getUserSystem().getContainer().getArtifactId());
        setIfConfigured(TASK_ASSIGNING_USER_SYSTEM_CONTAINER_VERSION, properties.getUserSystem().getContainer().getVersion());

        setIfConfigured(SimpleUserSystemService.USERS_FILE, properties.getUserSystem().getSimple().getUsers());
        setIfConfigured(SimpleUserSystemService.SKILLS_FILE, properties.getUserSystem().getSimple().getSkills());
        setIfConfigured(SimpleUserSystemService.AFFINITIES_FILE, properties.getUserSystem().getSimple().getAffinities());

        if (properties.getRuntimeDelegate().getPageSize() > 1) {
            setIfConfigured(TASK_ASSIGNING_RUNTIME_DELEGATE_PAGE_SIZE, Integer.toString(properties.getRuntimeDelegate().getPageSize()));
        }
    }

    private void setIfConfigured(String propertyName, String value) {
        if (isNotEmpty(value)) {
            restorableProperties.put(propertyName, System.getProperty(propertyName));
            System.getProperties().setProperty(propertyName, value);
        }
    }

    private void restoreProperties() {
        restorableProperties.forEach((property, value) -> {
            if (value != null) {
                System.setProperty(property, value);
            } else {
                System.clearProperty(property);
            }
        });
    }
}
