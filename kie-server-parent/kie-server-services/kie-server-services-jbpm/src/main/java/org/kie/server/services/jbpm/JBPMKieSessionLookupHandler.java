/*
 * Copyright 2015 JBoss Inc
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

import org.kie.api.command.Command;
import org.kie.api.runtime.CommandExecutor;
import org.kie.api.runtime.manager.Context;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.internal.runtime.manager.RuntimeManagerRegistry;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;
import org.kie.server.services.api.KieContainerInstance;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.api.KieSessionLookupHandler;

public class JBPMKieSessionLookupHandler implements KieSessionLookupHandler {

    @Override
    public CommandExecutor lookupKieSession(String kieSessionId, KieContainerInstance containerInstance, KieServerRegistry registry) {
        String managerId = kieSessionId;
        Long processInstanceId = null;
        if (kieSessionId.indexOf("#") != -1) {
            String[] managerAndInstanceIds = kieSessionId.split("#");
            managerId = managerAndInstanceIds[0];
            processInstanceId = Long.parseLong(managerAndInstanceIds[1]);
        }

        if (RuntimeManagerRegistry.get().isRegistered(managerId)) {
            RuntimeManager runtimeManager = RuntimeManagerRegistry.get().getManager(managerId);

            Context ctx = EmptyContext.get();
            if (processInstanceId != null) {
                ctx = ProcessInstanceIdContext.get(processInstanceId);
            }

            RuntimeEngine engine = runtimeManager.getRuntimeEngine(ctx);

            return new RuntimeEngineDisposableCommandExecutor(runtimeManager, engine);
        }

        return null;
    }

    private class RuntimeEngineDisposableCommandExecutor implements CommandExecutor {

        private RuntimeManager runtimeManager;
        private RuntimeEngine engine;

        public RuntimeEngineDisposableCommandExecutor(RuntimeManager runtimeManager, RuntimeEngine engine) {
            this.runtimeManager = runtimeManager;
            this.engine = engine;
        }

        @Override
        public <T> T execute(Command<T> command) {
            try {

                return engine.getKieSession().execute(command);

            } finally {
                runtimeManager.disposeRuntimeEngine(engine);
            }
        }
    }
}
