/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.log;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.maven.plugin.logging.Log;
import org.kie.server.api.model.Message;
import org.kie.server.controller.api.model.runtime.ServerInstanceKey;
import org.kie.server.controller.api.model.spec.Capability;
import org.kie.server.controller.api.model.spec.ContainerConfig;
import org.kie.server.controller.api.model.spec.ContainerSpec;
import org.kie.server.controller.api.model.spec.ServerTemplate;

public class LogHelper {

    public static void logServerTemplate(Log log, ServerTemplate serverTemplate) {

        log.info("--- Server Template --- ");

        log.info("Server Template: " + serverTemplate.getId());
        log.info("Capabilities: " + serverTemplate.getCapabilities());

        for (ServerInstanceKey server : serverTemplate.getServerInstanceKeys()) {
            log.info("  Server: " + server.getUrl());
        }

        logContainers(log, serverTemplate.getContainersSpec());
        log.info("----------------------- ");

    }

    public static void logContainers(Log log, Collection<ContainerSpec> containers) {

        for (ContainerSpec container : containers) {

            logContainer(log, container);


        }

    }

    public static void logContainer(Log log, ContainerSpec container) {

        log.info("  --- Container --- ");

        log.info("  Container: " + container.getId());
        log.info("  Release: " + container.getReleasedId());
        log.info("  Status: " + container.getStatus());

        for (Map.Entry<Capability, ContainerConfig> capability : container.getConfigs().entrySet()) {

            log.info("    Capability: " + capability.getKey().toString());
            log.info("    Config: " + capability.getValue());

        }

        log.info("  ----------------- ");

    }

    public static String read(List<Message> messageList) {

        StringBuilder stringBuilder = new StringBuilder();

        for (Message message : messageList) {
            stringBuilder.append(message.getMessages());
            stringBuilder.append(" on date ");
            stringBuilder.append(message.getTimestamp());
        }

        return stringBuilder.toString();

    }

}
