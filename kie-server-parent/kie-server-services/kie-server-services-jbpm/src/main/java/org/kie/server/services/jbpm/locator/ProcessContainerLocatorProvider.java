package org.kie.server.services.jbpm.locator;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import org.kie.server.api.KieServerConstants;
import org.kie.server.services.api.ContainerLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProcessContainerLocatorProvider {
    private static final Logger logger = LoggerFactory.getLogger(ProcessContainerLocatorProvider.class);

    private String processInstanceLocatorName =
        System.getProperty(KieServerConstants.KIE_SERVER_PROCESS_INSTANCE_CONTAINER_LOCATOR,
            ByProcessInstanceIdContainerLocator.class.getSimpleName());

    private static final ProcessContainerLocatorProvider INSTANCE = new ProcessContainerLocatorProvider();
    private final Map<String, ContainerLocatorFactory> locators = new HashMap<>();

    private ProcessContainerLocatorProvider() {
        ServiceLoader<ContainerLocatorFactory> containerLocators = ServiceLoader.load(ContainerLocatorFactory.class);
        containerLocators.forEach( l -> {
                locators.put(l.getClass().getSimpleName(), l);
                logger.info("Discovered '{}' container locator factory and registered under '{}'", l, l.getClass().getSimpleName());
            }
        );

        locators.put(ByProcessInstanceIdContainerLocator.class.getSimpleName(), ByProcessInstanceIdContainerLocator.Factory.get());
        locators.put(ByContextMappingInfoContainerLocator.class.getSimpleName(), ByContextMappingInfoContainerLocator.Factory.get());
    }

    public ContainerLocator getLocator(final Number processInstanceId) {
        ContainerLocatorFactory containerLocatorFactory = locators.get(processInstanceLocatorName);

        if (containerLocatorFactory == null) {
            throw new IllegalStateException("No container locator factory found under name " + processInstanceLocatorName);
        }

        logger.info("Container locator factory was finding '{}'", containerLocatorFactory.getClass().getSimpleName());
        return containerLocatorFactory.create(processInstanceId);
    }

    public void readSystemProperty() {
        processInstanceLocatorName = System.getProperty(KieServerConstants.KIE_SERVER_PROCESS_INSTANCE_CONTAINER_LOCATOR,
                ByProcessInstanceIdContainerLocator.class.getSimpleName());
    }

    public static ProcessContainerLocatorProvider get() {
        return INSTANCE;
    }
}
