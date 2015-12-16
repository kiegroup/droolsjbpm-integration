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
package org.kie.spring.namespace;

import org.drools.core.ClockType;
import org.drools.core.SessionConfiguration;
import org.drools.core.command.runtime.SetGlobalCommand;
import org.drools.core.command.runtime.process.SignalEventCommand;
import org.drools.core.command.runtime.process.StartProcessCommand;
import org.drools.core.command.runtime.rule.FireAllRulesCommand;
import org.drools.core.command.runtime.rule.FireUntilHaltCommand;
import org.drools.core.command.runtime.rule.InsertObjectCommand;
import org.springframework.util.StringUtils;
import org.kie.spring.factorybeans.KSessionFactoryBean;
import org.kie.spring.factorybeans.helper.StatefulKSessionFactoryBeanHelper;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.List;

import static org.kie.spring.namespace.DefinitionParserHelper.emptyAttributeCheck;


public class KSessionDefinitionParser extends AbstractBeanDefinitionParser {

    private static final String ATTRIBUTE_ID = "name";
    private static final String ATTRIBUTE_TYPE = "type";

    private static final String LISTENERS_REF_ATTRIBUTE = "listeners-ref";
    private static final String EMF_ATTRIBUTE = "entity-manager-factory";
    private static final String TX_MANAGER_ATTRIBUTE = "transaction-manager";
    private static final String FORCLASS_ATTRIBUTE = "for-class";
    private static final String IMPLEMENTATION_ATTRIBUTE = "implementation";

    private static final String KEEP_REFERENCE = "keep-reference";
    private static final String CLOCK_TYPE = "clock-type";
    private static final String SCOPE = "scope";

    private static final String WORK_ITEMS = "work-item-handlers";
    private static final String WORK_ITEM = "work-item-handler";
    private static final String ID_ATTRIBUTE = "id";

    private static final String ATTRIBUTE_DEFAULT = "default";


    @SuppressWarnings("unchecked")
    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(KSessionFactoryBean.class);

        String id = element.getAttribute(ATTRIBUTE_ID);
        emptyAttributeCheck(element.getLocalName(), ATTRIBUTE_ID, id);
        factory.addPropertyValue(ATTRIBUTE_ID, id);

        element.setAttribute("name", id);
        element.setAttribute("id", id);
        factory.addPropertyValue("def", element.getAttribute(ATTRIBUTE_DEFAULT));

        String type = element.getAttribute(ATTRIBUTE_TYPE);
        if (!StringUtils.hasLength(type)){
            type = "stateful";
        }
        factory.addPropertyValue(ATTRIBUTE_TYPE, type);

        String scope = element.getAttribute(SCOPE);
        if (StringUtils.hasLength(scope)){
            factory.addPropertyValue(SCOPE, scope);
        } else {
            factory.addPropertyValue(SCOPE, "singleton");
        }

        String listeners = element.getAttribute(LISTENERS_REF_ATTRIBUTE);
        if (StringUtils.hasText(listeners)) {
            factory.addPropertyValue("eventListenersFromGroup", new RuntimeBeanReference(listeners));
        }
        EventListenersUtil.parseEventListeners(parserContext, factory, element);

        LoggerUtil.parseRuntimeLoggers(parserContext, factory, element);

        Element ksessionConf = DomUtils.getChildElementByTagName(element, "configuration");
        parseSessionConf(factory, ksessionConf);

        Element batch = DomUtils.getChildElementByTagName(element, "batch");
        parseBatch(parserContext, factory, batch);

