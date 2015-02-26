package org.kie.server.integrationtests.shared;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.integrationtests.KieServerBaseIntegrationTest;

import javax.ws.rs.core.MediaType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public abstract class RestJmsSharedBaseIntegrationTest extends KieServerBaseIntegrationTest {
    
    @Parameterized.Parameters(name = "{index}: {0} {1}")
    public static Collection<Object[]> data() throws Exception {
        KieServicesConfiguration restConfiguration = createKieServicesRestConfiguration();

        Collection<Object[]> parameterData = new ArrayList<Object[]>(Arrays.asList(new Object[][]
                        {
                                {MediaType.APPLICATION_XML_TYPE, restConfiguration},
                                {MediaType.APPLICATION_JSON_TYPE, restConfiguration},
                        }
        ));
                

        if (PROVIDER_URL != null) {
            KieServicesConfiguration jmsConfiguration = createKieServicesJmsConfiguration();
            parameterData.addAll(Arrays.asList(new Object[][]
                            {
                                    {MediaType.APPLICATION_XML_TYPE, jmsConfiguration},
                                    {MediaType.APPLICATION_JSON_TYPE, jmsConfiguration}
                            })
            );
        }

        return parameterData;
    }

    @Parameterized.Parameter(0)
    public MediaType MEDIA_TYPE;

    @Parameterized.Parameter(1)
    public KieServicesConfiguration configuration;

    protected KieServicesClient createDefaultClient() throws Exception {
        if (LOCAL_SERVER) {
            return KieServicesFactory.newKieServicesRestClient(BASE_HTTP_URL, null, null);
        } else {
            return KieServicesFactory.newKieServicesClient(configuration);
        }
    }
    
}
