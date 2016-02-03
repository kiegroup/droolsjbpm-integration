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

package org.kie.server.client.helper;

import org.kie.server.api.KieServerConstants;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.RuleServicesClient;
import org.kie.server.client.SolverServicesClient;
import org.kie.server.client.impl.RuleServicesClientImpl;
import org.kie.server.client.impl.SolverServicesClientImpl;

import java.util.HashMap;
import java.util.Map;

public class OptaplannerServicesClientBuilder
        implements KieServicesClientBuilder {

    @Override
    public String getImplementedCapability() {
        return KieServerConstants.CAPABILITY_BRP;
    }

    @Override
    public Map<Class<?>, Object> build(KieServicesConfiguration configuration, ClassLoader classLoader) {
        Map<Class<?>, Object> services = new HashMap<Class<?>, Object>();

        services.put(SolverServicesClient.class, new SolverServicesClientImpl(configuration, classLoader));

        return services;
    }
}
