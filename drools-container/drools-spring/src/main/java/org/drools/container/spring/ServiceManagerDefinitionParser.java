package org.drools.container.spring;

import java.util.ArrayList;
import java.util.Collection;
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
import org.drools.vsm.ServiceManager;
import org.drools.xml.ExtensibleXmlParser;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.xml.AbstractBeanDefinitionParser;
import org.springframework.beans.factory.xml.AbstractSingleBeanDefinitionParser;
import org.springframework.beans.factory.xml.ParserContext;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

public class ServiceManagerDefinitionParser extends AbstractSingleBeanDefinitionParser  {
    protected Class getBeanClass(Element element) {
        return ServiceManager.class;
     }

     protected void doParse(Element element, BeanDefinitionBuilder bean) {         
         bean.getRawBeanDefinition().setBeanClassName( "org.drools.container.spring.SpringDroolsFactory" );
         bean.getRawBeanDefinition().setFactoryMethodName( "buildServiceManager" );     
         
//         List<Resource> resources = new ArrayList<Resource>();
         
         //Collection<RegistryEntry> registryEntries = new ArrayList<RegistryEntry>();
         
         ManagedMap map = new ManagedMap();
         NodeList registryList = element.getChildNodes();
         for ( int i = 0, registryListLength = registryList.getLength(); i < registryListLength; i++ ) {
             Node resourceNode = registryList.item( i );
             if ( resourceNode.getNodeType() == Node.ELEMENT_NODE ) {
                 Element resourceElement = ( Element ) resourceNode;
                 String name = resourceElement.getAttribute( "name" );
                 String ref = resourceElement.getAttribute( "ref" );
                 
                 emptyAttributeCheck( "resource",
                                      "ref",
                                      ref );   
                 
                 if ( name == null || name.trim().length() == 0 ) {                     
                     map.put(ref, new RuntimeBeanReference( ref ) );                     
                 } else {
                     map.put( name, new RuntimeBeanReference( ref ) );
                 }
             }
                 
         }
         
         ConstructorArgumentValues values = new ConstructorArgumentValues();
         values.addGenericArgumentValue( map );
         
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
