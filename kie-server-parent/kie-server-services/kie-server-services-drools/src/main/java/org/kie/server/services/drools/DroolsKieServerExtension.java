package org.kie.server.services.drools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.kie.server.services.api.KieContainerCommandService;
import org.kie.server.services.api.KieContainerInstance;
import org.kie.server.services.api.KieServerApplicationComponentsService;
import org.kie.server.services.api.KieServerExtension;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.api.SupportedTransports;
import org.kie.server.services.impl.KieContainerCommandServiceImpl;
import org.kie.server.services.impl.KieServerImpl;

public class DroolsKieServerExtension implements KieServerExtension {

    public static final String EXTENSION_NAME = "Drools";

    private static final Boolean disabled = Boolean.parseBoolean(System.getProperty("org.drools.server.ext.disabled", "false"));

    private KieContainerCommandService batchCommandService;

    @Override
    public boolean isActive() {
        return disabled == false;
    }

    @Override
    public void init(KieServerImpl kieServer, KieServerRegistry registry) {
        this.batchCommandService = new KieContainerCommandServiceImpl(kieServer, registry);
    }

    @Override
    public void destroy(KieServerImpl kieServer, KieServerRegistry registry) {
        // no-op?
    }

    @Override
    public void createContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
        // do any other bootstrapping rule service requires

        kieContainerInstance.addService(batchCommandService);
    }

    @Override
    public void disposeContainer(String id, Map<String, Object> parameters) {
        // no-op?
    }

    @Override
    public List<Object> getAppComponents(SupportedTransports type) {
        ServiceLoader<KieServerApplicationComponentsService> appComponentsServices
            = ServiceLoader.load(KieServerApplicationComponentsService.class);
        List<Object> appComponentsList =  new ArrayList<Object>();
        Object [] services = { 
                batchCommandService
        };
        for( KieServerApplicationComponentsService appComponentsService : appComponentsServices ) { 
            appComponentsList.addAll(appComponentsService.getAppComponents(EXTENSION_NAME, type, services));
        }
        return appComponentsList;
    }

    @Override
    public <T> T getAppComponents(Class<T> serviceType) {
        if (serviceType.isAssignableFrom(batchCommandService.getClass())) {
            return (T) batchCommandService;
        }

        return null;
    }

    @Override
    public String toString() {
        return EXTENSION_NAME + " KIE Server extension";
    }
}
