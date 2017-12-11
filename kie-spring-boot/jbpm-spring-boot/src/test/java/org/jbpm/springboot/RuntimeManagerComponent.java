package org.jbpm.springboot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.persistence.EntityManagerFactory;

import org.drools.core.audit.WorkingMemoryInMemoryLogger;
import org.jbpm.process.instance.event.DefaultSignalManagerFactory;
import org.jbpm.process.instance.impl.DefaultProcessInstanceManagerFactory;
import org.jbpm.runtime.manager.impl.DefaultRegisterableItemsFactory;
import org.jbpm.runtime.manager.impl.SimpleRegisterableItemsFactory;
import org.jbpm.services.task.identity.JBossUserGroupCallbackImpl;
import org.jbpm.test.util.PoolingDataSource;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.manager.Context;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeEnvironment;
import org.kie.api.runtime.manager.RuntimeEnvironmentBuilder;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.manager.RuntimeManagerFactory;
import org.kie.api.runtime.manager.audit.AuditService;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.kie.api.task.TaskLifeCycleEventListener;
import org.kie.api.task.UserGroupCallback;
import org.kie.internal.io.ResourceFactory;
import org.springframework.stereotype.Component;

@Component("runtimeManagerComponent")
public class RuntimeManagerComponent {

    protected boolean setupDataSource = false;
    protected boolean sessionPersistence = false;
    private String persistenceUnitName;

    private EntityManagerFactory emf;
    private PoolingDataSource ds;

    private TestWorkItemHandler workItemHandler = new TestWorkItemHandler();

    private RuntimeManagerFactory managerFactory = RuntimeManagerFactory.Factory.get();
    protected RuntimeManager manager;

    private AuditService logService;
    private WorkingMemoryInMemoryLogger inMemoryLogger;

    protected UserGroupCallback userGroupCallback = new JBossUserGroupCallbackImpl("classpath:/usergroups.properties");

    protected Set<RuntimeEngine> activeEngines = new HashSet<RuntimeEngine>();

    protected Map<String, WorkItemHandler> customHandlers = new HashMap<String, WorkItemHandler>();
    protected List<ProcessEventListener> customProcessListeners = new ArrayList<ProcessEventListener>();
    protected List<AgendaEventListener> customAgendaListeners = new ArrayList<AgendaEventListener>();
    protected List<TaskLifeCycleEventListener> customTaskListeners = new ArrayList<TaskLifeCycleEventListener>();
    protected Map<String, Object> customEnvironmentEntries = new HashMap<String, Object>();

    private final Map<String, Object> persistenceProperties = new HashMap<String, Object>();

    public enum Strategy {
        SINGLETON,
        REQUEST,
        PROCESS_INSTANCE;
    }

    protected RuntimeManager createRuntimeManager(String... process) {
        return createRuntimeManager(Strategy.SINGLETON,
                                    null,
                                    process);
    }

    protected RuntimeManager createRuntimeManager(Strategy strategy,
                                                  String identifier,
                                                  String... process) {
        Map<String, ResourceType> resources = new HashMap<String, ResourceType>();
        for (String p : process) {
            resources.put(p,
                          ResourceType.BPMN2);
        }
        return createRuntimeManager(strategy,
                                    resources,
                                    identifier);
    }

