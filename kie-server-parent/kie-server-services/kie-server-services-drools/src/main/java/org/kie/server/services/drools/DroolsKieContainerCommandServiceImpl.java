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

package org.kie.server.services.drools;

import org.drools.core.command.impl.ExecutableCommand;
import org.drools.core.command.runtime.BatchExecutionCommandImpl;
import org.kie.api.command.Command;
import org.kie.api.runtime.ExecutionResults;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.impl.KieContainerCommandServiceImpl;
import org.kie.server.services.impl.KieContainerInstanceImpl;
import org.kie.server.services.impl.KieServerImpl;
import org.kie.server.services.impl.locator.ContainerLocatorProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

public class DroolsKieContainerCommandServiceImpl extends KieContainerCommandServiceImpl {

    private static final Logger logger = LoggerFactory.getLogger(DroolsKieContainerCommandServiceImpl.class);

    private RulesExecutionService rulesExecutionService;

    public DroolsKieContainerCommandServiceImpl(KieServerImpl kieServer, KieServerRegistry context, RulesExecutionService rulesExecutionService) {
        super(kieServer, context);
        this.rulesExecutionService = rulesExecutionService;
    }

    @Override
    public ServiceResponse<ExecutionResults> callContainer(String containerId, String payload, MarshallingFormat marshallingFormat, String classType) {
        if( payload == null ) {
            return new ServiceResponse<ExecutionResults>(ServiceResponse.ResponseType.FAILURE, "Error calling container " + containerId + ". Empty payload. ");
        }
        try {
            KieContainerInstanceImpl kci = (KieContainerInstanceImpl) context.getContainer( containerId, ContainerLocatorProvider.get().getLocator());

            if (kci != null && kci.getKieContainer() != null) {

                Class<? extends Command> type =  BatchExecutionCommandImpl.class;
                if (classType != null && !classType.isEmpty()) {
                    type = (Class<? extends Command>) kci.getKieContainer().getClassLoader().loadClass(classType);
                }

                Command<?> cmd = kci.getMarshaller( marshallingFormat ).unmarshall(payload, type);

                if (!(cmd instanceof BatchExecutionCommandImpl)) {
                    cmd = new BatchExecutionCommandImpl(Arrays.asList(new ExecutableCommand<?>[]{(ExecutableCommand<?>) cmd} ));
                }

                if (cmd == null || ((BatchExecutionCommandImpl)cmd).getCommands() == null || ((BatchExecutionCommandImpl)cmd).getCommands().isEmpty()) {
                    return new ServiceResponse<ExecutionResults>(ServiceResponse.ResponseType.FAILURE, "Bad request, no commands to be executed - either wrong format or no data");
                }

                ExecutionResults results = rulesExecutionService.call(kci, (BatchExecutionCommandImpl) cmd);
                return new ServiceResponse<ExecutionResults>(ServiceResponse.ResponseType.SUCCESS, "Container " + containerId + " successfully called.", results);
            } else {
                return new ServiceResponse<ExecutionResults>(ServiceResponse.ResponseType.FAILURE, "Container " + containerId + " is not instantiated.");
            }

        } catch (Exception e) {
            logger.error("Error calling container '" + containerId + "'", e);
            return new ServiceResponse<ExecutionResults>(ServiceResponse.ResponseType.FAILURE, "Error calling container " + containerId + ": " + e.getMessage());
        }
    }
}
