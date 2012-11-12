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


package org.drools.container.spring.namespace;

import org.drools.event.DebugProcessEventListener;
import org.kie.event.rule.DebugAgendaEventListener;
import org.kie.event.rule.DebugWorkingMemoryEventListener;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.List;

public abstract class EventListenersUtil {

    public static final String TYPE_AGENDA_EVENT_LISTENER = "agenda-event-listener";
    public static final String TYPE_PROCESS_EVENT_LISTENER = "process-event-listener";
    public static final String TYPE_WORKING_MEMORY_EVENT_LISTENER = "working-memory-event-listener";

    public static final String ELEMENT_AGENDA_EVENT_LISTENER = "agendaEventListener";
    public static final String ELEMENT_PROCESS_EVENT_LISTENER = "processEventListener";
    public static final String ELEMENT_WORKING_MEMORY_EVENT_LISTENER = "workingMemoryEventListener";

    public static void parseEventListeners(ParserContext parserContext, BeanDefinitionBuilder factory, Element element) {
        ManagedMap completeListenersMap = new ManagedMap();

        List<Element> eventListeners = DomUtils.getChildElementsByTagName(element, ELEMENT_AGENDA_EVENT_LISTENER);
        if (eventListeners != null) {
            ManagedMap listeners = parseEventListenersByType(parserContext, eventListeners, TYPE_AGENDA_EVENT_LISTENER);
            completeListenersMap.putAll(listeners);
        }

        eventListeners = DomUtils.getChildElementsByTagName(element, ELEMENT_PROCESS_EVENT_LISTENER);
        if (eventListeners != null) {
            ManagedMap listeners = parseEventListenersByType(parserContext, eventListeners, TYPE_PROCESS_EVENT_LISTENER);
            completeListenersMap.putAll(listeners);
        }

        eventListeners = DomUtils.getChildElementsByTagName(element, ELEMENT_WORKING_MEMORY_EVENT_LISTENER);
        if (eventListeners != null) {
            ManagedMap listeners = parseEventListenersByType(parserContext, eventListeners, TYPE_WORKING_MEMORY_EVENT_LISTENER);
            completeListenersMap.putAll(listeners);
        }

        factory.addPropertyValue("eventListeners", completeListenersMap);
    }

    private static ManagedMap parseEventListenersByType(ParserContext parserContext, List<Element> eventListeners, String listenerType) {
        ManagedMap listeners = new ManagedMap();
        for (Element listener : eventListeners) {
            String ref = listener.getAttribute("ref");
            // if this a bean ref
            if (StringUtils.hasText(ref)) {
                if (TYPE_AGENDA_EVENT_LISTENER.equalsIgnoreCase(listenerType) || TYPE_PROCESS_EVENT_LISTENER.equalsIgnoreCase(listenerType) || TYPE_WORKING_MEMORY_EVENT_LISTENER.equalsIgnoreCase(listenerType)) {
                    ManagedList subList = (ManagedList) listeners.get(listenerType);
                    if (subList == null) {
                        subList = new ManagedList();
                        listeners.put(listenerType, subList);
                    }
                    subList.add(new RuntimeBeanReference(ref));
                } else {
                    throw new IllegalArgumentException("eventListener must be of type 'agenda-event-listener or 'process-event-listener' or 'working-memory-event-listener'.");
                }
            } else {
                //not a ref check if it is a nested bean
                Element nestedBean = DomUtils.getChildElementByTagName(listener, "bean");
                if (nestedBean == null) {
                    //no 'ref' and no nested beans, add the default debug listeners part of the core libs.
                    Object obj = null;
                    if (TYPE_AGENDA_EVENT_LISTENER.equalsIgnoreCase(listenerType)) {
                        obj = new DebugAgendaEventListener();
                    } else if (TYPE_PROCESS_EVENT_LISTENER.equalsIgnoreCase(listenerType)) {
                        obj = new DebugProcessEventListener();
                    } else if (TYPE_WORKING_MEMORY_EVENT_LISTENER.equalsIgnoreCase(listenerType)) {
                        obj = new DebugWorkingMemoryEventListener();
                    } else {
                        throw new IllegalArgumentException("eventListener must be of type 'agenda-event-listener or 'process-event-listener' or 'working-memory-event-listener'.");
                    }
                    ManagedList subList = (ManagedList) listeners.get(listenerType);
                    if (subList == null) {
                        subList = new ManagedList();
                        listeners.put(listenerType, subList);
                    }
                    subList.add(obj);
                } else {
                    //String type = StringUtils.hasText(listenerType) ? listenerType: "infer";
                    Object obj = parserContext.getDelegate().parsePropertySubElement(nestedBean, null, null);
                    ManagedList subList = (ManagedList) listeners.get(listenerType);
                    if (subList == null) {
                        subList = new ManagedList();
                        listeners.put(listenerType, subList);
                    }
                    subList.add(obj);
                }
            }
        }
        return listeners;
    }

}
