/*
 * Copyright 2011 JBoss Inc
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


package org.drools.container.spring.beans;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.drools.container.spring.namespace.EventListenersUtil;
import org.kie.api.event.process.ProcessEventListener;
import org.kie.api.event.rule.AgendaEventListener;
import org.kie.api.event.rule.WorkingMemoryEventListener;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;

public class EventListenersBeanFactory implements
        FactoryBean,
        InitializingBean {

    List<Object> eventListeners;

    public Object getObject() throws Exception {
        return eventListeners;
    }

    public Class getObjectType() {
        return List.class;
    }

    public boolean isSingleton() {
        return false;
    }

    public void afterPropertiesSet() throws Exception {

    }

    public void setEventListeners(Map<String, List> eventListenerMap) {
        eventListeners = new ArrayList<Object>();
        for (String key : eventListenerMap.keySet()) {
            List<Object> eventListenerList = eventListenerMap.get(key);
            if (EventListenersUtil.TYPE_AGENDA_EVENT_LISTENER.equalsIgnoreCase(key)) {
                for (Object eventListener : eventListenerList) {
                    if (eventListener instanceof AgendaEventListener) {
                        eventListeners.add((AgendaEventListener) eventListener);
                    }
                }
            } else if (EventListenersUtil.TYPE_WORKING_MEMORY_EVENT_LISTENER.equalsIgnoreCase(key)) {
                for (Object eventListener : eventListenerList) {
                    if (eventListener instanceof WorkingMemoryEventListener) {
                        eventListeners.add((WorkingMemoryEventListener) eventListener);
                    }
                }
            } else if (EventListenersUtil.TYPE_PROCESS_EVENT_LISTENER.equalsIgnoreCase(key)) {
                for (Object eventListener : eventListenerList) {
                    if (eventListener instanceof ProcessEventListener) {
                        eventListeners.add((ProcessEventListener) eventListener);
                    }
                }
            }
        }
    }
}
