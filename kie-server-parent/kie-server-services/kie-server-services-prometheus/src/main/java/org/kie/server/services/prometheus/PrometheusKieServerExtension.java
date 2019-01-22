package org.kie.server.services.prometheus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import io.prometheus.client.CollectorRegistry;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.model.Message;
import org.kie.server.api.model.Severity;
import org.kie.server.services.api.KieContainerInstance;
import org.kie.server.services.api.KieServerApplicationComponentsService;
import org.kie.server.services.api.KieServerExtension;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.api.SupportedTransports;
import org.kie.server.services.impl.KieServerImpl;

public class PrometheusKieServerExtension implements KieServerExtension {

    public static final String EXTENSION_NAME = "Prometheus";

    private static final Boolean disabled = Boolean.parseBoolean(System.getProperty(KieServerConstants.KIE_PROMETHEUS_SERVER_EXT_DISABLED, "true"));

    public static final CollectorRegistry registry = CollectorRegistry.defaultRegistry;

    private KieServerRegistry context;

    private List<Object> services = new ArrayList<>();
    private boolean initialized = false;

    private static PrometheusMetrics METRICS = null;

    public static PrometheusMetrics getMetrics() {
        if (METRICS == null) {
            METRICS = new PrometheusMetrics();
        }
        return METRICS;
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public boolean isActive() {
        return !disabled;
    }

    @Override
    public void init(KieServerImpl kieServer, KieServerRegistry registry) {
        this.context = registry;
        initialized = true;
    }

    @Override
    public void destroy(KieServerImpl kieServer, KieServerRegistry registry) {
        //no-op
    }

    @Override
    public void createContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
        //no-op
    }

    @Override
    public boolean isUpdateContainerAllowed(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
        return true;
    }

    @Override
    public void updateContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
        //no-op
    }

    @Override
    public void disposeContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
        //no-op
    }

    @Override
    public List<Object> getAppComponents(SupportedTransports type) {

        ServiceLoader<KieServerApplicationComponentsService> appComponentsServices
                = ServiceLoader.load(KieServerApplicationComponentsService.class);
        List<Object> appComponentsList = new ArrayList<Object>();
        Object[] services = {context};
        for (KieServerApplicationComponentsService appComponentsService : appComponentsServices) {
            appComponentsList.addAll(appComponentsService.getAppComponents(EXTENSION_NAME, type, services));
        }
        return appComponentsList;
    }

    @Override
    public <T> T getAppComponents(Class<T> serviceType) {
        return null;
    }

    @Override
    public String getImplementedCapability() {
        return KieServerConstants.CAPABILITY_PROMETHEUS;
    }

    @Override
    public List<Object> getServices() {
        return services;
    }

    @Override
    public String getExtensionName() {
        return EXTENSION_NAME;
    }

    @Override
    public Integer getStartOrder() {
        return Integer.MAX_VALUE;
    }

    @Override
    public String toString() {
        return EXTENSION_NAME + " KIE Server extension";
    }

    @Override
    public List<Message> healthCheck(boolean report) {
        List<Message> messages = KieServerExtension.super.healthCheck(report);

        if (report) {
            messages.add(new Message(Severity.INFO, getExtensionName() + " is alive"));
        }
        return messages;
    }
}
