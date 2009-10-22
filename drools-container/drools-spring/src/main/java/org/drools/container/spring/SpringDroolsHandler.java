package org.drools.container.spring;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class SpringDroolsHandler extends NamespaceHandlerSupport {

    public void init() {
        registerBeanDefinitionParser("kbase", new KnowledgeBaseDefinitionParser());
        
        registerBeanDefinitionParser("ksession", new KnowledgeSessionDefinitionParser());

    }

}
