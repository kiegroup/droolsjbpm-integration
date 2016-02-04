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

package org.kie.server.remote.rest.optaplanner;

import org.junit.Test;
import org.kie.server.services.api.KieServerApplicationComponentsService;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.api.SupportedTransports;
import org.kie.server.services.optaplanner.OptaplannerKieServerExtension;
import org.kie.server.services.optaplanner.SolverServiceBase;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

public class OptaplannerApplicationComponentsServiceTest {

    @Test
    public void createResources() {
        ServiceLoader<KieServerApplicationComponentsService> appComponentsServices
                = ServiceLoader.load( KieServerApplicationComponentsService.class );
        List<Object> appComponentsList = new ArrayList<Object>();
        Object[] services = {
                mock( SolverServiceBase.class ),
                mock( KieServerRegistry.class, Mockito.RETURNS_MOCKS )
        };
        for ( KieServerApplicationComponentsService appComponentsService : appComponentsServices ) {
            appComponentsList.addAll(
                    appComponentsService.getAppComponents(
                            OptaplannerKieServerExtension.EXTENSION_NAME,
                            SupportedTransports.REST, services ) );
        }

        int numComponents = 1;
        assertEquals( "Unexpected num application components!", numComponents, appComponentsList.size() );
        for ( Object appComponent : appComponentsList ) {
            assertTrue(
                    "Unexpected app component type: " + Object.class.getSimpleName(),
                    appComponent instanceof SolverResource
            );
        }
    }

}
