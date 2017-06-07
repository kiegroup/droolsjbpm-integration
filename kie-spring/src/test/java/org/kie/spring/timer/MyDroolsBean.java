/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
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

package org.kie.spring.timer;

import static org.junit.Assert.*;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import org.drools.core.base.MapGlobalResolver;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.persistence.jpa.KieStoreServices;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.KieSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.orm.jpa.JpaTransactionManager;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

public class MyDroolsBean {

    private static AtomicInteger timerTriggerCount = new AtomicInteger();
    private static long sessionId;

    private static Logger logger = LoggerFactory.getLogger(MyDroolsBean.class);

    private EntityManagerFactory emf;
    private KieBase kbase;
    private KieStoreServices kstore;
    private JpaTransactionManager txm;

    private TestWorkItemHandler workItemHandler = new TestWorkItemHandler();

    private static CyclicBarrier cyclicBarrier = new CyclicBarrier(2);
    
    public void initStartDisposeAndLoadSession() {
        try {
            EntityManager em = txm.getEntityManagerFactory().createEntityManager();
            // create new ksession with kstore
            KieSession ksession = kstore.newKieSession(kbase, null, getEnvironment());
            sessionId = ksession.getIdentifier();

            logger.debug("\n\tSession id: " + sessionId + "\n");

            ksession.getWorkItemManager().registerWorkItemHandler("testWorkItemHandler", workItemHandler);

            ksession.startProcess("timer-flow", null);
         
            // wait for process to start and for first timer to finish
            waitForOtherThread();
            
            ksession.dispose();
        } catch (Exception ex) {
            throw new IllegalStateException("The endTheProcess method has been interrupted", ex);
        }
        
    }

    public static void waitForOtherThread() { 
        try {
            cyclicBarrier.await();
        } catch( InterruptedException ie ) {
            // do nothing
        } catch( BrokenBarrierException bbe ) {
            logger.error("cyclic barrier has a broken state!", bbe );
        }
    }
    
    public static void incrementTimerTriggerCount() {
        timerTriggerCount.incrementAndGet();
    }

    public static int getTimerTriggerCount() {
        return timerTriggerCount.get();
    }

    public void endTheProcess() {
        try {
            KieSession ksession = kstore.loadKieSession(sessionId,
                    kbase,
                    null,
                    getEnvironment());

            //Sleep to check if the timer continues executing.
            logger.debug("\n\nSleeping to check that the timer is still running");

            // wait for a possible timer to run
            try {
                cyclicBarrier.await(3, TimeUnit.SECONDS);
            } catch( BrokenBarrierException e1 ) {
                // do nothing
            } catch( TimeoutException e1 ) {
                // do nothing
            }
                
            ksession.getWorkItemManager().completeWorkItem(TestWorkItemHandler.getWorkItem().getId(), null);

            logger.debug("\n\nSleeping to check that the timer is no longer running");
           
            boolean waitFailed = false;
            try {
                cyclicBarrier.await(3, TimeUnit.SECONDS);
                fail( "The timer should not have been running!" );
            } catch( BrokenBarrierException bbe ) {
                fail( "The barrier should not be broken: " + bbe.getMessage());
            } catch( TimeoutException e ) {
                waitFailed = true;
            }
            assertTrue( waitFailed );
            
            logger.debug("Ok");

            ksession.dispose();

        } catch (InterruptedException ex) {
            throw new IllegalStateException("The endTheProcess method has been interrupted", ex);
        }
    }

    private Environment getEnvironment() {
        Environment environment = KieServices.get().newEnvironment();
        environment.set(EnvironmentName.ENTITY_MANAGER_FACTORY,
                emf);
        environment.set(EnvironmentName.TRANSACTION_MANAGER,
                txm);
        environment.set(EnvironmentName.GLOBALS,
                new MapGlobalResolver());

        return environment;
    }

    public void setEmf(EntityManagerFactory emf) {
        this.emf = emf;
    }

    public void setKbase(KieBase kbase) {
        this.kbase = kbase;
    }

    public void setKstore(KieStoreServices kstore) {
        this.kstore = kstore;
    }

    public void setTxm(JpaTransactionManager txm) {
        this.txm = txm;
    }
}
