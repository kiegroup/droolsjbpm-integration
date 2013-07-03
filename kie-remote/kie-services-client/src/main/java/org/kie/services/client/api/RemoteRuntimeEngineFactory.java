package org.kie.services.client.api;

import org.kie.api.runtime.manager.RuntimeEngine;

public interface RemoteRuntimeEngineFactory {

    RuntimeEngine newRuntimeEngine();

}