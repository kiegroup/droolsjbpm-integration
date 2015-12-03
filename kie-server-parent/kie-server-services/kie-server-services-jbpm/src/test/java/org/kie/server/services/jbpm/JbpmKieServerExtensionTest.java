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

package org.kie.server.services.jbpm;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.executor.ExecutorService;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.model.KieServerConfig;
import org.kie.server.api.model.KieServerConfigItem;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.impl.KieServerImpl;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class JbpmKieServerExtensionTest extends JbpmKieServerExtensionBaseTest {

    @Mock
    private KieServerImpl kieServer;

    @Mock
    private KieServerRegistry kieServerRegistry;

    private JbpmKieServerExtension serverExtension;

    @Before
    public void setUp() throws Exception {
        serverExtension = new JbpmKieServerExtension();
    }

    @Test
    public void testExecutorServiceDisabling() throws Exception {
        KieServerConfigItem configItemTm = new KieServerConfigItem(KieServerConstants.CFG_PERSISTANCE_TM,
                "org.hibernate.service.jta.platform.internal.BitronixJtaPlatform", String.class.getName());
        KieServerConfigItem configItemExecutor = new KieServerConfigItem(KieServerConstants.CFG_EXECUTOR_DISABLED,
                "true", String.class.getName());
        KieServerConfig kieServerConfig = new KieServerConfig();
        kieServerConfig.addConfigItem(configItemTm);
        kieServerConfig.addConfigItem(configItemExecutor);
        when(kieServerRegistry.getConfig()).thenReturn(kieServerConfig);

        serverExtension.init(kieServer, kieServerRegistry);

        ExecutorService executorService = serverExtension.getAppComponents(ExecutorService.class);
        assertNull(executorService);
    }
}
