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

import static org.drools.container.spring.namespace.DefinitionParserHelper.emptyAttributeCheck;

import java.util.List;

import org.drools.ClockType;
import org.drools.SessionConfiguration;
import org.drools.command.runtime.SetGlobalCommand;
import org.drools.command.runtime.process.SignalEventCommand;
import org.drools.command.runtime.process.StartProcessCommand;
import org.drools.command.runtime.rule.FireAllRulesCommand;
import org.drools.command.runtime.rule.FireUntilHaltCommand;
import org.drools.command.runtime.rule.InsertObjectCommand;
import org.drools.container.spring.beans.KnowledgeAgentBeanFactory;
import org.drools.container.spring.beans.StatefulKnowledgeSessionBeanFactory;
import org.drools.container.spring.beans.StatefulKnowledgeSessionBeanFactory.JpaConfiguration;
import org.drools.container.spring.beans.StatelessKnowledgeSessionBeanFactory;
import org.springframework.beans.PropertyValue;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class KnowledgeSessionDefinitionParser extends AbstractBeanDefinitionParser {
    
    private static final String KBASE_ATTRIBUTE          = "kbase";
    private static final String EMF_ATTRIBUTE            = "entity-manager-factory";
    private static final String TX_MANAGER_ATTRIBUTE     = "transaction-manager";
    private static final String FORCLASS_ATTRIBUTE       = "for-class";
    private static final String IMPLEMENTATION_ATTRIBUTE = "implementation";    

    private static final String NAME_ATTRIBUTE           = "name";
    private static final String GRID_NODE_ATTRIBUTE = "node";
    private static final String TYPE_ATTRIBUTE           = "type";

    private static final String KEEP_REFERENCE           = "keep-reference";
    private static final String CLOCK_TYPE               = "clock-type";
    
    private static final String WORK_ITEMS               = "work-item-handlers";
    
    private static final String WORK_ITEM                = "work-item-handler";    
    
    @SuppressWarnings("unchecked")
	protected AbstractBeanDefinition parseInternal(Element element,
                                                   ParserContext parserContext) {
    	
        String id = element.getAttribute( "id" );
        emptyAttributeCheck( element.getLocalName(),
                             "id", 
                             id);
        
        
        String kbase = element.getAttribute( KBASE_ATTRIBUTE );
        emptyAttributeCheck( element.getLocalName(),
                             KBASE_ATTRIBUTE,
                             kbase );

        String sessionType = element.getAttribute( TYPE_ATTRIBUTE );
        BeanDefinitionBuilder factory;

        if ( "stateful".equals( sessionType ) ) {
            factory = BeanDefinitionBuilder.rootBeanDefinition( StatefulKnowledgeSessionBeanFactory.class );
        } else if ( "stateless".equals( sessionType ) ) {
            factory = BeanDefinitionBuilder.rootBeanDefinition( StatelessKnowledgeSessionBeanFactory.class );
        } else {
            throw new IllegalArgumentException( "Invalid value for " + TYPE_ATTRIBUTE + " attribute: " + sessionType );
        }

        factory.addPropertyReference( "kbase",
                                      kbase );

        String node = element.getAttribute( GRID_NODE_ATTRIBUTE );
        if ( node != null && node.length() > 0 ) {
            factory.addPropertyReference( "node",
                                          node );
        }

        String name = element.getAttribute( NAME_ATTRIBUTE );
        if ( StringUtils.hasText(name) ) {
            factory.addPropertyValue( "name",
                                      name );
        } else {
            factory.addPropertyValue( "name",
            						  id );        	
        }
        
        Element ksessionConf = DomUtils.getChildElementByTagName(element, "configuration");
        if ( ksessionConf != null ) {
            Element persistenceElm = DomUtils.getChildElementByTagName(ksessionConf, "jpa-persistence");
            if ( persistenceElm != null) {
                BeanDefinitionBuilder beanBuilder = BeanDefinitionBuilder.genericBeanDefinition( JpaConfiguration.class );
                
                String loadId = persistenceElm.getAttribute( "load" );
                if ( StringUtils.hasText( loadId ) ) {
                    beanBuilder.addPropertyValue( "id", Long.parseLong( loadId ) );
                }
                
                Element tnxMng = DomUtils.getChildElementByTagName(persistenceElm, TX_MANAGER_ATTRIBUTE);
                String ref = tnxMng.getAttribute( "ref" );
                
                beanBuilder.addPropertyReference( "platformTransactionManager", ref );
                
                Element emf = DomUtils.getChildElementByTagName(persistenceElm, EMF_ATTRIBUTE);
                ref = emf.getAttribute( "ref" );
                beanBuilder.addPropertyReference( "entityManagerFactory", ref ); 
                
                Element variablePersisters = DomUtils.getChildElementByTagName(persistenceElm, "variable-persisters");
                if ( variablePersisters != null && variablePersisters.hasChildNodes() ) {
                    List<Element> childPersisterElems = DomUtils.getChildElementsByTagName(variablePersisters, "persister");
                    ManagedMap persistors = new ManagedMap( childPersisterElems.size() );
                    for ( Element persisterElem : childPersisterElems) {
                        String forClass = persisterElem.getAttribute( FORCLASS_ATTRIBUTE );
                        String implementation = persisterElem.getAttribute( IMPLEMENTATION_ATTRIBUTE );
                        if ( !StringUtils.hasText( forClass ) ) {                        
                            throw new RuntimeException( "persister element must have valid for-class attribute" );
                        }
                        if ( !StringUtils.hasText( implementation ) ) {                        
                            throw new RuntimeException( "persister element must have valid implementation attribute" );
                        }                    
                        persistors.put( forClass,
                                        implementation );                    
                    }
                    beanBuilder.addPropertyValue( "variablePersisters", persistors );
                }
                
                factory.addPropertyValue( "jpaConfiguration", beanBuilder.getBeanDefinition() );                                           
            }
            BeanDefinitionBuilder rbaseConfBuilder = BeanDefinitionBuilder.rootBeanDefinition( SessionConfiguration.class );
            Element e = DomUtils.getChildElementByTagName(ksessionConf, KEEP_REFERENCE);
            if ( e != null && StringUtils.hasText( e.getAttribute( "enabled" ) )) {
                rbaseConfBuilder.addPropertyValue( "keepReference", Boolean.parseBoolean( e.getAttribute( "enabled" ) ) );
            }   
            
            e = DomUtils.getChildElementByTagName(ksessionConf, CLOCK_TYPE);
            if ( e != null && StringUtils.hasText( e.getAttribute( "type" ) )) {
                rbaseConfBuilder.addPropertyValue( "clockType", ClockType.resolveClockType( e.getAttribute( "type" ) ) );
            }                
            factory.addPropertyValue( "conf", rbaseConfBuilder.getBeanDefinition() );   
            
            e = DomUtils.getChildElementByTagName(ksessionConf, WORK_ITEMS);
            if ( e != null ) {
                List<Element> children = DomUtils.getChildElementsByTagName( e, WORK_ITEM );
                if ( children != null && !children.isEmpty() ) {
                    ManagedMap workDefs = new ManagedMap();
                    for ( Element child : children ) {
                        workDefs.put(  child.getAttribute( "name" ),
                                       new RuntimeBeanReference( child.getAttribute( "ref" ) ) );
                    }
                    factory.addPropertyValue( "workItems", workDefs );                    
                }
            }            
        }
        
        Element batch = DomUtils.getChildElementByTagName(element, "batch");
        if ( batch == null ) {
            // just temporary legacy suppport
            batch = DomUtils.getChildElementByTagName(element, "script");
        }
        if ( batch != null) {
            // we know there can only ever be one
            ManagedList children = new ManagedList();

            for (int i = 0, length = batch.getChildNodes().getLength(); i < length; i++) {
                Node n = batch.getChildNodes().item( i );
                if ( n instanceof Element ) {
                    Element e = ( Element ) n;
                    
                    BeanDefinitionBuilder beanBuilder = null;
                    if ( "insert-object".equals( e.getLocalName() ) ) {
                        String ref = e.getAttribute( "ref" );
                        Element nestedElm = getFirstElement( e.getChildNodes() );                   
                        beanBuilder = BeanDefinitionBuilder.genericBeanDefinition( InsertObjectCommand.class );                        
                        if ( StringUtils.hasText( ref ) ) {
                            beanBuilder.addConstructorArgReference( ref );
                        } else if ( nestedElm != null  ) {
                            beanBuilder.addConstructorArgValue( parserContext.getDelegate().parseBeanDefinitionElement( nestedElm ) );                         
                        } else {
                            throw new IllegalArgumentException( "insert-object must either specify a 'ref' attribute or have a nested bean" );
                        }
                    } else if ( "set-global".equals( e.getLocalName() ) ) {
                        String ref = e.getAttribute( "ref" );
                        Element nestedElm = getFirstElement( e.getChildNodes() );                   
                        beanBuilder = BeanDefinitionBuilder.genericBeanDefinition( SetGlobalCommand.class );
                        beanBuilder.addConstructorArgValue( e.getAttribute( "identifier" ) );
                        if ( StringUtils.hasText( ref ) ) {
                            beanBuilder.addConstructorArgReference( ref );
                        } else if ( nestedElm != null  ) {
                            beanBuilder.addConstructorArgValue( parserContext.getDelegate().parseBeanDefinitionElement( nestedElm ) );                         
                        } else {
                            throw new IllegalArgumentException( "set-global must either specify a 'ref' attribute or have a nested bean" );
                        }
                    }  else if ( "fire-until-halt".equals( e.getLocalName() ) ) {
                        beanBuilder = BeanDefinitionBuilder.genericBeanDefinition( FireUntilHaltCommand.class );
                    }  else if ( "fire-all-rules".equals( e.getLocalName() ) ) {
                        beanBuilder = BeanDefinitionBuilder.genericBeanDefinition( FireAllRulesCommand.class );
                        String max = e.getAttribute( "max" );
                        if ( StringUtils.hasText( max ) ) {
                            beanBuilder.addPropertyValue( "max", max );
                        }
                    }  else if ( "start-process".equals( e.getLocalName() ) ) {
                        
                        beanBuilder = BeanDefinitionBuilder.genericBeanDefinition( StartProcessCommand.class );
                        String processId = e.getAttribute( "process-id" );
                        if ( !StringUtils.hasText( processId ) ) {
                            throw new IllegalArgumentException( "start-process must specify a process-id" );
                        }
                        beanBuilder.addConstructorArgValue( processId );
                        
                        List<Element> params = DomUtils.getChildElementsByTagName( e, "parameter" );
                        if ( !params.isEmpty() ) {
                            ManagedMap map = new ManagedMap();
                            for ( Element param : params ) {
                                String identifier = param.getAttribute( "identifier" );
                                if ( !StringUtils.hasText( identifier ) ) {
                                    throw new IllegalArgumentException( "start-process paramaters must specify an identifier" );
                                }
                                
                                String ref = param.getAttribute( "ref" );
                                Element nestedElm = getFirstElement( param.getChildNodes() );  
                                if ( StringUtils.hasText( ref ) ) {
                                    map.put( identifier,  new RuntimeBeanReference( ref) );
                                } else if ( nestedElm != null  ) {
                                    map.put( identifier,  parserContext.getDelegate().parseBeanDefinitionElement( nestedElm ) );
                                } else {
                                    throw new IllegalArgumentException( "start-process paramaters must either specify a 'ref' attribute or have a nested bean" );
                                }                                    
                            }   
                            beanBuilder.addPropertyValue( "parameters", map );
                        }
                    } else if ( "signal-event".equals( e.getLocalName() ) ) {
                        beanBuilder = BeanDefinitionBuilder.genericBeanDefinition( SignalEventCommand.class );
                        String processInstanceId = e.getAttribute( "process-instance-id" );
                        if ( StringUtils.hasText( processInstanceId ) ) {
                            beanBuilder.addConstructorArgValue( processInstanceId );    
                        }
                        
                        beanBuilder.addConstructorArgValue( e.getAttribute( "event-type" ) );
                        
                        String ref = e.getAttribute( "ref" );
                        Element nestedElm = getFirstElement( e.getChildNodes() );                   
                        if ( StringUtils.hasText( ref ) ) {
                            beanBuilder.addConstructorArgReference( ref );
                        } else if ( nestedElm != null  ) {
                            beanBuilder.addConstructorArgValue( parserContext.getDelegate().parseBeanDefinitionElement( nestedElm ) );                         
                        } else {
                            throw new IllegalArgumentException( "signal-event must either specify a 'ref' attribute or have a nested bean" );
                        }             
                    }
                    if (beanBuilder == null) {
                    	throw new IllegalStateException("Unknow element: " + e.getLocalName());
                    }
                    children.add( beanBuilder.getBeanDefinition() );
                }
            }
            factory.addPropertyValue( "batch", children );
        }        
        
        // find any kagent's for the current kbase and assign
        for ( String beanName : parserContext.getRegistry().getBeanDefinitionNames() ) {
        	BeanDefinition def = parserContext.getRegistry().getBeanDefinition(beanName);
        	if ( def.getBeanClassName().equals( KnowledgeAgentBeanFactory.class.getName() ) ) {        		        		
        		 PropertyValue pvalue = def.getPropertyValues().getPropertyValue( "kbase" );
        		 RuntimeBeanReference tbf = ( RuntimeBeanReference ) pvalue.getValue();        		 
        		if ( kbase.equals( tbf.getBeanName() ) ) {
        			factory.addPropertyValue( "knowledgeAgent", new RuntimeBeanReference( beanName ) );
        		}
        	}       	
        }        

        return factory.getBeanDefinition();
    }
    
    private Element getFirstElement(NodeList list) {                    
        for (int j = 0, lengthj = list.getLength(); j < lengthj; j++) {
            if ( list.item( j ) instanceof Element ) {
                return ( Element ) list.item( j );
            }
        }   
        return null;
    }

}
