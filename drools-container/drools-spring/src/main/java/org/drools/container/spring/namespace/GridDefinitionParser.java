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

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.drools.container.spring.beans.GridBeanFactory;
import org.drools.container.spring.beans.GridBeanFactory.SocketServiceConfiguration;
import org.drools.grid.io.AcceptorFactoryService;
import org.drools.grid.remote.mina.MinaAcceptorFactoryService;
import org.drools.grid.service.directory.impl.JpaWhitePages;
import org.drools.grid.service.directory.impl.WhitePagesImpl;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * 
 * @author Lucas Amador
 *
 */
public class GridDefinitionParser extends AbstractBeanDefinitionParser {

    private static final String EMF_ATTRIBUTE = "entity-manager-factory";

    protected AbstractBeanDefinition parseInternal(Element element,
                                                   ParserContext parserContext) {

        BeanDefinitionBuilder factory = BeanDefinitionBuilder.rootBeanDefinition( GridBeanFactory.class );

        String id = element.getAttribute( "id" );
        factory.addPropertyValue( "id",
                                  id );

        for ( int i = 0, length = element.getChildNodes().getLength(); i < length; i++ ) {
            Node n = element.getChildNodes().item( i );
            if ( n instanceof Element ) {
                Element e = (Element) n;

                if ( "core-services".equals( e.getLocalName() ) ) {
                    String ref = e.getAttribute( "ref" );
                    Element nestedElm = getFirstElement( e.getChildNodes() );

                    if ( StringUtils.hasText( ref ) ) {
                        factory.addPropertyReference( "coreServices",
                                                      ref );
                    } else if ( nestedElm != null ) {
                        factory.addPropertyValue( "coreServices",
                                                  parserContext.getDelegate().parsePropertySubElement( nestedElm,
                                                                                                       null,
                                                                                                       null ) );
                    } else {
                        throw new IllegalArgumentException( "set-global must either specify a 'ref' attribute or have a nested bean" );
                    }
                } else if ( "whitepages".equals( e.getLocalName() ) ) {
                    Element persistenceElm = DomUtils.getChildElementByTagName( e,
                                                                                "jpa-persistence" );
                    if ( persistenceElm != null ) {
                        BeanDefinitionBuilder beanBuilder = BeanDefinitionBuilder.genericBeanDefinition( JpaWhitePages.class );

                        Element emf = DomUtils.getChildElementByTagName( persistenceElm,
                                                                         EMF_ATTRIBUTE );
                        String ref = emf.getAttribute( "ref" );

                        beanBuilder.addConstructorArgReference( ref );
                        factory.addPropertyValue( "whitePages",
                                                  beanBuilder.getBeanDefinition() );
                    } else {
                        String ref = e.getAttribute( "ref" );
                        Element nestedElm = getFirstElement( e.getChildNodes() );

                        if ( StringUtils.hasText( ref ) ) {
                            factory.addPropertyReference( "whitePages",
                                                          ref );
                        } else if ( nestedElm != null ) {
                            factory.addPropertyValue( "whitePages",
                                                      parserContext.getDelegate().parsePropertySubElement( nestedElm,
                                                                                                           null,
                                                                                                           null ) );
                        } else {
                            factory.addPropertyValue( "whitePages",
                                                      new WhitePagesImpl() );
                        }
                    }
                } else if ( "socket-service".equals( e.getLocalName() ) ) {
                    String acceptor = e.getAttribute( "acceptor" );
                    String ip = e.getAttribute( "ip" );

                    AcceptorFactoryService acc = null;
                    if ( StringUtils.hasText( acceptor ) ) {
                        if ( "mina".equals( acceptor ) ) {
                            acc = new MinaAcceptorFactoryService();
                        }
                    }

                    if ( acc == null ) {
                        acc = new MinaAcceptorFactoryService();
                    }

                    if ( !StringUtils.hasText( ip ) ) {
                        try {
                            ip = InetAddress.getLocalHost().getHostAddress();
                        } catch ( UnknownHostException e1 ) {
                            throw new RuntimeException( "socket-service did not specify an ip address and one could not be determined",
                                                        e1 );
                        }
                    }

                    if ( !StringUtils.hasText( ip ) ) {
                        throw new RuntimeException( "socket-service did not specify an ip address and one could not be determined" );
                    }

                    BeanDefinitionBuilder beanBuilder = BeanDefinitionBuilder.genericBeanDefinition( SocketServiceConfiguration.class );
                    beanBuilder.addPropertyValue( "ip",
                                                  ip );
                    beanBuilder.addPropertyValue( "acceptor",
                                                  acceptor );

                    //e.getChildNodes()
                    List<String[]> services = new ArrayList<String[]>();
                    for ( int j = 0, serviceLength = e.getChildNodes().getLength(); j < serviceLength; j++ ) {
                        Node e2 = e.getChildNodes().item( j );
                        if ( e2 instanceof Element && "service".equals( ((Element) e2).getLocalName() ) ) {
                            Element se = (Element) e2;
                            String name = se.getAttribute( "name" );
                            String port = se.getAttribute( "port" );
                            services.add( new String[]{name, port} );
                        }
                    }
                    beanBuilder.addPropertyValue( "services",
                                                  services );
                    factory.addPropertyValue( "socketServiceConfiguration",
                                              beanBuilder.getBeanDefinition() );
                }

            }
        }

        return factory.getBeanDefinition();
    }

    public void emptyAttributeCheck(final String element,
                                    final String attributeName,
                                    final String attribute) {
        if ( attribute == null || attribute.trim().length() == 0 ) {
            throw new IllegalArgumentException( "<" + element + "> requires a '" + attributeName + "' attribute" );
        }
    }

    private Element getFirstElement(NodeList list) {
        for ( int j = 0, lengthj = list.getLength(); j < lengthj; j++ ) {
            if ( list.item( j ) instanceof Element ) {
                return (Element) list.item( j );
            }
        }
        return null;
    }

}
