package org.kie.services.client.serialization.jaxb.impl.type;

public interface JaxbType<T> {

    public T getValue();

    public void setValue(T value);

}
