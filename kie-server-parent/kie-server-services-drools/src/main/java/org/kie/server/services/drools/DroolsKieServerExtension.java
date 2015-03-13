package org.kie.server.services.drools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.kie.server.services.api.KieContainerInstance;
import org.kie.server.services.api.KieServerExtension;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.api.SupportedTransports;
import org.kie.server.services.drools.impl.RuleServiceImpl;
import org.kie.server.services.drools.rest.RuleServiceResource;
import org.kie.server.services.impl.KieServerImpl;

public class DroolsKieServerExtension implements KieServerExtension {

    private static final Boolean disabled = Boolean.parseBoolean(System.getProperty("org.drools.server.ext.disabled", "false"));

    private KieServerImpl kieServer;
    private RuleService ruleService;

    @Override
    public boolean isActive() {
        return disabled == false;
    }

    @Override
    public void init(KieServerImpl kieServer, KieServerRegistry registry) {
        this.kieServer = kieServer;
        this.ruleService = new RuleServiceImpl(kieServer, registry);

    }

    @Override
    public void createContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
        // do any other bootstrapping rule service requires

        kieContainerInstance.addService(ruleService);
    }

    @Override
    public void disposeContainer(String id, Map<String, Object> parameters) {

    }

    @Override
    public List<Object> getAppComponents(SupportedTransports type) {
        List<Object> applicationComponents =  new ArrayList<Object>();
        if (type.equals(SupportedTransports.REST)) {
            applicationComponents.add(new RuleServiceResource(ruleService));
        } else if (type.equals(SupportedTransports.JMS)) {
            applicationComponents.add(ruleService);
        }

        return applicationComponents;
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
