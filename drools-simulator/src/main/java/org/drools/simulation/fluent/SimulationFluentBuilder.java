package org.drools.simulation.fluent;

import java.util.concurrent.TimeUnit;

public interface SimulationFluentBuilder<T> {

    T newStep(long distanceMillis);
    T newStep(long distanceMillis, TimeUnit timeUnit);
    T newRelativeStep(long relativeDistance);
    T newRelativeStep(long relativeDistance, TimeUnit timeUnit);

}
