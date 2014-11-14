package org.kie.server.client;

public interface SerializationProvider {

    String serialize(Object objectInput);

    Object deserialize(String serializedInput);

    <T> T deserialize(String serializedInput, Class<T> type);
}
