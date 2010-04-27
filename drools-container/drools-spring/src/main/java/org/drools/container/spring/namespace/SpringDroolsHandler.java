package org.drools.container.spring.namespace;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class SpringDroolsHandler extends NamespaceHandlerSupport {

    public void init() {
    	
    	registerBeanDefinitionParser("resource", new ResourceDefinitionParser());
    	
    	registerBeanDefinitionParser("resource-ref", new ResourceRefDefinitionParser());
    	
    	registerBeanDefinitionParser("model", new ResourceDefinitionParser());
    	
        registerBeanDefinitionParser("kbase", new KnowledgeBaseDefinitionParser());
        
        registerBeanDefinitionParser("ksession", new KnowledgeSessionDefinitionParser());
        
        registerBeanDefinitionParser("connection", new ConnectionDefinitionParser());
        
        registerBeanDefinitionParser("execution-node", new ExecutionNodeDefinitionParser());
        
        registerBeanDefinitionParser("jpaSessionServiceFactory", new JpaSessionServiceFactoryDefinitionParser());
        
    }

}