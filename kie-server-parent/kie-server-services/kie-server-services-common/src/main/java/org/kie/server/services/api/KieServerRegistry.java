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

package org.kie.server.services.api;

import java.util.List;
import java.util.Set;

import org.kie.internal.identity.IdentityProvider;
import org.kie.server.api.model.KieServerConfig;
import org.kie.server.services.impl.KieContainerInstanceImpl;
import org.kie.server.services.impl.KieSessionLookupManager;
import org.kie.server.services.impl.storage.KieServerStateRepository;

public interface KieServerRegistry {

    KieContainerInstanceImpl registerContainer(String id, KieContainerInstanceImpl kieContainerInstance);

    KieContainerInstanceImpl unregisterContainer(String id);

    KieContainerInstanceImpl getContainer(String id);

    KieContainerInstanceImpl getContainer(String alias, ContainerLocator locator);

    List<KieContainerInstanceImpl> getContainers();

    void registerIdentityProvider(IdentityProvider identityProvider);

    IdentityProvider unregisterIdentityProvider();

    IdentityProvider getIdentityProvider();

    void registerServerExtension(KieServerExtension kieServerExtension);

    void unregisterServerExtension(KieServerExtension kieServerExtension);

    List<KieServerExtension> getServerExtensions();

    KieServerExtension getServerExtension(String extensionName);

    void registerController(String controllerUrl);

    Set<String> getControllers();

    KieServerConfig getConfig();

    void registerStateRepository(KieServerStateRepository repository);

    KieServerStateRepository getStateRepository();

    KieSessionLookupManager getKieSessionLookupManager();
}
