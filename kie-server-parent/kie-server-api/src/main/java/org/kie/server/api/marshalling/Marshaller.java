package org.kie.server.api.marshalling;

/**
 * These Marshallers implementations must be thread-safe
 */
public interface Marshaller {

    public String marshall(Object input);

    public <T> T unmarshall(String input, Class<T> type);

    public <T> T unmarshall(String input, String type);
   
    public void dispose();

}
