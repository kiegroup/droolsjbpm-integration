/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.services.taskassigning.runtime;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.apache.commons.io.IOUtils;
import org.jbpm.kie.services.impl.query.SqlQueryDefinition;
import org.jbpm.services.api.UserTaskService;
import org.jbpm.services.api.query.QueryService;
import org.kie.server.api.exception.KieServicesException;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.Message;
import org.kie.server.api.model.Severity;
import org.kie.server.api.model.definition.QueryDefinition;
import org.kie.server.api.model.taskassigning.LocalDateTimeValue;
import org.kie.server.services.api.KieContainerInstance;
import org.kie.server.services.api.KieServerApplicationComponentsService;
import org.kie.server.services.api.KieServerExtension;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.api.SupportedTransports;
import org.kie.server.services.impl.KieServerImpl;
import org.kie.server.services.jbpm.JbpmKieServerExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.server.api.KieServerConstants.KIE_TASK_ASSIGNING_RUNTIME_EXT_DISABLED;
import static org.kie.server.api.model.taskassigning.QueryParamName.TO_TASK_ID;
import static org.kie.server.services.taskassigning.runtime.TaskAssigningRuntimeKieServerExtensionMessages.HEALTH_CHECK_ERROR;
import static org.kie.server.services.taskassigning.runtime.TaskAssigningRuntimeKieServerExtensionMessages.HEALTH_CHECK_IS_ALIVE_MESSAGE;
import static org.kie.server.services.taskassigning.runtime.TaskAssigningRuntimeKieServerExtensionMessages.MISSING_REQUIRED_JBPM_EXTENSION_ERROR;
import static org.kie.server.services.taskassigning.runtime.TaskAssigningRuntimeKieServerExtensionMessages.QUERIES_INITIALIZATION_ERROR;
import static org.kie.server.services.taskassigning.runtime.TaskAssigningRuntimeKieServerExtensionMessages.QUERIES_RESOURCE_NOT_FOUND;

