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

package org.kie.server.services.jbpm.jpa;

import javax.persistence.spi.PersistenceUnitInfo;

/**
 * Helper class for testing the PersistenceUnitExtensionsLoader mechanism.
 */
public class PersistenceUnitExtensionsLoaderMock implements PersistenceUnitExtensionsLoader {

    public static final String ENABLED_PROPERTY = "org.kie.server.services.jbpm.jpa.PersistenceUnitExtensionsLoaderMock.enabled";

    public static final String ENTITY_MOCK = "org.kie.server.services.jbpm.jpa.PersistenceUnitExtensionsLoaderEntityMock";

    public PersistenceUnitExtensionsLoaderMock() {
        //SPI constructor.
    }

    @Override
    public boolean isEnabled() {
        return "true".equals(System.getProperty(ENABLED_PROPERTY));
    }

    @Override
    public void loadExtensions(PersistenceUnitInfo info) {
        info.getManagedClassNames().add(ENTITY_MOCK);
    }
}
