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

package org.kie.server.jms;

import java.util.Set;

import javax.ejb.Asynchronous;
import javax.ejb.Singleton;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;

import org.kie.server.api.model.KieContainerResource;
import org.kie.server.controller.api.model.KieServerSetup;
import org.kie.server.services.impl.ContainerManager;
import org.kie.server.services.impl.KieServerImpl;
import org.kie.server.services.impl.storage.KieServerState;

@Singleton(name = "ContainerManagerEJB")
@TransactionManagement(TransactionManagementType.BEAN)
public class ContainerManagerEJB extends ContainerManager {

    @Asynchronous
    @Override
    public void installContainers(KieServerImpl kieServer, Set<KieContainerResource> containers, KieServerState currentState, KieServerSetup kieServerSetup) {
        super.installContainers(kieServer, containers, currentState, kieServerSetup);
    }
}
