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

package org.kie.server.jms.executor;

import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;

import org.jbpm.executor.impl.AvailableJobsExecutor;
import org.jbpm.executor.impl.ClassCacheManager;
import org.jbpm.executor.impl.ExecutorImpl;
import org.jbpm.executor.impl.ExecutorServiceImpl;
import org.kie.api.executor.ExecutorService;
import org.kie.server.services.api.KieServerExtension;
import org.kie.server.services.api.SupportedTransports;
import org.kie.server.services.impl.KieServerImpl;
import org.kie.server.services.impl.KieServerLocator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Stateless(name="AvailableJobsExecutor")
@TransactionManagement(TransactionManagementType.BEAN)
public class PollExecutorBean extends AvailableJobsExecutor {

    private static final Logger logger = LoggerFactory.getLogger(PollExecutorBean.class);

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
            logger.warn("No BPM capability found on the server, PollExecutorBean is deactivated");
            active = false;
            return;
        }


        ExecutorService executorService = bpmServerExtension.getAppComponents(ExecutorService.class);

        if (executorService == null) {
            logger.warn("Unable to find ExecutorService within {} extension, deactivating PollExecutorBean", bpmServerExtension);
            active = false;
            return;
        }
        setClassCacheManager(new ClassCacheManager());
        setQueryService(((ExecutorServiceImpl) executorService).getQueryService());
        setExecutorStoreService(((ExecutorImpl) ((ExecutorServiceImpl) executorService).getExecutor()).getExecutorStoreService());

    }

    @Override
    @Asynchronous
    public void executeJob() {
        if (!active) {
            logger.warn("PollExecutor is not active due to startup errors");
        }
        super.executeJob();
    }



}