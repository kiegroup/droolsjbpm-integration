package org.kie.server.remote.rest.jbpm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import org.jbpm.services.api.DefinitionService;
import org.jbpm.services.api.DeploymentService;
import org.jbpm.services.api.ProcessService;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.UserTaskService;
import org.junit.Test;
import org.kie.internal.executor.api.ExecutorService;
import org.kie.server.services.api.KieServerApplicationComponentsService;
import org.kie.server.services.api.SupportedTransports;
import org.kie.server.services.jbpm.JbpmKieServerExtension;

public class JbpmApplicationComponentsServiceTest {

    @Test
    public void createResources() {
        ServiceLoader<KieServerApplicationComponentsService> appComponentsServices
            = ServiceLoader .load(KieServerApplicationComponentsService.class);
        List<Object> appComponentsList = new ArrayList<Object>();
        Object[] services = {
                mock(DeploymentService.class),
                mock(DefinitionService.class),
                mock(ProcessService.class),
                mock(UserTaskService.class),
                mock(RuntimeDataService.class),
                mock(ExecutorService.class)
                };
        for( KieServerApplicationComponentsService appComponentsService : appComponentsServices ) {
            appComponentsList.addAll(appComponentsService.getAppComponents(
                    JbpmKieServerExtension.EXTENSION_NAME,
                    SupportedTransports.REST, services));
        }

        int numComponents = 3;
        assertEquals("Unexpected num application components!", numComponents, appComponentsList.size());
        for( Object appComponent : appComponentsList ) {
            assertTrue("Unexpected app component type: " + Object.class.getSimpleName(),
                    appComponent instanceof ProcessResource
                    || appComponent instanceof RuntimeDataServiceResource
                    || appComponent instanceof DefinitionServiceResource
                    );
        }
    }

}
