package org.kie.server.client;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.junit.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ServerSocket;

public abstract class BaseKieServicesClientTest {
    private static Logger logger = LoggerFactory.getLogger(BaseKieServicesClientTest.class);

    private final   int    port              = findFreePort();
    protected final String mockServerBaseUri = "http://localhost:" + port;
    protected final KieServicesConfiguration config;

    public BaseKieServicesClientTest() {
        try {
            config = KieServicesFactory.newRestConfiguration( mockServerBaseUri, null, null );
        } catch ( Exception e ) {
            // nothing to do.
            throw new RuntimeException( "Error instantiating configuration", e );
        }
    }

    // we need the rule (and thus the mock server) per class so that the tests can be executed in parallel without affecting
    // one another. That means each test class needs it own port that the server listens on
    @Rule
    public WireMockRule wireMockRule = new WireMockRule( port );

    public static int findFreePort() {
        int port = 0;
        try {
            ServerSocket server =
                    new ServerSocket( 0 );
            port = server.getLocalPort();
            server.close();
        } catch ( IOException e ) {
            // failed to dynamically allocate port, try to use hard coded one
            port = 9789;
        }
        logger.debug("Allocating port: " + port);
        return port;
    }
}
