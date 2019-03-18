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
package org.kie.server.controller.common;

import java.util.ServiceLoader;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.kie.server.controller.api.service.NotificationService;
import org.kie.server.controller.api.service.NotificationServiceFactory;
import org.kie.server.controller.api.service.PersistingServerTemplateStorageService;
import org.kie.server.controller.api.storage.KieServerTemplateStorage;
import org.kie.server.controller.impl.KieServerHealthCheckControllerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebListener
public class KieControllerServletListener implements ServletContextListener {

    private static final Logger logger = LoggerFactory.getLogger(KieControllerServletListener.class);
    private KieServerTemplateStorage storage;
    private NotificationService notificationService;
    private KieServerHealthCheckControllerImpl healthCheck;
    private ExecutorService executorService = Executors.newSingleThreadExecutor();

    public KieControllerServletListener() {
        healthCheck = new KieServerHealthCheckControllerImpl();
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent event) {
        if (storage != null) {
            storage.close();
            
            logger.debug("Template storage {} closed successfully", storage);
        }
        healthCheck.stop();
        executorService.shutdownNow();
    }

    @Override
    public void contextInitialized(ServletContextEvent event) {
        ServiceLoader<PersistingServerTemplateStorageService> storageServices = ServiceLoader.load(PersistingServerTemplateStorageService.class);
        
        if (storageServices != null && storageServices.iterator().hasNext()) {
            PersistingServerTemplateStorageService storageService = storageServices.iterator().next();
            storage = storageService.getTemplateStorage(); 
            
            logger.debug("Template storage {} initialized successfully", storage);
        }

        ServiceLoader<NotificationServiceFactory> notificationServiceLoader = ServiceLoader.load(NotificationServiceFactory.class);
        if (notificationServiceLoader != null && notificationServiceLoader.iterator().hasNext()) {
            notificationService = notificationServiceLoader.iterator().next().getNotificationService();

            logger.debug("Notification service {} initialized successfully", notificationService.toString());
        } 
        healthCheck.setExecutorService(executorService);
        healthCheck.setNotificationService(notificationService);
        healthCheck.setTemplateStorage(storage);
        healthCheck.start();
    }

}
