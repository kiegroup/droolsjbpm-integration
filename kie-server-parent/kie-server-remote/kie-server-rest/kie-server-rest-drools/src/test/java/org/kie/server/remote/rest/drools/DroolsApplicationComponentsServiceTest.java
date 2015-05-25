package org.kie.server.remote.rest.drools;

import java.util.List;

import org.junit.Test;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.api.SupportedTransports;
import org.kie.server.services.drools.DroolsKieServerExtension;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class DroolsApplicationComponentsServiceTest {

    @Test
    public void createResources() {

        DroolsKieServerExtension extension = new DroolsKieServerExtension();
        extension.init(null, mock(KieServerRegistry.class));
        List<Object> appComponentsList = extension.getAppComponents(SupportedTransports.REST);

        assertFalse("No application component retrieved!", appComponentsList.isEmpty());
        Object appComponent = appComponentsList.get(0);
        assertTrue("Expected a " + CommandResource.class.getSimpleName() + " instance",
                appComponent instanceof CommandResource);
    }

}
