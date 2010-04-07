package org.drools.container.spring.namespace;

import java.util.List;

import org.drools.container.spring.beans.KnowledgeBaseBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

public class KnowledgeBaseDefinitionParser extends AbstractBeanDefinitionParser {
	
	private static final String EXECUTION_NODE_ATTRIBUTE = "node";

	@SuppressWarnings("unchecked")
	@Override
	protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
		BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(KnowledgeBaseBeanFactory.class);

		String nodeRef = element.getAttribute(EXECUTION_NODE_ATTRIBUTE);
		factory.addPropertyReference(EXECUTION_NODE_ATTRIBUTE, nodeRef);
		List<Element> childElements = DomUtils.getChildElementsByTagName(element, "resource");
		
		ManagedList resources = null;
		if (childElements != null && !childElements.isEmpty()) {
			resources = new ManagedList();
			for (Element childResource : childElements) {
				BeanDefinition resourceDefinition = parserContext.getDelegate().parseCustomElement(childResource, factory.getBeanDefinition());
				resources.add(resourceDefinition);
			}
		}
		
		childElements = DomUtils.getChildElementsByTagName(element, "resource-ref");
		
		if (childElements != null && !childElements.isEmpty()) {
			if (resources==null) {
				resources = new ManagedList(childElements.size());
			}
			for (Element childResource : childElements) {
				BeanDefinition resourceDefinition = parserContext.getDelegate().parseCustomElement(childResource, factory.getBeanDefinition());
				resources.add(resourceDefinition);
			}
		}

		if (resources!=null) {
			factory.addPropertyValue("resources", resources);
		}

		childElements = DomUtils.getChildElementsByTagName(element, "model");

		if (childElements != null && !childElements.isEmpty()) {
			ManagedList models = new ManagedList(childElements.size());
			for (Element childResource : childElements) {
				BeanDefinition resourceDefinition = parserContext.getDelegate().parseCustomElement(childResource, factory.getBeanDefinition());
				models.add(resourceDefinition);
			}
			factory.addPropertyValue("models", models);
		}

		return factory.getBeanDefinition();
	}
	
}
