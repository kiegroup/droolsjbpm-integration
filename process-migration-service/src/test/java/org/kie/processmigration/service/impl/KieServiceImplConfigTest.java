package org.kie.processmigration.service.impl;

import java.net.URL;
import java.util.List;
import java.util.Properties;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.server.client.CredentialsProvider;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.common.rest.ClientCertificate;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wildfly.swarm.container.config.ConfigViewFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.times;

@RunWith(PowerMockRunner.class)
@PrepareForTest(KieServicesFactory.class)
@PowerMockIgnore("javax.management.*")
public class KieServiceImplConfigTest extends KieServiceImpl {

    public KieServiceImplConfigTest() {
        URL projectConfig = getClass().getClassLoader().getResource("client-cert.yml");
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
    }

    @Captor
    ArgumentCaptor<KieServicesConfiguration> configCaptor;

    @Test
    public void testClientCertConfig() {
        KieServiceImpl service = new KieServiceImplConfigTest();
        Mockito.when(KieServicesFactory.newRestConfiguration(Mockito.anyString(), Mockito.nullable(CredentialsProvider.class)))
                .thenCallRealMethod();

        service.loadConfigs();
        PowerMockito.verifyStatic(KieServicesFactory.class, times(2));
        KieServicesFactory.newKieServicesClient(configCaptor.capture());

        List<KieServicesConfiguration> values = configCaptor.getAllValues();
        assertEquals(2, values.size());

        assertClientCert(values, 0);
        assertNull(values.get(0).getCredentialsProvider());
        assertClientCert(values, 1);
        assertNotNull(values.get(1).getCredentialsProvider());
    }

    private void assertClientCert(List<KieServicesConfiguration> cfg, int index) {
        assertNotNull(cfg.get(index));
        ClientCertificate current = cfg.get(index).getClientCertificate();
        assertNotNull(current);
        assertEquals("cert" + index, current.getCertName());
        assertEquals("certPwd" + index, current.getCertPassword());
        assertEquals("/path/to/mykeystore" + index + ".jks", current.getKeystore());
        assertEquals("secret" + index, current.getKeystorePassword());
    }
}
