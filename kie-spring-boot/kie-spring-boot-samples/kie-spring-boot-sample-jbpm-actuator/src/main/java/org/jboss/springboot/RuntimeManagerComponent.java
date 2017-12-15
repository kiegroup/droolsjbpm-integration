/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.springboot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jbpm.process.instance.event.DefaultSignalManagerFactory;
import org.jbpm.process.instance.impl.DefaultProcessInstanceManagerFactory;
import org.jbpm.runtime.manager.impl.SimpleRegisterableItemsFactory;
import org.jbpm.services.task.identity.JBossUserGroupCallbackImpl;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.manager.Context;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeEnvironment;
import org.kie.api.runtime.manager.RuntimeEnvironmentBuilder;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.manager.RuntimeManagerFactory;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.task.TaskLifeCycleEventListener;
import org.kie.api.task.UserGroupCallback;
import org.kie.internal.io.ResourceFactory;
import org.springframework.stereotype.Component;

@Component("runtimeManagerComponent")
public class RuntimeManagerComponent {

    private RuntimeManagerFactory managerFactory = RuntimeManagerFactory.Factory.get();
    private RuntimeManager manager;

    private UserGroupCallback userGroupCallback = new JBossUserGroupCallbackImpl("classpath:/usergroups.properties");

    private Set<RuntimeEngine> activeEngines = new HashSet<>();

    private Map<String, WorkItemHandler> customHandlers = new HashMap<>();
    private List<ProcessEventListener> customProcessListeners = new ArrayList<>();
    private List<AgendaEventListener> customAgendaListeners = new ArrayList<>();
    private List<TaskLifeCycleEventListener> customTaskListeners = new ArrayList<>();
    private Map<String, Object> customEnvironmentEntries = new HashMap<>();

    public enum Strategy {
        SINGLETON,
        REQUEST,
        PROCESS_INSTANCE;
    }

    protected void createRuntimeManager(String... process) {
        createRuntimeManager(Strategy.SINGLETON, process);
    }

    private void createRuntimeManager(Strategy strategy, String... process) {
        Map<String, ResourceType> resources = new HashMap<>();
        for (String p : process) {
            resources.put(p, ResourceType.BPMN2);
        }
        createRuntimeManager(strategy, resources);
    }

    private void createRuntimeManager(Strategy strategy, Map<String, ResourceType> resources) {
        if (manager != null) {
            throw new IllegalStateException("There is already one RuntimeManager active");
        }

        RuntimeEnvironmentBuilder builder = RuntimeEnvironmentBuilder.Factory.get()
                .newEmptyBuilder()
                .addConfiguration("drools.processSignalManagerFactory", DefaultSignalManagerFactory.class.getName())
                .addConfiguration("drools.processInstanceManagerFactory", DefaultProcessInstanceManagerFactory.class.getName())
                .registerableItemsFactory(new SimpleRegisterableItemsFactory() {

                    @Override
                    public Map<String, WorkItemHandler> getWorkItemHandlers(RuntimeEngine runtime) {
                        Map<String, WorkItemHandler> handlers = new HashMap<>();
                        handlers.putAll(super.getWorkItemHandlers(runtime));
                        handlers.putAll(customHandlers);
                        return handlers;
                    }

                    @Override
                    public List<ProcessEventListener> getProcessEventListeners(RuntimeEngine runtime) {
                        List<ProcessEventListener> listeners = super.getProcessEventListeners(runtime);
                        listeners.addAll(customProcessListeners);
                        return listeners;
                    }

                    @Override
                    public List<AgendaEventListener> getAgendaEventListeners(RuntimeEngine runtime) {
                        List<AgendaEventListener> listeners = super.getAgendaEventListeners(runtime);
                        listeners.addAll(customAgendaListeners);
                        return listeners;
                    }

                    @Override
                    public List<TaskLifeCycleEventListener> getTaskListeners() {
                        List<TaskLifeCycleEventListener> listeners = super.getTaskListeners();
                        listeners.addAll(customTaskListeners);
                        return listeners;
                    }
                });
        builder.userGroupCallback(userGroupCallback);

        for (Map.Entry<String, Object> envEntry : customEnvironmentEntries.entrySet()) {
            builder.addEnvironmentEntry(envEntry.getKey(), envEntry.getValue());
        }

        for (Map.Entry<String, ResourceType> entry : resources.entrySet()) {
            builder.addAsset(ResourceFactory.newClassPathResource(entry.getKey()), entry.getValue());
        }

        createRuntimeManager(strategy, builder.get());
    }

    private void createRuntimeManager(Strategy strategy, RuntimeEnvironment environment) {
        if (manager != null) {
            throw new IllegalStateException("There is already one RuntimeManager active");
        }
        try {
            switch (strategy) {
                case SINGLETON:
                    manager = managerFactory.newSingletonRuntimeManager(environment);
                    break;
                case REQUEST:
                    manager = managerFactory.newPerRequestRuntimeManager(environment);
                    break;
                case PROCESS_INSTANCE:
                    manager = managerFactory.newPerProcessInstanceRuntimeManager(environment);
                    break;
                default:
                    manager = managerFactory.newSingletonRuntimeManager(environment);
                    break;
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected RuntimeEngine getRuntimeEngine(Context<?> context) {
        if (manager == null) {
            throw new IllegalStateException("RuntimeManager is not initialized, did you forgot to create it?");
        }

        RuntimeEngine runtimeEngine = manager.getRuntimeEngine(context);
        activeEngines.add(runtimeEngine);

        return runtimeEngine;
    }
}
