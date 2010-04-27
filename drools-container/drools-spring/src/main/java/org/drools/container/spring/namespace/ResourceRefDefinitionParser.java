package org.drools.container.spring.namespace;

import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import static org.drools.container.spring.namespace.DefinitionParserHelper.*;

public class ResourceRefDefinitionParser extends AbstractBeanDefinitionParser {

	private static final String ID_ATTRIBUTE = "id";

	@Override
	protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
		String id = element.getAttribute(ID_ATTRIBUTE);
		emptyAttributeCheck(element.getLocalName(), ID_ATTRIBUTE, id);
		return (AbstractBeanDefinition) parserContext.getRegistry().getBeanDefinition(id);
	}
}
