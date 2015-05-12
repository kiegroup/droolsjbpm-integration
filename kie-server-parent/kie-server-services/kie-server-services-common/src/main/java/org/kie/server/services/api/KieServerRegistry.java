package org.kie.server.services.api;

import java.util.List;
import java.util.Set;

import org.kie.internal.identity.IdentityProvider;
import org.kie.server.services.impl.KieContainerInstanceImpl;

public interface KieServerRegistry {

    KieContainerInstanceImpl registerContainer(String id, KieContainerInstanceImpl kieContainerInstance);

    KieContainerInstanceImpl unregisterContainer(String id);

    KieContainerInstanceImpl getContainer(String id);

    List<KieContainerInstanceImpl> getContainers();

    void registerIdentityProvider(IdentityProvider identityProvider);

    IdentityProvider unregisterIdentityProvider();

    IdentityProvider getIdentityProvider();

    void registerServerExtension(KieServerExtension kieServerExtension);

    void unregisterServerExtension(KieServerExtension kieServerExtension);

    List<KieServerExtension> getServerExtensions();

    void registerController(String controllerUrl);

    Set<String> getControllers();
}
