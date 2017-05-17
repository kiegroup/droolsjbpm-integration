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
import org.kie.server.controller.impl.storage.FileBasedKieServerTemplateStorage;
import org.kie.server.controller.rest.RestKieServerControllerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StandaloneKieServerControllerImpl extends RestKieServerControllerImpl {
    private static Logger logger = LoggerFactory.getLogger(StandaloneKieServerControllerImpl.class);


    public StandaloneKieServerControllerImpl() {
        super();
        ServiceLoader<KieServerTemplateStorage> storages = ServiceLoader.load(KieServerTemplateStorage.class);
        if (storages != null && storages.iterator().hasNext()) {
            KieServerTemplateStorage storage = storages.iterator().next();
            this.setTemplateStorage(storage);
            logger.debug("Server template storage for standalone kie server controller is {}",storage.getClass().getName());
            if (this.getTemplateStorage() instanceof FileBasedKieServerTemplateStorage) {
                logger.debug("Server template storage location is {}",((FileBasedKieServerTemplateStorage)storage).getTemplatesLocation());
            }
        }
    }

}
