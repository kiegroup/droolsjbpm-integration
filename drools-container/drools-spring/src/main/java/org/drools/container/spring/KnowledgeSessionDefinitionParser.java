package org.drools.container.spring;

import java.util.ArrayList;
import java.util.List;

import org.drools.KnowledgeBase;
import org.drools.builder.ResourceType;
import org.drools.io.InternalResource;
import org.drools.io.Resource;
import org.drools.io.impl.ClassPathResource;
import org.drools.io.impl.UrlResource;
import org.drools.runtime.CommandExecutor;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class KnowledgeSessionDefinitionParser extends AbstractSingleBeanDefinitionParser {
    protected Class getBeanClass(Element element) {
        return CommandExecutor.class;
    }

    protected void doParse(Element element,
                           BeanDefinitionBuilder bean) {
        String kbase = element.getAttribute( "kbase" );
        String sessionType = element.getAttribute( "type" );
        
        bean.getRawBeanDefinition().setBeanClassName( "org.drools.container.spring.SpringDroolsFactory" );
        
        if ( "stateful".equals( sessionType ) ) {
            bean.getRawBeanDefinition().setFactoryMethodName( "newStatefulKnowledgeSession" );
        } else if ( "stateless".equals( sessionType ) ) {
            bean.getRawBeanDefinition().setFactoryMethodName( "newStatelessKnowledgeSession" );
        } else {
            
        }

        bean.addConstructorArgReference( kbase );
    }

    public void emptyAttributeCheck(final String element,
                                    final String attributeName,
                                    final String attribute) {
        if ( attribute == null || attribute.trim().equals( "" ) ) {
            throw new IllegalArgumentException( "<" + element + "> requires a '" + attributeName + "' attribute" );
        }
    }

}
