package org.kie.server.integrationtests.shared;

import java.util.Arrays;
import java.util.Collection;

import javax.ws.rs.core.MediaType;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.integrationtests.config.TestConfig;

@RunWith(Parameterized.class)
public abstract class RestOnlyBaseIntegrationTest extends KieServerBaseIntegrationTest {

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{{MarshallingFormat.JAXB}, {MarshallingFormat.JSON}});
    }

    @Parameterized.Parameter
    public MarshallingFormat marshallingFormat;

    @Override
    protected KieServicesClient createDefaultClient() {
        KieServicesConfiguration config;
        if (TestConfig.isLocalServer()) {
            config = KieServicesFactory.newRestConfiguration(TestConfig.getHttpUrl(), null, null);
        } else {
            config = KieServicesFactory.newRestConfiguration(TestConfig.getHttpUrl(), TestConfig.getUsername(), TestConfig.getPassword());
        }
        config.setMarshallingFormat(marshallingFormat);
        return KieServicesFactory.newKieServicesClient(config);
    }

    protected MediaType getMediaType() {
        switch (marshallingFormat) {
            case JAXB:
                return MediaType.APPLICATION_XML_TYPE;
            case JSON:
                return MediaType.APPLICATION_JSON_TYPE;
            case XSTREAM:
                return MediaType.APPLICATION_XML_TYPE;
            default:
                throw new RuntimeException("Unrecognized marshalling format: " + marshallingFormat);
        }
    }

}
