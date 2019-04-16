/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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
package org.kie.server.services.prometheus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.apache.commons.lang3.StringUtils;
import org.dashbuilder.dataset.def.DataSetDefRegistry;
import org.jbpm.casemgmt.api.model.CaseStatus;
import org.jbpm.executor.impl.ExecutorServiceImpl;
import org.jbpm.kie.services.impl.KModuleDeploymentService;
import org.jbpm.kie.services.impl.query.QueryServiceImpl;
import org.jbpm.process.instance.ProcessInstance;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.model.ProcessInstanceDesc;
import org.kie.api.runtime.query.QueryContext;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.model.Message;
import org.kie.server.api.model.Severity;
import org.kie.server.api.model.cases.CaseInstance;
import org.kie.server.api.model.cases.CaseInstanceList;
import org.kie.server.services.api.KieContainerInstance;
import org.kie.server.services.api.KieServerApplicationComponentsService;
import org.kie.server.services.api.KieServerExtension;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.api.SupportedTransports;
import org.kie.server.services.casemgmt.CaseKieServerExtension;
import org.kie.server.services.casemgmt.CaseManagementRuntimeDataServiceBase;
import org.kie.server.services.impl.KieServerImpl;
import org.kie.server.services.jbpm.JbpmKieServerExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.util.Arrays.asList;
import static org.kie.server.services.prometheus.PrometheusCaseEventListener.recordRunningCaseInstance;
import static org.kie.server.services.prometheus.PrometheusProcessEventListener.recordRunningProcessInstance;

public class PrometheusKieServerExtension implements KieServerExtension {

    public static final String EXTENSION_NAME = "Prometheus";

    private static final Logger LOGGER = LoggerFactory.getLogger(PrometheusKieServerExtension.class);
    private static final Boolean disabled = Boolean.parseBoolean(System.getProperty(KieServerConstants.KIE_PROMETHEUS_SERVER_EXT_DISABLED, "true"));
    private static final String DESCRIPTOR = "org.kie.deployment.desc.location";
    private static PrometheusMetrics METRICS = null;
    
    private KieServerRegistry context;
    private boolean initialized = false;

    public static PrometheusMetrics getMetrics() {
        if (METRICS == null) {
            METRICS = new PrometheusMetrics();
        }
        return METRICS;
    }

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public boolean isActive() {
        return !disabled;
    }

    @Override
    public void init(KieServerImpl kieServer, KieServerRegistry registry) {
        this.context = registry;

        registerDefaultDescriptor();

        //Prometheus Monitoring
        KieServerExtension jBPMExtension = context.getServerExtension(JbpmKieServerExtension.EXTENSION_NAME);
        if (jBPMExtension != null) {

            final KModuleDeploymentService deploymentService = jBPMExtension.getAppComponents(KModuleDeploymentService.class);
            if (deploymentService != null) {
                deploymentService.addListener(new PrometheusDeploymentEventListener());
            }

            final ExecutorServiceImpl executorService = jBPMExtension.getAppComponents(ExecutorServiceImpl.class);
            if (executorService != null) {
                executorService.addAsyncJobListener(new PrometheusJobListener());
            }

            final QueryServiceImpl queryService = jBPMExtension.getAppComponents(QueryServiceImpl.class);
            if (queryService != null) {
                final DataSetDefRegistry dataSetDefRegistry = queryService.getDataSetDefRegistry();
                final PrometheusDataSetListener listener = new PrometheusDataSetListener(dataSetDefRegistry);
                listener.init();
                dataSetDefRegistry.addListener(listener);
            }

            final RuntimeDataService dataService = jBPMExtension.getAppComponents(RuntimeDataService.class);
            if (dataService != null) {
                final Collection<ProcessInstanceDesc> processInstances = dataService.getProcessInstances(
                        asList(ProcessInstance.STATE_ACTIVE), null, new QueryContext(0, Integer.MAX_VALUE));
                processInstances.forEach(pi -> recordRunningProcessInstance(pi.getDeploymentId(), pi.getProcessId()));
            }
        }

        KieServerExtension caseExtension = context.getServerExtension(CaseKieServerExtension.EXTENSION_NAME);
        if (caseExtension != null) {
            final CaseManagementRuntimeDataServiceBase caseRuntime = caseExtension.getAppComponents(CaseManagementRuntimeDataServiceBase.class);
            if (caseRuntime != null) {
                final CaseInstanceList caseInstances = caseRuntime.getCaseInstances(asList(CaseStatus.OPEN.getName()), 0, Integer.MAX_VALUE, null, false);
                for (CaseInstance instance : caseInstances.getCaseInstances()) {
                    recordRunningCaseInstance(instance.getCaseDefinitionId());
                }
            }
        }

        initialized = true;
        LOGGER.info("{} started", toString());
    }

    protected void registerDefaultDescriptor() {
        final String desc = System.getProperty(DESCRIPTOR);
        if(StringUtils.isBlank(desc)) {
            System.setProperty(DESCRIPTOR, "classpath:/META-INF/prometheus-deployment-descriptor-defaults.xml");
        } else {
            LOGGER.warn("{} property already defined, Case Mgmt, Process and Task metrics might not be available if listeners are not declared on {}", DESCRIPTOR, desc);
        }
    }

    @Override
    public void destroy(KieServerImpl kieServer, KieServerRegistry registry) {
        //no-op
    }

    @Override
    public void createContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
        //no-op
    }

    @Override
    public boolean isUpdateContainerAllowed(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
        return true;
    }

    @Override
    public void updateContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
        //no-op
    }

    @Override
    public void disposeContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
        //no-op
    }

    @Override
    public List<Object> getAppComponents(SupportedTransports type) {
        ServiceLoader<KieServerApplicationComponentsService> appComponentsServices
                = ServiceLoader.load(KieServerApplicationComponentsService.class);
        List<Object> appComponentsList = new ArrayList<Object>();
        Object[] services = {context};
        for (KieServerApplicationComponentsService appComponentsService : appComponentsServices) {
            appComponentsList.addAll(appComponentsService.getAppComponents(EXTENSION_NAME, type, services));
        }

        return appComponentsList;
    }

    @Override
    public <T> T getAppComponents(Class<T> serviceType) {
        return null;
    }

    @Override
    public String getImplementedCapability() {
        return KieServerConstants.CAPABILITY_PROMETHEUS;
    }

    @Override
    public List<Object> getServices() {
        return Collections.emptyList();
    }

    @Override
    public String getExtensionName() {
        return EXTENSION_NAME;
    }

    @Override
    public Integer getStartOrder() {
        return Integer.MAX_VALUE;
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