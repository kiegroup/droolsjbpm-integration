package org.kie.services.client.jaxb;

import org.kie.services.client.SerializationTest;
import org.kie.services.client.serialization.jaxb.JsonSerializationProvider;

public class JsonSerializationTest extends SerializationTest {

    public TestType getType() { 
        return TestType.JSON;
    }
    
    public Object testRoundtrip(Object in) throws Exception {
        String jsonStr = JsonSerializationProvider.convertJaxbObjectToJsonString(in);
        log.info(jsonStr);
        return JsonSerializationProvider.convertJsonStringToJaxbObject(jsonStr, in.getClass());
    }
 
}
