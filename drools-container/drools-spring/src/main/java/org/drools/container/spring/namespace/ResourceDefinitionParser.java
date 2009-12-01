package org.drools.container.spring.namespace;

import java.util.List;

import org.drools.builder.DecisionTableInputType;
import org.drools.builder.ResourceType;
import org.drools.builder.impl.DecisionTableConfigurationImpl;
import org.drools.container.spring.beans.DroolsResourceAdapter;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

public class ResourceDefinitionParser extends AbstractBeanDefinitionParser {
	
	private static final String WORKSHEET_NAME_ATTRIBUTE = "worksheet-name";
	private static final String INPUT_TYPE_ATTRIBUTE = "input-type";
	private static final String TYPE_ATTRIBUTE = "type";
	private static final String SOURCE_ATTRIBUTE = "source";

	@SuppressWarnings("unchecked")
	@Override
	protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
		BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(DroolsResourceAdapter.class);

		String source = element.getAttribute(SOURCE_ATTRIBUTE);
		emptyAttributeCheck(element.getLocalName(), SOURCE_ATTRIBUTE, source);
		factory.addPropertyValue("resource", source);
		
		String type = element.getAttribute(TYPE_ATTRIBUTE);
		//emptyAttributeCheck(element.getLocalName(), type, type);
		factory.addPropertyValue("resourceType", 
				type == null || type.length() == 0 ? 
						ResourceType.DRL.getName() : 
						type);
		
		List<Element> childElements = DomUtils.getChildElementsByTagName(element, "decisiontable-conf");
		if (childElements != null && !childElements.isEmpty()) {
			if (!ResourceType.DTABLE.getName().endsWith(type)) {
				throw new IllegalArgumentException("Only Desicion Tables can have configuration");
			}
			Element conf = childElements.get(0);
			DecisionTableConfigurationImpl dtableConf = new DecisionTableConfigurationImpl();
			
			String inputType = conf.getAttribute(INPUT_TYPE_ATTRIBUTE);
			emptyAttributeCheck(conf.getLocalName(), INPUT_TYPE_ATTRIBUTE, inputType);
			dtableConf.setInputType(DecisionTableInputType.valueOf(inputType));
			
			String worksheetName = conf.getAttribute(WORKSHEET_NAME_ATTRIBUTE);
			emptyAttributeCheck(conf.getLocalName(), WORKSHEET_NAME_ATTRIBUTE, worksheetName);
			dtableConf.setWorksheetName(worksheetName);
			
			factory.addPropertyValue("resourceConfiguration", dtableConf);
		}
		return factory.getBeanDefinition();
	}
	
	public void emptyAttributeCheck(final String element,
			final String attributeName,
			final String attribute) {
		if (attribute == null || attribute.trim().equals("")) {
			throw new IllegalArgumentException("<" + element + "> requires a '" + attributeName + "' attribute");
		}
	}
}
