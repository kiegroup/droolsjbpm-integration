package org.kie.services.client.serialization.jaxb.impl;

public interface JaxbCommandResponse<T> {

    public Integer getIndex();

    public String getCommandName();
    
    public T getResult();

}