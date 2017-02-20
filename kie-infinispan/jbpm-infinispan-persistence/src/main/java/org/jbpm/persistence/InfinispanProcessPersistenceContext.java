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

package org.jbpm.persistence;

import java.util.ArrayList;
import java.util.List;

import org.jbpm.persistence.PersistentCorrelationKey;
import org.jbpm.persistence.PersistentProcessInstance;
import org.drools.persistence.infinispan.InfinispanPersistenceContext;
import org.infinispan.Cache;
import org.jbpm.persistence.correlation.CorrelationKeyInfo;
import org.jbpm.persistence.processinstance.ProcessEntityHolder;
import org.jbpm.persistence.processinstance.ProcessInstanceInfo;
import org.kie.internal.process.CorrelationKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InfinispanProcessPersistenceContext extends InfinispanPersistenceContext
    implements
    ProcessPersistenceContext {

    private static Logger logger = LoggerFactory.getLogger(InfinispanProcessPersistenceContext.class);
    
	private static long PROCESSINSTANCEINFO_KEY = 1;
	private static long CORRELATIONKEYINFO_KEY = 1;
	private static final Object keyObject = new Object();

    public InfinispanProcessPersistenceContext(Cache<String, Object> cache ) {
        super( cache );
    }

    public PersistentProcessInstance persist(PersistentProcessInstance processInstance) {
        ProcessInstanceInfo processInstanceInfo = (ProcessInstanceInfo) processInstance;
    	String id = generateProcessInstanceInfoId(processInstanceInfo);
        getCache().put( id, new ProcessEntityHolder(id, processInstanceInfo) );
        return processInstanceInfo;
    }

    public ProcessInstanceInfo findProcessInstanceInfo(Long processInstanceId) {
    	String key = inferProcessInstanceInfoId(processInstanceId);
        ProcessEntityHolder holder = (ProcessEntityHolder) getCache().get( key );
        if (holder == null) {
        	return null;
        }
		return holder.getProcessInstanceInfo();
    }

	public void remove(PersistentProcessInstance processInstance) {
        ProcessInstanceInfo processInstanceInfo = (ProcessInstanceInfo) processInstance;
        getCache().remove( generateProcessInstanceInfoId(processInstanceInfo) );
        getCache().evict( generateProcessInstanceInfoId(processInstanceInfo) );
        List<CorrelationKeyInfo> correlations = getCorrelationKeysByProcessInstanceId(processInstanceInfo.getId());
        if (correlations != null) {
            for (CorrelationKeyInfo key : correlations) {
                getCache().remove(generateCorrelationKeyInfoId(key));
            }
        }
    }

    private String generateCorrelationKeyInfoId(CorrelationKeyInfo info) {
    	if (info != null && info.getId() <= 0) {
    		synchronized (keyObject) {
    			while (getCache().containsKey("correlationInfo" + CORRELATIONKEYINFO_KEY)) {
    				CORRELATIONKEYINFO_KEY++;
    			}
    		}
    		try {
	    		java.lang.reflect.Field idField = CorrelationKeyInfo.class.getDeclaredField("id");
	    		idField.setAccessible(true);
	    		idField.set(info, CORRELATIONKEYINFO_KEY);
    		} catch (Exception e) {
    			if( e instanceof RuntimeException ) { 
    			    throw (RuntimeException) e;
    			} else { 
    			    throw new RuntimeException(
    			            "Unable to set id field of " + CorrelationKeyInfo.class.getSimpleName() + " instance.", e );
    			}
    		}
    	}
    	return "correlationInfo" + info.getId();
	}

    private String generateProcessInstanceInfoId(ProcessInstanceInfo info) {
    	if (info != null && (info.getId() == null || info.getId() <= 0)) {
    		synchronized (keyObject) {
    			while (getCache().containsKey(inferProcessInstanceInfoId(PROCESSINSTANCEINFO_KEY))) {
    				PROCESSINSTANCEINFO_KEY++;
    			}
    		}
			info.setId(PROCESSINSTANCEINFO_KEY);
    	}
    	return inferProcessInstanceInfoId(info.getId());
	}

    private String inferProcessInstanceInfoId(Long processInstanceId) {
		return "processInstanceInfo" + processInstanceId;
	}

	private List<CorrelationKeyInfo> getCorrelationKeysByProcessInstanceId(Long pId) {
		Cache<String, Object> cache = getCache();
		List<CorrelationKeyInfo> retval = new ArrayList<CorrelationKeyInfo>();
		for (String key : cache.keySet()) {
			if (key.startsWith("correlationInfo")) {
				ProcessEntityHolder holder = (ProcessEntityHolder) cache.get(key);
				if (pId.equals(holder.getProcessInstanceId())) {
					retval.add(holder.getCorrelationKeyInfo());
				}
			}
		}
		return retval;
	}

    public List<Long> getProcessInstancesWaitingForEvent(String type) {
    	Cache<String, Object> cache = getCache();
    	List<Long> retval = new ArrayList<Long>();
    	for (String key : cache.keySet()) {
    		if (key.startsWith("processInstanceInfo")) {
    			ProcessEntityHolder holder = (ProcessEntityHolder) cache.get(key);
    			if (holder != null && holder.getProcessInstanceEventTypes() != null) {
    				if (holder.getProcessInstanceEventTypes().contains(type)) {
    					retval.add(holder.getProcessInstanceId());
    				}
    			}
    		}
    	}
		return retval;
    }

    public CorrelationKeyInfo persist(PersistentCorrelationKey correlationKey) {
        CorrelationKeyInfo correlationKeyInfo = (CorrelationKeyInfo) correlationKey;
        Long processInstanceId = getProcessInstanceByCorrelationKey(correlationKeyInfo);
        if (processInstanceId != null) {
            throw new RuntimeException(correlationKeyInfo + " already exists");
        }
    	String id = generateCorrelationKeyInfoId(correlationKeyInfo);
        getCache().put( id, new ProcessEntityHolder(id, correlationKeyInfo) );
        return correlationKeyInfo;
    }

    public Long getProcessInstanceByCorrelationKey(CorrelationKey correlationKey) {
    	String propertiesString = ProcessEntityHolder.generateString(correlationKey.getProperties());
    	Cache<String, Object> cache = getCache();
    	List<Long> retval = new ArrayList<Long>();
    	for (String key : cache.keySet()) {
    		if (key.startsWith("correlationInfo")) {
    			ProcessEntityHolder holder = (ProcessEntityHolder) cache.get(key);
    			if (holder.getCorrelationKeyId() == correlationKey.getProperties().size()) {
    				if (holder.getCorrelationKeyProperties().contains(propertiesString)) {
    					retval.add(holder.getProcessInstanceId());
    				}
    			}
    		}
    	}
    	return (retval.size() == 1) ? retval.iterator().next() : null;
    }

}
