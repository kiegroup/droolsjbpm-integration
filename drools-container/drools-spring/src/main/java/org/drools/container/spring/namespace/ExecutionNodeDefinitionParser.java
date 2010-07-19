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
