/*
 * Copyright 2015 JBoss Inc
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
