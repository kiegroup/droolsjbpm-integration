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

package org.jbpm.persistence.map.impl;

import static org.jbpm.persistence.util.PersistenceUtil.*;
import static org.kie.api.runtime.EnvironmentName.ENTITY_MANAGER_FACTORY;

import java.util.HashMap;
import java.util.Set;

import org.drools.persistence.jta.JtaTransactionManager;
import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;
import org.jbpm.persistence.util.PersistenceUtil;
import org.junit.After;
import org.junit.Before;
import org.kie.api.KieBase;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.EnvironmentName;
import org.kie.internal.persistence.infinispan.InfinispanKnowledgeService;
import org.kie.internal.runtime.StatefulKnowledgeSession;

public class InfinispanBasedPersistenceTest extends MapPersistenceTest {

    private HashMap<String, Object> context;
    private DefaultCacheManager cm;
    private JtaTransactionManager txm;
    private boolean useTransactions = false;
    
    @Before
    public void setUp() throws Exception {
        context = PersistenceUtil.setupWithPoolingDataSource(JBPM_PERSISTENCE_UNIT_NAME);
        cm = (DefaultCacheManager) context.get(ENTITY_MANAGER_FACTORY);
        
        useTransactions = false;
        Environment env = createEnvironment(context);
        Object tm = env.get( EnvironmentName.TRANSACTION_MANAGER );
        this.txm = new JtaTransactionManager( env.get( EnvironmentName.TRANSACTION ),
            env.get( EnvironmentName.TRANSACTION_SYNCHRONIZATION_REGISTRY ),
            tm );
    }
    
    @After
    public void tearDown() throws Exception {
       cleanUp(context); 
    }
    
    @Override
    protected StatefulKnowledgeSession createSession(KieBase kbase) {
        return InfinispanKnowledgeService.newStatefulKnowledgeSession( kbase, null, createEnvironment(context) );
    }

    @Override
    protected StatefulKnowledgeSession disposeAndReloadSession(StatefulKnowledgeSession ksession, long ksessionId,
                                                               KieBase kbase) {
        ksession.dispose();
        return InfinispanKnowledgeService.loadStatefulKnowledgeSession( ksessionId, kbase, null, createEnvironment(context) );
    }

    @Override
    protected int getProcessInstancesCount() {
    	boolean txOwner = false;
        if( useTransactions ) { 
            txOwner = txm.begin();
        }
        Cache<String, Object> cache = cm.getCache("jbpm-configured-cache");
        Set<String> keyset = cache.keySet();
        int size = 0;
        for (String key : keyset) {
        	if (key.startsWith("processInstanceInfo")) {
        		size++;
        	}
        }
        if( useTransactions ) { 
            txm.commit(txOwner);
        }
        return size;
    }

    @Override
    protected int getKnowledgeSessionsCount() {
    	boolean txOwner = false;
        if( useTransactions ) { 
            txOwner = txm.begin();
        }
        Cache<String, Object> cache = cm.getCache("jbpm-configured-cache");
        Set<String> keyset = cache.keySet();
        int size = 0;
        for (String key : keyset) {
        	if (key.startsWith("sessionInfo")) {
        		size++;
        	}
        }
        if( useTransactions ) { 
            txm.commit(txOwner);
        }
        return size;
    }

}
