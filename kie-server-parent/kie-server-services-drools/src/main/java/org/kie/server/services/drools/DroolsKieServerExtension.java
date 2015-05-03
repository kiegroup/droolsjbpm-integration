package org.kie.server.services.drools;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.kie.server.services.api.KieContainerInstance;
import org.kie.server.services.api.KieServerApplicationComponentsService;
import org.kie.server.services.api.KieServerExtension;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.api.SupportedTransports;
import org.kie.server.services.drools.impl.RuleServiceImpl;
import org.kie.server.services.impl.KieServerImpl;

public class DroolsKieServerExtension implements KieServerExtension {

    public static final String EXTENSION_NAME = "Drools";

    private static final Boolean disabled = Boolean.parseBoolean(System.getProperty("org.drools.server.ext.disabled", "false"));

    private RuleService ruleService;

    @Override
    public boolean isActive() {
        return disabled == false;
    }

    @Override
    public void init(KieServerImpl kieServer, KieServerRegistry registry) {
        this.ruleService = new RuleServiceImpl(kieServer, registry);
    }

    @Override
    public void destroy(KieServerImpl kieServer, KieServerRegistry registry) {
        // no-op?
    }

    @Override
    public void createContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
        // do any other bootstrapping rule service requires

        kieContainerInstance.addService(ruleService);
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
                ruleService
        };
        for( KieServerApplicationComponentsService appComponentsService : appComponentsServices ) { 
            appComponentsList.addAll(appComponentsService.getAppComponents(EXTENSION_NAME, type, services));
        }
        return appComponentsList;
    }

    @Override
    public <T> T getAppComponents(Class<T> serviceType) {
        if (serviceType.isAssignableFrom(ruleService.getClass())) {
            return (T) ruleService;
        }

        return null;
    }

    @Override
    public String toString() {
        return "Drools KIE Server extension";
    }
}
