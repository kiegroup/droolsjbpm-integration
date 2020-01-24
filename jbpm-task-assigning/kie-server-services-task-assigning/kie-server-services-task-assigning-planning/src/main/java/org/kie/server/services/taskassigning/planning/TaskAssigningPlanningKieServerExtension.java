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
import java.util.function.Function;

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
import org.kie.server.services.taskassigning.user.system.api.UserSystemService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.commons.lang3.StringUtils.isEmpty;
import static org.kie.server.api.KieServerConstants.KIE_TASK_ASSIGNING_PLANNING_EXT_DISABLED;
import static org.kie.server.services.taskassigning.planning.TaskAssigningConstants.JBPM_TASK_ASSIGNING_PROCESS_RUNTIME_PWD;
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
import static org.kie.server.services.taskassigning.planning.util.PropertyUtil.readSystemProperty;

public class TaskAssigningPlanningKieServerExtension implements KieServerExtension {

    private static final Logger LOGGER = LoggerFactory.getLogger(TaskAssigningPlanningKieServerExtension.class);

    private static final String CAPABILITY_TASK_ASSIGNING_PLANNING = "TaskAssigningPlanning";

    private static final boolean DISABLED = readSystemProperty(KIE_TASK_ASSIGNING_PLANNING_EXT_DISABLED, true, value -> !Boolean.FALSE.toString().equals(value));

    public static final String EXTENSION_NAME = "TaskAssigningPlanning";

    private static final String DEFAULT_SOLVER_CONFIG = "org/kie/server/services/taskassigning/solver/taskAssigningDefaultSolverConfig.xml";

    private static final String CREATE_CONTAINER_ERROR = "Container creation failed for containerId: %s, error: %s";
    private static final String ACTIVATE_CONTAINER_ERROR = "Container activation failed for containerId: %s, error: %s";
    private static final String EXTENSION_CONTAINER_NOT_IN_EXPECTED_STATUS_ERROR = "Container %s must be in %s status but is currently %s." +
            " " + EXTENSION_NAME + " won't operate properly.";

    private static final String CONTAINER_NOT_ACCESSIBLE_ERROR = "It was not possible get access to containerId: %s" +
            " " + EXTENSION_NAME + " won't operate properly.";

    private static final String PLANNER_CONTAINER_CONFIGURATION_ERROR = "Planner container is not properly configured, error: %s" +
            " " + EXTENSION_NAME + " won't operate properly";

    private static final String PLANNER_SOLVER_NOT_CONFIGURED_ERROR = "No solverConfigResource has been configured for starting the task assigning solver." +
            " " + EXTENSION_NAME + " won't operate properly.";

    private static final String PLANNER_SOLVER_INSTANTIATION_CHECK_ERROR = "An error was produced during solver instantiation check." +
            " It was not possible to create a solver for the provided configuration, error: %s." +
            " " + EXTENSION_NAME + " won't operate properly.";

    private static final String PLANNER_CONTAINER_NOT_AVAILABLE = "Planner container %s is not available." +
            " " + EXTENSION_NAME + " won't operate properly";

    private static final String UNDESIRED_EXTENSIONS_RUNNING_ERROR = "It's was detected that the following extensions %s" +
            " are running in current server, but it's not recommended to run them on the same server instance as the " + EXTENSION_NAME;

    private static final String USER_SYSTEM_NAME_NOT_CONFIGURED_ERROR = "No user system service name has been configured." +
            " " + EXTENSION_NAME + " won't operate properly. Please use the property %s to configure it";

    private static final String USER_SYSTEM_CONTAINER_CONFIGURATION_ERROR = "User system service container is not properly configured, error: %s" +
            " " + EXTENSION_NAME + " won't operate properly";

    private static final String USER_SYSTEM_CONTAINER_NOT_AVAILABLE = "User system service container %s is not available." +
            " " + EXTENSION_NAME + " won't operate properly";

    private static final String USER_SYSTEM_SERVICE_NOT_FOUND = "User system service %s was not found." +
            " " + EXTENSION_NAME + " won't operate properly";

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

    /**
     * Invoked by the KieServer initialization procedure to determine if current extension has been activated in current
     * installation.
     * @return true if the extension is activated and must be initialized, etc. False in any other case.
     */
    @Override
    public boolean isActive() {
        return !DISABLED;
    }

