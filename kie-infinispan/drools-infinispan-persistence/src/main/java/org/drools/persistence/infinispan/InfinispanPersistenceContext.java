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

package org.drools.persistence.infinispan;

import org.drools.persistence.PersistenceContext;
import org.drools.persistence.PersistentSession;
import org.drools.persistence.PersistentWorkItem;
import org.drools.persistence.info.EntityHolder;
import org.infinispan.Cache;

public class InfinispanPersistenceContext implements PersistenceContext {

	private static long SESSIONINFO_KEY = 1;
	private static long WORKITEMINFO_KEY = 1;
	private static final Object syncObject = new Object();

    private Cache<String, Object> cache;
    private boolean isJTA;

    public InfinispanPersistenceContext(Cache<String, Object> cache) {
        this(cache, true);
    }

    public InfinispanPersistenceContext(Cache<String, Object> cache, boolean isJTA) {
        this.cache = cache;
        this.isJTA = isJTA;
    }

    public PersistentSession persist(PersistentSession entity) {
    	if (entity.getId() == null) {
    		entity.setId(generateSessionInfoId());
    	}
    	String key = createSessionKey(entity.getId());
    	entity.transform();
        this.cache.put(key , new EntityHolder(key, entity) );
        return entity;
    }

    private Long generateSessionInfoId() {
    	synchronized (syncObject) {
    		while (cache.containsKey("sessionInfo" + SESSIONINFO_KEY)) {
    			SESSIONINFO_KEY++;
    		}
    	}
    	return SESSIONINFO_KEY;
    }

    private Long generateWorkItemInfoId() {
    	synchronized (syncObject) {
    		while (cache.containsKey("workItem" + WORKITEMINFO_KEY)) {
    			WORKITEMINFO_KEY++;
    		}
    	}
    	return WORKITEMINFO_KEY;
    }

	private String createSessionKey(Long id) {
		return "sessionInfo" + safeId(id);
	}

	private String createWorkItemKey(Long id) {
		return "workItem" + safeId(id);
	}

	private String safeId(Number id) {
		return String.valueOf(id); //TODO
	}

    public PersistentSession findSession(Long id) {
    	EntityHolder holder = (EntityHolder) this.cache.get( createSessionKey(id) );
    	if (holder == null) {
    		return null;
    	}
        return holder.getSessionInfo();
    }

    @Override
    public void remove(PersistentSession sessionInfo) {
        cache.remove( createSessionKey(sessionInfo.getId()) );
        cache.evict( createSessionKey(sessionInfo.getId()) );
    }

    public boolean isOpen() {
    	//cache doesn't close
        return true;
    }

    public void joinTransaction() {
    	if (isJTA) {
    		//cache.getAdvancedCache().getTransactionManager().getTransaction().???? TODO
    	}
    }

    public void close() {
        //cache doesn't close
    }

    public PersistentWorkItem persist(PersistentWorkItem workItemInfo) {
    	if (workItemInfo.getId() == null) {
    		workItemInfo.setId(generateWorkItemInfoId());
    	}
    	String key = createWorkItemKey(workItemInfo.getId());
    	workItemInfo.transform();
    	cache.put(key, new EntityHolder(key, workItemInfo));
    	return workItemInfo;
    }

    public PersistentWorkItem findWorkItem(Long id) {
    	EntityHolder holder = (EntityHolder) cache.get(createWorkItemKey(id));
    	if (holder == null) {
    		return null;
    	}
    	return holder.getWorkItemInfo();
    }

    public void remove(PersistentWorkItem workItemInfo) {
        cache.remove( createWorkItemKey(workItemInfo.getId()) );
        cache.evict( createWorkItemKey(workItemInfo.getId()) );
    }

    public PersistentWorkItem merge(PersistentWorkItem workItemInfo) {
    	String key = createWorkItemKey(workItemInfo.getId());
    	workItemInfo.transform();
    	EntityHolder entityHolder = new EntityHolder(key, workItemInfo);
    	cache.put(key, entityHolder);
    	return workItemInfo;
    }

    public Cache<String, Object> getCache() {
		return cache;
	}

    public void lock(PersistentWorkItem workItemInfo) {
        // no-op: no locking implemented here
    }
}
