package org.drools.grid;

public interface GridConnection<T> {
    T connect();

    void disconnect();
  
}
