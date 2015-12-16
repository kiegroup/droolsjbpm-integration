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

import java.util.Arrays;

import org.drools.core.command.impl.GenericCommand;
import org.drools.core.command.runtime.BatchExecutionCommandImpl;
import org.kie.api.command.Command;
import org.kie.api.runtime.ExecutionResults;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.impl.KieContainerCommandServiceImpl;
import org.kie.server.services.impl.KieContainerInstanceImpl;
import org.kie.server.services.impl.KieServerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DroolsKieContainerCommandServiceImpl extends KieContainerCommandServiceImpl {

    private static final Logger logger = LoggerFactory.getLogger(DroolsKieContainerCommandServiceImpl.class);

    private RulesExecutionService rulesExecutionService;

    public DroolsKieContainerCommandServiceImpl(KieServerImpl kieServer, KieServerRegistry context, RulesExecutionService rulesExecutionService) {
        super(kieServer, context);
        this.rulesExecutionService = rulesExecutionService;
    }

    @Override
    public ServiceResponse<String> callContainer(String containerId, String payload, MarshallingFormat marshallingFormat, String classType) {
        if( payload == null ) {
            return new ServiceResponse<String>(ServiceResponse.ResponseType.FAILURE, "Error calling container " + containerId + ". Empty payload. ");
        }
        try {
            KieContainerInstanceImpl kci = (KieContainerInstanceImpl) context.getContainer( containerId );

            if (kci != null && kci.getKieContainer() != null) {

                Class<? extends Command> type =  BatchExecutionCommandImpl.class;
                if (classType != null && !classType.isEmpty()) {
                    type = (Class<? extends Command>) Class.forName(classType, true, kci.getKieContainer().getClassLoader());
                }

                Command<?> cmd = kci.getMarshaller( marshallingFormat ).unmarshall(payload, type);

                if (cmd == null) {
                    return new ServiceResponse<String>(ServiceResponse.ResponseType.FAILURE, "Body of in message not of the expected type '" + Command.class.getName() + "'");
                }
                if (!(cmd instanceof BatchExecutionCommandImpl)) {
                    cmd = new BatchExecutionCommandImpl(Arrays.asList(new GenericCommand<?>[]{(GenericCommand<?>) cmd}));
                }

                ExecutionResults results = rulesExecutionService.call(kci, (BatchExecutionCommandImpl) cmd);
                String result = kci.getMarshaller( marshallingFormat ).marshall(results);
                return new ServiceResponse<String>(ServiceResponse.ResponseType.SUCCESS, "Container " + containerId + " successfully called.", result);
            } else {
                return new ServiceResponse<String>(ServiceResponse.ResponseType.FAILURE, "Container " + containerId + " is not instantiated.");
            }

        } catch (Exception e) {
            logger.error("Error calling container '" + containerId + "'", e);
            return new ServiceResponse<String>(ServiceResponse.ResponseType.FAILURE, "Error calling container " + containerId + ": " + e.getMessage());
        }
    }
}
