/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.controller.openshift.storage;

import java.lang.ref.SoftReference;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.kie.server.controller.api.KieServerControllerConstants;
import org.kie.server.controller.api.model.spec.ServerTemplate;
import org.kie.server.controller.api.model.spec.ServerTemplateKey;
import org.kie.server.controller.api.storage.KieServerTemplateStorage;
import org.kie.server.services.openshift.impl.storage.cloud.KieServerStateOpenShiftRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OpenShiftServerTemplateStorage implements KieServerTemplateStorage {
    private static final Logger logger = LoggerFactory.getLogger(OpenShiftServerTemplateStorage.class);
    private static final String SERVER_TEMPLATE_KEYS_CACHE_KEY = "server.template.keys.cache.key";
    private static final String SERVER_TEMPLATE_CACHE_KEY = "server.template.cache.key";
    private static final long DEFAULT_CACHE_TTL = 
            Long.parseLong(System.getProperty(KieServerControllerConstants.KIE_CONTROLLER_TEMPLATE_CACHE_TTL, "60000"));
    private static OpenShiftServerTemplateStorage instance;
    
    protected static SimpleInMemoryCache storageCache = new SimpleInMemoryCache();

    protected KieServerStateOpenShiftRepository repo;
    protected long cacheTTL = DEFAULT_CACHE_TTL;

    protected OpenShiftServerTemplateStorage() {
        this(new KieServerStateOpenShiftRepository());
        logger.info("ServerTemplate cache TTL: {} milliseconds", cacheTTL);
    }

    protected OpenShiftServerTemplateStorage(KieServerStateOpenShiftRepository repo) {
        this.repo = repo;
    }

    public static synchronized OpenShiftServerTemplateStorage getInstance() {
        if (instance == null) {
            instance = new OpenShiftServerTemplateStorage();
        }
        return instance;
    }

    @Override
    public ServerTemplate store(ServerTemplate serverTemplate) {
        if (exists(serverTemplate.getId())) {
            throw new IllegalArgumentException("Server template with id " + serverTemplate.getId() + " is already stored");
        }
        repo.create(ServerTemplateConverter.toState(serverTemplate));
        storageCache.clear();
        return ServerTemplateConverter.fromState(repo.load(serverTemplate.getId()));
    }

    @Override
    public List<ServerTemplateKey> loadKeys() {
        @SuppressWarnings("unchecked")
        List<ServerTemplateKey> result = (List<ServerTemplateKey>) storageCache.get(SERVER_TEMPLATE_KEYS_CACHE_KEY);
        if (result == null) {
            result = repo.retrieveAllKieServerIds()
                         .stream()
                         .map(id -> new ServerTemplateKey(id, id))
                         .collect(Collectors.toList());
            storageCache.add(SERVER_TEMPLATE_KEYS_CACHE_KEY, result, cacheTTL);
        }
        return result;
    }

    @Override
    public List<ServerTemplate> load() {
        @SuppressWarnings("unchecked")
        List<ServerTemplate> result = (List<ServerTemplate>) storageCache.get(SERVER_TEMPLATE_CACHE_KEY);
        if (result == null) {
            result = repo.retrieveAllKieServerStates()
                         .stream()
                         .map(ServerTemplateConverter::fromState)
                         .collect(Collectors.toList());
            storageCache.add(SERVER_TEMPLATE_CACHE_KEY, result, cacheTTL);
        }
        return result;
    }

    @Override
    public ServerTemplate load(String identifier) {
        ServerTemplate template = null;
        try {
            template = ServerTemplateConverter.fromState(repo.load(identifier));
        } catch (Exception e) {
            logger.error("Load server template failed.", e);
        }
        return template;
    }

    @Override
    public boolean exists(String identifier) {
        return repo.exists(identifier);
    }

    @Override
    public ServerTemplate update(ServerTemplate serverTemplate) {
        repo.store(serverTemplate.getId(),ServerTemplateConverter.toState(serverTemplate));
        storageCache.clear();
        return ServerTemplateConverter.fromState(repo.load(serverTemplate.getId()));
    }
    
    /**
     * @throws UnsupportedOperationException when deleting an attached KieServerState.
     */
    @Override
    public ServerTemplate delete(String identifier) {
        storageCache.clear();
        return ServerTemplateConverter.fromState(repo.delete(identifier));
    }

    public static interface Cache {

        void add(String key, Object value, long periodInMillis);

        void remove(String key);

        Object get(String key);

        void clear();

        long size();
    }

    public static class SimpleInMemoryCache implements Cache {

        private final ConcurrentHashMap<String, SoftReference<Object>> cache = new ConcurrentHashMap<>();
        private final DelayQueue<CacheObject> evictQueue = new DelayQueue<>();

        public SimpleInMemoryCache() {
            Thread evictor = new Thread(() -> {
                while (!Thread.currentThread().isInterrupted()) {
                    try {
                        CacheObject cacheEntry = evictQueue.take();
                        cache.remove(cacheEntry.getKey(), cacheEntry.getReference());
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            });
            evictor.setDaemon(true);
            evictor.start();
        }

        @Override
        public void add(String key, Object value, long periodInMillis) {
            if (key == null) {
                return;
            }
            if (value == null) {
                cache.remove(key);
            } else {
                long expiryTime = System.currentTimeMillis() + periodInMillis;
                SoftReference<Object> reference = new SoftReference<>(value);
                cache.put(key, reference);
                evictQueue.put(new CacheObject(key, reference, expiryTime));
            }
        }

        @Override
        public void remove(String key) {
            cache.remove(key);
        }

        @Override
        public Object get(String key) {
            return Optional.ofNullable(cache.get(key)).map(SoftReference::get).orElse(null);
        }

        @Override
        public void clear() {
            cache.clear();
        }

        @Override
        public long size() {
            return cache.size();
        }

        private static class CacheObject implements Delayed {

            public CacheObject(String key, SoftReference<Object> reference, long expiryTime) {
                this.key = key;
                this.reference = reference;
                this.expiryTime = expiryTime;
            }

            private final String key;
            private final SoftReference<Object> reference;
            private final long expiryTime;

            public String getKey() {
                return this.key;
            }

            public SoftReference<Object> getReference() {
                return this.reference;
            }

            @Override
            public long getDelay(TimeUnit unit) {
                return unit.convert(expiryTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
            }

            @Override
            public int compareTo(Delayed o) {
                return Long.compare(expiryTime, ((CacheObject) o).expiryTime);
            }

            @Override
            public boolean equals(Object obj) {
                if (!(obj instanceof CacheObject)) {
                    return false;
                }
                CacheObject cacheObj = (CacheObject) obj;
                return Objects.equals(cacheObj.key, key);
            }

            @Override
            public int hashCode() {
                return Objects.hashCode(key);
            }
        }
    }
}
