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

import org.drools.container.spring.beans.GridNodeBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * 
 * @author Lucas Amador
 *
 */
public class GridNodeDefinitionParser extends AbstractBeanDefinitionParser {

    private static final String GRID_ATTRIBUTE = "grid";
    private static final String PORT_ATTRIBUTE = "port";

    @Override
    protected AbstractBeanDefinition parseInternal(Element element,
                                                   ParserContext parserContext) {

        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition( GridNodeBeanFactory.class );

        String id = element.getAttribute( "id" );        
        factory.addPropertyValue( "id",
                                  id );          
        
        String connectionRef = element.getAttribute( GRID_ATTRIBUTE );
        if ( StringUtils.hasText( connectionRef ) ) {
            factory.addPropertyReference( GRID_ATTRIBUTE,
                                          connectionRef );
        }
        
        for (int i = 0, length = element.getChildNodes().getLength(); i < length; i++) {
            Node n = element.getChildNodes().item( i );
            if ( n instanceof Element ) {
                Element e = ( Element ) n;
                
                if ( "socket-service".equals( e.getLocalName() ) ) {
                    String port = e.getAttribute( PORT_ATTRIBUTE );
                    if ( StringUtils.hasText( port ) ) {
                        factory.addPropertyValue( "port", port );
                    }
                }
            }
        }
        return factory.getBeanDefinition();
    }
}
