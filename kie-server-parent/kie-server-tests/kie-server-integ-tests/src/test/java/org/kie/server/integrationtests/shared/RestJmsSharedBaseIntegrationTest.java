package org.kie.server.integrationtests.shared;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.integrationtests.KieServerBaseIntegrationTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public abstract class RestJmsSharedBaseIntegrationTest extends KieServerBaseIntegrationTest {

    @Parameterized.Parameters(name = "{index}: {0} {1}")
    public static Collection<Object[]> data() {
        KieServicesConfiguration restConfiguration = createKieServicesRestConfiguration();

        Collection<Object[]> parameterData = new ArrayList<Object[]>(Arrays.asList(new Object[][]
                        {
                                {MarshallingFormat.JAXB, restConfiguration},
                                {MarshallingFormat.JSON, restConfiguration},
                        }
        ));


        if (PROVIDER_URL != null) {
            KieServicesConfiguration jmsConfiguration = createKieServicesJmsConfiguration();
            parameterData.addAll(Arrays.asList(new Object[][]
                            {
                                    {MarshallingFormat.JAXB, jmsConfiguration},
                                    {MarshallingFormat.JSON, jmsConfiguration}
                            })
            );
        }

        return parameterData;
    }

    @Parameterized.Parameter(0)
    public MarshallingFormat marshallingFormat;

    @Parameterized.Parameter(1)
    public KieServicesConfiguration configuration;

    protected KieServicesClient createDefaultClient() {
        if (LOCAL_SERVER) {
            KieServicesConfiguration localServerConfig =
                    KieServicesFactory.newRestConfiguration(BASE_HTTP_URL, null, null).setMarshallingFormat(marshallingFormat);
            return KieServicesFactory.newKieServicesClient(localServerConfig);
        } else {
            configuration.setMarshallingFormat(marshallingFormat);
            return KieServicesFactory.newKieServicesClient(configuration);
        }
    }

}
