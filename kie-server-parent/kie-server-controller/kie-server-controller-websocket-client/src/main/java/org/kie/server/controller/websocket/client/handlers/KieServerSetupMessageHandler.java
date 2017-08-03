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

import java.util.concurrent.CountDownLatch;

import org.kie.server.controller.api.model.KieServerSetup;
import org.kie.server.controller.websocket.common.handlers.InternalMessageHandler;
import org.kie.server.services.api.KieServerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class KieServerSetupMessageHandler implements InternalMessageHandler {

    private static final Logger logger = LoggerFactory.getLogger(KieServerSetupMessageHandler.class);
    
    private KieServerRegistry context;
    private CountDownLatch waitLatch;
    private KieServerSetup kieServerSetup;
    
    public KieServerSetupMessageHandler(KieServerRegistry context, CountDownLatch waitLatch, KieServerSetup kieServerSetup) {
        this.context = context;
        this.waitLatch = waitLatch;
        this.kieServerSetup = kieServerSetup;
    }

    @Override
    public String onMessage(String message) {
        try {
            logger.debug("Received message with setup information {}", message);
            KieServerSetup setup = deserialize(message, KieServerSetup.class);
            
            kieServerSetup.setContainers(setup.getContainers());
            kieServerSetup.setServerConfig(setup.getServerConfig());
        } finally {
            waitLatch.countDown();
        }
        
        return null;
    }

    @Override
    public InternalMessageHandler getNextHandler() {
        return new CommandScriptMessageHandler(context);
    }
    
    

}