    protected RuntimeManager createRuntimeManager(Strategy strategy,
                                                  Map<String, ResourceType> resources,
                                                  String identifier) {
        if (manager != null) {
            throw new IllegalStateException("There is already one RuntimeManager active");
        }

        RuntimeEnvironmentBuilder builder = null;
        if (!setupDataSource) {
            builder = RuntimeEnvironmentBuilder.Factory.get()
                    .newEmptyBuilder()
                    .addConfiguration("drools.processSignalManagerFactory",
                                      DefaultSignalManagerFactory.class.getName())
                    .addConfiguration("drools.processInstanceManagerFactory",
                                      DefaultProcessInstanceManagerFactory.class.getName())
                    .registerableItemsFactory(new SimpleRegisterableItemsFactory() {

                        @Override
                        public Map<String, WorkItemHandler> getWorkItemHandlers(RuntimeEngine runtime) {
                            Map<String, WorkItemHandler> handlers = new HashMap<String, WorkItemHandler>();
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
        } else if (sessionPersistence) {
            builder = RuntimeEnvironmentBuilder.Factory.get()
                    .newDefaultBuilder()
                    .entityManagerFactory(emf)
                    .registerableItemsFactory(new DefaultRegisterableItemsFactory() {

                        @Override
                        public Map<String, WorkItemHandler> getWorkItemHandlers(RuntimeEngine runtime) {
                            Map<String, WorkItemHandler> handlers = new HashMap<String, WorkItemHandler>();
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
        } else {
            builder = RuntimeEnvironmentBuilder.Factory.get()
                    .newDefaultInMemoryBuilder()
                    .entityManagerFactory(emf)
                    .registerableItemsFactory(new DefaultRegisterableItemsFactory() {

                        @Override
                        public Map<String, WorkItemHandler> getWorkItemHandlers(RuntimeEngine runtime) {
                            Map<String, WorkItemHandler> handlers = new HashMap<String, WorkItemHandler>();
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
        }
        builder.userGroupCallback(userGroupCallback);

        for (Entry<String, Object> envEntry : customEnvironmentEntries.entrySet()) {
            builder.addEnvironmentEntry(envEntry.getKey(),
                                        envEntry.getValue());
        }

        for (Map.Entry<String, ResourceType> entry : resources.entrySet()) {
            builder.addAsset(ResourceFactory.newClassPathResource(entry.getKey()),
                             entry.getValue());
        }

        return createRuntimeManager(strategy,
                                    resources,
                                    builder.get(),
                                    identifier);
    }

    protected RuntimeManager createRuntimeManager(Strategy strategy,
                                                  Map<String, ResourceType> resources,
                                                  RuntimeEnvironment environment,
                                                  String identifier) {
        if (manager != null) {
            throw new IllegalStateException("There is already one RuntimeManager active");
        }
        try {
            switch (strategy) {
                case SINGLETON:
                    if (identifier == null) {
                        manager = managerFactory.newSingletonRuntimeManager(environment);
                    } else {
                        manager = managerFactory.newSingletonRuntimeManager(environment,
                                                                            identifier);
                    }
                    break;
                case REQUEST:
                    if (identifier == null) {
                        manager = managerFactory.newPerRequestRuntimeManager(environment);
                    } else {
                        manager = managerFactory.newPerRequestRuntimeManager(environment,
                                                                             identifier);
                    }
                    break;
                case PROCESS_INSTANCE:
                    if (identifier == null) {
                        manager = managerFactory.newPerProcessInstanceRuntimeManager(environment);
                    } else {
                        manager = managerFactory.newPerProcessInstanceRuntimeManager(environment,
                                                                                     identifier);
                    }
                    break;
                default:
                    if (identifier == null) {
                        manager = managerFactory.newSingletonRuntimeManager(environment);
                    } else {
                        manager = managerFactory.newSingletonRuntimeManager(environment,
                                                                            identifier);
                    }
                    break;
            }

            return manager;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    protected static class TestWorkItemHandler implements WorkItemHandler {

        public TestWorkItemHandler() {
        }

        private List<WorkItem> workItems = new ArrayList<WorkItem>();

        public void executeWorkItem(WorkItem workItem,
                                    WorkItemManager manager) {
            workItems.add(workItem);
        }

        public void abortWorkItem(WorkItem workItem,
                                  WorkItemManager manager) {
        }

        public WorkItem getWorkItem() {
            if (workItems.size() == 0) {
                return null;
            }
            if (workItems.size() == 1) {
                WorkItem result = workItems.get(0);
                this.workItems.clear();
                return result;
            } else {
                throw new IllegalArgumentException("More than one work item active");
            }
        }

        public List<WorkItem> getWorkItems() {
            List<WorkItem> result = new ArrayList<WorkItem>(workItems);
            workItems.clear();
            return result;
        }
    }

    protected RuntimeEngine getRuntimeEngine(Context<?> context) {
        if (manager == null) {
            throw new IllegalStateException("RuntimeManager is not initialized, did you forgot to create it?");
        }

        RuntimeEngine runtimeEngine = manager.getRuntimeEngine(context);
        activeEngines.add(runtimeEngine);
        if (sessionPersistence) {
            logService = runtimeEngine.getAuditService();
        } else {
            inMemoryLogger = new WorkingMemoryInMemoryLogger(runtimeEngine.getKieSession());
        }

        return runtimeEngine;
    }
}
