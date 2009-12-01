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
	
	private static final String RESOUCES_ATTRIBUTE = "resources";
	private static final String SERVICE_MANAGER_ATTRIBUTE = "serviceManager";

	@SuppressWarnings("unchecked")
	@Override
	protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
		BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(KnowledgeBaseBeanFactory.class);

		String smRef = element.getAttribute(SERVICE_MANAGER_ATTRIBUTE);
		factory.addPropertyReference(SERVICE_MANAGER_ATTRIBUTE, smRef);
		List<Element> childElements = DomUtils.getChildElementsByTagName(element, "resource");
		
		if (childElements != null && !childElements.isEmpty()) {
			ManagedList resources = new ManagedList(childElements.size());
			for (Element childResource : childElements) {
				BeanDefinition resourceDefinition = parserContext.getDelegate().parseCustomElement(childResource, factory.getBeanDefinition());
				resources.add(resourceDefinition);
			}
			factory.addPropertyValue("resources", resources);
		}
		
		return factory.getBeanDefinition();
	}
	
}
