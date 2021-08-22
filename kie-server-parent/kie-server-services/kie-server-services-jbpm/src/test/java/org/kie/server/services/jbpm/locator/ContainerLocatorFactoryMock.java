package org.kie.server.services.jbpm.locator;

import java.util.List;

import org.kie.server.services.api.ContainerLocator;
import org.kie.server.services.api.KieContainerInstance;

public class ContainerLocatorFactoryMock implements ContainerLocatorFactory {

    @Override
    public ContainerLocator create(Number processId) {
        return new ContainerLocatorMock();
    }

    public static class ContainerLocatorMock implements ContainerLocator {

        @Override
        public String locateContainer(String alias, List<? extends KieContainerInstance> containerInstances) {
            return "exampleContainer";
        }
    }
}
