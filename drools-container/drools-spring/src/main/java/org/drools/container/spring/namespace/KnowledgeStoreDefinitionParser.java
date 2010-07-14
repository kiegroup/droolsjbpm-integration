package org.drools.container.spring.namespace;

import java.util.List;

import org.drools.container.spring.beans.KnowledgeBaseBeanFactory;
import org.drools.container.spring.beans.KnowledgeStoreBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

public class KnowledgeStoreDefinitionParser extends AbstractBeanDefinitionParser {

    @SuppressWarnings("unchecked")
    @Override
    protected AbstractBeanDefinition parseInternal(Element element,
                                                   ParserContext parserContext) {        
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition( KnowledgeStoreBeanFactory.class );

        return factory.getBeanDefinition();
    }

}
