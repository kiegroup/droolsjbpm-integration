/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.services.jbpm;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.reflect.MethodUtils;
import org.jbpm.services.api.DeploymentService;
import org.kie.server.api.commands.CommandScript;
import org.kie.server.api.commands.DescriptorCommand;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.marshalling.ModelWrapper;
import org.kie.server.api.model.KieServerCommand;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.ServiceResponsesList;
import org.kie.server.api.model.Wrapped;
import org.kie.server.services.api.KieContainerCommandService;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.jbpm.admin.ProcessAdminServiceBase;
import org.kie.server.services.jbpm.admin.UserTaskAdminServiceBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JBPMKieContainerCommandServiceImpl implements KieContainerCommandService {

    private static final Logger logger = LoggerFactory.getLogger(JBPMKieContainerCommandServiceImpl.class);

    private KieServerRegistry context;

    private DeploymentService deploymentService;
    private DefinitionServiceBase definitionServiceBase;
    private ProcessServiceBase processServiceBase;
    private UserTaskServiceBase userTaskServiceBase;
    private RuntimeDataServiceBase runtimeDataServiceBase;
    private ExecutorServiceBase executorServiceBase;
    private QueryDataServiceBase queryDataServiceBase;
    private DocumentServiceBase documentServiceBase;

    // admin section
    private ProcessAdminServiceBase processAdminServiceBase;
    private UserTaskAdminServiceBase userTaskAdminServiceBase;


    public JBPMKieContainerCommandServiceImpl(KieServerRegistry context, DeploymentService deploymentService,
            DefinitionServiceBase definitionServiceBase, ProcessServiceBase processServiceBase, UserTaskServiceBase userTaskServiceBase,
            RuntimeDataServiceBase runtimeDataServiceBase, ExecutorServiceBase executorServiceBase, QueryDataServiceBase queryDataServiceBase,
            DocumentServiceBase documentServiceBase, ProcessAdminServiceBase processAdminServiceBase, UserTaskAdminServiceBase userTaskAdminServiceBase) {

        this.context = context;
        this.deploymentService = deploymentService;
        this.definitionServiceBase = definitionServiceBase;
        this.processServiceBase = processServiceBase;
        this.userTaskServiceBase = userTaskServiceBase;
        this.runtimeDataServiceBase = runtimeDataServiceBase;
        this.executorServiceBase = executorServiceBase;
        this.queryDataServiceBase = queryDataServiceBase;
        this.documentServiceBase = documentServiceBase;
        this.processAdminServiceBase = processAdminServiceBase;
        this.userTaskAdminServiceBase = userTaskAdminServiceBase;
    }

    @Override
    public ServiceResponse<String> callContainer(String containerId, String payload, MarshallingFormat marshallingFormat, String classType) {
        return null;
    }

    @Override
    public ServiceResponsesList executeScript(CommandScript commands, MarshallingFormat marshallingFormat, String classType) {
        List<ServiceResponse<? extends Object>> responses = new ArrayList<ServiceResponse<? extends Object>>();

        for (KieServerCommand command : commands.getCommands()) {
            if (!(command instanceof DescriptorCommand)) {
                logger.warn("Unsupported command '{}' given, will not process it", command.getClass().getName());
                continue;
            }

            boolean wrapResults = false;
            try {
                Object result = null;
                Object handler = null;

                DescriptorCommand descriptorCommand = (DescriptorCommand) command;
                // find out the handler to call to process given command
                if ("DefinitionService".equals(descriptorCommand.getService())) {
                    handler = definitionServiceBase;
                } else if ("ProcessService".equals(descriptorCommand.getService())) {
                    handler = processServiceBase;
                } else if ("UserTaskService".equals(descriptorCommand.getService())) {
                    handler = userTaskServiceBase;
                } else if ("QueryService".equals(descriptorCommand.getService())) {
                    handler = runtimeDataServiceBase;
                } else if ("JobService".equals(descriptorCommand.getService())) {
                    handler = executorServiceBase;
                } else if ("QueryDataService".equals(descriptorCommand.getService())) {
                    handler = queryDataServiceBase;
                    // enable wrapping as in case of embedded objects jaxb does not properly parse it due to possible unknown types (List<?> etc)
                    if (marshallingFormat.equals(MarshallingFormat.JAXB)) {
                        wrapResults = true;
                    }
                } else if ("DocumentService".equals(descriptorCommand.getService())) {
                    handler = documentServiceBase;
                } else if ("ProcessAdminService".equals(descriptorCommand.getService())) {
                    handler = processAdminServiceBase;
                } else if ("UserTaskAdminService".equals(descriptorCommand.getService())) {
                    handler = userTaskAdminServiceBase;
                } else {
                    throw new IllegalStateException("Unable to find handler for " + descriptorCommand.getService() + " service");
                }

                List<Object> arguments = new ArrayList();
                // process and unwrap arguments
                for (Object arg : descriptorCommand.getArguments()) {
                    logger.debug("Before :: Argument with type {} and value {}", arg.getClass(), arg);
                    if (arg instanceof Wrapped) {
                        arg = ((Wrapped) arg).unwrap();
                    }
                    logger.debug("After :: Argument with type {} and value {}", arg.getClass(), arg);
                    arguments.add(arg);
                }

                if (descriptorCommand.getPayload() != null && !descriptorCommand.getPayload().isEmpty()) {
                    arguments.add(descriptorCommand.getPayload());
                }
                if (descriptorCommand.getMarshallerFormat() != null && !descriptorCommand.getMarshallerFormat().isEmpty()) {
                    arguments.add(descriptorCommand.getMarshallerFormat());
                }

                logger.debug("About to execute {} operation on {} with args {}", descriptorCommand.getMethod(), handler, arguments);
                // process command via reflection and handler
                result = MethodUtils.invokeMethod(handler, descriptorCommand.getMethod(), arguments.toArray());
                logger.debug("Handler {} returned response {}", handler, result);

                if (wrapResults) {
                    result = ModelWrapper.wrap(result);
                    logger.debug("Wrapped response is {}", result);
                }
                // return successful result
                responses.add(new ServiceResponse(ServiceResponse.ResponseType.SUCCESS, "", result));
            } catch (InvocationTargetException e){
                logger.error("Error while processing {} command", command, e);
                responses.add(new ServiceResponse(ServiceResponse.ResponseType.FAILURE, e.getTargetException().getMessage()));
            } catch (Throwable e) {
                logger.error("Error while processing {} command", command, e);
                // return failure result
                responses.add(new ServiceResponse(ServiceResponse.ResponseType.FAILURE, e.getMessage()));
            }
        }
        logger.debug("About to return responses '{}'", responses);
        return new ServiceResponsesList(responses);
    }
}
