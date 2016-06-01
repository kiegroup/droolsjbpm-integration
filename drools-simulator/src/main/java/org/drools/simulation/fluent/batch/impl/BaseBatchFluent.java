package org.drools.simulation.fluent.batch.impl;

import org.drools.simulation.fluent.batch.BatchBuilderFluent;
import org.drools.simulation.fluent.session.KieSessionSimulationFluent;
import org.kie.api.command.Command;

public class BaseBatchFluent<T> {
    protected FluentContext fluentCtx;

    public BaseBatchFluent(FluentContext fluentCtx) {
        this.fluentCtx = fluentCtx;
    }

    protected T addCommand(Command command) {
        fluentCtx.addCommand(command);
        return (T) this;
    }


    public T after(long duration) {
        return null;
    }


    public T relativeAfter(long duration) {
        return null;
    }

}
