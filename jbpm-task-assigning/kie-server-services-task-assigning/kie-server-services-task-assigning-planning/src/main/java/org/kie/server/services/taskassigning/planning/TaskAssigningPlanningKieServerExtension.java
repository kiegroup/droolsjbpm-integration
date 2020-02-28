/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.services.taskassigning.planning;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.kie.server.api.exception.KieServicesException;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieContainerStatus;
import org.kie.server.api.model.KieServiceResponse;
import org.kie.server.api.model.Message;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.Severity;
import org.kie.server.client.TaskAssigningRuntimeClient;
import org.kie.server.client.TaskAssigningRuntimeClientFactory;
import org.kie.server.services.api.KieContainerInstance;
import org.kie.server.services.api.KieServer;
import org.kie.server.services.api.KieServerExtension;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.api.SupportedTransports;
import org.kie.server.services.impl.KieContainerInstanceImpl;
import org.kie.server.services.impl.KieServerImpl;
import org.kie.server.services.jbpm.JbpmKieServerExtension;
import org.kie.server.services.taskassigning.core.model.TaskAssigningSolution;
import org.kie.server.services.taskassigning.user.system.api.UserSystemService;
import org.optaplanner.core.api.solver.Solver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.kie.server.api.KieServerConstants.KIE_TASK_ASSIGNING_PLANNING_EXT_DISABLED;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.JBPM_TASK_ASSIGNING_PROCESS_RUNTIME_PWD;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.JBPM_TASK_ASSIGNING_PROCESS_RUNTIME_TIMEOUT;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.JBPM_TASK_ASSIGNING_PROCESS_RUNTIME_URL;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.JBPM_TASK_ASSIGNING_PROCESS_RUNTIME_USER;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_SOLVER_CONFIG_RESOURCE;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_SOLVER_CONTAINER_ARTIFACT_ID;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_SOLVER_CONTAINER_GROUP_ID;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_SOLVER_CONTAINER_ID;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_SOLVER_CONTAINER_VERSION;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_USER_SYSTEM_CONTAINER_ARTIFACT_ID;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_USER_SYSTEM_CONTAINER_GROUP_ID;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_USER_SYSTEM_CONTAINER_ID;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_USER_SYSTEM_CONTAINER_VERSION;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.TASK_ASSIGNING_USER_SYSTEM_NAME;
import static org.kie.server.services.taskassigning.planning.TaskAssigningPlanningKieServerExtensionMessages.ACTIVATE_CONTAINER_ERROR;
import static org.kie.server.services.taskassigning.planning.TaskAssigningPlanningKieServerExtensionMessages.CONTAINER_NOT_ACCESSIBLE_ERROR;
import static org.kie.server.services.taskassigning.planning.TaskAssigningPlanningKieServerExtensionMessages.CREATE_CONTAINER_ERROR;
import static org.kie.server.services.taskassigning.planning.TaskAssigningPlanningKieServerExtensionMessages.EXTENSION_CONTAINER_NOT_IN_EXPECTED_STATUS_ERROR;
import static org.kie.server.services.taskassigning.planning.TaskAssigningPlanningKieServerExtensionMessages.HEALTH_CHECK_IS_ALIVE_MESSAGE;
import static org.kie.server.services.taskassigning.planning.TaskAssigningPlanningKieServerExtensionMessages.PLANNER_CONTAINER_NOT_AVAILABLE;
import static org.kie.server.services.taskassigning.planning.TaskAssigningPlanningKieServerExtensionMessages.PLANNER_SOLVER_INSTANTIATION_CHECK_ERROR;
import static org.kie.server.services.taskassigning.planning.TaskAssigningPlanningKieServerExtensionMessages.PLANNER_SOLVER_NOT_CONFIGURED_ERROR;
import static org.kie.server.services.taskassigning.planning.TaskAssigningPlanningKieServerExtensionMessages.REQUIRED_PARAMETERS_FOR_CONTAINER_ARE_MISSING;
import static org.kie.server.services.taskassigning.planning.TaskAssigningPlanningKieServerExtensionMessages.SOLVER_CONFIGURATION_ERROR;
import static org.kie.server.services.taskassigning.planning.TaskAssigningPlanningKieServerExtensionMessages.UNDESIRED_EXTENSIONS_RUNNING_ERROR;
import static org.kie.server.services.taskassigning.planning.TaskAssigningPlanningKieServerExtensionMessages.USER_SYSTEM_CONFIGURATION_ERROR;
import static org.kie.server.services.taskassigning.planning.TaskAssigningPlanningKieServerExtensionMessages.USER_SYSTEM_CONTAINER_NOT_AVAILABLE;
import static org.kie.server.services.taskassigning.planning.TaskAssigningPlanningKieServerExtensionMessages.USER_SYSTEM_NAME_NOT_CONFIGURED_ERROR;
import static org.kie.server.services.taskassigning.planning.TaskAssigningPlanningKieServerExtensionMessages.USER_SYSTEM_SERVICE_NOT_FOUND;
import static org.kie.server.services.taskassigning.planning.TaskAssigningPlanningKieServerExtensionMessages.USER_SYSTEM_SERVICE_START_ERROR;
import static org.kie.server.services.taskassigning.planning.TaskAssigningPlanningKieServerExtensionMessages.addExtensionMessagePrefix;
import static org.kie.server.services.taskassigning.planning.util.PropertyUtil.readSystemProperty;

