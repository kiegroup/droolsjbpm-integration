/**
 * Copyright 2010 JBoss Inc
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

import org.drools.container.spring.beans.ExecutionNodeBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * 
 * @author Lucas Amador
 *
 */
public class ExecutionNodeDefinitionParser extends AbstractBeanDefinitionParser {

    private static final String CONNECTION_ATTRIBUTE = "connection";

    @Override
    protected AbstractBeanDefinition parseInternal(Element element,
                                                   ParserContext parserContext) {

        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition( ExecutionNodeBeanFactory.class );

        String connectionRef = element.getAttribute( CONNECTION_ATTRIBUTE );
        if ( StringUtils.hasText( connectionRef ) ) {
            factory.addPropertyReference( "connection",
                                          connectionRef );
        }

        return factory.getBeanDefinition();
    }
}
