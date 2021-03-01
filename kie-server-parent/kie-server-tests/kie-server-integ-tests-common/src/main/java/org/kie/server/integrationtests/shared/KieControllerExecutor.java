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

import javax.ws.rs.core.Application;

import org.jboss.resteasy.plugins.server.undertow.UndertowJaxrsServer;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.kie.server.controller.service.StandaloneKieServerControllerImpl;
import org.kie.server.controller.service.StandaloneSpecManagementServiceImpl;

import static io.undertow.Undertow.builder;
import static java.util.Arrays.asList;
import static org.kie.server.integrationtests.config.TestConfig.getControllerAllocatedPort;

public class KieControllerExecutor {

    protected UndertowJaxrsServer controller;

    public void startKieController() {
        if (controller != null) {
            throw new RuntimeException("Kie execution controller is already created!");
        }

        controller = new UndertowJaxrsServer();
        controller.start(builder().addHttpListener(getControllerAllocatedPort(), "localhost"));
        ResteasyDeployment deployment = new ResteasyDeployment();
        deployment.setApplication(new Application());
        deployment.setResources(asList(new StandaloneSpecManagementServiceImpl(), new StandaloneKieServerControllerImpl()));
        controller.deploy(deployment);
    }

    public void stopKieController() {
        if (controller == null) {
            throw new RuntimeException("Kie execution controller is already stopped!");
        }
        controller.stop();
        controller = null;
    }
}
