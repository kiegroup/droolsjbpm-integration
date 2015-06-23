/*
 * Copyright 2015 JBoss Inc
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

package org.jbpm.persistence.correlation;

import static org.jbpm.persistence.util.PersistenceUtil.JBPM_PERSISTENCE_UNIT_NAME;
import static org.jbpm.persistence.util.PersistenceUtil.cleanUp;
import static org.jbpm.persistence.util.PersistenceUtil.setupWithPoolingDataSource;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.naming.InitialContext;
import javax.transaction.Status;
import javax.transaction.UserTransaction;

import org.infinispan.Cache;
import org.infinispan.manager.DefaultCacheManager;
import org.jbpm.persistence.InfinispanProcessPersistenceContext;
import org.jbpm.persistence.processinstance.ProcessEntityHolder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.kie.api.runtime.EnvironmentName;
import org.kie.internal.KieInternalServices;
import org.kie.internal.process.CorrelationKeyFactory;

public class CorrelationPersistenceTest {
    
    private HashMap<String, Object> context;
    
    @Before
    public void before() throws Exception {
        context = setupWithPoolingDataSource(JBPM_PERSISTENCE_UNIT_NAME, false);
        CorrelationKeyFactory factory = KieInternalServices.Factory.get().newCorrelationKeyFactory();
        // populate table with test data
        DefaultCacheManager cm = (DefaultCacheManager) context.get(EnvironmentName.ENTITY_MANAGER_FACTORY);
        UserTransaction ut = InitialContext.doLookup("java:comp/UserTransaction");
        if (ut.getStatus() == Status.STATUS_NO_TRANSACTION) {
        	ut.begin();
        }
        Cache <String, Object> cache = cm.getCache("jbpm-configured-cache");

        cache.put("correlationInfo1", new ProcessEntityHolder("correlationInfo1", (CorrelationKeyInfo) factory.newCorrelationKey("test123")));

        List<String> props = new ArrayList<String>();
        props.add("test123");
        props.add("123test");
        cache.put("correlationInfo2", new ProcessEntityHolder("correlationInfo2", (CorrelationKeyInfo) factory.newCorrelationKey(props)));
        if (ut.getStatus() == Status.STATUS_ACTIVE) {
        	ut.commit();
        }
    }
    
    @After
    public void after() {  
        try {
            DefaultCacheManager cm = (DefaultCacheManager) context.get(EnvironmentName.ENTITY_MANAGER_FACTORY);
            UserTransaction ut = InitialContext.doLookup("java:comp/UserTransaction");
            if (ut.getStatus() == Status.STATUS_NO_TRANSACTION) {
            	ut.begin();
            }
            Cache<String, Object> cache = cm.getCache("jbpm-configured-cache");
            cache.clear();
            if (ut.getStatus() == Status.STATUS_ACTIVE) {
            	ut.commit();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        cleanUp(context);
    }

    @Test
    public void testCreateCorrelation() throws Exception {
        DefaultCacheManager cm = (DefaultCacheManager) context.get(EnvironmentName.ENTITY_MANAGER_FACTORY);
        Cache<String, Object> cache = cm.getCache("jbpm-configured-cache");
        
        CorrelationKeyInfo correlationKey = new CorrelationKeyInfo();
        correlationKey.addProperty(new CorrelationPropertyInfo("", "test123"));
        Long processInstance = new InfinispanProcessPersistenceContext(cache).getProcessInstanceByCorrelationKey(correlationKey);
        
        assertNotNull(processInstance);
        assertEquals(correlationKey.getProcessInstanceId(), processInstance.longValue());
    }
    
    @Test
    public void testCreateCorrelationMultiValueDoesNotMatch() throws Exception {
        DefaultCacheManager cm = (DefaultCacheManager) context.get(EnvironmentName.ENTITY_MANAGER_FACTORY);
        Cache<String, Object> cache = cm.getCache("jbpm-configured-cache");

        CorrelationKeyInfo correlationKey = new CorrelationKeyInfo();
        correlationKey.addProperty(new CorrelationPropertyInfo("", "asdf"));
        Long processInstance = new InfinispanProcessPersistenceContext(cache).getProcessInstanceByCorrelationKey(correlationKey);
        
        assertNull(processInstance);
    }
    
    @Test
    public void testCreateCorrelationMultiValueDoesMatch() throws Exception {
        DefaultCacheManager cm = (DefaultCacheManager) context.get(EnvironmentName.ENTITY_MANAGER_FACTORY);
        Cache<String, Object> cache = cm.getCache("jbpm-configured-cache");
        
        CorrelationKeyFactory factory = KieInternalServices.Factory.get().newCorrelationKeyFactory();
        List<String> props = new ArrayList<String>();
        props.add("test123");
        props.add("123test");
        CorrelationKeyInfo correlationKey = (CorrelationKeyInfo) factory.newCorrelationKey(props);

        Long processInstance = new InfinispanProcessPersistenceContext(cache).getProcessInstanceByCorrelationKey(correlationKey);
        
        assertNotNull(processInstance);
        assertEquals(correlationKey.getProcessInstanceId(), processInstance.longValue());
    }
}
