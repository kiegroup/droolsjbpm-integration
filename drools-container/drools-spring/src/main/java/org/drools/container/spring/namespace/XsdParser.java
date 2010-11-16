/**
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.container.spring.namespace;

import java.util.List;

import org.drools.builder.JaxbConfiguration;
import org.drools.builder.KnowledgeBuilderFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

import com.sun.tools.xjc.Language;
import com.sun.tools.xjc.Options;

public class XsdParser {
    private static final String SYSTEM_ID       = "system-id";
    private static final String SCHEMA_LANGUAGE = "schema-language";

    @SuppressWarnings("unchecked")
    public static void parse(Element element,
                             ParserContext parserContext,
                             BeanDefinitionBuilder factory) {

        List<Element> childElements = DomUtils.getChildElementsByTagName( element,
                                                                          "jaxb-conf" );
        if ( !childElements.isEmpty() ) {
            Element conf = childElements.get( 0 );

            String systemId = conf.getAttribute( SYSTEM_ID );
            systemId = (systemId != null && systemId.trim().length() > 0) ? systemId : "xsd";

            String schemaLanguage = conf.getAttribute( SCHEMA_LANGUAGE );
            schemaLanguage = (schemaLanguage != null && schemaLanguage.trim().length() > 0) ? schemaLanguage : "XMLSCHEMA";

            Options options = new Options();
            options.setSchemaLanguage( Language.valueOf( schemaLanguage ) );

            JaxbConfiguration jaxbConf = KnowledgeBuilderFactory.newJaxbConfiguration( new Options(),
                                                                                       systemId );

            factory.addPropertyValue( "resourceConfiguration",
                                      jaxbConf );
        } else {
            JaxbConfiguration jaxbConf = KnowledgeBuilderFactory.newJaxbConfiguration( new Options(),
                                                                                       "xsd" );

            factory.addPropertyValue( "resourceConfiguration",
                                      jaxbConf );
        }
    }
}
