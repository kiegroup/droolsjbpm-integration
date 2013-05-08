package org.kie.services.client.api.command;

import java.lang.reflect.Method;

import org.drools.core.command.impl.GenericCommand;

public interface CommandBuilder {

    public GenericCommand buildCommand(Method method, Object [] args);
}
