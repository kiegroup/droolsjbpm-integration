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

package org.kie.server.integrationtests.controller;

import org.codehaus.cargo.container.Container;
import org.codehaus.cargo.container.ContainerType;
import org.codehaus.cargo.container.RemoteContainer;
import org.codehaus.cargo.container.configuration.Configuration;
import org.codehaus.cargo.container.configuration.ConfigurationType;
import org.codehaus.cargo.container.deployable.DeployableType;
import org.codehaus.cargo.container.deployable.WAR;
import org.codehaus.cargo.container.deployer.Deployer;
import org.codehaus.cargo.container.property.RemotePropertySet;
import org.codehaus.cargo.container.property.ServletPropertySet;
import org.codehaus.cargo.container.weblogic.WebLogicPropertySet;
import org.codehaus.cargo.generic.DefaultContainerFactory;
import org.codehaus.cargo.generic.configuration.DefaultConfigurationFactory;
import org.codehaus.cargo.generic.deployable.DefaultDeployableFactory;
import org.codehaus.cargo.generic.deployer.DefaultDeployerFactory;
import org.kie.server.integrationtests.config.TestConfig;

public class ContainerRemoteController {

    private Configuration configuration;
    private Container container;
    private Deployer deployer;

    public ContainerRemoteController(String cargoContainerId, String containerPort) {
        configuration = new DefaultConfigurationFactory().createConfiguration(
                cargoContainerId, ContainerType.REMOTE, ConfigurationType.RUNTIME);
        container = (RemoteContainer) new DefaultContainerFactory().createContainer(
                cargoContainerId, ContainerType.REMOTE, configuration);
        deployer = new DefaultDeployerFactory().createDeployer(container);

        configuration.setProperty(ServletPropertySet.PORT, containerPort);

        if(TestConfig.isCargoRemoteUsernameProvided()) {
            configuration.setProperty(RemotePropertySet.USERNAME, TestConfig.getCargoRemoteUsername());
        }
        if(TestConfig.isCargoRemotePasswordProvided()) {
            configuration.setProperty(RemotePropertySet.PASSWORD, TestConfig.getCargoRemotePassword());
        }

        // WLS remote configuration
        if (TestConfig.isWebLogicHomeProvided()) {
            String wlserverHome = TestConfig.getWebLogicHome().matches(".*/wlserver") ?
                    TestConfig.getWebLogicHome() : TestConfig.getWebLogicHome() + "/wlserver";
            configuration.setProperty(WebLogicPropertySet.LOCAL_WEBLOGIC_HOME, wlserverHome);
        }
    }

    public void undeployWarFile(String context, String warFilePath) {
        WAR deployable = (WAR) new DefaultDeployableFactory().createDeployable(container.getId(), warFilePath, DeployableType.WAR);
        deployable.setContext(context);
        deployer.undeploy(deployable);
    }

    public void deployWarFile(String context, String warFilePath) {
        WAR deployable = (WAR) new DefaultDeployableFactory().createDeployable(container.getId(), warFilePath, DeployableType.WAR);
        deployable.setContext(context);
        deployer.deploy(deployable);
    }
}
