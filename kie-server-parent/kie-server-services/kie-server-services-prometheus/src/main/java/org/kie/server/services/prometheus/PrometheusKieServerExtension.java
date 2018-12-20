package org.kie.server.services.prometheus;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.Histogram;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.model.Message;
import org.kie.server.api.model.Severity;
import org.kie.server.services.api.KieContainerInstance;
import org.kie.server.services.api.KieServerApplicationComponentsService;
import org.kie.server.services.api.KieServerExtension;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.api.SupportedTransports;
import org.kie.server.services.impl.KieServerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PrometheusKieServerExtension implements KieServerExtension {
    public static final String EXTENSION_NAME = "Prometheus";

    private static final Boolean disabled = Boolean.parseBoolean(System.getProperty(KieServerConstants.KIE_PROMETHEUS_SERVER_EXT_DISABLED, "false"));

    public static final CollectorRegistry registry = CollectorRegistry.defaultRegistry;

    private KieServerRegistry context;

    Logger logger = LoggerFactory.getLogger(PrometheusKieServerExtension.class);

    private List<Object> services = new ArrayList<Object>();
    private boolean initialized = false;

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
                = ServiceLoader.load(KieServerApplicationComponentsService.class );
        List<Object> appComponentsList = new ArrayList<Object>();
        Object[] services = {context};
        for ( KieServerApplicationComponentsService appComponentsService : appComponentsServices ) {
            appComponentsList.addAll( appComponentsService.getAppComponents( EXTENSION_NAME, type, services ) );
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

    public void recordMetric() {

        logger.info("Recording metrics: " + registry.hashCode());

        Histogram histogram = Histogram.build().name("dmn_evaluate_decision_nanosecond" + System.nanoTime())
                .help("DMN Evaluation Time")
                .labelNames("decision_name")
                .buckets(HALF_SECOND_NANO, toNano(1), toNano(2), toNano(3), toNano(4))
                .register();

        for(int i = 0; i < 10; i++){
            int amt = 123456789 + i;
            histogram.labels("prova")
                    .observe(amt);

            logger.info("inserted = " + amt);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Number of nanoseconds in a second.
     */
    public static final long NANOSECONDS_PER_SECOND = 1_000_000_000;
    public static final long HALF_SECOND_NANO = 500_000_000;

    public static long toNano(long second) {
        return second * NANOSECONDS_PER_SECOND;
    }

}
