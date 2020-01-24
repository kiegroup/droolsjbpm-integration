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

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

import org.apache.commons.io.IOUtils;
import org.jbpm.kie.services.impl.query.SqlQueryDefinition;
import org.jbpm.services.api.UserTaskService;
import org.jbpm.services.api.query.QueryService;
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
import org.kie.server.services.api.KieServerRuntimeException;
import org.kie.server.services.api.SupportedTransports;
import org.kie.server.services.impl.KieServerImpl;
import org.kie.server.services.jbpm.JbpmKieServerExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.server.api.KieServerConstants.KIE_JBPM_SERVER_EXT_DISABLED;
import static org.kie.server.api.KieServerConstants.KIE_TASK_ASSIGNING_RUNTIME_EXT_DISABLED;

public class TaskAssigningRuntimeKieServerExtension implements KieServerExtension {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskAssigningRuntimeKieServerExtension.class);

    private static final String TASK_ASSIGNING_QUERY_DEFINITIONS_RESOURCE = "/task-assigning-query-definitions.json";

    public static final String TASK_ASSIGNING_RUNTIME = "TASK-ASSIGNING-RUNTIME";

    public static final String EXTENSION_NAME = "TaskAssigningRuntime";

    private static final String MISSING_REQUIRED_JBPM_EXTENSION_ERROR = JbpmKieServerExtension.EXTENSION_NAME + " extension is required for the task assigning api to work." +
            " The " + EXTENSION_NAME + " won't be initialized.";

    private static final Boolean DISABLED = Boolean.parseBoolean(System.getProperty(KIE_TASK_ASSIGNING_RUNTIME_EXT_DISABLED, "true"));
    private static final Boolean JBPM_DISABLED = "true".equals(System.getProperty(KIE_JBPM_SERVER_EXT_DISABLED));

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
        return !DISABLED && !JBPM_DISABLED;
    }

    @Override
    public void init(KieServerImpl kieServer, KieServerRegistry registry) {
        this.registry = registry;
        //JAXB and JSON required.
        registry.getExtraClasses().add(LocalDateTimeValue.class);
        KieServerExtension jbpmExtension = registry.getServerExtension(JbpmKieServerExtension.EXTENSION_NAME);
        if (jbpmExtension == null) {
            initialized = false;
            LOGGER.error(MISSING_REQUIRED_JBPM_EXTENSION_ERROR);
            kieServer.addServerMessage(new Message(Severity.ERROR, MISSING_REQUIRED_JBPM_EXTENSION_ERROR));
            return;
        }

        configureServices(registry);
        services.add(taskAssigningRuntimeServiceBase);

        try {
            registerQueries();
        } catch (Exception e) {
            String error = "An error was produced during " + EXTENSION_NAME + " queries initialization. " + e.getMessage();
            LOGGER.error(error, e);
            // this exception is properly cached by the KieServer initialization procedure and an proper error message
            // is added to the KieServer errors.
            throw new KieServerRuntimeException(error, e);
        }
        initialized = true;
    }

    private void configureServices(KieServerRegistry registry) {
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
        taskAssigningRuntimeServiceBase = new TaskAssigningRuntimeServiceBase(userTaskService, queryService);
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
        return TASK_ASSIGNING_RUNTIME;
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
        return 1000;
    }

    @Override
    public String toString() {
        return EXTENSION_NAME + " KIE Server extension";
    }

    private void registerQueries() throws Exception {
        try (InputStream stream = this.getClass().getResourceAsStream(TASK_ASSIGNING_QUERY_DEFINITIONS_RESOURCE)) {
            if (stream == null) {
                String msg = EXTENSION_NAME + " queries file was not found: " + TASK_ASSIGNING_QUERY_DEFINITIONS_RESOURCE;
                LOGGER.error(msg);
                throw new Exception(msg);
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
