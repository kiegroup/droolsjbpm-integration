/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.ReleaseId;
import org.kie.api.command.Command;
import org.kie.api.event.KieRuntimeEventManager;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.event.rule.RuleRuntimeEventListener;
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

public class KSessionFactoryBean
        implements
        FactoryBean,
        InitializingBean {

    private Object kSession;
    private String id;
    private String type;
    private KieBase kBase;
    private String kBaseName;
    private String name;
    private String clockType;
    private List<Command<?>> batch;
    private KieSessionConfiguration conf;
    private StatefulKSessionFactoryBeanHelper.JpaConfiguration jpaConfiguration;
    protected KSessionFactoryBeanHelper helper;
    protected ManagedList<LoggerAdaptor> loggerAdaptors = new ManagedList<LoggerAdaptor>();

    protected List<AgendaEventListener> agendaEventListeners;
    protected List<ProcessEventListener> processEventListeners;
    protected List<RuleRuntimeEventListener> ruleRuntimeEventListeners;
    protected List<Object> groupedListeners = new ArrayList<Object>();

    private ReleaseId releaseId;

    private String def;
    private String scope;

    public KSessionFactoryBean() {
        agendaEventListeners = new ArrayList<AgendaEventListener>();
        processEventListeners = new ArrayList<ProcessEventListener>();
        ruleRuntimeEventListeners = new ArrayList<RuleRuntimeEventListener>();
    }

    public ReleaseId getReleaseId() {
        return releaseId;
    }

    public void setReleaseId(ReleaseId releaseId) {
        this.releaseId = releaseId;
    }

    public KieSessionConfiguration getConf() {
        return conf;
    }

    public void setConf(KieSessionConfiguration conf) {
        this.conf = conf;
    }

    public String getKBaseName() {
        return kBaseName;
    }

    public void setKBaseName(String kBaseName) {
        this.kBaseName = kBaseName;
    }

    /**
     * Additional Setter to satisfy Spring Eclipse support (avoiding "No setter found" errors).
     */
    public void setkBaseName(String kBaseName) {
        this.kBaseName = kBaseName;
    }

    public List<Command<?>> getBatch() {
        return batch;
    }

    public void setBatch(List<Command<?>> commands) {
        this.batch = commands;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getClockType() {
        return clockType;
    }

    public void setClockType( String clockType ) {
        this.clockType = clockType;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public KieBase getKBase() {
        return kBase;
    }

    public void setKBase(KieBase kBase) {
        this.kBase = kBase;
    }

    /**
     * Additional Setter to satisfy Spring Eclipse support (avoiding "No setter found" errors).
     */
    public void setkBase(KieBase kBase) {
        this.kBase = kBase;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDef() {
        return def;
    }

    public void setDef(String def) {
        this.def = def;
    }

    public Object getObject() throws Exception {
        if ("prototype".equalsIgnoreCase(scope)) {
            helper.setKieBase(kBase);
            Object kSession = helper.internalNewObject();
            attachLoggers((KieRuntimeEventManager) kSession);
            attachListeners((KieRuntimeEventManager) kSession);
            return kSession;
        }
        return helper.internalGetObject();
    }

    public Class<? extends KieRuntime> getObjectType() {
        return KieRuntime.class;
    }

    public boolean isSingleton() {
        return "singleton".equalsIgnoreCase(scope);
    }

    public void afterPropertiesSet() throws Exception {

        if ( "singleton".equalsIgnoreCase(scope) ) {
            KieObjectsResolver kieObjectsResolver = new KieObjectsResolver();

            kSession = kieObjectsResolver.resolveKSession(name, releaseId);
            if (kSession instanceof StatelessKieSession) {
                helper = new StatelessKSessionFactoryBeanHelper(this, (StatelessKieSession) kSession);
            } else if (kSession instanceof KieSession) {
                helper = new StatefulKSessionFactoryBeanHelper(this, (KieSession) kSession);
            }
            helper.internalAfterPropertiesSet();
            // get ksession from helper as it might change the ksession when persistence is configured
            kSession = helper.internalGetObject();
            attachLoggers((KieRuntimeEventManager) kSession);
            attachListeners((KieRuntimeEventManager) kSession);
        } else {
            if ("stateless".equalsIgnoreCase(type)) {
                helper = new StatelessKSessionFactoryBeanHelper(this, null);
            } else {
                helper = new StatefulKSessionFactoryBeanHelper(this, null);
            }
        }
    }

    public StatefulKSessionFactoryBeanHelper.JpaConfiguration getJpaConfiguration() {
        return jpaConfiguration;
    }

    public void setJpaConfiguration(StatefulKSessionFactoryBeanHelper.JpaConfiguration jpaConfiguration) {
        this.jpaConfiguration = jpaConfiguration;
    }

    public void setEventListenersFromGroup(List<Object> eventListenerList) {
        for (Object eventListener : eventListenerList) {
            if (eventListener instanceof AgendaEventListener) {
                agendaEventListeners.add((AgendaEventListener) eventListener);
            }
            if (eventListener instanceof RuleRuntimeEventListener) {
                ruleRuntimeEventListeners.add((RuleRuntimeEventListener) eventListener);
            }
            if (eventListener instanceof ProcessEventListener) {
                processEventListeners.add((ProcessEventListener) eventListener);
            }
        }
        groupedListeners.addAll(eventListenerList);
    }

    public List<LoggerAdaptor> getKnowledgeRuntimeLoggers() {
        return loggerAdaptors;
    }

    public void setKnowledgeRuntimeLoggers(List<LoggerAdaptor> loggers) {
        this.loggerAdaptors.addAll(loggers);
    }

    public void attachLoggers(KieRuntimeEventManager ksession) {
        if (loggerAdaptors != null && !loggerAdaptors.isEmpty()) {
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
                    if (eventListener instanceof RuleRuntimeEventListener) {
                        ruleRuntimeEventListeners.add((RuleRuntimeEventListener) eventListener);
                    } else {
                        throw new IllegalArgumentException("The ruleRuntimeEventListener (" + eventListener.getClass()
                                + ") is not an instance of " + RuleRuntimeEventListener.class);
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

    public List<RuleRuntimeEventListener> getRuleRuntimeEventListeners() {
        return ruleRuntimeEventListeners;
    }

    public void setRuleRuntimeEventListeners(List<RuleRuntimeEventListener> ruleRuntimeEventListeners) {
        this.ruleRuntimeEventListeners = ruleRuntimeEventListeners;
    }

    public void attachListeners(KieRuntimeEventManager kieRuntimeEventManager) {
        for (AgendaEventListener agendaEventListener : getAgendaEventListeners()) {
            kieRuntimeEventManager.addEventListener(agendaEventListener);
        }
        for (ProcessEventListener processEventListener : getProcessEventListeners()) {
            kieRuntimeEventManager.addEventListener(processEventListener);
        }
        for (RuleRuntimeEventListener ruleRuntimeEventListener : getRuleRuntimeEventListeners()) {
            kieRuntimeEventManager.addEventListener(ruleRuntimeEventListener);
        }
    }
}
