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

import org.kie.spring.factorybeans.LoggerAdaptor;
import org.kie.spring.factorybeans.LoggerFactoryBean;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import static org.kie.spring.namespace.DefinitionParserHelper.emptyAttributeCheck;


public class LoggerDefinitionParser extends AbstractBeanDefinitionParser {

    public static final String LOGGER_ATTRIBUTE_FILE = "file";
    public static final String LOGGER_ATTRIBUTE_ID = "id";
    public static final String LOGGER_ATTRIBUTE_THREADED = "threaded";
    public static final String LOGGER_ATTRIBUTE_LOGGER_TYPE = "loggerType";
    public static final String LOGGER_ATTRIBUTE_INTERVAL = "interval";

    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(LoggerFactoryBean.class);
        String id = element.getAttribute(LOGGER_ATTRIBUTE_ID);
        emptyAttributeCheck(element.getLocalName(), LOGGER_ATTRIBUTE_ID, id);
        factory.addPropertyValue("name", id);
        if ("fileLogger".equalsIgnoreCase(element.getLocalName())) {
            String fileName = element.getAttribute(LOGGER_ATTRIBUTE_FILE);
            emptyAttributeCheck(element.getLocalName(), LOGGER_ATTRIBUTE_FILE, fileName);
            if (StringUtils.hasText(fileName)) {
                factory.addPropertyValue(LOGGER_ATTRIBUTE_FILE, fileName);
            }
            String threadedStr = element.getAttribute(LOGGER_ATTRIBUTE_THREADED);
            if (threadedStr != null && Boolean.valueOf(threadedStr)) {
                factory.addPropertyValue(LOGGER_ATTRIBUTE_LOGGER_TYPE, LoggerAdaptor.KNOWLEDGE_LOGGER_TYPE.LOGGER_TYPE_THREADED_FILE);
                String intervalStr = element.getAttribute(LOGGER_ATTRIBUTE_INTERVAL);
                if (intervalStr != null) {
                    try {
                        int interval = Integer.parseInt(intervalStr);
                        factory.addPropertyValue(LOGGER_ATTRIBUTE_INTERVAL, interval);
                    } catch (Exception e) {
                        //will never happen as the XSD would prevent non-integers
                        throw new IllegalArgumentException("Interval attribute must be of type integer for bean '" + id + "'");
                    }
                }
            } else {
                factory.addPropertyValue(LOGGER_ATTRIBUTE_LOGGER_TYPE, LoggerAdaptor.KNOWLEDGE_LOGGER_TYPE.LOGGER_TYPE_FILE);
            }
        } else if ("consoleLogger".equalsIgnoreCase(element.getLocalName())) {
            factory.addPropertyValue(LOGGER_ATTRIBUTE_LOGGER_TYPE, LoggerAdaptor.KNOWLEDGE_LOGGER_TYPE.LOGGER_TYPE_CONSOLE);
        }
        return factory.getBeanDefinition();
    }
}
