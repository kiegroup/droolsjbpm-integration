/*
 * Copyright 2015 JBoss Inc
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

import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.UIServicesClient;
import org.kie.server.client.impl.UIServicesClientImpl;

public class JBPMUIServicesClientBuilder implements KieServicesClientBuilder {

    @Override
    public String getImplementedCapability() {
        return "BPM-UI";
    }

    @Override
    public Map<Class<?>, Object> build(KieServicesConfiguration configuration, ClassLoader classLoader) {

        Map<Class<?>, Object> services = new HashMap<Class<?>, Object>();

        services.put(UIServicesClient.class, new UIServicesClientImpl(configuration, classLoader));

        return services;
    }
}
