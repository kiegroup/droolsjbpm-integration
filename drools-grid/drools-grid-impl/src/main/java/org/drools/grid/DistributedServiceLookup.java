package org.drools.grid;

public interface DistributedServiceLookup {
    public <T> T get(Class<T> serviceClass);
}
