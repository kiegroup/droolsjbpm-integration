/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.notification.listeners;

import org.jbpm.services.api.UserTaskService;
import org.jbpm.services.api.service.ServiceRegistry;
import org.jbpm.services.task.deadlines.NotificationListener;
import org.kie.internal.task.api.UserInfo;
import org.kie.internal.task.api.model.NotificationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Collections.singletonMap;

public class CustomNotificationListener implements NotificationListener {

    protected static final Logger logger = LoggerFactory.getLogger(CustomNotificationListener.class);
    
    private UserTaskService userTaskService = (UserTaskService) ServiceRegistry.get().service(ServiceRegistry.USER_TASK_SERVICE);

    @Override
    public void onNotification(NotificationEvent event, UserInfo userInfo) {
        logger.info("onNotification with event content {}", event.getContent());
        try {
            if ("TaskSaveContent".equals(event.getTask().getName())) {
                userTaskService.saveContent(event.getTask().getId(), singletonMap("grade", "E"));
                //User mary claims the task, so state switches from Ready to Reserved after saving content to be checked
                userTaskService.claim(event.getTask().getId(), "mary");
            }
        } catch (Exception e) {
            logger.error("Unexpected exception", e);
        }
    }
}