public class TaskAssigningPlanningKieServerExtension implements KieServerExtension {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskAssigningPlanningKieServerExtension.class);

    static final String CAPABILITY_TASK_ASSIGNING_PLANNING = "TaskAssigningPlanning";

    static final String EXTENSION_NAME = "TaskAssigningPlanning";

    static final int EXTENSION_START_ORDER = 1001;

    private static final String DEFAULT_SOLVER_CONFIG = "org/kie/server/services/taskassigning/solver/taskAssigningDefaultSolverConfig.xml";

    private KieServer kieServer;
    private KieServerRegistry registry;
    private final List<Object> services = new ArrayList<>();
    private boolean initialized = false;
    private TaskAssigningRuntimeClient runtimeClient;
    private UserSystemService userSystemService;
    private TaskAssigningService taskAssigningService;
    private ExecutorService executorService;
    private SolverDef solverDef;
    private String userSystemName;
    private KieContainerResource userSystemContainer = null;
    private List<Message> permanentErrors = new ArrayList<>();

    /**
     * Invoked by the KieServer initialization procedure to determine if current extension has been activated in current
     * installation.
     * @return true if the extension is activated and must be initialized, etc. False in any other case.
     */
    @Override
    public boolean isActive() {
        return Boolean.FALSE.toString().equals(System.getProperty(KIE_TASK_ASSIGNING_PLANNING_EXT_DISABLED, Boolean.TRUE.toString()));
    }

    /**
     * Invoked by the KieServer initialization procedure for performing current extension initialization.
     */
    @Override
    public void init(KieServerImpl kieServer, KieServerRegistry registry) {
        LOGGER.debug("Initializing " + EXTENSION_NAME + " extension.");
        this.kieServer = kieServer;
        this.registry = registry;

        KieServerExtension jbpmExtension = registry.getServerExtension(JbpmKieServerExtension.EXTENSION_NAME);
        if (jbpmExtension != null) {
            String msg = String.format(UNDESIRED_EXTENSIONS_RUNNING_ERROR, JbpmKieServerExtension.EXTENSION_NAME);
            LOGGER.warn(msg);
            kieServer.addServerMessage(new Message(Severity.WARN, addExtensionMessagePrefix(msg)));
        }

        initRuntimeClient();

        try {
            validateAndSetSolverConfiguration();
        } catch (TaskAssigningValidationException e) {
            throw new KieServicesException(String.format(SOLVER_CONFIGURATION_ERROR, e.getMessage()), e);
        }

        try {
            validateAndSetUserSystemServiceConfiguration();
        } catch (TaskAssigningValidationException e) {
            throw new KieServicesException(String.format(USER_SYSTEM_CONFIGURATION_ERROR, e.getMessage()), e);
        }

        this.executorService = Executors.newFixedThreadPool(3);
        this.taskAssigningService = createTaskAssigningService();
        this.services.add(taskAssigningService);

        this.initialized = true;
    }

    /**
     * Invoked by the KieServer initialization procedure after the init(kieServer, registry) invocation to determine
     * if current extension was properly initialized.
     * @return true if the extension was properly initialized, false in any other case.
     */
    @Override
    public boolean isInitialized() {
        return initialized;
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
        return new ArrayList<>();
    }

    @Override
    public <T> T getAppComponents(Class<T> serviceType) {
        if (serviceType.isAssignableFrom(taskAssigningService.getClass())) {
            return (T) taskAssigningService;
        }
        return null;
    }

    @Override
    public String getImplementedCapability() {
        return CAPABILITY_TASK_ASSIGNING_PLANNING;
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

    /**
     * Invoked by the KieServer initialization procedure to notify that server initialization has finished and the
     * KieServer is ready to operate. Controller was connected if any, and in particular all the configured containers
     * on current server were properly restored. Server is ready to operate.
     */
    @Override
    public void serverStarted() {
        if (initialized) {
            if (!initSolverRuntime()) {
                return;
            }

            if (!initUserSystemService()) {
                return;
            }
            //finally when everything is ok, start the task assigning service.
            taskAssigningService.setExecutorService(executorService);
            taskAssigningService.setDelegate(new TaskAssigningRuntimeDelegate(getRuntimeClient()));
            taskAssigningService.setUserSystemService(getUserSystemService());
            taskAssigningService.start(solverDef, registry);
        }
    }

    @Override
    public void destroy(KieServerImpl kieServer, KieServerRegistry registry) {
        LOGGER.debug("Destroying " + EXTENSION_NAME + " extension.");
        if (initialized) {
            taskAssigningService.destroy();
        }
    }

    @Override
    public List<Message> healthCheck(boolean report) {
        List<Message> messages = KieServerExtension.super.healthCheck(report);
        if (!permanentErrors.isEmpty()) {
            messages.addAll(permanentErrors);
        } else if (report) {
            messages.add(new Message(Severity.INFO, HEALTH_CHECK_IS_ALIVE_MESSAGE));
        }
        return messages;
    }

    private SolverDef getSolverDef() {
        final String containerId = readSystemProperty(TASK_ASSIGNING_SOLVER_CONTAINER_ID, null, value -> value);
        final String groupId = readSystemProperty(TASK_ASSIGNING_SOLVER_CONTAINER_GROUP_ID, null, value -> value);
        final String artifactId = readSystemProperty(TASK_ASSIGNING_SOLVER_CONTAINER_ARTIFACT_ID, null, value -> value);
        final String version = readSystemProperty(TASK_ASSIGNING_SOLVER_CONTAINER_VERSION, null, value -> value);
        final String solverConfigResource = readSystemProperty(TASK_ASSIGNING_SOLVER_CONFIG_RESOURCE, DEFAULT_SOLVER_CONFIG, value -> value);

        return new SolverDef(containerId,
                             groupId,
                             artifactId,
                             version,
                             solverConfigResource);
    }

    TaskAssigningService createTaskAssigningService() {
        return new TaskAssigningService();
    }

    private UserSystemService getUserSystemService() {
        return userSystemService;
    }

    private TaskAssigningRuntimeClient getRuntimeClient() {
        return runtimeClient;
    }

    private void validateAndSetSolverConfiguration() throws TaskAssigningValidationException {
        this.solverDef = getSolverDef();
        if (isEmpty(solverDef.getSolverConfigResource())) {
            throw new TaskAssigningValidationException(PLANNER_SOLVER_NOT_CONFIGURED_ERROR);
        }

        if (!isEmpty(solverDef.getContainerId())) {
            KieContainerResource resource = new KieContainerResource(solverDef.getContainerId(),
                                                                     new ReleaseId(solverDef.getGroupId(),
                                                                                   solverDef.getArtifactId(),
                                                                                   solverDef.getVersion()));

            validateContainerRequiredParams(resource);
        }
    }

    private boolean initSolverRuntime() {
        if (!isEmpty(solverDef.getContainerId())) {
            KieContainerResource resource = new KieContainerResource(solverDef.getContainerId(),
                                                                     new ReleaseId(solverDef.getGroupId(),
                                                                                   solverDef.getArtifactId(),
                                                                                   solverDef.getVersion()));
            KieContainerInstanceImpl container = prepareContainer(resource);
            if (container == null) {
                String msg = String.format(PLANNER_CONTAINER_NOT_AVAILABLE, solverDef.getContainerId());
                LOGGER.error(msg);
                registerMessage(Severity.ERROR, msg);
                return false;
            }
        }

        try {
            //early check that solver can be properly started.
            createSolver(registry, solverDef);
        } catch (Exception e) {
            String msg = String.format(PLANNER_SOLVER_INSTANTIATION_CHECK_ERROR, e.getMessage());
            LOGGER.error(msg, e);
            registerMessage(Severity.ERROR, msg);
            return false;
        }
        return true;
    }

    Solver<TaskAssigningSolution> createSolver(KieServerRegistry registry, SolverDef solverDef) {
        return SolverBuilder.create()
                .registry(registry)
                .solverDef(solverDef)
                .build();
    }

    private void validateAndSetUserSystemServiceConfiguration() throws TaskAssigningValidationException {
        this.userSystemName = readSystemProperty(TASK_ASSIGNING_USER_SYSTEM_NAME, null, value -> value);
        if (isEmpty(userSystemName)) {
            String msg = String.format(USER_SYSTEM_NAME_NOT_CONFIGURED_ERROR, TASK_ASSIGNING_USER_SYSTEM_NAME);
            throw new TaskAssigningValidationException(msg);
        }

        final String containerId = readSystemProperty(TASK_ASSIGNING_USER_SYSTEM_CONTAINER_ID, null, value -> value);
        if (!isEmpty(containerId)) {
            final String groupId = readSystemProperty(TASK_ASSIGNING_USER_SYSTEM_CONTAINER_GROUP_ID, null, value -> value);
            final String artifactId = readSystemProperty(TASK_ASSIGNING_USER_SYSTEM_CONTAINER_ARTIFACT_ID, null, value -> value);
            final String version = readSystemProperty(TASK_ASSIGNING_USER_SYSTEM_CONTAINER_VERSION, null, value -> value);

            this.userSystemContainer = new KieContainerResource(containerId,
                                                                new ReleaseId(groupId,
                                                                              artifactId,
                                                                              version));

            validateContainerRequiredParams(userSystemContainer);
        }
    }

    private boolean initUserSystemService() {
        ClassLoader classLoader;
        if (userSystemContainer != null) {
            LOGGER.debug("User system service {} will be loaded from container {} class loader", userSystemName, userSystemContainer.getContainerId());
            KieContainerInstanceImpl container = prepareContainer(userSystemContainer);
            if (container == null) {
                String msg = String.format(USER_SYSTEM_CONTAINER_NOT_AVAILABLE, userSystemContainer.getContainerId());
                LOGGER.error(msg);
                registerMessage(Severity.ERROR, msg);
                return false;
            }
            classLoader = container.getKieContainer().getClassLoader();
        } else {
            LOGGER.debug("User system service {} will be loaded from application class loader", userSystemName);
            classLoader = this.getClass().getClassLoader();
        }
        userSystemService = lookupUserSystem(userSystemName, classLoader);
        if (userSystemService == null) {
            final String msg = String.format(USER_SYSTEM_SERVICE_NOT_FOUND, userSystemName);
            LOGGER.error(msg);
            registerMessage(Severity.ERROR, msg);
            return false;
        }

        try {
            userSystemService.start();
        } catch (Exception e) {
            final String msg = String.format(USER_SYSTEM_SERVICE_START_ERROR, userSystemName, e.getMessage());
            LOGGER.error(msg, e);
            registerMessage(Severity.ERROR, msg);
            return false;
        }

        try {
            userSystemService.test();
            LOGGER.debug("User system service {} test check was successful.", userSystemName);
        } catch (Exception e) {
            LOGGER.warn("User system service {} test check failed, but " + EXTENSION_NAME + " startup procedure will continue. error: ", e.getMessage());
        }
        return true;
    }

    UserSystemService lookupUserSystem(String userSystemName, ClassLoader classLoader) {
        final Map<String, UserSystemService> userServices = UserSystemServiceLoader.loadServices(classLoader);
        return userServices.get(userSystemName);
    }

    private void initRuntimeClient() {
        String url = readSystemProperty(JBPM_TASK_ASSIGNING_PROCESS_RUNTIME_URL, "http://localhost:8080/kie-server/services/rest/server", value -> value);
        String user = readSystemProperty(JBPM_TASK_ASSIGNING_PROCESS_RUNTIME_USER, "wbadmin", value -> value);
        String pwd = readSystemProperty(JBPM_TASK_ASSIGNING_PROCESS_RUNTIME_PWD, null, value -> value);
        long timeout = readSystemProperty(JBPM_TASK_ASSIGNING_PROCESS_RUNTIME_TIMEOUT, 90000L, Long::parseLong);
        this.runtimeClient = createRuntimeClient(url, user, pwd, timeout);
    }

    TaskAssigningRuntimeClient createRuntimeClient(String url, String user, String pwd, long timeout) {
        return TaskAssigningRuntimeClientFactory.newRuntimeClient(url, user, pwd, timeout);
    }

    private void validateContainerRequiredParams(KieContainerResource resource) throws TaskAssigningValidationException {
        final String containerId = resource.getContainerId();
        final String groupId = resource.getReleaseId().getGroupId();
        final String artifactId = resource.getReleaseId().getArtifactId();
        final String version = resource.getReleaseId().getVersion();
        if (isEmpty(containerId) || isEmpty(artifactId) || isEmpty(groupId) || isEmpty(version)) {
            throw new TaskAssigningValidationException(String.format(REQUIRED_PARAMETERS_FOR_CONTAINER_ARE_MISSING, containerId, groupId, artifactId, version));
        }
    }

    private KieContainerInstanceImpl prepareContainer(KieContainerResource resource) {
        KieContainerInstanceImpl container = registry.getContainer(resource.getContainerId());
        if (container == null) {
            LOGGER.debug("Container {} is not available in current server. It'll be created now.", resource.getContainerId());
            try {
                ServiceResponse<KieContainerResource> response = kieServer.createContainer(resource.getContainerId(), resource);
                if (response.getType() == KieServiceResponse.ResponseType.FAILURE) {
                    String msg = String.format(CREATE_CONTAINER_ERROR, resource.getContainerId(), response.getMsg());
                    LOGGER.error(msg);
                    registerMessage(Severity.ERROR, msg);
                    return null;
                }

                container = registry.getContainer(resource.getContainerId());

                if (container == null) {
                    final String msg = String.format(CONTAINER_NOT_ACCESSIBLE_ERROR, resource.getContainerId());
                    LOGGER.error(msg);
                    registerMessage(Severity.ERROR, msg);
                    return null;
                }
            } catch (Exception e) {
                String msg = String.format(CREATE_CONTAINER_ERROR, resource.getContainerId(), e.getMessage());
                LOGGER.error(msg, e);
                registerMessage(Severity.ERROR, msg);
                return null;
            }
        }

        if (container.getStatus() == KieContainerStatus.DEACTIVATED) {
            LOGGER.debug("Container {} is currently {}. It needs to be activated.", resource.getContainerId(), container.getStatus());
            try {
                ServiceResponse<KieContainerResource> response = ((KieServerImpl) kieServer).activateContainer(resource.getContainerId());
                if (response.getType() == KieServiceResponse.ResponseType.FAILURE) {
                    String msg = String.format(ACTIVATE_CONTAINER_ERROR, resource.getContainerId(), response.getMsg());
                    LOGGER.error(msg);
                    registerMessage(Severity.ERROR, msg);
                    return null;
                }
            } catch (Exception e) {
                String msg = String.format(ACTIVATE_CONTAINER_ERROR, resource.getContainerId(), e.getMessage());
                LOGGER.error(msg, e);
                registerMessage(Severity.ERROR, msg);
                return null;
            }
        }

        if (container.getStatus() != KieContainerStatus.STARTED) {
            String msg = String.format(EXTENSION_CONTAINER_NOT_IN_EXPECTED_STATUS_ERROR, resource.getContainerId(), KieContainerStatus.STARTED, container.getStatus());
            LOGGER.error(msg);
            registerMessage(Severity.ERROR, msg);
            return null;
        }
        return container;
    }

    private void registerMessage(Severity severity, String msg) {
        final String prefixedMsg = addExtensionMessagePrefix(msg);
        kieServer.addServerMessage(new Message(severity, prefixedMsg));
        permanentErrors.add(new Message(severity, prefixedMsg));
    }
}
