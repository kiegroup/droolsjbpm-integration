package org.drools.container.spring.namespace;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;

/**
 * 
 * @author Lucas Amador
 *
 */
public class KnowledgeServiceConfigurationRefDefinitionParser extends AbstractBeanDefinitionParser {

	private static final String ID_ATTRIBUTE = "id";

	@Override
	protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
		String id = element.getAttribute(ID_ATTRIBUTE);
		emptyAttributeCheck(element.getLocalName(), ID_ATTRIBUTE, id);
		return (AbstractBeanDefinition) parserContext.getRegistry().getBeanDefinition(id);
	}

	public void emptyAttributeCheck(final String element, final String attributeName, final String attribute) {
		if (attribute == null || attribute.trim().length() == 0) {
			throw new IllegalArgumentException("<" + element + "> requires a '" + attributeName + "' attribute");
		}
	}

}
