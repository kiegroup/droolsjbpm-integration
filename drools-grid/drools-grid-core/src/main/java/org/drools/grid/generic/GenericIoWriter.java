package org.drools.grid.generic;

public interface GenericIoWriter {
    void write(Message msg,
               MessageResponseHandler responseHandler);
}