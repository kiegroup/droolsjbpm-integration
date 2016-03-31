package org.drools.karaf.itest.beans;

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
