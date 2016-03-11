package org.drools.karaf.itest.beans;

import org.jbpm.services.task.identity.JBossUserGroupCallbackImpl;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeEnvironment;
import org.kie.api.runtime.manager.RuntimeEnvironmentBuilder;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.manager.RuntimeManagerFactory;
import org.kie.internal.runtime.manager.context.EmptyContext;

import javax.persistence.EntityManagerFactory;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;
import javax.transaction.UserTransaction;
import java.util.Properties;

/**
 * A bean creating a KieSession for starting a process with persistence.
 */
public class ProcessWithPersistenceBean {

    private static final KieServices KIE_SERVICES = KieServices.Factory.get();

    private final EntityManagerFactory entityManagerFactory;
    private final TransactionManager transactionManager;
    private final UserTransaction userTransaction;
    private final TransactionSynchronizationRegistry transactionSynchronizationRegistry;

    private RuntimeManager runtimeManager;
    private RuntimeEngine runtimeEngine;

    public ProcessWithPersistenceBean(final EntityManagerFactory emf, final TransactionManager tm,
                                      final UserTransaction ut, TransactionSynchronizationRegistry tsr) {
        this.entityManagerFactory = emf;
        this.transactionManager = tm;
        this.userTransaction = ut;
        this.transactionSynchronizationRegistry = tsr;
    }

    public void init() {
        final KieContainer kieContainer = KIE_SERVICES.newKieClasspathContainer(this.getClass().getClassLoader());
        final KieBase kieBase = kieContainer.getKieBase();
        final RuntimeEnvironment runtimeEnvironment = this.getRuntimeEnvironment(kieBase);

        RuntimeManagerFactory factory = RuntimeManagerFactory.Factory.get(this.getClass().getClassLoader());
        this.runtimeManager = factory.newSingletonRuntimeManager(runtimeEnvironment);
        this.runtimeEngine = this.runtimeManager.getRuntimeEngine(EmptyContext.get());
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

    private RuntimeEnvironment getRuntimeEnvironment(final KieBase kieBase) {
        return RuntimeEnvironmentBuilder.Factory.get()
                .newEmptyBuilder()
                .knowledgeBase(kieBase)
                .classLoader(this.getClass().getClassLoader())
                .entityManagerFactory(entityManagerFactory)
                .persistence(true)
                 // the default MVELUserGroupCallback does not work due to NCDFError, see the error in debugger - BZ TO-DO
                .userGroupCallback(new JBossUserGroupCallbackImpl(new Properties()))
                .addEnvironmentEntry(EnvironmentName.ENTITY_MANAGER_FACTORY, entityManagerFactory)
                .addEnvironmentEntry(EnvironmentName.TRANSACTION_MANAGER, transactionManager)
                 // uncomment the following two and the test will pass (however it is not possible to set those from blueprint XML)
                 //.addEnvironmentEntry(EnvironmentName.TRANSACTION, userTransaction)
                 //.addEnvironmentEntry(EnvironmentName.TRANSACTION_SYNCHRONIZATION_REGISTRY, transactionSynchronizationRegistry)
                .get();
    }

}
