/*
* Copyright 2012 JBoss Inc
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


import static org.drools.container.spring.namespace.DefinitionParserHelper.emptyAttributeCheck;

import java.util.ArrayList;
import java.util.List;

import org.drools.container.spring.beans.EnvironmentDefBeanFactory;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

public class EnvironmentDefinitionParser extends AbstractBeanDefinitionParser {

    public static final String ATTRIBUTE_ID = "id";
    public static final String ATTRIBUTE_REF = "ref";
    public static final String ATTRIBUTE_SCOPE = "scope";
    public static final String ATTRIBUTE_STRATEGY_ACCEPTOR_REF = "strategy-acceptor-ref";
    public static final String ATTRIBUTE_ENV_REF = "env-ref";

    public static final String ELEMENT_ENTITY_MANAGER_FACTORY = "entity-manager-factory";
    public static final String ELEMENT_TRANSACTION_MANAGER = "transaction-manager";
    public static final String ELEMENT_GLOBALS = "globals";
    public static final String ELEMENT_DATE_FORMATS = "date-formats";
    public static final String ELEMENT_CALENDARS = "calendars";
    public static final String ELEMENT_OBJECT_MARSHALLING_STRATEGIES = "object-marshalling-strategies";
    public static final String ELEMENT_SERIALIZABLE_PLACEHOLDER_RESOLVER_STRATEGY = "serializable-placeholder-resolver-strategy";
    public static final String ELEMENT_IDENTITY_PLACEHOLDER_RESOLVER_STRATEGY = "identity-placeholder-resolver-strategy";
    public static final String ELEMENT_PROCESS_INSTANCE_RESOLVER_STRATEGY = "process-instance-resolver-strategy";
    public static final String ELEMENT_JPA_PLACEHOLDER_RESOLVER_STRATEGY = "jpa-placeholder-resolver-strategy";
    public static final String ELEMENT_SCOPED_ENTITY_MANAGER = "scoped-entity-manager";
    public static final String ELEMENT_CUSTOM_MARSHALLING_STRATEGY = "custom-marshalling-strategy";
    public static final String ELEMENT_BEAN = "bean";
    public static final String ELEMENT_STRATEGY_ACCEPTOR = "strategy-acceptor";

    public static final String PROPERTY_ENTITY_MANAGER_FACTORY = "entityManagerFactory";
    public static final String PROPERTY_TRANSACTION_MANAGER = "transactionManager";
    public static final String PROPERTY_GLOBALS = "globals";
    public static final String PROPERTY_CALENDARS = "calendars";
    public static final String PROPERTY_DATE_FORMATS = "dateFormats";
    public static final String PROPERTY_JPA_PLACE_HOLDER_RESOLVER_STRATEGY_ENV = "jpaPlaceHolderResolverStrategyEnv";
    public static final String PROPERTY_APP_SCOPED_ENTITY_MANAGER = "appScopedEntityManager";
    public static final String PROPERTY_CMD_SCOPED_ENTITY_MANAGER = "cmdScopedEntityManager";
    public static final String PROPERTY_SERIALIZABLE_PLACEHOLDER_RESOLVER_STRATEGY_ACCEPTOR = "serializablePlaceholderResolverStrategyAcceptor";
    public static final String PROPERTY_IDENTITY_PLACEHOLDER_RESOLVER_STRATEGY_ACCEPTOR = "identityPlaceholderResolverStrategyAcceptor";
    public static final String PROPERTY_CUSTOM_MARSHALLING_STRATEGY = "customMarshallingStrategies";
    public static final String PROPERTY_OBJECT_MARSHALLING_ORDER = "objectMarshallersOrder";

    public static final String PROPERTY_NAME = "name";

    protected AbstractBeanDefinition parseInternal(Element element,
            ParserContext parserContext) {

        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(EnvironmentDefBeanFactory.class);
        String id = element.getAttribute(ATTRIBUTE_ID);
        emptyAttributeCheck(element.getLocalName(), ATTRIBUTE_ID, id);
        factory.addPropertyValue(PROPERTY_NAME, id);

        checkRefOrNestedBean(element, ELEMENT_ENTITY_MANAGER_FACTORY, PROPERTY_ENTITY_MANAGER_FACTORY, ATTRIBUTE_REF, factory, parserContext);
        checkRefOrNestedBean(element, ELEMENT_TRANSACTION_MANAGER, PROPERTY_TRANSACTION_MANAGER, ATTRIBUTE_REF, factory, parserContext);
        checkRefOrNestedBean(element, ELEMENT_GLOBALS, PROPERTY_GLOBALS, ATTRIBUTE_REF, factory, parserContext);
        checkRefOrNestedBean(element, ELEMENT_DATE_FORMATS, PROPERTY_DATE_FORMATS, ATTRIBUTE_REF, factory, parserContext);
        checkRefOrNestedBean(element, ELEMENT_CALENDARS, PROPERTY_CALENDARS, ATTRIBUTE_REF, factory, parserContext);

        Element objectMarshallingStrategiesElement = DomUtils.getChildElementByTagName(element, ELEMENT_OBJECT_MARSHALLING_STRATEGIES);
        if ( objectMarshallingStrategiesElement != null ) {
            List<String> marshallerOrderList = new ArrayList<String>();
            List<Element> children = DomUtils.getChildElements(objectMarshallingStrategiesElement);
            ManagedList managedCustomList = new ManagedList();

            for (Element child : children) {
                String localName = child.getLocalName();
                marshallerOrderList.add(localName);

                if (ELEMENT_SERIALIZABLE_PLACEHOLDER_RESOLVER_STRATEGY.equalsIgnoreCase(localName)){
                    parsePlaceholderResolverStrategyElement(parserContext, factory, child, PROPERTY_SERIALIZABLE_PLACEHOLDER_RESOLVER_STRATEGY_ACCEPTOR);
                } else if (ELEMENT_IDENTITY_PLACEHOLDER_RESOLVER_STRATEGY.equalsIgnoreCase(localName)){
                    parsePlaceholderResolverStrategyElement(parserContext, factory, child, PROPERTY_IDENTITY_PLACEHOLDER_RESOLVER_STRATEGY_ACCEPTOR);
                } else if (ELEMENT_PROCESS_INSTANCE_RESOLVER_STRATEGY.equalsIgnoreCase(localName)){
                    //do nothing, the bean will be created in the EnvDefBeanFactory
                } else if (ELEMENT_JPA_PLACEHOLDER_RESOLVER_STRATEGY.equalsIgnoreCase(localName)){
                        String envRef = child.getAttribute(ATTRIBUTE_ENV_REF);
                        if ( StringUtils.hasText(envRef)) {
                            factory.addPropertyValue(PROPERTY_JPA_PLACE_HOLDER_RESOLVER_STRATEGY_ENV, new RuntimeBeanReference(envRef));
                        }
                } else if (ELEMENT_CUSTOM_MARSHALLING_STRATEGY.equalsIgnoreCase(localName)){
                    String ref = child.getAttribute(ATTRIBUTE_REF);
                    if ( StringUtils.hasText(ref)){
                        managedCustomList.add(new RuntimeBeanReference(ref));
                    } else {
                        Element nestedBean = DomUtils.getChildElementByTagName(child, ELEMENT_BEAN);
                        if (nestedBean != null) {
                            Object obj = parserContext.getDelegate().parsePropertySubElement(nestedBean, null, null);
                            managedCustomList.add(obj);
                        } else {
                            throw new IllegalArgumentException(ELEMENT_CUSTOM_MARSHALLING_STRATEGY+" must have either a "+ATTRIBUTE_REF+" attribute or an embedded "+ELEMENT_BEAN+" child element!");
                        }
                    }
                }
            }
            factory.addPropertyValue(PROPERTY_CUSTOM_MARSHALLING_STRATEGY, managedCustomList);
            factory.addPropertyValue(PROPERTY_OBJECT_MARSHALLING_ORDER, marshallerOrderList);
        }

        Element scopedEM = DomUtils.getChildElementByTagName(element, ELEMENT_SCOPED_ENTITY_MANAGER);
        if ( scopedEM != null ) {
            String ref = scopedEM.getAttribute(ATTRIBUTE_REF);
            Object runtimeBean = null;
            if (StringUtils.hasText(ref)) {
                runtimeBean = new RuntimeBeanReference(ref);
            } else {
                Element beanElement = DomUtils.getChildElementByTagName(scopedEM, ELEMENT_BEAN);
                if ( beanElement != null) {
                    runtimeBean = parserContext.getDelegate().parsePropertySubElement(beanElement, null, null);
                } else {
                    throw new IllegalArgumentException(ELEMENT_SCOPED_ENTITY_MANAGER+" must have an embedded "+ELEMENT_BEAN+" element, or a '"+ATTRIBUTE_REF+"' attribute");
                }
            }
            String scope = scopedEM.getAttribute(ATTRIBUTE_SCOPE);
            if ( "app".equalsIgnoreCase(scope)) {
                factory.addPropertyValue(PROPERTY_APP_SCOPED_ENTITY_MANAGER,runtimeBean);
            } else if ("cmd".equalsIgnoreCase(scope)) {
                factory.addPropertyValue(PROPERTY_CMD_SCOPED_ENTITY_MANAGER,runtimeBean);
            } else {
                throw new IllegalArgumentException(ELEMENT_SCOPED_ENTITY_MANAGER+": '"+ATTRIBUTE_SCOPE+"' attribute must be either 'app' or 'cmd'");
            }
        }
        return factory.getBeanDefinition();
    }

    private void parsePlaceholderResolverStrategyElement(ParserContext parserContext, BeanDefinitionBuilder factory, Element actualElement,
            String strategyAcceptorPropName) {
        if ( actualElement != null ) {
            String ref = actualElement.getAttribute(ATTRIBUTE_STRATEGY_ACCEPTOR_REF);
            if ( StringUtils.hasText(ref)) {
                factory.addPropertyValue(strategyAcceptorPropName, new RuntimeBeanReference(ref));
            } else {
                Element acceptorElement = DomUtils.getChildElementByTagName(actualElement, ELEMENT_STRATEGY_ACCEPTOR);
                if ( acceptorElement != null ) {
                    Element beanElement = DomUtils.getChildElementByTagName(acceptorElement, ELEMENT_BEAN);
                    if ( beanElement != null) {
                        Object obj = parserContext.getDelegate().parsePropertySubElement(beanElement, null, null);
                        factory.addPropertyValue(strategyAcceptorPropName, obj);
                    } else {
                        throw new IllegalArgumentException(ELEMENT_STRATEGY_ACCEPTOR+" must have an embedded "+ELEMENT_BEAN+" element!");
                    }
                } else {
                    throw new IllegalArgumentException(actualElement.getTagName()+" must have either a "+ATTRIBUTE_STRATEGY_ACCEPTOR_REF+" attribute or an embedded "+ELEMENT_STRATEGY_ACCEPTOR+" child element!");
                }
            }
        }
    }

    protected void checkRefOrNestedBean(Element environmentElement, String tagName, String property, String refAttribute, BeanDefinitionBuilder factory, ParserContext parserContext) {
        Element element = DomUtils.getChildElementByTagName(environmentElement, tagName);
        if (element != null) {
            String ref = element.getAttribute(refAttribute);
            if (StringUtils.hasText(ref)) {
                factory.addPropertyValue(property, new RuntimeBeanReference(ref));
            } else {
                Element nestedBean = DomUtils.getChildElementByTagName(element, ELEMENT_BEAN);
                if (nestedBean != null) {
                    Object obj = parserContext.getDelegate().parsePropertySubElement(nestedBean, null, null);
                    factory.addPropertyValue(property, obj);
                } else {
                    throw new IllegalArgumentException(tagName+" must have either a "+refAttribute+" attribute or an embedded "+ELEMENT_BEAN+" child element!");
                }
            }
        }
    }
}
