package org.drools.grid.internal;

public interface GenericIoWriter {
    void write(Message msg,
               MessageResponseHandler responseHandler);
}