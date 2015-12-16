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

package org.kie.server.services.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.kie.internal.identity.IdentityProvider;
import org.kie.server.api.KieServerEnvironment;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieServerConfig;
import org.kie.server.services.api.KieServerExtension;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.impl.storage.KieServerState;
import org.kie.server.services.impl.storage.KieServerStateRepository;

public class KieServerRegistryImpl implements KieServerRegistry {

    private final ConcurrentMap<String, KieContainerInstanceImpl> containers = new ConcurrentHashMap<String, KieContainerInstanceImpl>();
    private IdentityProvider identityProvider;
    private ConcurrentMap<String, KieServerExtension> serverExtensions = new ConcurrentHashMap<String, KieServerExtension>();

    private Set<String> controllers = new CopyOnWriteArraySet<String>();

    private KieServerStateRepository repository;

    private KieSessionLookupManager kieSessionLookupManager = new KieSessionLookupManager();

    @Override
    public KieContainerInstanceImpl registerContainer(String id, KieContainerInstanceImpl kieContainerInstance) {
        synchronized ( containers ) {
            KieContainerInstanceImpl kci = containers.putIfAbsent(id, kieContainerInstance);
            if( kci != null && kci.getStatus() == KieContainerStatus.FAILED ) {
                // if previous container failed, allow override
                containers.put(id, kieContainerInstance);
                return null;
            }
            return kci;
        }
    }

    @Override
    public KieContainerInstanceImpl unregisterContainer(String id) {
        return containers.remove(id);
    }

    @Override
    public KieContainerInstanceImpl getContainer(String id) {
        return containers.get(id);
    }

    @Override
    public List<KieContainerInstanceImpl> getContainers() {
        return new ArrayList<KieContainerInstanceImpl>(containers.values());
    }

    @Override
    public void registerIdentityProvider(IdentityProvider identityProvider) {
        this.identityProvider = identityProvider;
    }

    @Override
    public IdentityProvider unregisterIdentityProvider() {
        IdentityProvider unregistered = this.identityProvider;

        this.identityProvider = null;
        return unregistered;
    }

    @Override
    public IdentityProvider getIdentityProvider() {
        return this.identityProvider;
    }

    @Override
    public void registerServerExtension(KieServerExtension kieServerExtension) {
        this.serverExtensions.putIfAbsent(kieServerExtension.getExtensionName(), kieServerExtension);
    }

    @Override
    public void unregisterServerExtension(KieServerExtension kieServerExtension) {
        this.serverExtensions.remove(kieServerExtension);
    }

    @Override
    public List<KieServerExtension> getServerExtensions() {
        return new ArrayList<KieServerExtension>(serverExtensions.values());
    }

    @Override
    public KieServerExtension getServerExtension(String extensionName) {
        return serverExtensions.get(extensionName);
    }

    @Override
    public void registerController(String controllerUrl) {
        this.controllers.add(controllerUrl);
    }

    @Override
    public Set<String> getControllers() {

        return new HashSet<String>(controllers);
    }

    @Override
    public void registerStateRepository(KieServerStateRepository repository) {
        this.repository = repository;
    }

    @Override
    public KieServerStateRepository getStateRepository() {
        return this.repository;
    }

    @Override
    public KieSessionLookupManager getKieSessionLookupManager() {
        return kieSessionLookupManager;
    }

    @Override
    public KieServerConfig getConfig() {
        KieServerState currentState = repository.load(KieServerEnvironment.getServerId());
        return currentState.getConfiguration();
    }
}
