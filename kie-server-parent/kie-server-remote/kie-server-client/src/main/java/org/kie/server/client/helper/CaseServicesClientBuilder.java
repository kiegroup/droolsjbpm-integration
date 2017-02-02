/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.client.helper;

import java.util.HashMap;
import java.util.Map;

import org.kie.server.api.KieServerConstants;
import org.kie.server.client.CaseServicesClient;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.admin.CaseAdminServicesClient;
import org.kie.server.client.admin.impl.CaseAdminServicesClientImpl;
import org.kie.server.client.impl.CaseServicesClientImpl;

public class CaseServicesClientBuilder implements KieServicesClientBuilder {

    @Override
    public String getImplementedCapability() {
        return KieServerConstants.CAPABILITY_CASE;
    }

    @Override
    public Map<Class<?>, Object> build(KieServicesConfiguration configuration, ClassLoader classLoader) {

        Map<Class<?>, Object> services = new HashMap<Class<?>, Object>();

        services.put(CaseServicesClient.class, new CaseServicesClientImpl(configuration, classLoader));
        // admin clients
        services.put(CaseAdminServicesClient.class, new CaseAdminServicesClientImpl(configuration, classLoader));

        return services;
    }
}
