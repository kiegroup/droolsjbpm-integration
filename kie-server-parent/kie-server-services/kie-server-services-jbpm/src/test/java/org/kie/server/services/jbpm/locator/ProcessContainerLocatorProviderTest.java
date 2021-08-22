package org.kie.server.services.jbpm.locator;

import org.hamcrest.MatcherAssert;
import org.junit.Before;
import org.junit.Test;
import org.kie.server.api.KieServerConstants;
import org.kie.server.services.api.ContainerLocator;

import static org.hamcrest.CoreMatchers.instanceOf;

public class ProcessContainerLocatorProviderTest {

    @Before
    public void setUp() {
        System.clearProperty(KieServerConstants.KIE_SERVER_PROCESS_INSTANCE_CONTAINER_LOCATOR);
        ProcessContainerLocatorProvider.get().readSystemProperty();
    }

    @Test
    public void testShouldGetDefaultLocator() {
        ContainerLocator containerLocator = ProcessContainerLocatorProvider.get().getLocator(1L);

        MatcherAssert.assertThat(containerLocator, instanceOf(ByProcessInstanceIdContainerLocator.class));
    }

    @Test
    public void testShouldReturnLocatorSetInProperty() {
        System.setProperty(KieServerConstants.KIE_SERVER_PROCESS_INSTANCE_CONTAINER_LOCATOR,
            ByContextMappingInfoContainerLocator.class.getSimpleName());
        ProcessContainerLocatorProvider.get().readSystemProperty();

        ContainerLocator containerLocator = ProcessContainerLocatorProvider.get().getLocator(1L);

        MatcherAssert.assertThat(containerLocator, instanceOf(ByContextMappingInfoContainerLocator.class));
    }

    @Test
    public void testShouldReturnLocatorFromServices() {
        System.setProperty(KieServerConstants.KIE_SERVER_PROCESS_INSTANCE_CONTAINER_LOCATOR,
            "ContainerLocatorFactoryMock");
        ProcessContainerLocatorProvider.get().readSystemProperty();

        ContainerLocator containerLocator = ProcessContainerLocatorProvider.get().getLocator(1L);

        MatcherAssert.assertThat(containerLocator, instanceOf(ContainerLocatorFactoryMock.ContainerLocatorMock.class));
    }

    @Test(expected = IllegalStateException.class)
    public void testLocatorNotExists() {
        System.setProperty(KieServerConstants.KIE_SERVER_PROCESS_INSTANCE_CONTAINER_LOCATOR, "NotExistsContainerLocator");
        ProcessContainerLocatorProvider.get().readSystemProperty();

        ProcessContainerLocatorProvider.get().getLocator(1L);
    }
}
