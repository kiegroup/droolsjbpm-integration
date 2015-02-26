package org.kie.server.integrationtests.rest;

import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.core.executors.ApacheHttpClient4Executor;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.integrationtests.KieServerBaseIntegrationTest;

import javax.ws.rs.core.MediaType;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collection;

@RunWith(Parameterized.class)
public abstract class RestOnlyBaseIntegrationTest extends KieServerBaseIntegrationTest {

    @Parameterized.Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{{MediaType.APPLICATION_XML_TYPE}, {MediaType.APPLICATION_JSON_TYPE}});
    }

    @Parameterized.Parameter
    public MediaType MEDIA_TYPE;

    public KieServicesConfiguration configuration;

    protected ClientRequest newRequest(String uriString) {
        URI uri;
        try {
            uri = new URI(uriString);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Malformed request URI was specified: '" + uriString + "'!", e);
        }
        if (LOCAL_SERVER) {
            return new ClientRequest(uriString);
        } else {
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(
                    new AuthScope(uri.getHost(), uri.getPort()),
                    new UsernamePasswordCredentials(DEFAULT_USERNAME, DEFAULT_PASSWORD)
            );
            HttpClient client = HttpClientBuilder.create().setDefaultCredentialsProvider(credentialsProvider).build();
            ApacheHttpClient4Executor executor = new ApacheHttpClient4Executor(client);
            return new ClientRequest(uriString, executor);
        }
    }

    @Override
    protected KieServicesClient createDefaultClient() {
        if (LOCAL_SERVER) {
            return KieServicesFactory.newKieServicesRestClient(BASE_HTTP_URL, null, null);
        } else {
            return KieServicesFactory.newKieServicesRestClient(BASE_HTTP_URL, DEFAULT_USERNAME, DEFAULT_PASSWORD);
        }
    }

}
