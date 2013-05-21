/*
 * Copyright 2013 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.spring.factorybeans;

import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.command.Command;
import org.kie.api.event.KieRuntimeEventManager;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.event.rule.WorkingMemoryEventListener;
import org.kie.api.logger.KieLoggers;
import org.kie.api.logger.KieRuntimeLogger;
import org.kie.api.runtime.KieRuntime;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.KieSessionConfiguration;
import org.kie.api.runtime.StatelessKieSession;
import org.kie.spring.KieObjectsResolver;
import org.kie.spring.factorybeans.helper.KSessionFactoryBeanHelper;
import org.kie.spring.factorybeans.helper.StatefulKSessionFactoryBeanHelper;
import org.kie.spring.factorybeans.helper.StatelessKSessionFactoryBeanHelper;
import org.kie.spring.namespace.EventListenersUtil;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.support.ManagedList;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class KSessionFactoryBean
        implements
        FactoryBean,
        InitializingBean {

    protected List<AgendaEventListener> agendaEventListeners;
    protected List<ProcessEventListener> processEventListeners;
    protected List<WorkingMemoryEventListener> workingMemoryEventListeners;
    protected List<Object> groupedListeners = new ArrayList<Object>();

    protected String id;
    protected String type;
    protected String kbaseName;
    private KieSessionConfiguration conf;
    private List<Command<?>> batch;
    private ReleaseId releaseId;

    protected ManagedList<LoggerAdaptor> loggerAdaptors = new ManagedList<LoggerAdaptor>();
    protected KSessionFactoryBeanHelper helper;
    protected boolean refLookup;

    public ReleaseId getReleaseId() {
        return releaseId;
    }

    public void setReleaseId(ReleaseId releaseId) {
        this.releaseId = releaseId;
    }

    public boolean getRefLookup() {
        return refLookup;
    }

    public void setRefLookup(boolean refLookup) {
        this.refLookup = refLookup;
    }

    public String getKbaseName() {
        return kbaseName;
    }

    public void setKbaseName(String kbase) {
        this.kbaseName = kbase;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public KieSessionConfiguration getConf() {
        return conf;
    }

    public void setConf(KieSessionConfiguration conf) {
        this.conf = conf;
    }

    public List<Command<?>> getBatch() {
        return batch;
    }

    public void setBatch(List<Command<?>> commands) {
        this.batch = commands;
    }

    public KSessionFactoryBean() {
        agendaEventListeners = new ArrayList<AgendaEventListener>();
        processEventListeners = new ArrayList<ProcessEventListener>();
        workingMemoryEventListeners = new ArrayList<WorkingMemoryEventListener>();
    }

    public Object getObject() throws Exception {
        return helper.internalGetObject();
    }

    public Class<?> getObjectType() {
        return internalGetObjectType();
    }

    public boolean isSingleton() {
        return true;
    }

    public final void afterPropertiesSet() throws Exception {

        KieObjectsResolver kieObjectsResolver = KieObjectsResolver.get();

        Object obj = null;
        if (refLookup) {
            obj = kieObjectsResolver.resolveKSession(id, releaseId);
            if (obj instanceof StatelessKieSession) {
                helper = new StatelessKSessionFactoryBeanHelper(this, (StatelessKieSession) obj);
            } else if (obj instanceof KieSession) {
                helper = new StatefulKSessionFactoryBeanHelper(this, (KieSession) obj);
            }
        } else {
            //new create
            if ("stateless".equalsIgnoreCase(type)) {
                obj = kieObjectsResolver.newStatelessSession(kbaseName, releaseId, conf);
                helper = new StatelessKSessionFactoryBeanHelper(this, (StatelessKieSession) obj);
            } else {
                obj = kieObjectsResolver.newStatefulSession(kbaseName, releaseId, conf);
                helper = new StatefulKSessionFactoryBeanHelper(this, (KieSession) obj);
            }
        }

        helper.internalAfterPropertiesSet();
        attachLoggers((KieRuntimeEventManager) obj);
        attachListeners((KieRuntimeEventManager) obj);
    }

    private StatefulKSessionFactoryBeanHelper.JpaConfiguration jpaConfiguration;

    public StatefulKSessionFactoryBeanHelper.JpaConfiguration getJpaConfiguration() {
        return jpaConfiguration;
    }

    public void setJpaConfiguration(StatefulKSessionFactoryBeanHelper.JpaConfiguration jpaConfiguration) {
        this.jpaConfiguration = jpaConfiguration;
    }

    protected Class<? extends KieRuntime> internalGetObjectType() {
        return KieRuntime.class;
    }

    public void setEventListenersFromGroup(List<Object> eventListenerList) {
        for (Object eventListener : eventListenerList) {
            if (eventListener instanceof AgendaEventListener) {
                agendaEventListeners.add((AgendaEventListener) eventListener);
            }
            if (eventListener instanceof WorkingMemoryEventListener) {
                workingMemoryEventListeners.add((WorkingMemoryEventListener) eventListener);
            }
            if (eventListener instanceof ProcessEventListener) {
                processEventListeners.add((ProcessEventListener) eventListener);
            }
        }
        groupedListeners.addAll(eventListenerList);
        // System.out.println("adding listener-group elements " + groupedListeners.size());
    }

    public void setEventListeners(Map<String, List> eventListenerMap) {
        for (Map.Entry<String, List> entry : eventListenerMap.entrySet()) {
            String key = entry.getKey();
            List<Object> eventListenerList = entry.getValue();
            if (EventListenersUtil.TYPE_AGENDA_EVENT_LISTENER.equalsIgnoreCase(key)) {
                for (Object eventListener : eventListenerList) {
                    if (eventListener instanceof AgendaEventListener) {
                        agendaEventListeners.add((AgendaEventListener) eventListener);
                    } else {
                        throw new IllegalArgumentException("The agendaEventListener (" + eventListener.getClass()
                                + ") is not an instance of " + AgendaEventListener.class);
                    }
                }
            } else if (EventListenersUtil.TYPE_WORKING_MEMORY_EVENT_LISTENER.equalsIgnoreCase(key)) {
                for (Object eventListener : eventListenerList) {
                    if (eventListener instanceof WorkingMemoryEventListener) {
                        workingMemoryEventListeners.add((WorkingMemoryEventListener) eventListener);
                    } else {
                        throw new IllegalArgumentException("The workingMemoryEventListener (" + eventListener.getClass()
                                + ") is not an instance of " + WorkingMemoryEventListener.class);
                    }
                }
            } else if (EventListenersUtil.TYPE_PROCESS_EVENT_LISTENER.equalsIgnoreCase(key)) {
                for (Object eventListener : eventListenerList) {
                    if (eventListener instanceof ProcessEventListener) {
                        processEventListeners.add((ProcessEventListener) eventListener);
                    } else {
                        throw new IllegalArgumentException("The processEventListener (" + eventListener.getClass()
                                + ") is not an instance of " + ProcessEventListener.class);
                    }
                }
            }
        }
    }

    public List<AgendaEventListener> getAgendaEventListeners() {
        return agendaEventListeners;
    }

    public void setAgendaEventListeners(List<AgendaEventListener> agendaEventListeners) {
        this.agendaEventListeners = agendaEventListeners;
    }

    public List<ProcessEventListener> getProcessEventListeners() {
        return processEventListeners;
    }

    public void setProcessEventListeners(List<ProcessEventListener> processEventListeners) {
        this.processEventListeners = processEventListeners;
    }

    public List<WorkingMemoryEventListener> getWorkingMemoryEventListeners() {
        return workingMemoryEventListeners;
    }

    public void setWorkingMemoryEventListeners(List<WorkingMemoryEventListener> workingMemoryEventListeners) {
        this.workingMemoryEventListeners = workingMemoryEventListeners;
    }

    public List<LoggerAdaptor> getKnowledgeRuntimeLoggers() {
        return loggerAdaptors;
    }

    public void setKnowledgeRuntimeLoggers(List<LoggerAdaptor> loggers) {
        this.loggerAdaptors.addAll(loggers);
    }

    public void attachListeners(KieRuntimeEventManager kieRuntimeEventManager) {
        for (AgendaEventListener agendaEventListener : getAgendaEventListeners()) {
            kieRuntimeEventManager.addEventListener(agendaEventListener);
        }
        for (ProcessEventListener processEventListener : getProcessEventListeners()) {
            kieRuntimeEventManager.addEventListener(processEventListener);
        }
        for (WorkingMemoryEventListener workingMemoryEventListener : getWorkingMemoryEventListeners()) {
            kieRuntimeEventManager.addEventListener(workingMemoryEventListener);
        }
    }

    public void attachLoggers(KieRuntimeEventManager ksession) {
        if (loggerAdaptors != null && loggerAdaptors.size() > 0) {
            KieServices ks = KieServices.Factory.get();
            KieLoggers loggers = ks.getLoggers();
            for (LoggerAdaptor adaptor : loggerAdaptors) {
                KieRuntimeLogger runtimeLogger;
                switch (adaptor.getLoggerType()) {
                    case LOGGER_TYPE_FILE:
                        runtimeLogger = loggers.newFileLogger(ksession, adaptor.getFile());
                        adaptor.setRuntimeLogger(runtimeLogger);
                        break;
                    case LOGGER_TYPE_THREADED_FILE:
                        runtimeLogger = loggers.newThreadedFileLogger(ksession, adaptor.getFile(), adaptor.getInterval());
                        adaptor.setRuntimeLogger(runtimeLogger);
                        break;
                    case LOGGER_TYPE_CONSOLE:
                        runtimeLogger = loggers.newConsoleLogger(ksession);
                        adaptor.setRuntimeLogger(runtimeLogger);
                        break;
                }
            }
        }
    }
}
