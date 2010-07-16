package org.drools.container.spring.namespace;

import java.util.List;

import org.drools.RuleBaseConfiguration;
import org.drools.conf.EventProcessingOption;
import org.drools.container.spring.beans.KnowledgeBaseBeanFactory;
import org.drools.core.util.StringUtils;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

public class KnowledgeBaseDefinitionParser extends AbstractBeanDefinitionParser {

    private static final String EXECUTION_NODE_ATTRIBUTE         = "node";

    private static final String ADVANCED_PROCESS_RULE_INTEGRATED = "advanced-process-rule-integration";

    private static final String MULTITHREADS                     = "multithread";
    private static final String MAX_THREADS                      = "max-threads";

    private static final String MBEABS                           = "mbeans";

    private static final String EVENT_PROCESSING_MODE            = "event-processing-mode";
    
    private static final String WORK_ITEM_DEFINITIONS            = "work-item-definitions";
    
    private static final String WORK_ITEM_DEFINITION            = "work-item-definition";

    @SuppressWarnings("unchecked")
    @Override
    protected AbstractBeanDefinition parseInternal(Element element,
                                                   ParserContext parserContext) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition( KnowledgeBaseBeanFactory.class );
        
        Element kbaseConf = DomUtils.getChildElementByTagName(element, "configuration");
        if ( kbaseConf != null ) {
            BeanDefinitionBuilder rbaseConfBuilder = BeanDefinitionBuilder.rootBeanDefinition( RuleBaseConfiguration.class );
            Element e = DomUtils.getChildElementByTagName(kbaseConf, ADVANCED_PROCESS_RULE_INTEGRATED);
            if ( e != null && !StringUtils.isEmpty( e.getAttribute( "enabled" ) )) {
                rbaseConfBuilder.addPropertyValue( "advancedProcessRuleIntegration", Boolean.parseBoolean( e.getAttribute( "enabled" ) ) );
            }
            
            e = DomUtils.getChildElementByTagName(kbaseConf, MULTITHREADS);
            if ( e != null && !StringUtils.isEmpty( e.getAttribute( "enabled" ) )) {
                rbaseConfBuilder.addPropertyValue( "multithreadEvaluation", Boolean.parseBoolean( e.getAttribute( "enabled" ) ));
                if (  !StringUtils.isEmpty( e.getAttribute( MAX_THREADS )  ) ) {
                    rbaseConfBuilder.addPropertyValue( "maxThreads" , Integer.parseInt( e.getAttribute( MAX_THREADS ) ) );                    
                }
            }    
            
            e = DomUtils.getChildElementByTagName(kbaseConf, MBEABS);
            if ( e != null && !StringUtils.isEmpty( e.getAttribute( "enabled" ) )) {
                rbaseConfBuilder.addPropertyValue( "MBeansEnabled", Boolean.parseBoolean( e.getAttribute( "enabled" ) ) );
            }   
            
            e = DomUtils.getChildElementByTagName(kbaseConf, EVENT_PROCESSING_MODE);
            if ( e != null && !StringUtils.isEmpty( e.getAttribute( "mode" ) )) {
                rbaseConfBuilder.addPropertyValue( "eventProcessingMode", EventProcessingOption.valueOf( e.getAttribute( "mode" ) ) );
            }                
            
            e = DomUtils.getChildElementByTagName(kbaseConf, WORK_ITEM_DEFINITIONS);
            if ( e != null ) {
                List<Element> children = DomUtils.getChildElementsByTagName( e, WORK_ITEM_DEFINITION );
                if ( children != null && !children.isEmpty() ) {
                    ManagedMap workDefs = new ManagedMap();
                    for ( Element child : children ) {
                        workDefs.put(  child.getAttribute( "name" ),
                                       new RuntimeBeanReference( child.getAttribute( "ref" ) ) );
                    }
                    factory.addPropertyValue( "workDefinitions", workDefs );                    
                }
            }
            
            factory.addPropertyValue( "conf", rbaseConfBuilder.getBeanDefinition() );
        }

        String nodeRef = element.getAttribute( EXECUTION_NODE_ATTRIBUTE );
        if ( nodeRef != null && nodeRef.length() > 0 ) {
            factory.addPropertyReference( EXECUTION_NODE_ATTRIBUTE,
                                          nodeRef );
        }

        ManagedList resources = getResources( element,
                                              parserContext,
                                              factory );

        if ( resources != null ) {
            factory.addPropertyValue( "resources",
                                      resources );
        }

        return factory.getBeanDefinition();
    }

    public static ManagedList getResources(Element element,
                                           ParserContext parserContext,
                                           BeanDefinitionBuilder factory) {
        Element resourcesElm = DomUtils.getChildElementByTagName( element,
                                                                  "resources" );
        ManagedList resources = null;

        if ( resourcesElm != null ) {
            List<Element> childElements = DomUtils.getChildElementsByTagName( resourcesElm,
                                                                              "resource" );
            if ( childElements != null && !childElements.isEmpty() ) {
                resources = new ManagedList();
                for ( Element childResource : childElements ) {
                    BeanDefinition resourceDefinition = parserContext.getDelegate().parseCustomElement( childResource,
                                                                                                        factory.getBeanDefinition() );
                    resources.add( resourceDefinition );
                }
            }

        }
        return resources;
    }

}
