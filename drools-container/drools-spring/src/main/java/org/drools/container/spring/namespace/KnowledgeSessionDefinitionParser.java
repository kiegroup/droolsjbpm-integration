package org.drools.container.spring.namespace;

import org.drools.container.spring.beans.StatefulKnowledgeSessionBeanFactory;
import org.drools.container.spring.beans.StatelessKnowledgeSessionBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

public class KnowledgeSessionDefinitionParser extends AbstractBeanDefinitionParser {

	private static final String NAME_ATTRIBUTE = "name";
	private static final String EXECUTION_NODE_ATTRIBUTE = "node";
	private static final String KBASE_ATTRIBUTE = "kbase";
	private static final String TYPE_ATTRIBUTE = "type";

	protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
		String kbase = element.getAttribute(KBASE_ATTRIBUTE);
		emptyAttributeCheck(element.getLocalName(), KBASE_ATTRIBUTE, kbase);

		String sessionType = element.getAttribute(TYPE_ATTRIBUTE);
		BeanDefinitionBuilder factory;

		if ("stateful".equals(sessionType)) {
			factory = BeanDefinitionBuilder.rootBeanDefinition(StatefulKnowledgeSessionBeanFactory.class);
		} else if ("stateless".equals(sessionType)) {
			factory = BeanDefinitionBuilder.rootBeanDefinition(StatelessKnowledgeSessionBeanFactory.class);
		} else {
			throw new IllegalArgumentException("Invalid value for " + TYPE_ATTRIBUTE + " attribute: " + sessionType);
		}

		factory.addPropertyReference("kbase", kbase);

		String node = element.getAttribute(EXECUTION_NODE_ATTRIBUTE);
		if (node != null && node.length() > 0) {
			factory.addPropertyReference("node", node);
		}

		String name = element.getAttribute(NAME_ATTRIBUTE);
		if (name != null && name.length() > 0) {
			factory.addPropertyValue("name", name);
		}
		
		return factory.getBeanDefinition();
	}

	public void emptyAttributeCheck(final String element,
			final String attributeName,
			final String attribute) {
		if (attribute == null || attribute.trim().length() == 0) {
			throw new IllegalArgumentException("<" + element + "> requires a '" + attributeName + "' attribute");
		}
	}

}
