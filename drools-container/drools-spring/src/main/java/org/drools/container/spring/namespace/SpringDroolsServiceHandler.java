package org.drools.container.spring.namespace;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

public class SpringDroolsServiceHandler extends NamespaceHandlerSupport {

	public void init() {

		registerBeanDefinitionParser("definition", new KnowledgeServiceDefinitionParser());

		registerBeanDefinitionParser("configuration", new KnowledgeServiceConfigurationDefinitionParser());

		registerBeanDefinitionParser("configuration-ref", new KnowledgeServiceConfigurationRefDefinitionParser());

	}

}