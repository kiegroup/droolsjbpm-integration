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

import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.common.rest.Authenticator;
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
    protected KieServicesClient createDefaultClient() throws Exception {
        KieServicesConfiguration config;
        if (TestConfig.isLocalServer()) {
            config = KieServicesFactory.newRestConfiguration(TestConfig.getKieServerHttpUrl(), null, null);
        } else {
            config = KieServicesFactory.newRestConfiguration(TestConfig.getKieServerHttpUrl(), TestConfig.getUsername(), TestConfig.getPassword());
        }
        return createDefaultClient(config, marshallingFormat);
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

    private static Client httpClient;

    protected WebTarget newRequest(String uriString) {

        if(httpClient == null) {
            httpClient = new ResteasyClientBuilder()
                    .establishConnectionTimeout(10, TimeUnit.SECONDS)
                    .socketTimeout(10, TimeUnit.SECONDS)
                    .register(new Authenticator(TestConfig.getUsername(), TestConfig.getPassword()))
                    .build();
        }
        return httpClient.target(uriString);
    }

    protected <T> Entity<T> createEntity(T requestObject) {
        return Entity.entity(requestObject, getMediaType());
    }
}
