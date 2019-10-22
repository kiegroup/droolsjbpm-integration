/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
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
import org.kie.server.client.TaskAssigningRuntimeClient;
import org.kie.server.client.impl.TaskAssigningRuntimeClientImpl;

import static org.kie.server.api.KieServerConstants.CAPABILITY_TASK_ASSIGNING_RUNTIME;

public class TaskAssigningRuntimeServicesClientBuilder implements KieServicesClientBuilder {

    @Override
    public String getImplementedCapability() {
        return CAPABILITY_TASK_ASSIGNING_RUNTIME;
    }

    @Override
    public Map<Class<?>, Object> build(KieServicesConfiguration configuration, ClassLoader classLoader) {
        final Map<Class<?>, Object> services = new HashMap<>();
        services.put(TaskAssigningRuntimeClient.class, new TaskAssigningRuntimeClientImpl(configuration, classLoader));
        return services;
    }
}
