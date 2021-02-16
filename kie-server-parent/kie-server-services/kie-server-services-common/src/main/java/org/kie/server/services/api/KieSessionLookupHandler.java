package org.kie.server.services.api;

import org.kie.api.runtime.CommandExecutor;

public interface KieSessionLookupHandler {

    CommandExecutor lookupKieSession(String kieSessionId, KieContainerInstance containerInstance, KieServerRegistry registry);

    default void postLookupKieSession(String kieSessionId, KieContainerInstance containerInstance, CommandExecutor ks, KieServerRegistry registry) {
    }
}
