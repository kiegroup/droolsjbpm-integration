package org.kie.server.services.api;


public interface KieServerRegistryAware {

    void setRegistry(KieServerRegistry registry);
    
    KieServerRegistry getRegistry();
}
