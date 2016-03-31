package org.drools.karaf.itest.beans;

import org.jbpm.services.task.identity.JBossUserGroupCallbackImpl;
import org.kie.api.KieBase;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.manager.RuntimeEnvironment;
import org.kie.api.runtime.manager.RuntimeEnvironmentBuilder;

import javax.persistence.EntityManagerFactory;
import java.util.Properties;

/**
 * A bean creating a KieSession for starting a process with persistence.
 *
 * This implementation uses the injected org.kie.api.runtime.Environment instance as the source of EntityManagerFactory,
 * UserTransaction, TransactionManager, and TransactionSynchronizationRegistry.
 */
public class ProcessWithPersistenceEnvBean extends AbstractProcessWithPersistenceBean {

    private final Environment environment;

    public ProcessWithPersistenceEnvBean(final Environment environment) {
        this.environment = environment;
    }

    @Override
    protected RuntimeEnvironment getRuntimeEnvironment(final KieBase kieBase) {
        final EntityManagerFactory emf = (EntityManagerFactory) environment.get(EnvironmentName.ENTITY_MANAGER_FACTORY);
        return RuntimeEnvironmentBuilder.Factory.get()
                .newEmptyBuilder()
                .knowledgeBase(kieBase)
                .classLoader(this.getClass().getClassLoader())
                .entityManagerFactory(emf)
                .persistence(true)
                 // the default MVELUserGroupCallback does not work due to NCDFError, see the error in debugger - BZ 1316974
                .userGroupCallback(new JBossUserGroupCallbackImpl(new Properties()))
                .addEnvironmentEntry(EnvironmentName.ENTITY_MANAGER_FACTORY, emf)
                .addEnvironmentEntry(EnvironmentName.TRANSACTION_MANAGER, environment.get(EnvironmentName.TRANSACTION_MANAGER))
                .addEnvironmentEntry(EnvironmentName.TRANSACTION, environment.get(EnvironmentName.TRANSACTION))
                .addEnvironmentEntry(EnvironmentName.TRANSACTION_SYNCHRONIZATION_REGISTRY, environment.get(EnvironmentName.TRANSACTION_SYNCHRONIZATION_REGISTRY))
                .get();
    }

}
