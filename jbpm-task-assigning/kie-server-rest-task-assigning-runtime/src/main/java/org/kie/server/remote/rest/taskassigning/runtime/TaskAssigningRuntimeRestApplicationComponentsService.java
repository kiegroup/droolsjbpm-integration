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

package org.kie.server.remote.rest.taskassigning.runtime;

import java.util.Collection;
import java.util.Collections;

import org.kie.server.services.api.KieServerApplicationComponentsService;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.api.SupportedTransports;
import org.kie.server.services.taskassigning.runtime.TaskAssigningRuntimeKieServerExtension;
import org.kie.server.services.taskassigning.runtime.TaskAssigningRuntimeServiceBase;

public class TaskAssigningRuntimeRestApplicationComponentsService implements KieServerApplicationComponentsService {

    private static final String OWNER_EXTENSION = TaskAssigningRuntimeKieServerExtension.EXTENSION_NAME;

    @Override
    public Collection<Object> getAppComponents(String extension, SupportedTransports type, Object... services) {
        if (!OWNER_EXTENSION.equals(extension)) {
            return Collections.emptyList();
        }
        TaskAssigningRuntimeServiceBase taskAssigningRuntimeServiceBase = null;
        KieServerRegistry context = null;

        for (Object object : services) {
            // in case given service is null (meaning was not configured) continue with next one
            if (object == null) {
                continue;
            }
            if (TaskAssigningRuntimeServiceBase.class.isAssignableFrom(object.getClass())) {
                taskAssigningRuntimeServiceBase = (TaskAssigningRuntimeServiceBase) object;
                continue;
            } else if (KieServerRegistry.class.isAssignableFrom(object.getClass())) {
                context = (KieServerRegistry) object;
                continue;
            }
        }

        return Collections.singleton(new TaskAssigningRuntimeResource(taskAssigningRuntimeServiceBase, context));
    }
}
