/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.services.jbpm.cluster;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.naming.Context;
import javax.naming.InitialContext;

import org.infinispan.Cache;
import org.infinispan.CacheCollection;
import org.infinispan.CacheSet;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.notifications.Listener;
import org.infinispan.notifications.Listener.Observation;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryCreated;
import org.infinispan.notifications.cachelistener.annotation.CacheEntryRemoved;
import org.infinispan.notifications.cachelistener.event.CacheEntryCreatedEvent;
import org.infinispan.notifications.cachelistener.event.CacheEntryRemovedEvent;
import org.infinispan.notifications.cachemanagerlistener.annotation.ViewChanged;
import org.infinispan.notifications.cachemanagerlistener.event.Event;
import org.infinispan.notifications.cachemanagerlistener.event.ViewChangedEvent;
import org.infinispan.remoting.transport.Address;
import org.kie.api.cluster.ClusterAwareService;
import org.kie.api.cluster.ClusterListener;
import org.kie.api.cluster.ClusterNode;
import org.kie.server.api.KieServerConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

@Listener(observation = Observation.POST)
public class InfinispanClusterAwareService implements ClusterAwareService {

    private static final Logger logger = LoggerFactory.getLogger(InfinispanClusterAwareService.class);

    private List<ClusterListener> listeners;

    private String kieServerId;
    private String kieServerLocation;

    public InfinispanClusterAwareService(String kieServerId, String kieServerLocation) {
        this.kieServerId = kieServerId;
        this.kieServerLocation = kieServerLocation;
        listeners = new ArrayList<>();
    }

    public InfinispanClusterAwareService() {
        this(System.getProperty(KieServerConstants.KIE_SERVER_ID), System.getProperty(KieServerConstants.KIE_SERVER_LOCATION));
    }

    
    @Override
    public ClusterNode getThisNode() {
        return new ClusterNode(kieServerId, kieServerLocation);
    }

    private EmbeddedCacheManager lookup () {
        try {
            Context context = new InitialContext();
            return (EmbeddedCacheManager) context.lookup(EJBCacheInitializer.CACHE_NAME_LOOKUP);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    public void init() {
        EmbeddedCacheManager cacheManager = lookup();
        cacheManager.addListener(this);
        Cache<Address, ClusterNode> nodes = cacheManager.<Address, ClusterNode> getCache(CLUSTER_NODES_KEY);
        nodes.addListener(this);
        logger.info("This node is about to join the jBPM cluster {}-{}", kieServerId, kieServerLocation);
        nodes.put(cacheManager.getAddress(), getThisNode());

    }

    @CacheEntryCreated
    public void nodeJoined(CacheEntryCreatedEvent<Address, ClusterNode> event) {
        if(!isCoordinator()) {
            return;
        }
        logger.info("jBPM cluster member joined {}", event.getValue());
        synchronized (listeners) {
            listeners.forEach(listener -> listener.nodeJoined(event.getValue()));
        }
    }

    @CacheEntryRemoved
    public void nodeLeft(CacheEntryRemovedEvent<Address, ClusterNode> event) {
        if(!isCoordinator()) {
            return;
        }
        logger.info("jBPM cluster member left {}", event.getOldValue());
        synchronized (listeners) {
            listeners.forEach(listener -> listener.nodeLeft(event.getOldValue()));
        }
    }

    @ViewChanged
    public void viewChanged(ViewChangedEvent event) {
        EmbeddedCacheManager cacheManager = lookup();
        logger.info("jBPM cluster view changed. Current active nodes: {}", event.getNewMembers());
        if (Event.Type.VIEW_CHANGED.equals(event.getType()) && isCoordinator()) {
            List<Address> changedAddress = event.getNewMembers();
            Cache<Address, ClusterNode> nodes = cacheManager.<Address, ClusterNode> getCache(CLUSTER_NODES_KEY);
            CacheSet<Address> currentView = cacheManager.<Address, ClusterNode> getCache(CLUSTER_NODES_KEY).keySet();

            List<Address> membersLeft = new ArrayList<>();
            Iterator<Address> oldAddresses = currentView.iterator();
            while(oldAddresses.hasNext()) {
                Address address = oldAddresses.next();
                if(!changedAddress.contains(address)) {
                    membersLeft.add(address);
                }
            }
            // forcefully removed
            // address change we remove the nodes from the cache
            membersLeft.forEach(node -> nodes.remove(node));

        }
    }

    @Override
    public boolean isCoordinator() {
        EmbeddedCacheManager cacheManager = lookup();
        return cacheManager.isCoordinator();
    }

    @Override
    public Collection<ClusterNode> getActiveClusterNodes() {
        EmbeddedCacheManager cacheManager = lookup();
        return cacheManager.<Address, ClusterNode> getCache(CLUSTER_NODES_KEY).values();
    }

    @Override
    public <T> void removeData(String key, String partition, T value) {
        EmbeddedCacheManager cacheManager = lookup();
        if (!cacheManager.cacheExists(key)) {
            return;
        }
        Cache<String, List<T>> cache = cacheManager.<String, List<T>> getCache(key);

        synchronized (this) {
            List<T> values = cache.get(partition);
            values = (values == null) ? new ArrayList<>() : new ArrayList<>(values);
            values.remove(value);
            cache.put(partition, values);
        }

    }

    @Override
    public <T> void addData(String key, String partition, T value) {
        EmbeddedCacheManager cacheManager = lookup();
        if (!cacheManager.cacheExists(key)) {
            return;
        }

        Cache<String, List<T>> cache = cacheManager.<String, List<T>> getCache(key);

        synchronized (this) {
            List<T> values = cache.get(partition);
            values = (values == null) ? new ArrayList<>() : new ArrayList<>(values);
            values.add(value);
            cache.put(partition, values);
        }

    }

    @Override
    public <T> List<T> getData(String key) {
        EmbeddedCacheManager cacheManager = lookup();
        if (!cacheManager.cacheExists(key)) {
            return emptyList();
        }
        synchronized (this) {
            CacheCollection<List<T>> values = cacheManager.<String, List<T>> getCache(key).values();
            return values.stream().flatMap(Collection::stream).collect(toList());
        }
    }

    @Override
    public <T> List<T> getDataFromPartition(String key, String partition) {
        EmbeddedCacheManager cacheManager = lookup();
        if (!cacheManager.cacheExists(key)) {
            return emptyList();
        }
        synchronized (this) {
            List<T> values = cacheManager.<String, List<T>> getCache(key).get(partition);
            return values == null ? emptyList() : values;
        }
    }

    @Override
    public void addClusterListener(ClusterListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

}
