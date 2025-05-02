package org.kie.server.services.api;


public interface KieServerRegistryAware {

    void setRegistry(KieServerRegistry registry);
    
    KieServerRegistry getRegistry();


    /**
     * Determine the priority of the loaded KieServerController loaded through the ServiceLoader.
     * @return A priority for the KieServerController. 0 being the highest, Integer.MAX_VALUE being the lowest. If null, then the lowest priority is assumed.
     */
    Integer getPriority();

    /**
     * Determine if a KieServerController supports a specific connection point.
     * @param url The URL to check.
     * @return true if the KieServerController supports the endpoint, false otherwise.
     */
    boolean supports(String url);
}
