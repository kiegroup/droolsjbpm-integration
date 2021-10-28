/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.controller.impl.client;

import org.kie.server.api.KieServerConstants;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.client.KieServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.KieServicesFactory;
import org.kie.server.client.credentials.EnteredTokenCredentialsProvider;
import org.kie.server.common.KeyStoreHelperUtil;

public class RestKieServicesClientProvider implements KieServicesClientProvider {

    @Override
    public boolean supports(String url) {        
        return url.toLowerCase().startsWith("http");
    }

    @Override
    public KieServicesClient get(String url) {
        KieServicesConfiguration configuration = KieServicesFactory.newRestConfiguration(url, getUser(), getPassword());
        configuration.setTimeout(getTimeout());

        configuration.setMarshallingFormat(MarshallingFormat.JSON);

        String authToken = getToken();
        if (authToken != null && !authToken.isEmpty()) {
            configuration.setCredentialsProvider(new EnteredTokenCredentialsProvider(authToken));
        }

        KieServicesClient kieServicesClient =  KieServicesFactory.newKieServicesClient(configuration);

        return kieServicesClient;
    }


    protected String getUser() {
        return System.getProperty(KieServerConstants.CFG_KIE_USER, "kieserver");
    }

    protected String getPassword() {
        return KeyStoreHelperUtil.loadServerPassword();
    }

    protected String getToken() {
        return System.getProperty(KieServerConstants.CFG_KIE_TOKEN);
    }

    protected Long getTimeout()  {
        try {
           return Long.parseLong(System.getProperty(KieServerConstants.CFG_KIE_CONTROLLER_TIMEOUT, "60000"));
        } catch (NumberFormatException ex) {
           throw new RuntimeException("An invalid value is specified for system property "+KieServerConstants.CFG_KIE_CONTROLLER_TIMEOUT, ex);
        }
    }

    @Override
    public Integer getPriority() {
        return 100;
    }
}
