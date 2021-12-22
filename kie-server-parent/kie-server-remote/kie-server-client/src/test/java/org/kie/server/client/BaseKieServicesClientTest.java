/*
 * Copyright 2015 - 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.client;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Collections;

import org.junit.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.tomakehurst.wiremock.junit.WireMockRule;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

public abstract class BaseKieServicesClientTest {
    private static Logger logger = LoggerFactory.getLogger(BaseKieServicesClientTest.class);

    private final   int    port              = findFreePort();
    private final int sslPort = findFreePort();
    protected final String mockServerBaseUri = "http://localhost:" + port;
    protected final String mockSecureServerBaseUri = "https://localhost:" + sslPort;
    protected final KieServicesConfiguration config;
    protected final KieServicesConfiguration sslConfig;

    public BaseKieServicesClientTest() {
        try {
            config = KieServicesFactory.newRestConfiguration( mockServerBaseUri, null, null );
            // set capabilities so the client will not to try to get them from the server
            config.setCapabilities(Collections.emptyList());

            sslConfig = KieServicesFactory.newRestConfiguration(mockSecureServerBaseUri, null, null);
            config.setCapabilities(Collections.emptyList());
        } catch ( Exception e ) {
            // nothing to do.
            throw new RuntimeException( "Error instantiating configuration", e );
        }
    }

    // we need the rule (and thus the mock server) per class so that the tests can be executed in parallel without affecting
    // one another. That means each test class needs it own port that the server listens on
    @Rule
    public WireMockRule wireMockRule = new WireMockRule(wireMockConfig()
            .port(port)
            .httpsPort(sslPort)
            .needClientAuth(true)
            .keystorePath("src/test/resources/2wayssl/wiremock.jks")
            .keystorePassword("password")
            .trustStorePath("src/test/resources/2wayssl/server-truststore.jks")
            .trustStorePassword("password")
    );

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
