package org.drools.karaf.itest.beans;

import org.jbpm.services.task.identity.JBossUserGroupCallbackImpl;
import org.kie.api.KieBase;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.manager.RuntimeEnvironment;
import org.kie.api.runtime.manager.RuntimeEnvironmentBuilder;

import javax.persistence.EntityManagerFactory;
import javax.transaction.TransactionManager;
import javax.transaction.TransactionSynchronizationRegistry;
import javax.transaction.UserTransaction;
import java.util.Properties;

/**
 * A bean creating a KieSession for starting a process with persistence.
 *
 * This implementation uses the injected EntityManagerFactory, TransactionManager, UserTransaction,
 * and TransactionSynchronizationRegistry instances. It does not use org.kie.api.runtime.Environment.
 */
public class ProcessWithPersistenceDirectBean extends AbstractProcessWithPersistenceBean {

    private final EntityManagerFactory entityManagerFactory;
    private final TransactionManager transactionManager;
    private final UserTransaction userTransaction;
    private final TransactionSynchronizationRegistry transactionSynchronizationRegistry;

    public ProcessWithPersistenceDirectBean(final EntityManagerFactory emf, final TransactionManager tm,
                                            final UserTransaction ut, final TransactionSynchronizationRegistry tsr) {
        this.entityManagerFactory = emf;
        this.transactionManager = tm;
        this.userTransaction = ut;
        this.transactionSynchronizationRegistry = tsr;
    }

    @Override
    protected RuntimeEnvironment getRuntimeEnvironment(final KieBase kieBase) {
        return RuntimeEnvironmentBuilder.Factory.get()
                .newEmptyBuilder()
                .knowledgeBase(kieBase)
                .classLoader(this.getClass().getClassLoader())
                .entityManagerFactory(entityManagerFactory)
                .persistence(true)
                 // the default MVELUserGroupCallback does not work due to NCDFError, see the error in debugger - BZ 1316974
                .userGroupCallback(new JBossUserGroupCallbackImpl(new Properties()))
                .addEnvironmentEntry(EnvironmentName.ENTITY_MANAGER_FACTORY, entityManagerFactory)
                .addEnvironmentEntry(EnvironmentName.TRANSACTION_MANAGER, transactionManager)
                .addEnvironmentEntry(EnvironmentName.TRANSACTION, userTransaction)
                .addEnvironmentEntry(EnvironmentName.TRANSACTION_SYNCHRONIZATION_REGISTRY, transactionSynchronizationRegistry)
                .get();
    }

}
