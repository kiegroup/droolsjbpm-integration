/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.camel;

import java.io.IOException;
import java.net.ServerSocket;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import org.apache.camel.test.junit4.CamelTestSupport;
import org.junit.Ignore;
import org.junit.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Ignore
public class BaseKieComponentTest extends CamelTestSupport {
    private static Logger logger = LoggerFactory.getLogger( BaseKieComponentTest.class );

    private final   int    port              = findFreePort();
    protected final String mockServerBaseUri = "http://localhost:" + port;

    public BaseKieComponentTest() { }

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

    protected String getAuthenticadUrl(String username, String password) {
        return "http://" + username + ":" + password + "@localhost:" + port;
    }
}
