package org.drools.container.spring.namespace;

import org.drools.container.spring.beans.ConnectionBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * 
 * @author Lucas Amador
 *
 */
public class ConnectionDefinitionParser extends AbstractBeanDefinitionParser {

	private static final String TYPE_ATTRIBUTE = "type";

	protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {

		BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(ConnectionBeanFactory.class);

		String type = element.getAttribute(TYPE_ATTRIBUTE);
		emptyAttributeCheck(element.getLocalName(), TYPE_ATTRIBUTE, type);

		factory.addPropertyValue("type", type);

		return factory.getBeanDefinition();
	}

	public void emptyAttributeCheck(final String element, final String attributeName, final String attribute) {
		if (attribute == null || attribute.trim().length() == 0) {
			throw new IllegalArgumentException("<" + element + "> requires a '" + attributeName + "' attribute");
		}
	}

}
