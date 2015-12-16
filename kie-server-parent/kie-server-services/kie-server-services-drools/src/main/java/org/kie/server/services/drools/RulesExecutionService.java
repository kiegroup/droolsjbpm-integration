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

import org.drools.core.command.runtime.BatchExecutionCommandImpl;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.runtime.CommandExecutor;
import org.kie.api.runtime.ExecutionResults;
import org.kie.server.services.api.KieContainerInstance;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.impl.KieContainerInstanceImpl;

/**
 * Direct rules execution service that allow use of typed objects instead of string only
 */
public class RulesExecutionService {

    private KieServerRegistry context;

    public RulesExecutionService(KieServerRegistry context) {
        this.context = context;
    }

    public ExecutionResults call(KieContainerInstance kci, BatchExecutionCommand executionCommand) {

        BatchExecutionCommandImpl command = (BatchExecutionCommandImpl) executionCommand;

        if (kci != null && kci.getKieContainer() != null) {
            // find the session
            CommandExecutor ks = null;
            if( command.getLookup() != null ) {
                ks = context.getKieSessionLookupManager().lookup(command.getLookup(), kci, context);
            } else {
                // if no session ID is defined, then the default is a stateful session
                ks = ((KieContainerInstanceImpl)kci).getKieContainer().getKieSession();
            }

            if (ks != null) {
                ExecutionResults results = ks.execute(command);

                return results;
            } else {
                throw new IllegalStateException("Session '" + command.getLookup() + "' not found on container '" + kci.getContainerId() + "'.");
            }
        }

        throw new IllegalStateException("Unable to execute command " + command);
    }
}
