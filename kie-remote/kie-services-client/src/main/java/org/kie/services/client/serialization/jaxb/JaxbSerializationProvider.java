package org.kie.services.client.serialization.jaxb;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

// TODO: Add object version checking
public class JaxbSerializationProvider {
   
    private static Class<?> [] jaxbClasses = { 
        JaxbCommandMessage.class
    };
    
    public static String convertJaxbObjectToString(Object object) throws JAXBException {
        Marshaller marshaller = JAXBContext.newInstance(jaxbClasses).createMarshaller();
        StringWriter stringWriter = new StringWriter();
        
        marshaller.marshal(object, stringWriter);
        String output = stringWriter.toString();
        
        return output;
    }
    
    public static Object convertStringToJaxbObject(String xmlStr) throws JAXBException {
        Unmarshaller unmarshaller = JAXBContext.newInstance(jaxbClasses).createUnmarshaller();
        ByteArrayInputStream xmlStrInputStream = new ByteArrayInputStream(xmlStr.getBytes());
        
        Object jaxbObj = unmarshaller.unmarshal(xmlStrInputStream);
        
        return jaxbObj;
    }

}
