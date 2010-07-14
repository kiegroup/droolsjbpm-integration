package org.drools.container.spring.namespace;

import static org.drools.container.spring.namespace.DefinitionParserHelper.emptyAttributeCheck;

import org.drools.container.spring.beans.ExecutionNodeBeanFactory;
import org.drools.container.spring.beans.KnowledgeAgentBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;

/**
 * 
 * @author Lucas Amador
 *
 */
public class KnowledgeAgentDefinitionParser extends AbstractBeanDefinitionParser {

    @Override
    protected AbstractBeanDefinition parseInternal(Element element,
                                                   ParserContext parserContext) {

        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition( KnowledgeAgentBeanFactory.class );

        String id = element.getAttribute( "id" );
        emptyAttributeCheck( element.getLocalName(),
                             "id", 
                             id);
        factory.addPropertyValue( "id",
                                  id );
        
        String kbase = element.getAttribute( "kbase" );
        if ( StringUtils.hasText( kbase ) ) {
            factory.addPropertyReference( "kbase",
                                          kbase );     
        }
        
        String newInstance = element.getAttribute( "new-instance" );
        if ( StringUtils.hasText( newInstance ) ) {
            factory.addPropertyValue( "newInstance",
                                      newInstance );                  
        }
        
        ManagedList resources = KnowledgeBaseDefinitionParser.getResources(element, parserContext, factory);

        if ( resources != null ) {
            factory.addPropertyValue( "resources",
                                      resources );
        }        


        return factory.getBeanDefinition();
    }
}
