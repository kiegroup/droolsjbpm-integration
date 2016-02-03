/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.controller.impl.service;

import java.util.List;

import org.kie.server.controller.api.model.events.ServerInstanceDeleted;
import org.kie.server.controller.api.model.events.ServerInstanceUpdated;
import org.kie.server.controller.api.model.events.ServerTemplateDeleted;
import org.kie.server.controller.api.model.events.ServerTemplateUpdated;
import org.kie.server.controller.api.model.runtime.Container;
import org.kie.server.controller.api.model.spec.ContainerSpec;
import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.kie.server.controller.api.service.NotificationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoggingNotificationService implements NotificationService {

    private static final Logger logger = LoggerFactory.getLogger(LoggingNotificationService.class);

    private static LoggingNotificationService INSTANCE = new LoggingNotificationService();

    protected LoggingNotificationService() {

    }

    public static LoggingNotificationService getInstance() {
        return INSTANCE;
    }

    @Override
    public void notify(ServerTemplate serverTemplate, ContainerSpec containerSpec, List<Container> containers) {
         logger.info("Notification about change requested on server {} with container spec {} with following result {}", serverTemplate, containerSpec, containers);
    }

    @Override
    public void notify(ServerTemplateUpdated serverTemplateUpdated) {
        logger.info("Notify on update :: {}", serverTemplateUpdated);
    }

    @Override
    public void notify(ServerTemplateDeleted serverTemplateDeleted) {
        logger.info("Notify on delete :: {}", serverTemplateDeleted);
    }

    @Override
    public void notify(ServerInstanceUpdated serverInstanceUpdated) {
        logger.info("Notify on instance update :: {}", serverInstanceUpdated);
    }

    @Override
    public void notify(ServerInstanceDeleted serverInstanceDeleted) {
        logger.info("Notify on instance delete :: {}", serverInstanceDeleted);
    }
}
