package org.kie.services.client.jaxb;

import org.kie.services.client.SerializationTest;
import org.kie.services.client.serialization.jaxb.JaxbSerializationProvider;

public class JaxbSerializationTest extends SerializationTest {

    public TestType getType() { 
        return TestType.JAXB;
    }
    
    public Object testRoundtrip(Object in) throws Exception {
        String xmlObject = JaxbSerializationProvider.convertJaxbObjectToString(in);
        log.debug(xmlObject);
        return JaxbSerializationProvider.convertStringToJaxbObject(xmlObject);
    }

}
