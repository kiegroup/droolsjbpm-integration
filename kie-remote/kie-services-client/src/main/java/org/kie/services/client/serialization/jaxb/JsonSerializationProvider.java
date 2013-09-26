package org.kie.services.client.serialization.jaxb;

import static org.kie.services.client.serialization.jaxb.JaxbSerializationProvider.jaxbClasses;

import java.io.ByteArrayInputStream;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.codehaus.jettison.mapped.Configuration;
import org.codehaus.jettison.mapped.MappedNamespaceConvention;
import org.codehaus.jettison.mapped.MappedXMLStreamReader;
import org.codehaus.jettison.mapped.MappedXMLStreamWriter;
import org.kie.services.client.serialization.jaxb.impl.JaxbOtherResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbProcessInstanceListResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbProcessInstanceResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbProcessInstanceWithVariablesResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbVariablesResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbWorkItem;
import org.kie.services.client.serialization.jaxb.rest.JaxbGenericResponse;

// TODO: Add object version checking
public class JsonSerializationProvider {
   
    public static String convertJaxbObjectToJsonString(Object object) throws JAXBException {
        Marshaller marshaller = JAXBContext.newInstance(jaxbClasses).createMarshaller();
        StringWriter stringWriter = new StringWriter();
        XMLStreamWriter xmlStreamWriter = new MappedXMLStreamWriter(
                new MappedNamespaceConvention(new Configuration()), stringWriter);
        
        marshaller.marshal(object, xmlStreamWriter);
        String output = stringWriter.toString();
        
        return output;
    }
    
    public static Object convertJsonStringToJaxbObject(String jsonStr) throws JAXBException, JSONException, XMLStreamException {
        Unmarshaller unmarshaller = JAXBContext.newInstance(jaxbClasses).createUnmarshaller();
        
        JSONObject jsonObg = new JSONObject(jsonStr);
        XMLStreamReader xmlStreamReader = new MappedXMLStreamReader(jsonObg, 
                 new MappedNamespaceConvention(new Configuration()));
        
        Object jaxbObj = unmarshaller.unmarshal(xmlStreamReader);
        
        return jaxbObj;
    }

}
