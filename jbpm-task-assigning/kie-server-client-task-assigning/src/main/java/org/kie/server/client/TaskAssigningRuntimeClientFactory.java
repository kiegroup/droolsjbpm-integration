/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.client;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.taskassigning.LocalDateTimeValue;

import static org.kie.server.api.KieServerConstants.CAPABILITY_BPM;
import static org.kie.server.api.KieServerConstants.CAPABILITY_TASK_ASSIGNING_RUNTIME;

public class TaskAssigningRuntimeClientFactory {

    static {
        // Ensure user bypass is on to be able to e.g. let the client "admin" user to claim/delegate tasks on behalf
        // of other users
        System.setProperty("org.kie.server.bypass.auth.user", Boolean.TRUE.toString());
    }

    static KieServicesClient createKieServicesClient(final String endpoint,
                                                     final String login,
                                                     final String password) {

        final KieServicesConfiguration configuration = KieServicesFactory.newRestConfiguration(endpoint, login, password);
        configuration.setTimeout(90000);
        configuration.setCapabilities(Arrays.asList(CAPABILITY_BPM, CAPABILITY_TASK_ASSIGNING_RUNTIME));
        configuration.setMarshallingFormat(MarshallingFormat.JSON);
        Set<Class<?>> extraClasses = new HashSet<>();
        //JAXB and JSON required.
        extraClasses.add(LocalDateTimeValue.class);
        configuration.setExtraClasses(extraClasses);
        return KieServicesFactory.newKieServicesClient(configuration);
    }

    public static TaskAssigningRuntimeClient newRuntimeClient(final String endpoint,
                                                              final String login,
                                                              final String password) {
        KieServicesClient servicesClient = createKieServicesClient(endpoint, login, password);
        return servicesClient.getServicesClient(TaskAssigningRuntimeClient.class);
    }
}
