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


import org.kie.spring.factorybeans.KBaseFactoryBean;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.List;

import static org.kie.spring.namespace.DefinitionParserHelper.emptyAttributeCheck;


public class KBaseDefinitionParser extends AbstractBeanDefinitionParser {

    private static final String ATTRIBUTE_ID = "name";
    private static final String ATTRIBUTE_PACKAGES = "packages";
    private static final String ATTRIBUTE_INCLUDES = "includes";
    private static final String ATTRIBUTE_EVENT_MODE = "eventProcessingMode";
    private static final String ATTRIBUTE_EQUALS = "equalsBehavior";
    private static final String ATTRIBUTE_DECLARATIVE_AGENDA = "declarativeAgenda";
    private static final String ATTRIBUTE_SCOPE = "scope";
    private static final String ATTRIBUTE_DEFAULT = "default";

    @SuppressWarnings("unchecked")
    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(KBaseFactoryBean.class);

        String id = element.getAttribute(ATTRIBUTE_ID);
        emptyAttributeCheck(element.getLocalName(), ATTRIBUTE_ID, id);
        factory.addPropertyValue("kBaseName", id);
        factory.addPropertyValue("id", id);
        factory.addPropertyValue("packages", element.getAttribute(ATTRIBUTE_PACKAGES));
        factory.addPropertyValue("includes", element.getAttribute(ATTRIBUTE_INCLUDES));
        factory.addPropertyValue("eventProcessingMode", element.getAttribute(ATTRIBUTE_EVENT_MODE));
        factory.addPropertyValue("equalsBehavior", element.getAttribute(ATTRIBUTE_EQUALS));
        factory.addPropertyValue("declarativeAgenda", element.getAttribute(ATTRIBUTE_DECLARATIVE_AGENDA));
        factory.addPropertyValue("scope", element.getAttribute(ATTRIBUTE_SCOPE));
        factory.addPropertyValue("def", element.getAttribute(ATTRIBUTE_DEFAULT));

        element.setAttribute("name", id);
        List<Element> ksessionElements = DomUtils.getChildElementsByTagName(element, "ksession");
        if (ksessionElements != null && !ksessionElements.isEmpty()) {
            for (Element kbaseElement : ksessionElements){
                BeanDefinitionHolder obj = (BeanDefinitionHolder) parserContext.getDelegate().parsePropertySubElement(kbaseElement, null);
                obj.getBeanDefinition().getPropertyValues().addPropertyValue("kBaseName", id);
                obj.getBeanDefinition().getPropertyValues().addPropertyValue("kBase", new RuntimeBeanReference(id));
            }
        }

        return factory.getBeanDefinition();
    }
}