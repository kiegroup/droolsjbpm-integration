package org.kie.services.client.serialization;

public interface SerializationProvider {

    Object serialize(Object objectInput);

    Object deserialize(Object serializedInput);
    
}
