package org.kie.server.remote.rest.drools;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;
import org.kie.server.remote.rest.drools.CommandResource;
import org.kie.server.services.api.SupportedTransports;
import org.kie.server.services.drools.DroolsKieServerExtension;

public class DroolsApplicationComponentsServiceTest {

    @Test
    public void createResources() {
        DroolsKieServerExtension extension = new DroolsKieServerExtension();
        extension.init(null, null);
        List<Object> appComponentsList = extension.getAppComponents(SupportedTransports.REST);

        assertFalse("No application component retrieved!", appComponentsList.isEmpty());
        Object appComponent = appComponentsList.get(0);
        assertTrue("Expected a " + CommandResource.class.getSimpleName() + " instance",
                appComponent instanceof CommandResource);
    }

}
