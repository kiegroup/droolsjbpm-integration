package org.drools.container.spring.timer;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.drools.KnowledgeBase;
import org.drools.KnowledgeBaseFactory;
import org.drools.base.MapGlobalResolver;
import org.drools.persistence.jpa.KnowledgeStoreService;
import org.drools.runtime.Environment;
import org.drools.runtime.EnvironmentName;
import org.drools.runtime.StatefulKnowledgeSession;
import org.slf4j.LoggerFactory;
import org.springframework.orm.jpa.JpaTransactionManager;

public class MyDroolsBean {

    private static int timerTriggerCount;
    private static int sessionId;

    private EntityManagerFactory  emf;
    private KnowledgeBase         kbase;
    private KnowledgeStoreService kstore;
    private JpaTransactionManager txm;

    org.slf4j.Logger              logger          = LoggerFactory.getLogger( getClass() );

    private TestWorkItemHandler   workItemHandler = new TestWorkItemHandler();

    public void initStartDisposeAndLoadSession() {
        try {
            EntityManager em = txm.getEntityManagerFactory().createEntityManager();
            // create new ksession with kstore
            StatefulKnowledgeSession ksession = kstore.newStatefulKnowledgeSession( kbase,
                                                                                    null,
                                                                                    getEnvironment() );
            sessionId = ksession.getId();

            logger.info( "\n\tSession id: " + sessionId + "\n" );

            ksession.getWorkItemManager().registerWorkItemHandler( "testWorkItemHandler",
                                                                   workItemHandler );

            ksession.startProcess( "timer-flow",
                                   null );
            Thread.sleep( 4000 );
            ksession.dispose();
        } catch ( Exception ex ) {
            logger.error( "Exception",
                          ex );
        }

    }

    /**
     * Thread safe increment.
     */
    public static synchronized void incrementTimerTriggerCount() {
        timerTriggerCount++;
    }

    /**
     * Thread safe getter.
     * Note that if this method is not synchronized, there is no visibility guarantee,
     * so the returned value might be a stale cache.
     * @return >= 0
     */
    public static synchronized int getTimerTriggerCount() {
        return timerTriggerCount;
    }

    public void endTheProcess() {
        try {
            StatefulKnowledgeSession ksession = kstore.loadStatefulKnowledgeSession( sessionId,
                                                                                     kbase,
                                                                                     null,
                                                                                     getEnvironment() );

            //Sleep to check if the timer continues executing.
            logger.info( "\n\nSleeping to check that the timer is still running" );
            Thread.sleep( 5000 );

            ksession.getWorkItemManager().completeWorkItem( TestWorkItemHandler.getWorkItem().getId(),
                                                            null );

            logger.info( "\n\nSleeping to check that the timer is no longer running" );
            Thread.sleep( 3000 );
            logger.info( "Ok" );

            ksession.dispose();

        } catch ( InterruptedException ex ) {
            logger.error( "Exception",
                          ex );
        }
    }

    private Environment getEnvironment() {
        Environment environment = KnowledgeBaseFactory.newEnvironment();
        environment.set( EnvironmentName.ENTITY_MANAGER_FACTORY,
                         emf );
        environment.set( EnvironmentName.TRANSACTION_MANAGER,
                         txm );
        environment.set( EnvironmentName.GLOBALS,
                         new MapGlobalResolver() );

        return environment;
    }

    public void setEmf(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public void setKbase(KnowledgeBase kbase) {
        this.kbase = kbase;
    }

    public void setKstore(KnowledgeStoreService kstore) {
        this.kstore = kstore;
    }

    public void setTxm(JpaTransactionManager txm) {
        this.txm = txm;
    }
}
