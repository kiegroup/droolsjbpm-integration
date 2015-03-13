package org.kie.server.services.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

import org.kie.internal.identity.IdentityProvider;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.services.api.KieServerExtension;
import org.kie.server.services.api.KieServerRegistry;

public class KieServerRegistryImpl implements KieServerRegistry {

    private final ConcurrentMap<String, KieContainerInstanceImpl> containers = new ConcurrentHashMap<String, KieContainerInstanceImpl>();
    private IdentityProvider identityProvider;
    private Set<KieServerExtension> serverExtensions = new CopyOnWriteArraySet<KieServerExtension>();

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
        this.serverExtensions.add(kieServerExtension);
    }

    @Override
    public void unregisterServerExtension(KieServerExtension kieServerExtension) {
        this.serverExtensions.remove(kieServerExtension);
    }

    @Override
    public List<KieServerExtension> getServerExtensions() {
        return new ArrayList<KieServerExtension>(serverExtensions);
    }
}
