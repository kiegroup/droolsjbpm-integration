package org.kie.services.client.serialization.jaxb;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;
import java.util.ArrayList;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

import org.kie.services.client.api.command.AcceptedCommands;
import org.kie.services.client.serialization.jaxb.impl.JaxbHistoryLogList;
import org.kie.services.client.serialization.jaxb.impl.JaxbOtherResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbProcessInstanceListResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbProcessInstanceResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbProcessInstanceWithVariablesResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbVariablesResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbWorkItem;
import org.kie.services.client.serialization.jaxb.rest.JaxbGenericResponse;

// TODO: Add object version checking
public class JaxbSerializationProvider {
   
    static Class<?> [] jaxbClasses = {
        JaxbCommandsRequest.class, 
        JaxbCommandsResponse.class,
        JaxbVariablesResponse.class,
        JaxbGenericResponse.class,
        JaxbProcessInstanceResponse.class,
        JaxbProcessInstanceWithVariablesResponse.class,
        JaxbProcessInstanceListResponse.class,
        JaxbWorkItem.class,
        JaxbOtherResponse.class,
        JaxbHistoryLogList.class
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
