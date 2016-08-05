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

package org.kie.server.client;

import org.kie.api.command.Command;
import org.kie.api.runtime.ExecutionResults;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.client.jms.ResponseHandler;

public interface RuleServicesClient {

    /**
     * @deprecated use #executeCommandsWithResults
     */
    @Deprecated
    ServiceResponse<String> executeCommands(String id, String payload);

    /**
     * @deprecated use #executeCommandsWithResults
     */
    @Deprecated
    ServiceResponse<String> executeCommands(String id, Command<?> cmd);

    ServiceResponse<ExecutionResults> executeCommandsWithResults(String id, String payload);

    ServiceResponse<ExecutionResults> executeCommandsWithResults(String id, Command<?> cmd);

    void setResponseHandler(ResponseHandler responseHandler);
}

