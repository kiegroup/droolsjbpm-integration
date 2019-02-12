/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.integrationtests.shared.basetests;

import java.util.concurrent.TimeUnit;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.client.jaxrs.BasicAuthentication;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.integrationtests.config.TestConfig;

public abstract class RestTextOnlyBaseIntegrationTest extends KieServerBaseIntegrationTest {

    private MarshallingFormat marshallingFormat = MarshallingFormat.XSTREAM;
    private KieServicesConfiguration configuration = KieServicesFactory.newRestConfiguration(TestConfig.getKieServerHttpUrl(), TestConfig.getUsername(), TestConfig.getPassword());

    @Override
    protected KieServicesClient createDefaultClient() throws Exception {
        return createDefaultClient(configuration, marshallingFormat);
    }

    protected MediaType getMediaType() {
        return MediaType.TEXT_PLAIN_TYPE;
    }

    private static Client httpClient;

    protected WebTarget newRequest(String uriString) {
        if (httpClient == null) {
            httpClient = new ResteasyClientBuilder()
                    .establishConnectionTimeout(10, TimeUnit.SECONDS)
                    .socketTimeout(10, TimeUnit.SECONDS)
                    .build();
        }
        WebTarget webTarget = httpClient.target(uriString);
        webTarget.register(new BasicAuthentication(configuration.getUserName(), configuration.getPassword()));
        return webTarget;
    }
}
