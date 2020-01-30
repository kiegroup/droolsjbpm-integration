/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.services.taskassigning.planning;

import java.util.concurrent.ExecutorService;

import org.kie.server.services.taskassigning.user.system.api.UserSystemService;
import org.kie.server.services.api.KieServerRegistry;

public class TaskAssigningService {

    private TaskAssigningRuntimeDelegate delegate;
    private UserSystemService userSystemService;
    private ExecutorService executorService;

    private SolverHandler solverHandler;

    public void setDelegate(TaskAssigningRuntimeDelegate delegate) {
        this.delegate = delegate;
    }

    public void setUserSystemService(UserSystemService userSystemService) {
        this.userSystemService = userSystemService;
    }

    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public void start(SolverDef solverDef, KieServerRegistry registry) {
        solverHandler = createSolverHandler(solverDef, registry, delegate, userSystemService, executorService);
        solverHandler.start();
    }

    public void destroy() {
        if (solverHandler != null) {
            solverHandler.destroy();
        }
    }

    SolverHandler createSolverHandler(SolverDef solverDef, KieServerRegistry registry,
                                      TaskAssigningRuntimeDelegate delegate, UserSystemService userSystemService,
                                      ExecutorService executorService) {
        return new SolverHandler(solverDef, registry, delegate, userSystemService, executorService);
    }
}
