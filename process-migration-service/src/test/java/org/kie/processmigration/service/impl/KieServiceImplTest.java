/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.processmigration.service.impl;

import java.net.URL;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.processmigration.model.KieServerConfig;
import org.kie.server.client.CredentialsProvider;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.common.rest.NoEndpointFoundException;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wildfly.swarm.container.config.ConfigViewFactory;

import static org.mockito.Mockito.times;

@RunWith(PowerMockRunner.class)
@PrepareForTest(KieServicesFactory.class)
@PowerMockIgnore("javax.management.*")
public class KieServiceImplTest extends KieServiceImpl {

    private static CountDownLatch countDownLatch;

    public KieServiceImplTest() {
        URL projectConfig = getClass().getClassLoader().getResource("project-test.yml");
        ConfigViewFactory configViewFactory = new ConfigViewFactory(new Properties());
        try {
            configViewFactory.load("test", projectConfig);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        this.configView = configViewFactory.get();
    }

    @Before
    public void init() {
        PowerMockito.mockStatic(KieServicesFactory.class);
        countDownLatch = null;
    }

    @Test
    public void testKieServerClients() throws InterruptedException {
        KieServiceImpl kieService = new KieServiceImplTest();
        Mockito.when(KieServicesFactory.newRestConfiguration(Mockito.anyString(), Mockito.any(CredentialsProvider.class)))
            .thenAnswer(new Answer<KieServicesConfiguration>() {
                private int count = 0;

                @Override
                public KieServicesConfiguration answer(InvocationOnMock invocation) {
                    if (count++ == 1) {
                        return getKieServicesConfig();
                    }
                    throw new NoEndpointFoundException("Mock error");
                }
            });
        Mockito.when(KieServicesFactory.newKieServicesClient(Mockito.any(KieServicesConfiguration.class)))
            .thenReturn(Mockito.mock(KieServicesClient.class));

        countDownLatch = new CountDownLatch(1);
        kieService.loadConfigs();
        countDownLatch.await();


        PowerMockito.verifyStatic(KieServicesFactory.class, times(2));
        KieServicesFactory.newRestConfiguration(Mockito.anyString(), Mockito.any(CredentialsProvider.class));
        PowerMockito.verifyStatic(KieServicesFactory.class);
        KieServicesFactory.newKieServicesClient(Mockito.any(KieServicesConfiguration.class));
        Collection<KieServerConfig> configs = kieService.getConfigs();
        Assert.assertNotNull(configs);
        Assert.assertEquals(1, configs.size());
        KieServerConfig config = configs.iterator().next();
        Assert.assertNotNull(config.getClient());
    }

    private KieServicesConfiguration getKieServicesConfig() {
        return Mockito.mock(KieServicesConfiguration.class);
    }

    @Override
    void retryConnection(KieServerConfig kieConfig) {
        executorService.schedule(new MockKieServerClientConnector(kieConfig), 500, TimeUnit.MILLISECONDS);
    }

    class MockKieServerClientConnector extends KieServerClientConnector {

        private MockKieServerClientConnector(KieServerConfig kieConfig) {
            super(kieConfig);
        }

        @Override
        public void run() {
            super.run();
            countDownLatch.countDown();
        }
    }
}
