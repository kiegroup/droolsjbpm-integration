/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.services.jbpm;

import org.jbpm.services.api.query.QueryService;
import org.junit.Test;
import org.kie.server.api.KieServerEnvironment;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.impl.KieServerRegistryImpl;
import org.kie.server.services.impl.storage.file.KieServerStateFileRepository;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

import java.io.File;
import java.util.UUID;

public class JbpmKieServerExtensionTest {

    @Test
    public void testLoadDefaultQueryDefinitions() {
        KieServerEnvironment.setServerId(UUID.randomUUID().toString());
        QueryService queryService = Mockito.mock(QueryService.class);
        
        KieServerRegistry context = new KieServerRegistryImpl();
        context.registerStateRepository(new KieServerStateFileRepository(new File("target")));
        
        JbpmKieServerExtension extension = new JbpmKieServerExtension();
        extension.setQueryService(queryService);
        extension.setContext(context);
        
        extension.registerDefaultQueryDefinitions();
        
        verify(queryService, times(10)).replaceQuery(any());
    }
}
