package org.drools.container.spring;

import java.util.ArrayList;
import java.util.List;

import org.drools.KnowledgeBase;
import org.drools.builder.DecisionTableInputType;
import org.drools.builder.ResourceType;
import org.drools.builder.impl.DecisionTableConfigurationImpl;
import org.drools.io.InternalResource;
import org.drools.io.Resource;
import org.drools.io.impl.ClassPathResource;
import org.drools.io.impl.KnowledgeResource;
import org.drools.io.impl.UrlResource;
import org.drools.xml.ExtensibleXmlParser;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class KnowledgeBaseDefinitionParser extends AbstractSingleBeanDefinitionParser  {
    protected Class getBeanClass(Element element) {
        return KnowledgeBase.class;
     }

     protected void doParse(Element element, BeanDefinitionBuilder bean) {         
         bean.getRawBeanDefinition().setBeanClassName( "org.drools.container.spring.SpringDroolsFactory" );
         bean.getRawBeanDefinition().setFactoryMethodName( "buildKnowledgeBase" );     
         
         List<Resource> resources = new ArrayList<Resource>();
         
         NodeList resourcesList = element.getChildNodes();
         for ( int i = 0, resourcesListLength = resourcesList.getLength(); i < resourcesListLength; i++ ) {
             Node resourceNode = resourcesList.item( i );
             if ( resourceNode.getNodeType() == Node.ELEMENT_NODE ) {
                 Element resourceElement = ( Element ) resourceNode;
                 String source = resourceElement.getAttribute( "source" );
                 String type = resourceElement.getAttribute( "type" );
                 
                 emptyAttributeCheck( "resource",
                                      "source",
                                      source );
                 
                 emptyAttributeCheck( "resource",
                                      "type",
                                      type );        
                 InternalResource resource = null;                
                 
                 if ( source.trim().startsWith( "classpath:" ) ) {
                     resource = new ClassPathResource( source.substring( source.indexOf( ':' ) + 1 ), ClassPathResource.class.getClassLoader() );
                 } else {
                     resource = new UrlResource( source );
                 }
                 
                 resource.setResourceType( ResourceType.getResourceType( type ) );
                 
                 NodeList configurationList = resourceElement.getChildNodes();
                 for ( int j = 0, configurationListLength = configurationList.getLength(); j < configurationListLength; j++ ) {
                     Node configurationNode = configurationList.item( j );
                     if ( configurationNode.getNodeType() == Node.ELEMENT_NODE ) {
                         Element configurationElement = ( Element ) configurationNode;
                         if ( "decisiontable-conf".equals( configurationElement.getLocalName() ) ) {
                             String inputType = configurationElement.getAttribute( "input-type" );
                             String worksheetName = configurationElement.getAttribute( "worksheet-name" );
                             
                             DecisionTableConfigurationImpl dtableConf = new DecisionTableConfigurationImpl();
                             dtableConf.setInputType( DecisionTableInputType.valueOf( inputType ) );
                             dtableConf.setWorksheetName( worksheetName );
                             resource.setConfiguration( dtableConf );
                         }      
                     }
                 }
                 
                 resources.add( resource );
             }
         }
         
         ConstructorArgumentValues values = new ConstructorArgumentValues();
         values.addGenericArgumentValue( resources );
         
         bean.getRawBeanDefinition().setConstructorArgumentValues( values );
     }
     
     public void emptyAttributeCheck(final String element,
                                     final String attributeName,
                                     final String attribute) {
         if ( attribute == null || attribute.trim().equals( "" ) ) {
             throw new IllegalArgumentException( "<" + element + "> requires a '" + attributeName + "' attribute" );
         }
     }     

//    protected AbstractBeanDefinition parseInternal(Element elm,
//                                                   ParserContext ctx) {
//        System.out.println( elm.getAttribute( "value" ) );
//        
//        
//        return elm.getAttribute( "value" );
//    }

}
