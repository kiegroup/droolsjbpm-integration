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

package org.jbpm.persistence.session;

import static org.jbpm.persistence.util.PersistenceUtil.JBPM_PERSISTENCE_UNIT_NAME;
import static org.jbpm.persistence.util.PersistenceUtil.cleanUp;
import static org.jbpm.persistence.util.PersistenceUtil.createEnvironment;
import static org.jbpm.persistence.util.PersistenceUtil.setupWithPoolingDataSource;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.HashMap;

import javax.naming.InitialContext;
import javax.transaction.UserTransaction;

import org.jbpm.persistence.processinstance.InfinispanProcessInstanceManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.Environment;
import org.kie.internal.builder.KnowledgeBuilder;
import org.kie.internal.builder.KnowledgeBuilderFactory;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.persistence.infinispan.InfinispanKnowledgeService;
import org.kie.internal.runtime.StatefulKnowledgeSession;

/**
 * This test looks at the behavior of the  {@link InfinispanProcessInstanceManager} 
 * with regards to created (but not started) process instances 
 * and whether the process instances are available or not after creation.
 */
public class GetProcessInstancesTest {
    
    private HashMap<String, Object> context;
    
    private Environment env;
    private KieBase kbase;
    private long sessionId;

    @Before
    public void setUp() throws Exception {
        context = setupWithPoolingDataSource(JBPM_PERSISTENCE_UNIT_NAME);
        env = createEnvironment(context);

        kbase = createBase();
        StatefulKnowledgeSession ksession = InfinispanKnowledgeService.newStatefulKnowledgeSession(kbase, null, env);
        sessionId = ksession.getIdentifier();
        ksession.dispose();
    }

    @After
    public void tearDown() throws Exception {
        cleanUp(context);
    }

    @Test
    public void getEmptyProcessInstances() throws Exception {
        StatefulKnowledgeSession ksession = reloadKnowledgeSession();
        assertEquals(0, ksession.getProcessInstances().size());
        ksession.dispose();
    }

    @Test
    public void create2ProcessInstances() throws Exception {
        long[] processId = new long[2];

        StatefulKnowledgeSession ksession = reloadKnowledgeSession();
        processId[0] = ksession.createProcessInstance("org.jbpm.processinstance.helloworld", null).getId();
        processId[1] = ksession.createProcessInstance("org.jbpm.processinstance.helloworld", null).getId();
        ksession.dispose();

        assertProcessInstancesExist(processId);
    }

    /**
     * Helper functions
     */
    
    private void assertProcessInstancesExist(long[] processId) {
        StatefulKnowledgeSession ksession = reloadKnowledgeSession();

        for (long id : processId) {
            assertNotNull("Process instance " + id + " should not exist!", ksession.getProcessInstance(id));
        }
    }

    private void assertProcessInstancesNotExist(long[] processId) {
        StatefulKnowledgeSession ksession = reloadKnowledgeSession();

        for (long id : processId) {
            assertNull(ksession.getProcessInstance(id));
        }
    }

    private KieBase createBase() {
        KnowledgeBuilder kbuilder = KnowledgeBuilderFactory.newKnowledgeBuilder();
        kbuilder.add(ResourceFactory.newClassPathResource("processinstance/HelloWorld.rf"), ResourceType.DRF);
        assertFalse(kbuilder.getErrors().toString(), kbuilder.hasErrors());

        return kbuilder.newKieBase();
    }
    
    private StatefulKnowledgeSession reloadKnowledgeSession() {
        return InfinispanKnowledgeService.loadStatefulKnowledgeSession(sessionId, kbase, null, env);
    }

    private StatefulKnowledgeSession reloadKnowledgeSession(StatefulKnowledgeSession ksession) {
        ksession.dispose();
        return reloadKnowledgeSession();
    }
}
