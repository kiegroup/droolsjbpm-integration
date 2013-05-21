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

import org.drools.core.util.StringUtils;
import org.kie.spring.factorybeans.KBaseRefFactoryBean;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import static org.kie.spring.namespace.DefinitionParserHelper.emptyAttributeCheck;


public class KBaseRefDefinitionParser extends AbstractBeanDefinitionParser {

    private static final String ATTRIBUTE_ID = "id";

    @SuppressWarnings("unchecked")
    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(KBaseRefFactoryBean.class);

        String id = element.getAttribute(ATTRIBUTE_ID);
        emptyAttributeCheck(element.getLocalName(), ATTRIBUTE_ID, id);
        factory.addPropertyValue(ATTRIBUTE_ID, id);

        String releaseIdRef = element.getAttribute("releaseId");
        if (!StringUtils.isEmpty(releaseIdRef)) {
            factory.addPropertyReference("releaseId", releaseIdRef);
        }

        return factory.getBeanDefinition();
    }
}
