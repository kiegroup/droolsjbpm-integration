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

package org.kie.server.services.impl.storage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieServerConfig;
import org.kie.server.api.model.KieServerConfigItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@XStreamAlias( "kie-server" )
public class KieServerState {

    private static final Logger logger = LoggerFactory.getLogger(KieServerState.class);

    @XStreamImplicit(itemFieldName = "controller")
    private Set<String> controllers = new HashSet<String>();

    private KieServerConfig configuration;

    @XStreamAlias( "containers" )
    @XStreamImplicit
    private Set<KieContainerResource> containers = new HashSet<KieContainerResource>();

    public KieServerState() {
        String defaultController = System.getProperty(KieServerConstants.KIE_SERVER_CONTROLLER);
        if (defaultController != null) {
            String[] controllerList = defaultController.split(",");

            for (String controller : controllerList) {
                controllers.add(controller.trim());
                logger.info("Added default controller located at {}", controller);
            }
        }
    }

    public Set<String> getControllers() {
        return controllers;
    }

    public void setControllers(Set<String> controllers) {
        this.controllers = controllers;
    }

    public Set<KieContainerResource> getContainers() {
        return containers;
    }

    public void setContainers(Set<KieContainerResource> containers) {
        this.containers = containers;
    }

    public KieServerConfig getConfiguration() {
        return configuration;
    }

    public void setConfiguration(KieServerConfig configuration) {
        if (this.configuration != null) {
            // if config already exists merge it with precedence given by the argument
            for (KieServerConfigItem item : configuration.getConfigItems()) {
                KieServerConfigItem existing = this.configuration.getConfigItem(item.getName());
                if (existing != null) {
                    this.configuration.removeConfigItem(existing);
                }
                configuration.addConfigItem(item);
            }
        } else {
            this.configuration = configuration;
        }
    }
}