public class TaskAssigningRuntimeKieServerExtension implements KieServerExtension {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskAssigningRuntimeKieServerExtension.class);

    public static final String EXTENSION_NAME = "TaskAssigningRuntime";

    static final String CAPABILITY_TASK_ASSIGNING_RUNTIME = "TaskAssigningRuntime";

    static final int EXTENSION_START_ORDER = 1000;

    static final String TASK_ASSIGNING_QUERY_DEFINITIONS_RESOURCE = "/task-assigning-query-definitions.json";

    private KieServerRegistry registry;
    private TaskAssigningRuntimeServiceBase taskAssigningRuntimeServiceBase;
    private QueryService queryService = null;

    private List<Object> services = new ArrayList<>();
    private boolean initialized = false;

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public boolean isActive() {
        return Boolean.FALSE.toString().equals(System.getProperty(KIE_TASK_ASSIGNING_RUNTIME_EXT_DISABLED, Boolean.TRUE.toString()));
    }

    @Override
    public void init(KieServerImpl kieServer, KieServerRegistry registry) {
        this.registry = registry;
        //JAXB and JSON required.
        registry.getExtraClasses().add(LocalDateTimeValue.class);

        KieServerExtension jbpmExtension = registry.getServerExtension(JbpmKieServerExtension.EXTENSION_NAME);
        if (jbpmExtension == null) {
            throw new KieServicesException(MISSING_REQUIRED_JBPM_EXTENSION_ERROR);
        }

        configureServices(kieServer, registry);
        services.add(taskAssigningRuntimeServiceBase);

        try {
            registerQueries();
        } catch (Exception e) {
            throw new KieServicesException(String.format(QUERIES_INITIALIZATION_ERROR, e.getMessage()), e);
        }
        initialized = true;
    }

    private void configureServices(KieServerImpl kieServer, KieServerRegistry registry) {
        KieServerExtension jbpmExtension = registry.getServerExtension(JbpmKieServerExtension.EXTENSION_NAME);
        List<Object> jbpmServices = jbpmExtension.getServices();
        UserTaskService userTaskService = null;

        for (Object object : jbpmServices) {
            // in case given service is null (meaning was not configured) continue with next one
            if (object != null) {
                if (UserTaskService.class.isAssignableFrom(object.getClass())) {
                    userTaskService = (UserTaskService) object;
                } else if (QueryService.class.isAssignableFrom(object.getClass())) {
                    queryService = (QueryService) object;
                }
            }
        }
        taskAssigningRuntimeServiceBase = new TaskAssigningRuntimeServiceBase(kieServer, registry, userTaskService, queryService);
    }

    @Override
    public void destroy(KieServerImpl kieServer, KieServerRegistry registry) {
        // no-op are required for this extension.
    }

    @Override
    public void createContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
        // no-op are required for this extension.
    }

    @Override
    public void updateContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
        // no-op are required for this extension.
    }

    @Override
    public boolean isUpdateContainerAllowed(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
        return true;
    }

    @Override
    public void disposeContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
        // no-op are required for this extension.
    }

    @Override
    public List<Object> getAppComponents(SupportedTransports type) {
        ServiceLoader<KieServerApplicationComponentsService> appComponentsServices
                = ServiceLoader.load(KieServerApplicationComponentsService.class);
        List<Object> appComponentsList = new ArrayList<>();
        Object[] componentServices = {taskAssigningRuntimeServiceBase, registry};
        for (KieServerApplicationComponentsService appComponentsService : appComponentsServices) {
            appComponentsList.addAll(appComponentsService.getAppComponents(EXTENSION_NAME, type, componentServices));
        }
        return appComponentsList;
    }

    @Override
    public <T> T getAppComponents(Class<T> serviceType) {
        if (serviceType.isAssignableFrom(taskAssigningRuntimeServiceBase.getClass())) {
            return (T) taskAssigningRuntimeServiceBase;
        }
        return null;
    }

    @Override
    public String getImplementedCapability() {
        return CAPABILITY_TASK_ASSIGNING_RUNTIME;
    }

    @Override
    public List<Object> getServices() {
        return services;
    }

    @Override
    public String getExtensionName() {
        return EXTENSION_NAME;
    }

    @Override
    public Integer getStartOrder() {
        return EXTENSION_START_ORDER;
    }

    @Override
    public String toString() {
        return EXTENSION_NAME + " KIE Server extension";
    }

    @Override
    public List<Message> healthCheck(boolean report) {
        List<Message> messages = KieServerExtension.super.healthCheck(report);
        try {
            // execute dummy check query to ensure the base service was instantiated well and queries are working.
            Map<String, Object> params = new HashMap<>();
            params.put(TO_TASK_ID, -1L);
            taskAssigningRuntimeServiceBase.executeFindTasksQuery(params);
            if (report) {
                messages.add(new Message(Severity.INFO, HEALTH_CHECK_IS_ALIVE_MESSAGE));
            }
        } catch (Exception e) {
            messages.add(new Message(Severity.ERROR, String.format(HEALTH_CHECK_ERROR, e.getMessage())));
        }
        return messages;
    }

    private void registerQueries() throws Exception {
        try (InputStream stream = this.getClass().getResourceAsStream(TASK_ASSIGNING_QUERY_DEFINITIONS_RESOURCE)) {
            if (stream == null) {
                throw new FileNotFoundException(QUERIES_RESOURCE_NOT_FOUND);
            }
            final Marshaller marshaller = MarshallerFactory.getMarshaller(MarshallingFormat.JSON,
                                                                          getClass().getClassLoader());
            final String queriesString = IOUtils.toString(stream, StandardCharsets.UTF_8);
            final QueryDefinition[] queries = marshaller.unmarshall(queriesString, QueryDefinition[].class);

            if (queries == null || queries.length == 0) {
                LOGGER.info("No queries were found");
                return;
            }
            registerQueries(queries);
        }
    }

    private void registerQueries(QueryDefinition[] queries) {
        for (QueryDefinition query : queries) {
            SqlQueryDefinition sqlQuery = new SqlQueryDefinition(query.getName(), query.getSource());
            sqlQuery.setTarget(org.jbpm.services.api.query.model.QueryDefinition.Target.valueOf(query.getTarget()));
            sqlQuery.setExpression(query.getExpression());
            queryService.replaceQuery(sqlQuery);
        }
    }
}
