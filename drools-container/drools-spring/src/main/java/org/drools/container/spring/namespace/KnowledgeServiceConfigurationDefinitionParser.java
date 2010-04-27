package org.drools.container.spring.namespace;

import java.util.ArrayList;
import java.util.List;

import org.drools.container.spring.beans.KnowledgeServiceConfigurationBeanFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import static org.drools.container.spring.namespace.DefinitionParserHelper.*;

/**
 * 
 * @author Lucas Amador
 *
 */
public class KnowledgeServiceConfigurationDefinitionParser extends AbstractBeanDefinitionParser {

	private static final String ID_ATTRIBUTE = "id";
	private static final String SESSION_ATTRIBUTE = "session";
	private static final String MARSHALLER_ATTRIBUTE = "marshaller";
	private static final String CLASS_ELEMENT = "class";
	private static final String STARTUP_COMMAND_ELEMENT = "startup-command";

	@SuppressWarnings("unchecked")
	@Override
	protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {

		String id = element.getAttribute(ID_ATTRIBUTE);

		BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(KnowledgeServiceConfigurationBeanFactory.class);
		if (id!=null && id.length() > 0) {
			factory.addPropertyValue("id", id);
		}

		String ksession = element.getAttribute(SESSION_ATTRIBUTE);
		emptyAttributeCheck(element.getLocalName(), SESSION_ATTRIBUTE, ksession);
		factory.addPropertyReference("session", ksession);
		factory.addPropertyValue("sessionId", ksession);

		String marshaller = element.getAttribute(MARSHALLER_ATTRIBUTE);
		emptyAttributeCheck(element.getLocalName(), MARSHALLER_ATTRIBUTE, marshaller);
		factory.addPropertyValue("marshaller", marshaller);

		List<Element> childElements = DomUtils.getChildElementsByTagName(element, CLASS_ELEMENT);

		if (childElements != null && !childElements.isEmpty()) {
			List<String> classes = new ArrayList<String>();
			for (Element childResource : childElements) {
				NodeList childNodes = childResource.getChildNodes();
				for (int i = 0; i < childNodes.getLength(); i++) {
					Node item = childNodes.item(i);
					classes.add(item.getNodeValue());
				}
			}
			factory.addPropertyValue("classes", classes);
		}
		
		childElements = DomUtils.getChildElementsByTagName(element, STARTUP_COMMAND_ELEMENT);

		if (childElements != null && !childElements.isEmpty()) {
			List<String> commands = new ArrayList<String>();
			for (Element childResource : childElements) {
				NodeList childNodes = childResource.getChildNodes();
				for (int i = 0; i < childNodes.getLength(); i++) {
					Node item = childNodes.item(i);
					if (item.getNodeValue().trim().length() > 0) {
						commands.add(item.getNodeValue().trim());
					}
				}
			}
			factory.addPropertyValue("commands", commands);
		}
		
		return factory.getBeanDefinition();
	}
}
