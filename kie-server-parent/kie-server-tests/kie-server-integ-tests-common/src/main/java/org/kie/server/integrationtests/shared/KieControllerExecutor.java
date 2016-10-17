/*
 * Copyright 2016 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.server.integrationtests.shared;

import org.jboss.resteasy.plugins.server.tjws.TJWSEmbeddedJaxrsServer;
import org.kie.server.controller.rest.RestKieServerControllerImpl;
import org.kie.server.controller.rest.RestSpecManagementServiceImpl;
import org.kie.server.integrationtests.config.TestConfig;

public class KieControllerExecutor {

    protected TJWSEmbeddedJaxrsServer controller;

    public void startKieController() {
        if (controller != null) {
            throw new RuntimeException("Kie execution controller is already created!");
        }

        controller = new TJWSEmbeddedJaxrsServer();
        controller.setPort(TestConfig.getControllerAllocatedPort());
        controller.start();
        controller.getDeployment().getRegistry().addSingletonResource(new RestKieServerControllerImpl());
        controller.getDeployment().getRegistry().addSingletonResource(new RestSpecManagementServiceImpl());
    }

    public void stopKieController() {
        if (controller == null) {
            throw new RuntimeException("Kie execution controller is already stopped!");
        }
        controller.stop();
        controller = null;
    }
}
