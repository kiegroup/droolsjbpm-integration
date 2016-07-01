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

import org.drools.core.util.StringUtils;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import java.util.List;

public class LoggerUtil {
    static int i = 0;

    public static void parseRuntimeLoggers(ParserContext parserContext, BeanDefinitionBuilder factory, Element element) {
        ManagedList loggerAdaptors = new ManagedList();
        List<Element> fileLoggerElements = DomUtils.getChildElementsByTagName(element, "fileLogger");
        if (fileLoggerElements != null) {
            for (Element fileLoggerElement : fileLoggerElements) {
                String id = checkAndSetID(element, fileLoggerElement);
                parserContext.getDelegate().parsePropertySubElement(fileLoggerElement, null, null);
                loggerAdaptors.add(new RuntimeBeanReference(id));
            }
        }
        Element consoleLoggerElement = DomUtils.getChildElementByTagName(element, "consoleLogger");
        if (consoleLoggerElement != null) {
            String id = checkAndSetID(element, consoleLoggerElement);
            parserContext.getDelegate().parsePropertySubElement(consoleLoggerElement, null, null);
            loggerAdaptors.add(new RuntimeBeanReference(id));
        }
        if (!loggerAdaptors.isEmpty()) {
            factory.addPropertyValue("knowledgeRuntimeLoggers", loggerAdaptors);
        }
    }

    private static String checkAndSetID(Element parent, Element element) {
        String id = element.getAttribute("id");
        if (StringUtils.isEmpty(id)) {
            // this is an anonymous (no id) bean, set a temp id to ensure we can reference it internally.
            id = parent.getAttribute("id") + "_fl" + i;
            element.setAttribute("id", id);
            i++;
        }
        return id;
    }
}
