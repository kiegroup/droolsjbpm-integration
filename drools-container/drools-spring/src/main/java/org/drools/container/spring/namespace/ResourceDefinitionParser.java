/*
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

import org.drools.builder.DecisionTableInputType;
import org.drools.builder.ResourceType;
import org.drools.builder.conf.impl.DecisionTableConfigurationImpl;
import org.drools.container.spring.beans.DroolsResourceAdapter;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

public class ResourceDefinitionParser extends AbstractBeanDefinitionParser {

    private static final String WORKSHEET_NAME_ATTRIBUTE       = "worksheet-name";
    private static final String INPUT_TYPE_ATTRIBUTE           = "input-type";
    private static final String TYPE_ATTRIBUTE                 = "type";
    private static final String SOURCE_ATTRIBUTE               = "source";
    private static final String BASIC_AUTHENTICATION_ATTRIBUTE = "basic-authentication";
    private static final String USERNAME_ATTRIBUTE             = "username";
    private static final String PASSWORD_ATTRIBUTE             = "password";
    private static final String REF                            = "ref";
    private static final String NAME                            = "name";
    private static final String DESCRIPTION                            = "description";

    @SuppressWarnings("unchecked")
    @Override
    protected AbstractBeanDefinition parseInternal(Element element,
                                                   ParserContext parserContext) {
        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition( DroolsResourceAdapter.class );

        if ( StringUtils.hasText( element.getAttribute( REF ) ) ) {
            String ref = element.getAttribute( REF );
            emptyAttributeCheck( element.getLocalName(),
                                 REF,
                                 ref );
            return (AbstractBeanDefinition) parserContext.getRegistry().getBeanDefinition( ref );
        }

        String source = element.getAttribute( SOURCE_ATTRIBUTE );
        emptyAttributeCheck( element.getLocalName(),
                             SOURCE_ATTRIBUTE,
                             source );
        factory.addPropertyValue( "resource",
                                  source );

        String type = element.getAttribute( TYPE_ATTRIBUTE );

        String resourceType = type == null || type.length() == 0 ? ResourceType.DRL.getName() : type;

        factory.addPropertyValue( "resourceType",
                                  resourceType );

        boolean basicAuthenticationEnabled = element.getAttribute( BASIC_AUTHENTICATION_ATTRIBUTE ) != null && element.getAttribute( BASIC_AUTHENTICATION_ATTRIBUTE ).equalsIgnoreCase( "enabled" );
        factory.addPropertyValue( "basicAuthenticationEnabled",
                                  basicAuthenticationEnabled );

        if ( basicAuthenticationEnabled ) {
            String username = element.getAttribute( USERNAME_ATTRIBUTE );
            factory.addPropertyValue( "basicAuthenticationUsername",
                                      username );

            String password = element.getAttribute( PASSWORD_ATTRIBUTE );
            factory.addPropertyValue( "basicAuthenticationPassword",
                                      password );
        }
        
        String name = element.getAttribute( NAME );
        factory.addPropertyValue( "name",
                                  org.drools.core.util.StringUtils.isEmpty(name) ? null : name);
        
        String description = element.getAttribute( DESCRIPTION );
        factory.addPropertyValue( "description",
                                  org.drools.core.util.StringUtils.isEmpty(description) ? null : description);
        

        if ( "xsd".equals( resourceType.toLowerCase() ) ) {
            XsdParser.parse( element,
                             parserContext,
                             factory );
        } else if ( "dtable".equals( resourceType.toLowerCase() ) ) {
            List<Element> childElements = DomUtils.getChildElementsByTagName( element,
                                                                              "decisiontable-conf" );
            if ( !childElements.isEmpty() ) {
                Element conf = childElements.get( 0 );
                DecisionTableConfigurationImpl dtableConf = new DecisionTableConfigurationImpl();

                String inputType = conf.getAttribute( INPUT_TYPE_ATTRIBUTE );
                emptyAttributeCheck( conf.getLocalName(),
                                     INPUT_TYPE_ATTRIBUTE,
                                     inputType );
                dtableConf.setInputType( DecisionTableInputType.valueOf( inputType ) );

                String worksheetName = conf.getAttribute( WORKSHEET_NAME_ATTRIBUTE );
                emptyAttributeCheck( conf.getLocalName(),
                                     WORKSHEET_NAME_ATTRIBUTE,
                                     worksheetName );
                dtableConf.setWorksheetName( worksheetName );

                factory.addPropertyValue( "resourceConfiguration",
                                          dtableConf );
            }
        }

        return factory.getBeanDefinition();
    }

    public void emptyAttributeCheck(final String element,
                                    final String attributeName,
                                    final String attribute) {
        if ( attribute == null || attribute.trim().equals( "" ) ) {
            throw new IllegalArgumentException( "<" + element + "> requires a '" + attributeName + "' attribute" );
        }
    }
}
