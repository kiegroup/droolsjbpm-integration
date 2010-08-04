package org.drools.grid.internal;

public interface MessageResponseHandler {
    public void setError(RuntimeException error);

    public void receive(Message message);
}