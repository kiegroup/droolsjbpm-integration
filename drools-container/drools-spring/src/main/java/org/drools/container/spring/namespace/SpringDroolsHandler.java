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

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class SpringDroolsHandler extends NamespaceHandlerSupport {

    public void init() {

        registerBeanDefinitionParser( "resource",
                                      new ResourceDefinitionParser() );

        registerBeanDefinitionParser( "resource-change-scanner",
                					  new ResourceChangeScannerDefinitionParser() );        

        registerBeanDefinitionParser( "model",
                                      new ResourceDefinitionParser() );

        registerBeanDefinitionParser( "kbase",
                                      new KnowledgeBaseDefinitionParser() );
        
        registerBeanDefinitionParser( "kagent",
                                      new KnowledgeAgentDefinitionParser() );        
        
        registerBeanDefinitionParser( "kstore",
                                      new KnowledgeStoreDefinitionParser() );        

        registerBeanDefinitionParser( "ksession",
                                      new KnowledgeSessionDefinitionParser() );

        registerBeanDefinitionParser( "connection",
                                      new ConnectionDefinitionParser() );

        registerBeanDefinitionParser( "execution-node",
                                      new ExecutionNodeDefinitionParser() );
    }

}