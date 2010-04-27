package org.drools.container.spring.namespace;

import static org.drools.container.spring.namespace.DefinitionParserHelper.emptyAttributeCheck;

import java.util.List;

import org.drools.container.spring.beans.JPASingleSessionCommandServiceFactory;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

public class JpaSessionServiceFactoryDefinitionParser extends AbstractBeanDefinitionParser {

	private static final String KBASE_ATTRIBUTE = "kbase";
	private static final String EMF_ATTRIBUTE = "entityManagerFactory";
	private static final String TX_MANAGER_ATTRIBUTE = "transactionManager";
	private static final String FORCLASS_ATTRIBUTE = "forClass";
	private static final String IMPLEMENTATION_ATTRIBUTE = "implementation";

	@SuppressWarnings("unchecked")
	@Override
	protected AbstractBeanDefinition parseInternal(Element element, ParserContext parserContext) {
		BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition(JPASingleSessionCommandServiceFactory.class);
		
		String kbase = element.getAttribute(KBASE_ATTRIBUTE);
		emptyAttributeCheck(element.getLocalName(), KBASE_ATTRIBUTE, kbase);
		factory.addPropertyReference("knowledgeBase", kbase);
		
		String entityManagerFactory = element.getAttribute(EMF_ATTRIBUTE);
		emptyAttributeCheck(element.getLocalName(), EMF_ATTRIBUTE, entityManagerFactory);
		factory.addPropertyReference("entityManagerFactory", entityManagerFactory);
		
		String txMngr = element.getAttribute(TX_MANAGER_ATTRIBUTE);
		emptyAttributeCheck(element.getLocalName(), TX_MANAGER_ATTRIBUTE, txMngr);
		factory.addPropertyReference("transactionManager", txMngr);
		
		List<Element> childElems = DomUtils.getChildElementsByTagName(element, "variablePersisters");
		if (childElems != null && childElems.size() > 0) {
			List<Element> elems = DomUtils.getChildElementsByTagName(childElems.get(0), "persister");
			ManagedMap persistors = new ManagedMap(childElems.size());
			for (Element persister : elems) {
				persistors.put(persister.getAttribute(FORCLASS_ATTRIBUTE), persister.getAttribute(IMPLEMENTATION_ATTRIBUTE));
			}
			factory.addPropertyValue("variablePersisters", persistors);
		}
		
		return factory.getBeanDefinition();
	}
}
