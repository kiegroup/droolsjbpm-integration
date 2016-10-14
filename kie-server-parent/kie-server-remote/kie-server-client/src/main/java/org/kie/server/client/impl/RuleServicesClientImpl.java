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

package org.kie.server.client.impl;

import java.util.Collections;

import org.drools.core.runtime.impl.ExecutionResultImpl;
import org.kie.api.command.Command;
import org.kie.api.runtime.ExecutionResults;
import org.kie.server.api.commands.CallContainerCommand;
import org.kie.server.api.commands.CommandScript;
import org.kie.server.api.model.KieServerCommand;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.KieServicesConfiguration;
import org.kie.server.client.RuleServicesClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RuleServicesClientImpl extends AbstractKieServicesClientImpl implements RuleServicesClient {

    private static final Logger logger = LoggerFactory.getLogger(RuleServicesClientImpl.class);

    public RuleServicesClientImpl(KieServicesConfiguration config) {
        super(config);
    }

    public RuleServicesClientImpl(KieServicesConfiguration config, ClassLoader classLoader) {
        super(config, classLoader);
    }


    @Override
    public ServiceResponse<ExecutionResults> executeCommandsWithResults(String id, String payload) {
        if( config.isRest() ) {
            return makeHttpPostRequestAndCreateServiceResponse( loadBalancer.getUrl() + "/containers/instances/" + id, payload, (Class) ExecutionResultImpl.class);
        } else {
            CommandScript script = new CommandScript( Collections.singletonList((KieServerCommand) new CallContainerCommand(id, payload)) );
            ServiceResponse response = executeJmsCommand( script, null, null, id ).getResponses().get( 0 );
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            if (response.getResult() instanceof String) {
                response.setResult(deserialize((String) response.getResult(), (Class) ExecutionResultImpl.class));
            }
            return response;
        }
    }


    @Override
    public ServiceResponse<ExecutionResults> executeCommandsWithResults(String id, Command<?> cmd) {
        if( config.isRest() ) {
            return makeHttpPostRequestAndCreateServiceResponse( loadBalancer.getUrl() + "/containers/instances/" + id, cmd, (Class)ExecutionResultImpl.class, getHeaders(cmd) );
        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand) new CallContainerCommand( id, serialize(cmd) ) ) );
            ServiceResponse response = executeJmsCommand( script, cmd.getClass().getName(), null, id ).getResponses().get( 0 );
            if (shouldReturnWithNullResponse(response)) {
                return null;
            }
            if (response.getResult() instanceof String) {
                response.setResult(deserialize((String) response.getResult(), (Class) ExecutionResultImpl.class));
            }
            return response;
        }
    }


    /**
     * @deprecated use #executeCommandsWithResults
     */
    @Deprecated
    @Override
    public ServiceResponse<String> executeCommands(String id, String payload) {
        if( config.isRest() ) {
            return makeBackwardCompatibleHttpPostRequestAndCreateServiceResponse(loadBalancer.getUrl() + "/containers/instances/" + id, payload, String.class);
        } else {
            CommandScript script = new CommandScript( Collections.singletonList((KieServerCommand) new CallContainerCommand(id, payload)) );
            return (ServiceResponse<String>) executeJmsCommand( script, null, null, id ).getResponses().get( 0 );
        }
    }


    /**
     * @deprecated use #executeCommandsWithResults
     */
    @Deprecated
    @Override
    public ServiceResponse<String> executeCommands(String id, Command<?> cmd) {
        if( config.isRest() ) {
            return makeBackwardCompatibleHttpPostRequestAndCreateServiceResponse( loadBalancer.getUrl() + "/containers/instances/" + id, cmd, String.class, getHeaders(cmd) );
        } else {
            CommandScript script = new CommandScript( Collections.singletonList( (KieServerCommand) new CallContainerCommand( id, serialize(cmd) ) ) );
            return (ServiceResponse<String>) executeJmsCommand( script, cmd.getClass().getName(), null, id ).getResponses().get( 0 );
        }
    }


}
