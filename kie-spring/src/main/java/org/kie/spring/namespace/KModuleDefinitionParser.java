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
package org.kie.spring.namespace;

import org.kie.spring.factorybeans.KModuleFactoryBean;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.expression.spel.ast.BeanReference;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.Arrays;
import java.util.List;

import static org.kie.spring.namespace.DefinitionParserHelper.emptyAttributeCheck;


public class KModuleDefinitionParser extends AbstractBeanDefinitionParser {

    private static final String ATTRIBUTE_ID = "id";

    @SuppressWarnings("unchecked")
    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(KModuleFactoryBean.class);
        String id = element.getAttribute(ATTRIBUTE_ID);
        emptyAttributeCheck(element.getLocalName(), ATTRIBUTE_ID, id);
        factory.addPropertyValue(ATTRIBUTE_ID, id);
        List<Element> kbaseElements = DomUtils.getChildElementsByTagName(element, "kbase");
        AbstractBeanDefinition beanDefinition = factory.getBeanDefinition();
        if (kbaseElements != null && kbaseElements.size() > 0) {
            for (Element kbaseElement : kbaseElements){
                kbaseElement.setAttribute("id", kbaseElement.getAttribute("name"));
                BeanDefinitionHolder obj = (BeanDefinitionHolder) parserContext.getDelegate().parsePropertySubElement(kbaseElement, null, null);
                //obj.getBeanDefinition().getPropertyValues().addPropertyValue("kModule", new RuntimeBeanReference(id));
            }
        }
        return beanDefinition;
    }
}