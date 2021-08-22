package org.kie.server.services.jbpm.locator;

import org.kie.server.services.api.ContainerLocator;

public interface ContainerLocatorFactory {
    ContainerLocator create(final Number processId);
}
