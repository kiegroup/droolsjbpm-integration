package org.drools.container.spring.namespace;

public class DefinitionParserHelper {
	
	public static void emptyAttributeCheck(final String element,
			final String attributeName,
			final String attribute) {
		if (attribute == null || attribute.trim().length() == 0) {
			throw new IllegalArgumentException("<" + element + "> requires a '" + attributeName + "' attribute");
		}
	}
}
