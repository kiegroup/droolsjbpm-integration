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

package org.kie.server.jms.executor;

import javax.annotation.PostConstruct;
import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.jms.Message;

import org.jbpm.executor.impl.ClassCacheManager;
import org.jbpm.executor.impl.ExecutorImpl;
import org.jbpm.executor.impl.ExecutorServiceImpl;
import org.jbpm.executor.impl.jms.JmsAvailableJobsExecutor;
import org.kie.api.executor.ExecutorService;
import org.kie.server.services.api.KieServerExtension;
import org.kie.server.services.impl.KieServerImpl;
import org.kie.server.services.impl.KieServerLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@TransactionManagement(TransactionManagementType.BEAN)
@MessageDriven(name = "KieExecutorMDB", activationConfig = {
        @ActivationConfigProperty(propertyName = "destination", propertyValue = "queue/KIE.SERVER.EXECUTOR"),
        @ActivationConfigProperty(propertyName = "destinationType", propertyValue = "javax.jms.Queue"),
        @ActivationConfigProperty(propertyName = "acknowledgeMode", propertyValue = "Auto-acknowledge")})
public class KieExecutorMDB extends JmsAvailableJobsExecutor {

    private static final Logger logger = LoggerFactory.getLogger(KieExecutorMDB.class);

    private boolean active = true;
    private KieServerImpl kieServer;

    @PostConstruct
    public void init() {
        kieServer = KieServerLocator.getInstance();

        KieServerExtension bpmServerExtension = null;

        for (KieServerExtension extension : kieServer.getServerRegistry().getServerExtensions()) {
            if (extension.isActive() && "BPM".equals(extension.getImplementedCapability())) {
                bpmServerExtension = extension;
            }
        }

        if (bpmServerExtension == null) {
            logger.warn("No BPM capability found on the server, ExecutorMDB is deactivated");
            active = false;
            return;
        }

        ExecutorService executorService = bpmServerExtension.getAppComponents(ExecutorService.class);

        if (executorService == null) {
            logger.warn("Unable to find ExecutorService within {} extension, deactivating ExecutorMDB", bpmServerExtension);
            active = false;
            return;
        }
        setClassCacheManager(new ClassCacheManager());
        setQueryService(((ExecutorServiceImpl) executorService).getQueryService());
        setExecutorStoreService(((ExecutorImpl) ((ExecutorServiceImpl) executorService).getExecutor()).getExecutorStoreService());
        setExecutor(((ExecutorServiceImpl) executorService).getExecutor());

    }

    @Override
    public void onMessage(Message message) {
        if (!active) {
            throw new RuntimeException("ExecutorMDB is not active most likely due to missing KieServer capabilities, check startup logs");
        }

        super.onMessage(message);
    }

}