    /**
     * Invoked by the KieServer initialization procedure for performing current extension initialization.
     */
    @Override
    public void init(KieServerImpl kieServer, KieServerRegistry registry) {
        LOGGER.debug("Initializing " + EXTENSION_NAME + " extension.");
        this.kieServer = kieServer;
        this.registry = registry;
        if (DISABLED) {
            LOGGER.debug(EXTENSION_NAME + " is currently disabled. Use the " + KIE_TASK_ASSIGNING_PLANNING_EXT_DISABLED + " to enable it if needed.");
            return;
        }

        KieServerExtension jbpmExtension = registry.getServerExtension(JbpmKieServerExtension.EXTENSION_NAME);
        if (jbpmExtension != null) {
            String msg = String.format(UNDESIRED_EXTENSIONS_RUNNING_ERROR, JbpmKieServerExtension.EXTENSION_NAME);
            LOGGER.warn(msg);
            kieServer.addServerMessage(new Message(Severity.WARN, msg));
        }

        initRuntimeClient();

        if (!validateAndSetSolverConfiguration()) {
            return;
        }
        if (!validateAndSetUserSystemServiceConfiguration()) {
            return;
        }

        this.executorService = Executors.newFixedThreadPool(3);
        this.taskAssigningService = new TaskAssigningService();
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
    public void activateContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
        // no-op are required for this extension.
    }

    @Override
    public void deactivateContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
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
        return 1001;
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
        if (DISABLED || !initialized) {
            return;
        }
        taskAssigningService.destroy();
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

    private UserSystemService getUserSystemService() {
        return userSystemService;
    }

    private TaskAssigningRuntimeClient getRuntimeClient() {
        return runtimeClient;
    }

