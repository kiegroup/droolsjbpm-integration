/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.karaf.itest.beans;

import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeEnvironment;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.manager.RuntimeManagerFactory;
import org.kie.internal.runtime.manager.context.EmptyContext;
import org.kie.internal.runtime.manager.context.ProcessInstanceIdContext;

/**
 * A bean creating a KieSession for starting a process with persistence.
 */
public abstract class AbstractProcessWithPersistenceBean {

    private static final KieServices KIE_SERVICES = KieServices.Factory.get();

    private RuntimeManager runtimeManager;
    private RuntimeEngine runtimeEngine;

    public void init() {
        final KieContainer kieContainer = KIE_SERVICES.newKieClasspathContainer(this.getClass().getClassLoader());
        final KieBase kieBase = kieContainer.getKieBase();
        final RuntimeEnvironment runtimeEnvironment = this.getRuntimeEnvironment(kieBase);

        RuntimeManagerFactory factory = RuntimeManagerFactory.Factory.get(this.getClass().getClassLoader());
        this.runtimeManager = factory.newPerProcessInstanceRuntimeManager(runtimeEnvironment, this.getClass().getSimpleName());
        this.runtimeEngine = this.runtimeManager.getRuntimeEngine(ProcessInstanceIdContext.get());
    }

    public void destroy() {
        if (this.runtimeManager != null) {
            if (this.runtimeEngine != null) {
                this.runtimeManager.disposeRuntimeEngine(this.runtimeEngine);
            }
            this.runtimeManager.close();
        }
    }

    public KieSession createKieSession() {
        return this.runtimeEngine.getKieSession();
    }

    protected abstract RuntimeEnvironment getRuntimeEnvironment(final KieBase kieBase);

}
