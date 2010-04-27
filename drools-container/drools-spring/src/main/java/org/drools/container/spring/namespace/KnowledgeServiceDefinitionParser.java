package org.drools.container.spring.namespace;

import java.util.List;

import org.drools.container.spring.beans.KnowledgeServiceBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedList;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import static org.drools.container.spring.namespace.DefinitionParserHelper.*;

/**
 * 
 * @author Lucas Amador
 *
 */
public class KnowledgeServiceDefinitionParser extends AbstractBeanDefinitionParser {

	private static final String CAMEL_CONTEXT_ATTRIBUTE = "camelContext";
	private static final String EXECUTION_NODE_ID_ATTRIBUTE = "node";
	private static final String CONFIGURATION_ELEMENT = "configuration";
	private static final String CONFIGURATION_REF_ELEMENT = "configuration-ref";

	@SuppressWarnings("unchecked")
	@Override
	protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {

		BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(KnowledgeServiceBeanFactory.class);

		String nodeId = element.getAttribute(EXECUTION_NODE_ID_ATTRIBUTE);
		emptyAttributeCheck(element.getLocalName(), EXECUTION_NODE_ID_ATTRIBUTE, nodeId);

		factory.addPropertyValue("nodeId", nodeId);
		
		String camelContextId = element.getAttribute(CAMEL_CONTEXT_ATTRIBUTE);
		emptyAttributeCheck(element.getLocalName(), CAMEL_CONTEXT_ATTRIBUTE, camelContextId);
		
		factory.addPropertyReference(CAMEL_CONTEXT_ATTRIBUTE, camelContextId);

		List<Element> childElements = DomUtils.getChildElementsByTagName(element, CONFIGURATION_ELEMENT);
		ManagedList configurations = null;

		if (childElements != null && !childElements.isEmpty()) {
			configurations = new ManagedList(childElements.size());
			for (Element childResource : childElements) {
				BeanDefinition resourceDefinition = parserContext.getDelegate().parseCustomElement(childResource, factory.getBeanDefinition());
				configurations.add(resourceDefinition);
			}
		}

		childElements = DomUtils.getChildElementsByTagName(element, CONFIGURATION_REF_ELEMENT);

		if (childElements != null && !childElements.isEmpty()) {
			if (configurations==null) {
				configurations = new ManagedList(childElements.size());
			}
			for (Element childResource : childElements) {
				BeanDefinition resourceDefinition = parserContext.getDelegate().parseCustomElement(childResource, factory.getBeanDefinition());
				configurations.add(resourceDefinition);
			}
		}

		if (configurations!=null) {
			factory.addPropertyValue("configurations", configurations);
		}

		return factory.getBeanDefinition();
	}
}
