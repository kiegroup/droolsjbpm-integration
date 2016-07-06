package org.drools.simulation.fluent.batch.impl;

import org.drools.core.command.EndConversationCommand;
import org.drools.core.command.JoinConversationCommand;
import org.drools.core.command.OutCommand;
import org.drools.core.command.StartConversationCommand;
import org.kie.api.command.Command;
import org.kie.internal.fluent.ContextFluent;
import org.kie.internal.fluent.Scope;

public class BaseBatchFluent<T> implements ContextFluent<T> {
    protected FluentContext fluentCtx;

    public BaseBatchFluent(FluentContext fluentCtx) {
        this.fluentCtx = fluentCtx;
    }

    public T addCommand(Command command) {
        fluentCtx.addCommand(command);
        return (T) this;
    }


    public T after(long duration) {
        return null;
    }


    public T relativeAfter(long duration) {
        return null;
    }

    @Override
    public T out() {
        fluentCtx.addCommand( new OutCommand<Object>());
        return (T) this;
    }

    @Override
    public T out(String name) {
        fluentCtx.addCommand( new OutCommand<Object>(name));
        return (T) this;
    }


    @Override
    public T set(String name, Scope scope) {
        fluentCtx.addCommand( new SetCommand<Object>(name, scope));
        return (T) this;
    }

    @Override
    public T set(String name) {
        fluentCtx.addCommand( new SetCommand<Object>(name));
        return (T) this;
    }


    @Override
    public T newApplicationContext(String name) {
        return null;
    }

    @Override
    public T getApplicationContext(String name) {
        return null;
    }

    @Override
    public T startConversation() {
        fluentCtx.addCommand(new StartConversationCommand());
        return (T) this;
    }

    @Override
    public T joinConversation(long id) {
        fluentCtx.addCommand(new JoinConversationCommand(id));
        return (T) this;
    }

    @Override
    public T leaveConversation(long id) {
        //fluentCtx.addCommand(new LeaveConversationCommand(id));
        return (T) this;
    }

    @Override
    public T endConversation(long id) {
        fluentCtx.addCommand(new EndConversationCommand(id));
        return (T) this;
    }
}
