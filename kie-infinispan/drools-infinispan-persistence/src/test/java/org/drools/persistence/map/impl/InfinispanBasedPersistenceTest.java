/*
 * Copyright 2011 Red Hat Inc.
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
package org.drools.persistence.map.impl;

import static org.drools.persistence.util.PersistenceUtil.DROOLS_PERSISTENCE_UNIT_NAME;
import static org.drools.persistence.util.PersistenceUtil.createEnvironment;
import static org.kie.api.runtime.EnvironmentName.ENTITY_MANAGER_FACTORY;
import static org.kie.api.runtime.EnvironmentName.TRANSACTION_MANAGER;

import java.util.HashMap;

import org.drools.persistence.jpa.marshaller.JPAPlaceholderResolverStrategy;
import org.drools.persistence.jta.JtaTransactionManager;
import org.drools.persistence.util.PersistenceUtil;
import org.infinispan.manager.DefaultCacheManager;
import org.junit.After;
import org.junit.Before;
import org.kie.api.KieBase;
import org.kie.api.runtime.KieSession;
import org.kie.internal.persistence.infinispan.InfinispanKnowledgeService;
import org.kie.internal.runtime.StatefulKnowledgeSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InfinispanBasedPersistenceTest extends MapPersistenceTest {

    private static Logger logger = LoggerFactory.getLogger(JPAPlaceholderResolverStrategy.class);

    private HashMap<String, Object> context;
    private DefaultCacheManager cm;
    private JtaTransactionManager txm;
    private boolean useTransactions = false;

    @Before
    public void setUp() throws Exception {
        context = PersistenceUtil.setupWithPoolingDataSource(DROOLS_PERSISTENCE_UNIT_NAME);
        cm = (DefaultCacheManager) context.get(ENTITY_MANAGER_FACTORY);
        this.txm = (JtaTransactionManager) context.get(TRANSACTION_MANAGER);
        /*
        if( useTransactions() ) {
            useTransactions = true;
            Environment env = createEnvironment(context);
            Object tm = env.get( EnvironmentName.TRANSACTION_MANAGER );
            this.txm = new JtaTransactionManager( env.get( EnvironmentName.TRANSACTION ),
                env.get( EnvironmentName.TRANSACTION_SYNCHRONIZATION_REGISTRY ),
                tm );
        }*/ //TODO
    }

    @After
    public void tearDown() throws Exception {
        PersistenceUtil.tearDown(context);
    }


    @Override
    protected StatefulKnowledgeSession createSession(KieBase kbase) {
        return InfinispanKnowledgeService.newStatefulKnowledgeSession( kbase, null, createEnvironment(context) );
    }

    @Override
    protected StatefulKnowledgeSession disposeAndReloadSession(KieSession ksession,
                                                               KieBase kbase) {
        long ksessionId = ksession.getIdentifier();
        ksession.dispose();
        return InfinispanKnowledgeService.loadStatefulKnowledgeSession(ksessionId, kbase, null, createEnvironment(context));
    }

    @Override
    protected long getSavedSessionsCount() {
    	return cm.getCache("jbpm-configured-cache").size();
    }

}
