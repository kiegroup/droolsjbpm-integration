/*
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

import java.util.List;

import org.drools.RuleBaseConfiguration;
import org.drools.RuleBaseConfiguration.AssertBehaviour;
import org.kie.conf.EventProcessingOption;
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

    private static final String ASSERT_BEHAVIOR                  = "assert-behavior";

    private static final String ACCUMULATE_FUNCTIONS             = "accumulate-functions";

    private static final String ACCUMULATE_FUNCTION              = "accumulate-function";

    private static final String EVALUATORS                       = "evaluators";

    private static final String EVALUATOR                        = "evaluator";

    private static final String CONSEQUENCE_EXCEPTION_HANDLER    = "consequenceExceptionHandler";

    @SuppressWarnings("unchecked")
    @Override
    protected AbstractBeanDefinition parseInternal(Element element,
                                                   ParserContext parserContext) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition( KnowledgeBaseBeanFactory.class );

        Element kbaseConf = DomUtils.getChildElementByTagName( element,
                                                               "configuration" );
        if ( kbaseConf != null ) {
            BeanDefinitionBuilder rbaseConfBuilder = BeanDefinitionBuilder.rootBeanDefinition( RuleBaseConfiguration.class );
            Element e = DomUtils.getChildElementByTagName( kbaseConf,
                                                           ADVANCED_PROCESS_RULE_INTEGRATED );
            if ( e != null && !StringUtils.isEmpty( e.getAttribute( "enabled" ) ) ) {
                rbaseConfBuilder.addPropertyValue( "advancedProcessRuleIntegration",
                                                   Boolean.parseBoolean( e.getAttribute( "enabled" ) ) );
            }

            e = DomUtils.getChildElementByTagName( kbaseConf,
                                                   MULTITHREADS );
            if ( e != null && !StringUtils.isEmpty( e.getAttribute( "enabled" ) ) ) {
                rbaseConfBuilder.addPropertyValue( "multithreadEvaluation",
                                                   Boolean.parseBoolean( e.getAttribute( "enabled" ) ) );
                if ( !StringUtils.isEmpty( e.getAttribute( MAX_THREADS ) ) ) {
                    rbaseConfBuilder.addPropertyValue( "maxThreads",
                                                       Integer.parseInt( e.getAttribute( MAX_THREADS ) ) );
                }
            }

            e = DomUtils.getChildElementByTagName( kbaseConf,
                                                   MBEABS );
            if ( e != null && !StringUtils.isEmpty( e.getAttribute( "enabled" ) ) ) {
                rbaseConfBuilder.addPropertyValue( "MBeansEnabled",
                                                   Boolean.parseBoolean( e.getAttribute( "enabled" ) ) );
            }

            e = DomUtils.getChildElementByTagName( kbaseConf,
                                                   EVENT_PROCESSING_MODE );
            if ( e != null && !StringUtils.isEmpty( e.getAttribute( "mode" ) ) ) {
                rbaseConfBuilder.addPropertyValue( "eventProcessingMode",
                                                   EventProcessingOption.valueOf( e.getAttribute( "mode" ) ) );
            }

            e = DomUtils.getChildElementByTagName( kbaseConf,
                                                   ASSERT_BEHAVIOR );
            if ( e != null && !StringUtils.isEmpty( e.getAttribute( "mode" ) ) ) {
                rbaseConfBuilder.addPropertyValue( "assertBehaviour",
                                                   AssertBehaviour.determineAssertBehaviour( e.getAttribute( "mode" ) ) );
            }

            e = DomUtils.getChildElementByTagName( kbaseConf,
                                                   ACCUMULATE_FUNCTIONS );
            if ( e != null ) {
                List<Element> children = DomUtils.getChildElementsByTagName( e,
                                                                             ACCUMULATE_FUNCTION );
                if ( children != null && !children.isEmpty() ) {
                    ManagedMap functions = new ManagedMap();
                    for ( Element child : children ) {
                        functions.put( child.getAttribute( "name" ),
                                        new RuntimeBeanReference( child.getAttribute( "ref" ) ) );
                    }
                    factory.addPropertyValue( "accumulateFunctions",
                                              functions );
                }
            }

            e = DomUtils.getChildElementByTagName( kbaseConf,
                                                   EVALUATORS );
            if ( e != null ) {
                List<Element> children = DomUtils.getChildElementsByTagName( e,
                                                                             EVALUATOR );
                if ( children != null && !children.isEmpty() ) {
                    ManagedMap evaluators = new ManagedMap();
                    for ( Element child : children ) {
                        evaluators.put( child.getAttribute( "name" ),
                                        new RuntimeBeanReference( child.getAttribute( "ref" ) ) );
                    }
                    factory.addPropertyValue( "evaluators",
                                              evaluators );
                }
            }

            e = DomUtils.getChildElementByTagName( kbaseConf,
                                                   CONSEQUENCE_EXCEPTION_HANDLER );
            if ( e != null && !StringUtils.isEmpty( e.getAttribute( "handler" ) ) ) {
                rbaseConfBuilder.addPropertyValue( "consequenceExceptionHandler",
                                                   e.getAttribute( "handler" ) );
            }

            factory.addPropertyValue( "conf",
                                      rbaseConfBuilder.getBeanDefinition() );
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

    @SuppressWarnings("unchecked")
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
