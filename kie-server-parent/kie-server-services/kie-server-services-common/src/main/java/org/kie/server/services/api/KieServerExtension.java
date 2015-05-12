package org.kie.server.services.api;

import java.util.List;
import java.util.Map;

import org.kie.server.services.impl.KieServerImpl;

public interface KieServerExtension {

    boolean isActive();

    void init(KieServerImpl kieServer, KieServerRegistry registry);

    void destroy(KieServerImpl kieServer, KieServerRegistry registry);

    void createContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters);

    void disposeContainer(String id, Map<String, Object> parameters);

    List<Object> getAppComponents(SupportedTransports type);

    <T> T getAppComponents(Class<T> serviceType);
}
