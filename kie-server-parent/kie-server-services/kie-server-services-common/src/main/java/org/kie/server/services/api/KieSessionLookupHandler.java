package org.kie.server.services.api;

import org.kie.api.runtime.CommandExecutor;
import org.kie.server.api.model.KieContainerResource;

public interface KieSessionLookupHandler {

    CommandExecutor lookupKieSession(String kieSessionId, KieContainerInstance containerInstance, KieServerRegistry registry);
}
