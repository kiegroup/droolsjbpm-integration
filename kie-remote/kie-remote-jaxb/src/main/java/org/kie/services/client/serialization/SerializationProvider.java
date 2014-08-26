package org.kie.services.client.serialization;

public interface SerializationProvider {

    String serialize(Object objectInput);

    Object deserialize(String serializedInput);
   
    void dispose();
}
