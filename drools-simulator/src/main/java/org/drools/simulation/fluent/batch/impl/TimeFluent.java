package org.drools.simulation.fluent.batch.impl;

public interface TimeFluent <T> {
    //T at(long time);
    T after(long duration);
    T relativeAfter(long duration);
}
