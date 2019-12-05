package org.kie.server.integrationtests.controller;

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.client.jaxrs.BasicAuthentication;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.integrationtests.shared.basetests.KieServerBaseIntegrationTest;

public class RestOnlyBaseIntegrationOneTest extends KieServerBaseIntegrationTest {


    public MarshallingFormat marshallingFormat = MarshallingFormat.JAXB;

    public KieServicesConfiguration configuration = createKieServicesRestConfiguration();

    @Override
    protected KieServicesClient createDefaultClient() throws Exception {
        if (TestConfig.isLocalServer()) {
            configuration = KieServicesFactory.newRestConfiguration(TestConfig.getKieServerHttpUrl(), null, null);
        }
        return createDefaultClient(configuration, marshallingFormat);
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

    /**
     * Change user used by client.
     *
     * @param username Name of user, default user taken from TestConfig in case of null parameter.
     */
    protected void changeUser(String username, String password) throws Exception {
        if(username == null) {
            username = TestConfig.getUsername();
        }
        if(password == null) {
            password = TestConfig.getPassword();
        }
        configuration.setUserName(username);
        configuration.setPassword(password);
        client = createDefaultClient();
        closeHttpClient();
    }

    private static Client httpClient;

    protected WebTarget newRequest(String uriString) {
        if(httpClient == null) {
            httpClient = new ResteasyClientBuilder()
                    .establishConnectionTimeout(10, TimeUnit.SECONDS)
                    .socketTimeout(10, TimeUnit.SECONDS)
                    .build();
        }
        WebTarget webTarget = httpClient.target(uriString);
        webTarget.register(new BasicAuthentication(configuration.getUserName(), configuration.getPassword()));
        return webTarget;
    }

    private void closeHttpClient() {
        httpClient.close();
        httpClient = null;
    }

    protected <T> Entity<T> createEntity(T requestObject) {
        return Entity.entity(requestObject, getMediaType());
    }

    protected static KieServicesConfiguration createKieServicesRestConfiguration() {
        return KieServicesFactory.newRestConfiguration(TestConfig.getKieServerHttpUrl(), TestConfig.getUsername(), TestConfig.getPassword());
    }
}
