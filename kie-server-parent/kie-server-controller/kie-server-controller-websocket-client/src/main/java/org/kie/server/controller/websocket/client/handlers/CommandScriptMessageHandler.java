/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.controller.websocket.client.handlers;

import org.kie.server.api.KieServerConstants;
import org.kie.server.api.commands.CommandScript;
import org.kie.server.api.commands.DescriptorCommand;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.ServiceResponsesList;
import org.kie.server.controller.websocket.common.handlers.InternalMessageHandler;
import org.kie.server.services.api.KieContainerCommandService;
import org.kie.server.services.api.KieServerExtension;
import org.kie.server.services.api.KieServerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class CommandScriptMessageHandler implements InternalMessageHandler {
    
    private static final Logger logger = LoggerFactory.getLogger(CommandScriptMessageHandler.class);

    private static final String TARGET_CAPABILITY = "KieServer";

    
    private KieServerRegistry context;

    public CommandScriptMessageHandler(KieServerRegistry context) {
        this.context = context;
    }
    
    @Override
    public String onMessage(String message) {
        CommandScript script = deserialize(message, CommandScript.class);
        String capability = TARGET_CAPABILITY;
        boolean hasBPMCommand = script.getCommands().stream().anyMatch(cmd -> cmd instanceof DescriptorCommand);
        if (hasBPMCommand) {
            capability = KieServerConstants.CAPABILITY_BPM;
        }
        
        KieContainerCommandService<?> executor = null;
        for (KieServerExtension extension : context.getServerExtensions()) {
            KieContainerCommandService<?> tmp = extension.getAppComponents(KieContainerCommandService.class);

            if (tmp != null && extension.getImplementedCapability().equalsIgnoreCase(capability)) {
                executor = tmp;
                logger.debug("Extension {} returned command executor {} with capability {}", extension, executor, extension.getImplementedCapability());
                break;
            }
        }
        if (executor == null) {
            throw new IllegalStateException("No executor found for script execution");
        }

        // 4. process request
        ServiceResponsesList response = executor.executeScript(script, MarshallingFormat.JSON, null);
        
        String reply = serialize(response);
        return reply;
    }
    
    
    
    @Override
    public InternalMessageHandler getNextHandler() {        
        return this;
    }
    

}
