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

package org.kie.server.services.taskassigning.planning;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import org.kie.server.services.taskassigning.user.system.api.UserSystemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserSystemServiceLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserSystemServiceLoader.class);

    private UserSystemServiceLoader() {
    }

    public static Map<String, UserSystemService> loadServices(ClassLoader cl) {
        final Map<String, UserSystemService> result = new HashMap<>();
        final ServiceLoader<UserSystemService> availableServices = ServiceLoader.load(UserSystemService.class, cl);
        for (UserSystemService service : availableServices) {
            Object existed = result.putIfAbsent(service.getName(), service);
            if (existed == null) {
                LOGGER.debug("UserSystemService {} was added to the result", service.getName());
            } else {
                LOGGER.warn("UserSystemService {} was already added to the result, this another instance will be discarded.", service);
            }
        }
        return result;
    }
}
