package org.drools.grid.generic;

public interface MessageResponseHandler {
    public void setError(RuntimeException error);

    public void receive(Message message);
}