        return factory.getBeanDefinition();
    }

    private void parseSessionConf(BeanDefinitionBuilder factory, Element ksessionConf) {
        if (ksessionConf != null) {
            Element persistenceElm = DomUtils.getChildElementByTagName(ksessionConf, "jpa-persistence");
            if (persistenceElm != null) {
                BeanDefinitionBuilder beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(StatefulKSessionFactoryBeanHelper.JpaConfiguration.class);

                String loadId = persistenceElm.getAttribute("load");
                if (org.springframework.util.StringUtils.hasText(loadId)) {
                    beanBuilder.addPropertyValue(ID_ATTRIBUTE, Long.parseLong(loadId));
                }

                Element tnxMng = DomUtils.getChildElementByTagName(persistenceElm, TX_MANAGER_ATTRIBUTE);
                String ref = tnxMng.getAttribute("ref");

                beanBuilder.addPropertyReference("platformTransactionManager", ref);

                Element emf = DomUtils.getChildElementByTagName(persistenceElm, EMF_ATTRIBUTE);
                ref = emf.getAttribute("ref");
                beanBuilder.addPropertyReference("entityManagerFactory", ref);

                Element variablePersisters = DomUtils.getChildElementByTagName(persistenceElm, "variable-persisters");
                if (variablePersisters != null && variablePersisters.hasChildNodes()) {
                    List<Element> childPersisterElems = DomUtils.getChildElementsByTagName(variablePersisters,
                            "persister");
                    ManagedMap persistors = new ManagedMap(childPersisterElems.size());
                    for (Element persisterElem : childPersisterElems) {
                        String forClass = persisterElem.getAttribute(FORCLASS_ATTRIBUTE);
                        String implementation = persisterElem.getAttribute(IMPLEMENTATION_ATTRIBUTE);
                        if (!org.springframework.util.StringUtils.hasText(forClass)) {
                            throw new RuntimeException("persister element must have valid for-class attribute");
                        }
                        if (!org.springframework.util.StringUtils.hasText(implementation)) {
                            throw new RuntimeException("persister element must have valid implementation attribute");
                        }
                        persistors.put(forClass, implementation);
                    }
                    beanBuilder.addPropertyValue("variablePersisters", persistors);
                }
                factory.addPropertyValue("jpaConfiguration", beanBuilder.getBeanDefinition());
            }
            BeanDefinitionBuilder rbaseConfBuilder = BeanDefinitionBuilder.rootBeanDefinition(SessionConfiguration.class, "newInstance");
            Element e = DomUtils.getChildElementByTagName(ksessionConf, KEEP_REFERENCE);
            if (e != null && org.springframework.util.StringUtils.hasText(e.getAttribute("enabled"))) {
                rbaseConfBuilder.addPropertyValue("keepReference", Boolean.parseBoolean(e.getAttribute("enabled")));
            }

            e = DomUtils.getChildElementByTagName(ksessionConf, CLOCK_TYPE);
            if (e != null && org.springframework.util.StringUtils.hasText(e.getAttribute("type"))) {
                rbaseConfBuilder.addPropertyValue("clockType", ClockType.resolveClockType(e.getAttribute("type")));
            }
            factory.addPropertyValue("conf", rbaseConfBuilder.getBeanDefinition());

            e = DomUtils.getChildElementByTagName(ksessionConf, WORK_ITEMS);
            if (e != null) {
                List<Element> children = DomUtils.getChildElementsByTagName(e, WORK_ITEM);
                if (children != null && !children.isEmpty()) {
                    ManagedMap workDefs = new ManagedMap();
                    for (Element child : children) {
                        workDefs.put(child.getAttribute("name"), new RuntimeBeanReference(child.getAttribute("ref")));
                    }
                    factory.addPropertyValue("workItems", workDefs);
                }
            }
        }
    }

    private void parseBatch(ParserContext parserContext, BeanDefinitionBuilder factory, Element batch) {
        if (batch != null) {
            // we know there can only ever be one
            ManagedList children = new ManagedList();

            for (int i = 0, length = batch.getChildNodes().getLength(); i < length; i++) {
                Node n = batch.getChildNodes().item(i);
                if (n instanceof Element) {
                    Element e = (Element) n;

                    BeanDefinitionBuilder beanBuilder = null;
                    if ("insert-object".equals(e.getLocalName())) {
                        String ref = e.getAttribute("ref");
                        Element nestedElm = getFirstElement(e.getChildNodes());
                        beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(InsertObjectCommand.class);
                        if (org.springframework.util.StringUtils.hasText(ref)) {
                            beanBuilder.addConstructorArgReference(ref);
                        } else if (nestedElm != null) {
                            beanBuilder.addConstructorArgValue(parserContext.getDelegate().parsePropertySubElement(nestedElm,
                                    null,
                                    null));
                        } else {
                            throw new IllegalArgumentException("insert-object must either specify a 'ref' attribute or have a nested bean");
                        }
                    } else if ("set-global".equals(e.getLocalName())) {
                        String ref = e.getAttribute("ref");
                        Element nestedElm = getFirstElement(e.getChildNodes());
                        beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(SetGlobalCommand.class);
                        beanBuilder.addConstructorArgValue(e.getAttribute("identifier"));
                        if (org.springframework.util.StringUtils.hasText(ref)) {
                            beanBuilder.addConstructorArgReference(ref);
                        } else if (nestedElm != null) {
                            beanBuilder.addConstructorArgValue(parserContext.getDelegate().parsePropertySubElement(nestedElm,
                                    null,
                                    null));
                        } else {
                            throw new IllegalArgumentException("set-global must either specify a 'ref' attribute or have a nested bean");
                        }
                    } else if ("fire-until-halt".equals(e.getLocalName())) {
                        beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(FireUntilHaltCommand.class);
                    } else if ("fire-all-rules".equals(e.getLocalName())) {
                        beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(FireAllRulesCommand.class);
                        String max = e.getAttribute("max");
                        if (org.springframework.util.StringUtils.hasText(max)) {
                            beanBuilder.addPropertyValue("max", max);
                        }
                    } else if ("start-process".equals(e.getLocalName())) {

                        beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(StartProcessCommand.class);
                        String processId = e.getAttribute("process-id");
                        if (!org.springframework.util.StringUtils.hasText(processId)) {
                            throw new IllegalArgumentException("start-process must specify a process-id");
                        }
                        beanBuilder.addConstructorArgValue(processId);

                        List<Element> params = DomUtils.getChildElementsByTagName(e, "parameter");
                        if (!params.isEmpty()) {
                            ManagedMap map = new ManagedMap();
                            for (Element param : params) {
                                String identifier = param.getAttribute("identifier");
                                if (!org.springframework.util.StringUtils.hasText(identifier)) {
                                    throw new IllegalArgumentException("start-process paramaters must specify an identifier");
                                }

                                String ref = param.getAttribute("ref");
                                Element nestedElm = getFirstElement(param.getChildNodes());
                                if (org.springframework.util.StringUtils.hasText(ref)) {
                                    map.put(identifier, new RuntimeBeanReference(ref));
                                } else if (nestedElm != null) {
                                    map.put(identifier, parserContext.getDelegate().parsePropertySubElement(nestedElm,
                                            null,
                                            null));
                                } else {
                                    throw new IllegalArgumentException("start-process parameters must either specify a 'ref' attribute or have a nested bean");
                                }
                            }
                            beanBuilder.addPropertyValue("parameters", map);
                        }
                    } else if ("signal-event".equals(e.getLocalName())) {
                        beanBuilder = BeanDefinitionBuilder.genericBeanDefinition(SignalEventCommand.class);
                        String processInstanceId = e.getAttribute("process-instance-id");
                        if (org.springframework.util.StringUtils.hasText(processInstanceId)) {
                            beanBuilder.addConstructorArgValue(processInstanceId);
                        }

                        beanBuilder.addConstructorArgValue(e.getAttribute("event-type"));

                        String ref = e.getAttribute("ref");
                        Element nestedElm = getFirstElement(e.getChildNodes());
                        if (org.springframework.util.StringUtils.hasText(ref)) {
                            beanBuilder.addConstructorArgReference(ref);
                        } else if (nestedElm != null) {
                            beanBuilder.addConstructorArgValue(parserContext.getDelegate().parsePropertySubElement(nestedElm,
                                    null,
                                    null));
                        } else {
                            throw new IllegalArgumentException("signal-event must either specify a 'ref' attribute or have a nested bean");
                        }
                    }
                    if (beanBuilder == null) {
                        throw new IllegalStateException("Unknow element: " + e.getLocalName());
                    }
                    children.add(beanBuilder.getBeanDefinition());
                }
            }
            factory.addPropertyValue("batch", children);
        }
    }

    private Element getFirstElement(NodeList list) {
        for (int j = 0, lengthj = list.getLength(); j < lengthj; j++) {
            if (list.item(j) instanceof Element) {
                return (Element) list.item(j);
            }
        }
        return null;
    }
}