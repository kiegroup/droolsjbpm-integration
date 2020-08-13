/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.services.impl;

import java.util.List;
import java.util.Map;

import org.kie.api.builder.Message.Level;
import org.kie.api.builder.ReleaseId;
import org.kie.api.event.kiescanner.KieScannerEventListener;
import org.kie.api.event.kiescanner.KieScannerStatusChangeEvent;
import org.kie.api.event.kiescanner.KieScannerUpdateResultsEvent;
import org.kie.server.services.api.KieServerExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class KieServerScannerEventListener implements KieScannerEventListener {

    private static final Logger logger = LoggerFactory.getLogger(KieServerScannerEventListener.class);
    
    private KieServerImpl kieServer;
    private KieContainerInstanceImpl kieContainerInstance;

    public KieServerScannerEventListener(KieServerImpl kieServer, KieContainerInstanceImpl kieContainerInstance) {
        super();
        this.kieServer = kieServer;
        this.kieContainerInstance = kieContainerInstance;
    }

    @Override
    public void onKieScannerStatusChangeEvent(KieScannerStatusChangeEvent statusChange) {
        
    }

    @Override
    public void onKieScannerUpdateResultsEvent(KieScannerUpdateResultsEvent updateResults) {
        if (!updateResults.getResults().hasMessages(Level.ERROR)) {
            List<KieServerExtension> extensions = kieServer.getServerExtensions();
            String containerId = kieContainerInstance.getContainerId();
            ReleaseId releaseId = kieContainerInstance.getKieContainer().getContainerReleaseId();

            Map<String, Object> parameters = kieServer.getContainerParameters(releaseId, 
                    kieServer.getMessagesForContainer(containerId));
            
            for (KieServerExtension extension : extensions) {
                boolean allowed = extension.isUpdateContainerAllowed(containerId, kieContainerInstance, parameters);
                if (!allowed) {
                    logger.error("Unable to update container {} due to it is not allowed by extension {}", containerId, extension);
                    return;
                }
            }
            
            kieContainerInstance.clearExtraClasses();
            boolean disposedMarshallers = kieContainerInstance.updateReleaseId();
            // on scanner successful update dispose marshallers
            if (!disposedMarshallers) {
                kieContainerInstance.disposeMarshallers();
            }
            

            // once the upgrade was successful, notify all extensions so they can be upgraded (if needed)
            for (KieServerExtension extension : extensions) {
                extension.updateContainer(containerId, kieContainerInstance, parameters);  
                logger.debug("Container {} (for release id {}) on {} updated successfully", containerId, releaseId, extension);
            }
        }
    }

}
