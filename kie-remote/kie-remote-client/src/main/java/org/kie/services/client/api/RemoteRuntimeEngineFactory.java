package org.kie.services.client.api;

import java.util.Collection;

import org.kie.services.client.api.command.RemoteRuntimeEngine;


public interface RemoteRuntimeEngineFactory {

    RemoteRuntimeEngine newRuntimeEngine();

    void addExtraJaxbClasses(Collection<Class<?>> extraJaxbClasses );
}