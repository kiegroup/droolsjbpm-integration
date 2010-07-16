package org.drools.container.spring.namespace;

import org.drools.container.spring.beans.ConnectionBeanFactory;
import org.drools.container.spring.beans.ResourceChangeScannerBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * 
 * @author Lucas Amador
 *
 */
public class ResourceChangeScannerDefinitionParser extends AbstractBeanDefinitionParser {

    private static final String INTERVAL = "interval";

    protected AbstractBeanDefinition parseInternal(Element element,
                                                   ParserContext parserContext) {

        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition( ResourceChangeScannerBeanFactory.class );

        factory.addPropertyValue( INTERVAL,
        						  element.getAttribute( INTERVAL ) );

        return factory.getBeanDefinition();
    }

}
