package org.kie.services.client.jaxb;

import org.kie.services.client.SerializationTest;
import org.kie.services.client.serialization.JsonSerializationProvider;

public class JsonSerializationTest extends SerializationTest {

    public TestType getType() { 
        return TestType.JSON;
    }
    
    protected JsonSerializationProvider jsonProvider = new JsonSerializationProvider();
    
    public Object testRoundtrip(Object in) throws Exception {
        String jsonStr = jsonProvider.serialize(in);
        log.debug(jsonStr);
        jsonProvider.setDeserializeOutputClass(in.getClass());
        return jsonProvider.deserialize(jsonStr);
    }
 
}