    private boolean validateAndSetSolverConfiguration() {
        this.solverDef = getSolverDef();
        if (isEmpty(solverDef.getSolverConfigResource())) {
            String msg = PLANNER_SOLVER_NOT_CONFIGURED_ERROR;
            LOGGER.error(msg);
            kieServer.addServerMessage(new Message(Severity.ERROR, msg));
            return false;
        }

        if (!isEmpty(solverDef.getContainerId())) {
            KieContainerResource resource = new KieContainerResource(solverDef.getContainerId(),
                                                                     new ReleaseId(solverDef.getGroupId(),
                                                                                   solverDef.getArtifactId(),
                                                                                   solverDef.getVersion()));

            return validateContainerConfiguration(resource, e -> String.format(PLANNER_CONTAINER_CONFIGURATION_ERROR, e.getMessage()));
        }
        return true;
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
                kieServer.addServerMessage(new Message(Severity.ERROR, msg));
                return false;
            }
        }

        try {
            //early check that solver can be properly started.
            SolverBuilder.builder()
                    .registry(registry)
                    .solverDef(solverDef)
                    .build();
        } catch (Exception e) {
            String msg = String.format(PLANNER_SOLVER_INSTANTIATION_CHECK_ERROR, e.getMessage());
            LOGGER.error(msg, e);
            kieServer.addServerMessage(new Message(Severity.ERROR, msg));
            return false;
        }
        return true;
    }

    private boolean validateAndSetUserSystemServiceConfiguration() {
        this.userSystemName = readSystemProperty(TASK_ASSIGNING_USER_SYSTEM_NAME, null, value -> value);
        if (isEmpty(userSystemName)) {
            String msg = String.format(USER_SYSTEM_NAME_NOT_CONFIGURED_ERROR, TASK_ASSIGNING_USER_SYSTEM_NAME);
            LOGGER.error(msg);
            kieServer.addServerMessage(new Message(Severity.ERROR, msg));
            return false;
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

            return validateContainerConfiguration(userSystemContainer, e -> String.format(USER_SYSTEM_CONTAINER_CONFIGURATION_ERROR, e.getMessage()));
        }
        return true;
    }

    private boolean initUserSystemService() {
        ClassLoader classLoader;
        if (userSystemContainer != null) {
            LOGGER.debug("User system service {} will be loaded from container {} class loader", userSystemName, userSystemContainer.getContainerId());
            KieContainerInstanceImpl container = prepareContainer(userSystemContainer);
            if (container == null) {
                String msg = String.format(USER_SYSTEM_CONTAINER_NOT_AVAILABLE, userSystemContainer.getContainerId());
                LOGGER.error(msg);
                kieServer.addServerMessage(new Message(Severity.ERROR, msg));
                return false;
            }
            classLoader = container.getKieContainer().getClassLoader();
        } else {
            LOGGER.debug("User system service {} will be loaded from application class loader", userSystemName);
            classLoader = this.getClass().getClassLoader();
        }

        final Map<String, UserSystemService> userServices = UserSystemServiceLoader.loadServices(classLoader);
        userSystemService = userServices.get(userSystemName);
        if (userSystemService == null) {
            final String msg = String.format(USER_SYSTEM_SERVICE_NOT_FOUND, userSystemName);
            LOGGER.error(msg);
            kieServer.addServerMessage(new Message(Severity.ERROR, msg));
            return false;
        }

        userSystemService.start();
        try {
            userSystemService.test();
            LOGGER.debug("User system service {} test check was successful.", userSystemName);
        } catch (Exception e) {
            LOGGER.warn("User system service {} test check failed, but " + EXTENSION_NAME + " startup procedure will continue. error: ", e.getMessage());
        }
        return true;
    }

    private void initRuntimeClient() {
        String url = readSystemProperty(JBPM_TASK_ASSIGNING_PROCESS_RUNTIME_URL, "http://localhost:8080/kie-server/services/rest/server", value -> value);
        String user = readSystemProperty(JBPM_TASK_ASSIGNING_PROCESS_RUNTIME_USER, "wbadmin", value -> value);
        String pwd = readSystemProperty(JBPM_TASK_ASSIGNING_PROCESS_RUNTIME_PWD, null, value -> value);
        this.runtimeClient = TaskAssigningRuntimeClientFactory.newRuntimeClient(url, user, pwd);
    }

    private boolean validateContainerConfiguration(KieContainerResource resource, Function<Exception, String> printableErrorMessage) {
        try {
            validateContainerRequiredParams(resource);
        } catch (Exception e) {
            String msg = printableErrorMessage.apply(e);
            LOGGER.error(msg);
            kieServer.addServerMessage(new Message(Severity.ERROR, msg));
            return false;
        }
        return true;
    }

    private void validateContainerRequiredParams(KieContainerResource resource) throws Exception {
        final String containerId = resource.getContainerId();
        final String groupId = resource.getReleaseId().getGroupId();
        final String artifactId = resource.getReleaseId().getArtifactId();
        final String version = resource.getReleaseId().getVersion();
        if (isEmpty(containerId) || isEmpty(artifactId) || isEmpty(groupId) || isEmpty(version)) {
            throw new Exception("Required parameters for container configuration are missing." +
                                        " containerId: " + containerId +
                                        ", groupId: " + groupId + ", artifactId: " + artifactId + ", version: " + version + ".");
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
                    kieServer.addServerMessage(new Message(Severity.ERROR, msg));
                    return null;
                }

                container = registry.getContainer(resource.getContainerId());

                if (container == null) {
                    final String msg = String.format(CONTAINER_NOT_ACCESSIBLE_ERROR, resource.getContainerId());
                    LOGGER.error(msg);
                    kieServer.addServerMessage(new Message(Severity.ERROR, msg));
                    return null;
                }
            } catch (Exception e) {
                String msg = String.format(CREATE_CONTAINER_ERROR, resource.getContainerId(), e.getMessage());
                LOGGER.error(msg, e);
                kieServer.addServerMessage(new Message(Severity.ERROR, msg));
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
                    kieServer.addServerMessage(new Message(Severity.ERROR, msg));
                    return null;
                }
            } catch (Exception e) {
                String msg = String.format(ACTIVATE_CONTAINER_ERROR, resource.getContainerId(), e.getMessage());
                LOGGER.error(msg, e);
                kieServer.addServerMessage(new Message(Severity.ERROR, msg));
                return null;
            }
        }

        if (container.getStatus() != KieContainerStatus.STARTED) {
            String msg = String.format(EXTENSION_CONTAINER_NOT_IN_EXPECTED_STATUS_ERROR, resource.getContainerId(), KieContainerStatus.STARTED, container.getStatus());
            LOGGER.error(msg);
            kieServer.addServerMessage(new Message(Severity.ERROR, msg));
            return null;
        }
        return container;
    }
}
