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

package org.kie.server.controller.service;

import java.util.ServiceLoader;

import org.kie.server.controller.api.storage.KieServerTemplateStorage;
import org.kie.server.controller.rest.RestSpecManagementServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StandaloneSpecManagementServiceImpl extends RestSpecManagementServiceImpl {
    private static Logger logger = LoggerFactory.getLogger(StandaloneSpecManagementServiceImpl.class);

    public StandaloneSpecManagementServiceImpl() {
        super();
        ServiceLoader<KieServerTemplateStorage> templateStorages = ServiceLoader.load(KieServerTemplateStorage.class);
        if (templateStorages != null && templateStorages.iterator().hasNext()) {
            KieServerTemplateStorage storage = templateStorages.iterator().next();
            this.setTemplateStorage(storage);
            logger.debug("Setting template storage for SpecManagementService to {}",storage.getClass().getName());
        }
    }
}
