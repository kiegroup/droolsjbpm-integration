package org.kie.server.services.marshalling;

/**
 * These Marshallers implementations must be thread-safe
 */
public interface Marshaller {

    public String marshall(Object objectInput);

    public Object unmarshall(String serializedInput);
   
    public void dispose();

}
