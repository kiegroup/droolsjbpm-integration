/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.controller.websocket.management;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;

import org.apache.commons.lang3.reflect.MethodUtils;
import org.kie.server.api.model.Wrapped;
import org.kie.server.api.model.KieServiceResponse.ResponseType;
import org.kie.server.controller.api.commands.KieServerControllerDescriptorCommand;
import org.kie.server.controller.api.model.KieServerControllerServiceResponse;
import org.kie.server.controller.api.service.NotificationService;
import org.kie.server.controller.api.service.NotificationServiceFactory;
import org.kie.server.controller.api.service.PersistingServerTemplateStorageService;
import org.kie.server.controller.api.service.RuleCapabilitiesService;
import org.kie.server.controller.api.service.RuntimeManagementService;
import org.kie.server.controller.api.service.SpecManagementService;
import org.kie.server.controller.impl.service.RuleCapabilitiesServiceImpl;
import org.kie.server.controller.impl.service.RuntimeManagementServiceImpl;
import org.kie.server.controller.impl.service.SpecManagementServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KieServerMgmtCommandServiceImpl implements KieServerMgmtCommandService {

    private static final Logger LOGGER = LoggerFactory.getLogger(KieServerMgmtCommandServiceImpl.class);

    private SpecManagementServiceImpl specManagementService = new SpecManagementServiceImpl();
    private RuntimeManagementServiceImpl runtimeManagementService = new RuntimeManagementServiceImpl();
    private RuleCapabilitiesServiceImpl ruleCapabilitiesService = new RuleCapabilitiesServiceImpl();

    private static KieServerMgmtCommandService INSTANCE = new KieServerMgmtCommandServiceImpl();

    public static KieServerMgmtCommandService getInstance(){
        return INSTANCE;
    }

    private KieServerMgmtCommandServiceImpl() {
        ServiceLoader<PersistingServerTemplateStorageService> storageServices = ServiceLoader.load(PersistingServerTemplateStorageService.class);
        if (storageServices != null && storageServices.iterator().hasNext()) {
            PersistingServerTemplateStorageService storageService = storageServices.iterator().next();
            specManagementService.setTemplateStorage(storageService.getTemplateStorage());
            runtimeManagementService.setTemplateStorage(storageService.getTemplateStorage());
            ruleCapabilitiesService.setTemplateStorage(storageService.getTemplateStorage());
            LOGGER.debug("Server template storage for standalone kie server controller is {}",
                         storageService.getTemplateStorage().toString());
        } else {
            LOGGER.debug("No server template storage defined. Default storage: InMemoryKieServerTemplateStorage will be used");
        }

        ServiceLoader<NotificationServiceFactory> notificationServiceLoader = ServiceLoader.load(NotificationServiceFactory.class);
        if (notificationServiceLoader != null && notificationServiceLoader.iterator().hasNext()) {
            final NotificationService notificationService = notificationServiceLoader.iterator().next().getNotificationService();
            specManagementService.setNotificationService(notificationService);
            ruleCapabilitiesService.setNotificationService(notificationService);

            LOGGER.debug("Notification service for standalone kie server controller is {}",
                         notificationService.toString());
        } else {
            LOGGER.warn("Notification service not defined. Default notification: LoggingNotificationService will be used");
        }
    }

    @Override
    public KieServerControllerServiceResponse executeCommand(final KieServerControllerDescriptorCommand command) {
        if (command == null) {
            return new KieServerControllerServiceResponse(ResponseType.FAILURE,
                                                          "Command can not be null");
        }

        try {
            Object result = null;
            Object handler = null;
            // find out the handler to call to process given command
            if (SpecManagementService.class.getName().equals(command.getService())) {
                handler = specManagementService;
            } else if (RuntimeManagementService.class.getName().equals(command.getService())) {
                handler = runtimeManagementService;
            } else if (RuleCapabilitiesService.class.getName().equals(command.getService())) {
                handler = ruleCapabilitiesService;
            } else {
                throw new IllegalStateException("Unable to find handler for " + command.getService() + " service");
            }

            LOGGER.debug("Service handler: {}",
                         handler);
            LOGGER.debug("Command arguments size: {}",
                         command.getArguments().size());

            List<Object> arguments = new ArrayList<>();
            // process and unwrap arguments
            for (Object arg : command.getArguments()) {
                LOGGER.debug("Before :: Argument with type {} and value {}",
                             arg == null ? "null" : arg.getClass(),
                             arg);
                if (arg instanceof Wrapped) {
                    arg = ((Wrapped) arg).unwrap();
                }
                LOGGER.debug("After :: Argument with type {} and value {}",
                             arg == null ? "null" : arg.getClass(),
                             arg);
                arguments.add(arg);
            }

            LOGGER.debug("About to execute {} operation on {} with args {}",
                         command.getMethod(),
                         handler,
                         arguments);

            // process command via reflection and handler
            result = MethodUtils.invokeMethod(handler,
                                              command.getMethod(),
                                              arguments.toArray());
            LOGGER.debug("Handler {} returned response {}",
                         handler,
                         result);
            // return successful result
            return new KieServerControllerServiceResponse(ResponseType.SUCCESS,
                                                          "",
                                                          result);
        } catch (InvocationTargetException e) {
            LOGGER.error("Failed to invoke service method",
                         e);
            return new KieServerControllerServiceResponse(ResponseType.FAILURE,
                                                          e.getTargetException().getMessage());
        } catch (Throwable e) {
            LOGGER.error("Error while processing {} command",
                         command,
                         e);
            // return failure result
            return new KieServerControllerServiceResponse(ResponseType.FAILURE,
                                                          e.getMessage());
        }
    }
}
