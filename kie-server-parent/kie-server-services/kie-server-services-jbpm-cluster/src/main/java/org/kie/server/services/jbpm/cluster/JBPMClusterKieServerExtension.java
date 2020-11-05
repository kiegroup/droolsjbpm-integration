/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.services.jbpm.cluster;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.jbpm.executor.impl.ExecutorServiceImpl;
import org.kie.api.cluster.ClusterAwareService;
import org.kie.api.executor.ExecutorService;
import org.kie.api.internal.utils.ServiceRegistry;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.model.Message;
import org.kie.server.api.model.Severity;
import org.kie.server.services.api.KieContainerInstance;
import org.kie.server.services.api.KieServerApplicationComponentsService;
import org.kie.server.services.api.KieServerExtension;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.api.SupportedTransports;
import org.kie.server.services.impl.KieServerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JBPMClusterKieServerExtension implements KieServerExtension {

    public static final String EXTENSION_NAME = "jBPM-Cluster";

    private static final Logger logger = LoggerFactory.getLogger(JBPMClusterKieServerExtension.class);

    private static final Boolean disabled = Boolean.parseBoolean(System.getProperty(KieServerConstants.KIE_JBPM_CLUSTER_SERVER_EXT_DISABLED, "false"));
    private static final Boolean jbpmDisabled = Boolean.parseBoolean(System.getProperty(KieServerConstants.KIE_JBPM_SERVER_EXT_DISABLED, "false"));

    private boolean initialized = false;

    private KieServerRegistry registry;

    private ExecutorService jbpmExecutorService;
    private ClusterAwareService clusterService;

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public boolean isActive() {
        return !disabled && !jbpmDisabled;
    }

    @Override
    public void init(KieServerImpl kieServer, KieServerRegistry registry) {

        this.registry = registry;
        KieServerExtension jbpmExtension = registry.getServerExtension("jBPM");
        if (jbpmExtension == null) {
            initialized = false;
            logger.warn("jBPM extension not found, jBPM Cluster cannot work without jBPM extension, disabling itself");
            return;
        }

        configureServices();

        // this implements fail over for jobs
        ExecutorServiceImpl service = (ExecutorServiceImpl) jbpmExecutorService;
        ClusteredJobFailOverListener clusteredJobFailOverListener = new ClusteredJobFailOverListener(clusterService, service);
        service.addAsyncJobListener(clusteredJobFailOverListener);
        clusterService.addClusterListener(clusteredJobFailOverListener);

        initialized = true;
    }

    private void configureServices () {
        KieServerExtension jbpmExtension = registry.getServerExtension("jBPM");
        List<Object> jbpmServices = jbpmExtension.getServices();
        

        for (Object object : jbpmServices) {
            // in case given service is null (meaning was not configured) continue with next one
            if (object == null) {
                continue;
            }
            if (ExecutorService.class.isAssignableFrom(object.getClass())) {
                this.jbpmExecutorService = (ExecutorService) object;
            } 
        }

        clusterService = ServiceRegistry.getService(ClusterAwareService.class);
    }
    
    @Override
    public void destroy(KieServerImpl kieServer, KieServerRegistry registry) {
        // do nothing
    }

    @Override
    public void createContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
        // do nothing
    }

    @Override
    public void updateContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
        // do nothing
    }

    @Override
    public boolean isUpdateContainerAllowed(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
        return true;
    }

    @Override
    public void disposeContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
        // do nothing
    }

    @Override
    public List<Object> getAppComponents(SupportedTransports type) {
        List<Object> appComponentsList = new ArrayList<>();
        Object[] services = {clusterService};
        ServiceLoader<KieServerApplicationComponentsService> appComponentsServices = ServiceLoader.load(KieServerApplicationComponentsService.class);

        for( KieServerApplicationComponentsService appComponentsService : appComponentsServices ) {
            appComponentsList.addAll(appComponentsService.getAppComponents(EXTENSION_NAME, type, services));
        }
        return appComponentsList;


    }

    @Override
    public <T> T getAppComponents(Class<T> serviceType) {
        Object[] services = {clusterService};

        for (Object service : services) {
            if (service != null && serviceType.isAssignableFrom(service.getClass())) {
                return serviceType.cast(service);
            }
        }
        return null;
    }

    @Override
    public String getImplementedCapability() {
        return KieServerConstants.CAPABILITY_JBPM_CLUSTER;
    }

    @Override
    public List<Object> getServices() {
        return Collections.singletonList(clusterService);
    }

    @Override
    public String getExtensionName() {
        return EXTENSION_NAME;
    }

    @Override
    public Integer getStartOrder() {
        return 10;
    }

    @Override
    public String toString() {
        return EXTENSION_NAME + " KIE Server extension";
    }

    @Override
    public List<Message> healthCheck(boolean report) {
        List<Message> messages = KieServerExtension.super.healthCheck(report);
        
        if (report) {
            messages.add(new Message(Severity.INFO, getExtensionName() + " is alive"));
        }        
        return messages;
    }
}
