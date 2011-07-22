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

import org.drools.container.spring.beans.KnowledgeAgentBeanFactory;
import org.drools.container.spring.beans.KnowledgeLoggerBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

import static org.drools.container.spring.namespace.DefinitionParserHelper.emptyAttributeCheck;


public class KnowledgeLoggerFileParser extends AbstractBeanDefinitionParser {
    @Override
    protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(KnowledgeLoggerBeanFactory.class);
        String localName = element.getLocalName();

        String id = element.getAttribute("id");
        emptyAttributeCheck(element.getLocalName(), "id", id);
        factory.addPropertyValue("name", id);

        String ksession = element.getAttribute("ksession");
        emptyAttributeCheck(element.getLocalName(), "ksession", ksession);
        if (StringUtils.hasText(ksession)) {
            factory.addPropertyReference("ksession", ksession);
        }

        if ( "fileKnowledgeLogger".equalsIgnoreCase(localName) || "threadedFileKnowledgeLogger".equalsIgnoreCase(localName) ) {
            String file = element.getAttribute("file");
            emptyAttributeCheck(element.getLocalName(), "file", file);
            if (StringUtils.hasText(file)) {
                factory.addPropertyValue("file", file);
            }
        }

        if ( "fileKnowledgeLogger".equalsIgnoreCase(localName)) {
            factory.addPropertyValue("loggerType", KnowledgeLoggerBeanFactory.KNOWLEDGE_LOGGER_TYPE.LOGGER_TYPE_FILE);
        } else if ("threadedFileKnowledgeLogger".equalsIgnoreCase(localName)){
            factory.addPropertyValue("loggerType", KnowledgeLoggerBeanFactory.KNOWLEDGE_LOGGER_TYPE.LOGGER_TYPE_THREADED_FILE);
        } else if ("consoleKnowledgeLogger".equalsIgnoreCase(localName)) {
            factory.addPropertyValue("loggerType", KnowledgeLoggerBeanFactory.KNOWLEDGE_LOGGER_TYPE.LOGGER_TYPES_CONSOLE);
        }
        return factory.getBeanDefinition();
    }
}
