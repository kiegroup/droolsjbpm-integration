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

package org.kie.server.services.taskassigning.runtime;

import java.util.Collection;
import java.util.Collections;

import org.kie.server.services.api.KieServerApplicationComponentsService;
import org.kie.server.services.api.SupportedTransports;

/**
 * Dummy class intended for the TaskAssigningRuntimeKieServerExtensionTest
 */
public class DummyKieServerApplicationComponentService implements KieServerApplicationComponentsService {

    public class DummyComponent {
    }

    public DummyKieServerApplicationComponentService() {
        //SPI constructor
    }

    @Override
    public Collection<Object> getAppComponents(String extension, SupportedTransports type, Object... services) {
        return TaskAssigningRuntimeKieServerExtension.EXTENSION_NAME.equals(extension) ? Collections.singletonList(new DummyComponent()) : null;
    }
}
