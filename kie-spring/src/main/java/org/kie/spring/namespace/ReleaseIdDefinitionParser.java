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

import org.kie.spring.factorybeans.ReleaseIdFactoryBean;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

import static org.kie.spring.namespace.DefinitionParserHelper.emptyAttributeCheck;

public class ReleaseIdDefinitionParser extends AbstractBeanDefinitionParser {

    private static final String ATTRIBUTE_ID = "id";
    private static final String ATTRIBUTE_GROUPID = "groupId";
    private static final String ATTRIBUTE_ARTIFACTID = "artifactId";
    private static final String ATTRIBUTE_VERSION = "version";

    @Override
    protected AbstractBeanDefinition parseInternal(Element element,
                                                   ParserContext parserContext) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(ReleaseIdFactoryBean.class);

        String value = element.getAttribute(ATTRIBUTE_ID);
        emptyAttributeCheck(element.getLocalName(), ATTRIBUTE_ID, value);
        factory.addPropertyValue("id", value);

        value = element.getAttribute(ATTRIBUTE_GROUPID);
        emptyAttributeCheck(element.getLocalName(), ATTRIBUTE_GROUPID, value);
        factory.addPropertyValue("groupId", value);

        value = element.getAttribute(ATTRIBUTE_ARTIFACTID);
        emptyAttributeCheck(element.getLocalName(), ATTRIBUTE_ARTIFACTID, value);
        factory.addPropertyValue("artifactId", value);

        value = element.getAttribute(ATTRIBUTE_VERSION);
        emptyAttributeCheck(element.getLocalName(), ATTRIBUTE_VERSION, value);
        factory.addPropertyValue("version", value);

        return factory.getBeanDefinition();
    }

}
