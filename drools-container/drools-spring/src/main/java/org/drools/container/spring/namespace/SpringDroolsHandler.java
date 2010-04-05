package org.drools.container.spring.namespace;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class SpringDroolsHandler extends NamespaceHandlerSupport {

    public void init() {
    	//registerBeanDefinitionParser("serviceManager", new ServiceManagerDefinitionParser());
    	
    	registerBeanDefinitionParser("resource", new ResourceDefinitionParser());
    	
    	registerBeanDefinitionParser("resource-ref", new ResourceRefDefinitionParser());
    	
    	registerBeanDefinitionParser("model", new ResourceDefinitionParser());
    	
        registerBeanDefinitionParser("kbase", new KnowledgeBaseDefinitionParser());
        
        registerBeanDefinitionParser("ksession", new KnowledgeSessionDefinitionParser());
        
        registerBeanDefinitionParser("service", new KnowledgeServiceDefinitionParser());
        
        registerBeanDefinitionParser("service-conf", new KnowledgeServiceConfigurationDefinitionParser());
        
        registerBeanDefinitionParser("service-conf-ref", new KnowledgeServiceConfigurationRefDefinitionParser());

    }

}