/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.server.remote.rest.jbpm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import org.jbpm.services.api.DefinitionService;
import org.jbpm.services.api.DeploymentService;
import org.jbpm.services.api.ProcessService;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.UserTaskService;
import org.jbpm.services.api.query.QueryService;
import org.junit.Test;
import org.kie.internal.executor.api.ExecutorService;
import org.kie.server.api.model.KieServerConfig;
import org.kie.server.remote.rest.jbpm.admin.ProcessAdminResource;
import org.kie.server.remote.rest.jbpm.admin.UserTaskAdminResource;
import org.kie.server.services.api.KieServerApplicationComponentsService;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.api.SupportedTransports;
import org.kie.server.services.jbpm.JbpmKieServerExtension;
import org.mockito.Answers;
import org.mockito.Mockito;

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
                mock(ExecutorService.class),
                mock(QueryService.class),
                mock(KieServerRegistry.class, Mockito.RETURNS_MOCKS)
                };
        for( KieServerApplicationComponentsService appComponentsService : appComponentsServices ) {
            appComponentsList.addAll(appComponentsService.getAppComponents(
                    JbpmKieServerExtension.EXTENSION_NAME,
                    SupportedTransports.REST, services));
        }

        int numComponents = 9;
        assertEquals("Unexpected num application components!", numComponents, appComponentsList.size());
        for( Object appComponent : appComponentsList ) {
            assertTrue("Unexpected app component type: " + Object.class.getSimpleName(),
                    appComponent instanceof ProcessResource
                    || appComponent instanceof RuntimeDataResource
                    || appComponent instanceof DefinitionResource
                    || appComponent instanceof UserTaskResource
                    || appComponent instanceof ExecutorResource
                    || appComponent instanceof QueryDataResource
                    || appComponent instanceof DocumentResource
                    || appComponent instanceof ProcessAdminResource
                    || appComponent instanceof UserTaskAdminResource
                    );
        }
    }

}
