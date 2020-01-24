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

package org.kie.server.services.jbpm;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.naming.InitialContext;
import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceProviderResolverHolder;
import javax.persistence.spi.PersistenceUnitInfo;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.drools.core.impl.InternalKieContainer;
import org.jbpm.document.service.impl.DocumentImpl;
import org.jbpm.executor.ExecutorServiceFactory;
import org.jbpm.executor.impl.ExecutorImpl;
import org.jbpm.executor.impl.ExecutorServiceImpl;
import org.jbpm.kie.services.impl.AbstractDeploymentService;
import org.jbpm.kie.services.impl.FormManagerService;
import org.jbpm.kie.services.impl.FormManagerServiceImpl;
import org.jbpm.kie.services.impl.KModuleDeploymentService;
import org.jbpm.kie.services.impl.KModuleDeploymentUnit;
import org.jbpm.kie.services.impl.ProcessServiceImpl;
import org.jbpm.kie.services.impl.RuntimeDataServiceImpl;
import org.jbpm.kie.services.impl.UserTaskServiceImpl;
import org.jbpm.kie.services.impl.admin.ProcessInstanceAdminServiceImpl;
import org.jbpm.kie.services.impl.admin.ProcessInstanceMigrationServiceImpl;
import org.jbpm.kie.services.impl.admin.UserTaskAdminServiceImpl;
import org.jbpm.kie.services.impl.bpmn2.BPMN2DataServiceImpl;
import org.jbpm.kie.services.impl.query.QueryServiceImpl;
import org.jbpm.kie.services.impl.utils.PreUndeployOperations;
import org.jbpm.runtime.manager.impl.RuntimeManagerFactoryImpl;
import org.jbpm.runtime.manager.impl.deploy.DeploymentDescriptorManagerUtil;
import org.jbpm.runtime.manager.impl.deploy.DeploymentDescriptorMerger;
import org.jbpm.runtime.manager.impl.identity.UserDataServiceProvider;
import org.jbpm.runtime.manager.impl.jpa.EntityManagerFactoryManager;
import org.jbpm.services.api.DefinitionService;
import org.jbpm.services.api.DeploymentService;
import org.jbpm.services.api.ProcessService;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.UserTaskService;
import org.jbpm.services.api.admin.ProcessInstanceAdminService;
import org.jbpm.services.api.admin.ProcessInstanceMigrationService;
import org.jbpm.services.api.admin.UserTaskAdminService;
import org.jbpm.services.api.model.DeployedUnit;
import org.jbpm.services.api.model.ProcessInstanceDesc;
import org.jbpm.services.api.query.QueryMapperRegistry;
import org.jbpm.services.api.query.QueryNotFoundException;
import org.jbpm.services.api.query.QueryService;
import org.jbpm.services.task.HumanTaskServiceFactory;
import org.jbpm.services.task.audit.TaskAuditServiceFactory;
import org.jbpm.services.task.identity.JAASUserGroupCallbackImpl;
import org.jbpm.shared.services.impl.TransactionalCommandService;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.model.KieSessionModel;
import org.kie.api.executor.ExecutorService;
import org.kie.api.runtime.process.ProcessInstance;
import org.kie.api.runtime.query.QueryContext;
import org.kie.api.task.TaskService;
import org.kie.api.task.UserGroupCallback;
import org.kie.internal.runtime.conf.AuditMode;
import org.kie.internal.runtime.conf.DeploymentDescriptor;
import org.kie.internal.runtime.conf.MergeMode;
import org.kie.internal.runtime.conf.NamedObjectModel;
import org.kie.internal.runtime.conf.ObjectModel;
import org.kie.internal.runtime.conf.RuntimeStrategy;
import org.kie.internal.runtime.manager.deploy.DeploymentDescriptorManager;
import org.kie.internal.task.api.UserInfo;
import org.kie.scanner.KieModuleMetaData;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.KieServerEnvironment;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.marshalling.MarshallingException;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.KieServerConfig;
import org.kie.server.api.model.KieServerMode;
import org.kie.server.api.model.Message;
import org.kie.server.api.model.Severity;
import org.kie.server.api.model.definition.QueryDefinition;
import org.kie.server.services.api.KieContainerCommandService;
import org.kie.server.services.api.KieContainerInstance;
import org.kie.server.services.api.KieServerApplicationComponentsService;
import org.kie.server.services.api.KieServerExtension;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.api.SupportedTransports;
import org.kie.server.services.impl.KieServerImpl;
import org.kie.server.services.jbpm.admin.ProcessAdminServiceBase;
import org.kie.server.services.jbpm.admin.UserTaskAdminServiceBase;
import org.kie.server.services.jbpm.jpa.PersistenceUnitInfoImpl;
import org.kie.server.services.jbpm.jpa.PersistenceUnitInfoLoader;
import org.kie.server.services.jbpm.security.JMSUserGroupAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.kie.server.api.KieServerConstants.KIE_TASK_ASSIGNING_RUNTIME_EXT_DISABLED;

public class JbpmKieServerExtension implements KieServerExtension {

    public static final String EXTENSION_NAME = "jBPM";
    private static final String PERSISTENCE_XML_LOCATION = "/jpa/META-INF/persistence.xml";
    private static final String IS_DISPOSE_CONTAINER_PARAM = "jBPMExtensionIsDisposeContainer";

    private static final Logger logger = LoggerFactory.getLogger(JbpmKieServerExtension.class);

    private static final Boolean disabled = Boolean.parseBoolean(System.getProperty(KieServerConstants.KIE_JBPM_SERVER_EXT_DISABLED, "false"));

    protected static final Pattern PARAMETER_MATCHER = Pattern.compile("\\$\\{([\\S&&[^\\}]]+)\\}", Pattern.DOTALL);

    protected boolean isExecutorAvailable = false;

    protected String persistenceUnitName = KieServerConstants.KIE_SERVER_PERSISTENCE_UNIT_NAME;

    protected KieServerImpl kieServer;
    protected KieServerRegistry context;

    protected DeploymentService deploymentService;
    protected DefinitionService definitionService;
    protected ProcessService processService;
    protected UserTaskService userTaskService;
    protected RuntimeDataService runtimeDataService;
    protected FormManagerService formManagerService;

    protected ProcessInstanceMigrationService processInstanceMigrationService;
    protected ProcessInstanceAdminService processInstanceAdminService;
    protected UserTaskAdminService userTaskAdminService;

    protected ExecutorService executorService;

    protected QueryService queryService;

    protected KieContainerCommandService kieContainerCommandService;

    protected DeploymentDescriptorManager deploymentDescriptorManager = new DeploymentDescriptorManager(persistenceUnitName);
    protected DeploymentDescriptorMerger merger = new DeploymentDescriptorMerger();

    protected List<Object> services = new ArrayList<Object>();
    protected boolean initialized = false;

    protected Map<String, List<String>> containerMappers = new ConcurrentHashMap<String, List<String>>();
    protected Map<String, List<String>> containerQueries = new ConcurrentHashMap<String, List<String>>();

    @Override
    public boolean isInitialized() {
        return initialized;
    }

    @Override
    public boolean isActive() {
        return disabled == false;
    }

    @Override
    public void init(KieServerImpl kieServer, KieServerRegistry registry) {

        this.kieServer = kieServer;
        this.context = registry;
        configureServices(kieServer, registry);

        if (registry.getKieSessionLookupManager() != null) {
            registry.getKieSessionLookupManager().addHandler(new JBPMKieSessionLookupHandler());
        }

        services.add(formManagerService);
        services.add(deploymentService);
        services.add(definitionService);
        services.add(processService);
        services.add(userTaskService);
        services.add(runtimeDataService);
        services.add(executorService);
        services.add(queryService);
        services.add(processInstanceMigrationService);
        services.add(processInstanceAdminService);
        services.add(userTaskAdminService);

        registerDefaultQueryDefinitions();
        initialized = true;
    }

    protected void configureServices(KieServerImpl kieServer, KieServerRegistry registry) {
        KieServerConfig config = registry.getConfig();

        // loaded from system property as callback info isn't stored as configuration in kie server repository
        String callbackConfig = System.getProperty(KieServerConstants.CFG_HT_CALLBACK);

        // if no other callback set, use jaas by default
        if (callbackConfig == null || callbackConfig.isEmpty()) {
            System.setProperty(KieServerConstants.CFG_HT_CALLBACK, "jaas");
            JAASUserGroupCallbackImpl.addExternalUserGroupAdapter(new JMSUserGroupAdapter());
        }

        this.isExecutorAvailable = isExecutorOnClasspath();

        EntityManagerFactory emf = build(getPersistenceProperties(config));
        EntityManagerFactoryManager.get().addEntityManagerFactory(persistenceUnitName, emf);

        formManagerService = new FormManagerServiceImpl();

        // build definition service
        definitionService = new BPMN2DataServiceImpl();

        // build deployment service
        deploymentService = new KModuleDeploymentService();
        ((KModuleDeploymentService) deploymentService).setBpmn2Service(definitionService);
        ((KModuleDeploymentService) deploymentService).setEmf(emf);
        ((KModuleDeploymentService) deploymentService).setIdentityProvider(registry.getIdentityProvider());
        ((KModuleDeploymentService) deploymentService).setManagerFactory(new RuntimeManagerFactoryImpl());
        ((KModuleDeploymentService) deploymentService).setFormManagerService(formManagerService);

        // configure user group callback
        UserGroupCallback userGroupCallback = UserDataServiceProvider.getUserGroupCallback();

        UserInfo userInfo = UserDataServiceProvider.getUserInfo();

        TaskService taskService = HumanTaskServiceFactory.newTaskServiceConfigurator()
                .entityManagerFactory(emf)
                .userGroupCallback(userGroupCallback)
                .userInfo(userInfo)
                .getTaskService();

        // build runtime data service
        runtimeDataService = new RuntimeDataServiceImpl();
        ((RuntimeDataServiceImpl) runtimeDataService).setCommandService(new TransactionalCommandService(emf));
        ((RuntimeDataServiceImpl) runtimeDataService).setIdentityProvider(registry.getIdentityProvider());
        ((RuntimeDataServiceImpl) runtimeDataService).setTaskService(taskService);
        ((RuntimeDataServiceImpl) runtimeDataService).setTaskAuditService(TaskAuditServiceFactory.newTaskAuditServiceConfigurator()
                                                                                  .setTaskService(taskService)
                                                                                  .getTaskAuditService());
        ((KModuleDeploymentService) deploymentService).setRuntimeDataService(runtimeDataService);

        // build process service
        processService = new ProcessServiceImpl();
        ((ProcessServiceImpl) processService).setDataService(runtimeDataService);
        ((ProcessServiceImpl) processService).setDeploymentService(deploymentService);

        // build user task service
        userTaskService = new UserTaskServiceImpl();
        ((UserTaskServiceImpl) userTaskService).setDataService(runtimeDataService);
        ((UserTaskServiceImpl) userTaskService).setDeploymentService(deploymentService);

        // build query service
        queryService = new QueryServiceImpl();
        ((QueryServiceImpl) queryService).setIdentityProvider(registry.getIdentityProvider());
        ((QueryServiceImpl) queryService).setCommandService(new TransactionalCommandService(emf));
        Function<String, String> kieServerDataSourceResolver = input -> {
            String dataSource = input;
            Matcher matcher = PARAMETER_MATCHER.matcher(dataSource);
            while (matcher.find()) {
                String paramName = matcher.group(1);
                KieServerConfig configuration = context.getStateRepository().load(KieServerEnvironment.getServerId()).getConfiguration();
                dataSource = configuration.getConfigItemValue(paramName, "java:jboss/datasources/ExampleDS");
                logger.info("Data source expression {} resolved to {}", input, dataSource);
            }

            return dataSource;
        };
        ((QueryServiceImpl) queryService).setDataSourceResolver(kieServerDataSourceResolver);
        ((QueryServiceImpl) queryService).init();

        // set runtime data service as listener on deployment service
        ((KModuleDeploymentService) deploymentService).addListener(((RuntimeDataServiceImpl) runtimeDataService));
        ((KModuleDeploymentService) deploymentService).addListener(((BPMN2DataServiceImpl) definitionService));
        ((KModuleDeploymentService) deploymentService).addListener(((QueryServiceImpl) queryService));

        if (config.getConfigItemValue(KieServerConstants.CFG_EXECUTOR_DISABLED, "false").equalsIgnoreCase("false")) {
            String executorQueueName = config.getConfigItemValue(KieServerConstants.CFG_EXECUTOR_JMS_QUEUE, "queue/KIE.SERVER.EXECUTOR");

            // build executor service
            executorService = ExecutorServiceFactory.newExecutorService(emf);
            executorService.setInterval(Integer.parseInt(config.getConfigItemValue(KieServerConstants.CFG_EXECUTOR_INTERVAL, "0")));
            executorService.setRetries(Integer.parseInt(config.getConfigItemValue(KieServerConstants.CFG_EXECUTOR_RETRIES, "3")));
            executorService.setThreadPoolSize(Integer.parseInt(config.getConfigItemValue(KieServerConstants.CFG_EXECUTOR_POOL, "1")));
            executorService.setTimeunit(TimeUnit.valueOf(config.getConfigItemValue(KieServerConstants.CFG_EXECUTOR_TIME_UNIT, "SECONDS")));

            ((ExecutorImpl) ((ExecutorServiceImpl) executorService).getExecutor()).setQueueName(executorQueueName);

            ((KModuleDeploymentService) deploymentService).setExecutorService(executorService);
        }

        // admin services
        this.processInstanceMigrationService = new ProcessInstanceMigrationServiceImpl();
        this.processInstanceAdminService = new ProcessInstanceAdminServiceImpl();
        ((ProcessInstanceAdminServiceImpl) this.processInstanceAdminService).setProcessService(processService);
        ((ProcessInstanceAdminServiceImpl) this.processInstanceAdminService).setRuntimeDataService(runtimeDataService);
        ((ProcessInstanceAdminServiceImpl) this.processInstanceAdminService).setCommandService(new TransactionalCommandService(emf));
        ((ProcessInstanceAdminServiceImpl) this.processInstanceAdminService).setIdentityProvider(registry.getIdentityProvider());

        this.userTaskAdminService = new UserTaskAdminServiceImpl();
        ((UserTaskAdminServiceImpl) this.userTaskAdminService).setRuntimeDataService(runtimeDataService);
        ((UserTaskAdminServiceImpl) this.userTaskAdminService).setUserTaskService(userTaskService);
        ((UserTaskAdminServiceImpl) this.userTaskAdminService).setIdentityProvider(context.getIdentityProvider());
        ((UserTaskAdminServiceImpl) this.userTaskAdminService).setCommandService(new TransactionalCommandService(emf));

        this.kieContainerCommandService = new JBPMKieContainerCommandServiceImpl(context, deploymentService, new DefinitionServiceBase(definitionService, context),
                                                                                 new ProcessServiceBase(processService, definitionService, runtimeDataService, context), new UserTaskServiceBase(userTaskService, context),
                                                                                 new RuntimeDataServiceBase(runtimeDataService, context), new ExecutorServiceBase(executorService, context), new QueryDataServiceBase(queryService, context),
                                                                                 new DocumentServiceBase(context), new ProcessAdminServiceBase(processInstanceMigrationService, processInstanceAdminService, context),
                                                                                 new UserTaskAdminServiceBase(userTaskAdminService, context));
    }

    @Override
    public void destroy(KieServerImpl kieServer, KieServerRegistry registry) {
        ((AbstractDeploymentService) deploymentService).shutdown();

        if (executorService != null) {
            executorService.destroy();
        }

        EntityManagerFactory emf = EntityManagerFactoryManager.get().remove(persistenceUnitName);
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }

    @Override
    public void serverStarted() {
        if (executorService != null) {
            executorService.init();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void createContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
        List<Message> messages = (List<Message>) parameters.get(KieServerConstants.KIE_SERVER_PARAM_MESSAGES);
        try {
            KieModuleMetaData metaData = (KieModuleMetaData) parameters.get(KieServerConstants.KIE_SERVER_PARAM_MODULE_METADATA);
            if (metaData.getProcesses() == null || metaData.getProcesses().isEmpty()) {
                logger.info("Container {} does not include processes, {} skipped", id, this);
                return;
            }

            boolean hasStatefulSession = false;
            boolean hasDefaultSession = false;
            // let validate if they are any stateful sessions defined and in case there are not, skip this container
            InternalKieContainer kieContainer = (InternalKieContainer) kieContainerInstance.getKieContainer();
            Collection<String> kbaseNames = kieContainer.getKieBaseNames();
            Collection<String> ksessionNames = new ArrayList<String>();
            for (String kbaseName : kbaseNames) {
                ksessionNames = kieContainer.getKieSessionNamesInKieBase(kbaseName);

                for (String ksessionName : ksessionNames) {
                    KieSessionModel model = kieContainer.getKieSessionModel(ksessionName);
                    if (model.getType().equals(KieSessionModel.KieSessionType.STATEFUL)) {
                        hasStatefulSession = true;
                    }

                    if (model.isDefault()) {
                        hasDefaultSession = true;
                    }
                }
            }
            if (!hasStatefulSession) {
                logger.info("Container {} does not define stateful ksession thus cannot be handled by extension {}", id, this);
                return;
            }
            ReleaseId releaseId = kieContainerInstance.getKieContainer().getReleaseId();

            KModuleDeploymentUnit unit = new CustomIdKmoduleDeploymentUnit(id, releaseId.getGroupId(), releaseId.getArtifactId(), releaseId.getVersion());

            if (!hasDefaultSession) {
                unit.setKbaseName(kbaseNames.iterator().next());
                unit.setKsessionName(ksessionNames.iterator().next());
            }
            // override defaults if config options are given
            KieServerConfig config = new KieServerConfig(kieContainerInstance.getResource().getConfigItems());

            String runtimeStrategy = config.getConfigItemValue(KieServerConstants.PCFG_RUNTIME_STRATEGY);
            if (runtimeStrategy != null && !runtimeStrategy.isEmpty()) {
                unit.setStrategy(RuntimeStrategy.valueOf(runtimeStrategy));
            }
            String mergeMode = config.getConfigItemValue(KieServerConstants.PCFG_MERGE_MODE);
            if (mergeMode != null && !mergeMode.isEmpty()) {
                unit.setMergeMode(MergeMode.valueOf(mergeMode));
            }
            String ksession = config.getConfigItemValue(KieServerConstants.PCFG_KIE_SESSION);
            unit.setKsessionName(ksession);
            String kbase = config.getConfigItemValue(KieServerConstants.PCFG_KIE_BASE);
            unit.setKbaseName(kbase);

            // reuse kieContainer to avoid unneeded bootstrap
            unit.setKieContainer(kieContainer);

            addAsyncHandler(unit, kieContainer);

            if (System.getProperty(KieServerConstants.CFG_JBPM_TASK_CLEANUP_LISTENER, "true").equalsIgnoreCase("true")) {
                logger.debug("Registering TaskCleanUpProcessEventListener");
                addTaskCleanUpProcessListener(unit, kieContainer);
            }

            if (System.getProperty(KieServerConstants.CFG_JBPM_TASK_BAM_LISTENER, "true").equalsIgnoreCase("true")) {
                logger.debug("Registering BAMTaskEventListener");
                addTaskBAMEventListener(unit, kieContainer);
            }

            if (System.getProperty(KieServerConstants.CFG_JBPM_PROCESS_IDENTITY_LISTENER, "true").equalsIgnoreCase("true")) {
                logger.debug("Registering IdentityProviderAwareProcessListener");
                addProcessIdentityProcessListener(unit, kieContainer);
            }

            deploymentService.deploy(unit);
            // in case it was deployed successfully pass all known classes to marshallers (jaxb, json etc)
            DeployedUnit deployedUnit = deploymentService.getDeployedUnit(unit.getIdentifier());
            Set<Class<?>> customClasses = new HashSet<Class<?>>(deployedUnit.getDeployedClasses());
            // add custom classes that come from extension itself
            customClasses.add(DocumentImpl.class);
            kieContainerInstance.addExtraClasses(customClasses);

            // add any query result mappers from kjar
            List<String> addedMappers = QueryMapperRegistry.get().discoverAndAddMappers(kieContainer.getClassLoader());
            if (addedMappers != null && !addedMappers.isEmpty()) {
                containerMappers.put(id, addedMappers);
            }
            // add any query param builder factories
            QueryParamBuilderManager.get().discoverAndAddQueryFactories(id, kieContainer.getClassLoader());

            // add any query definition found in kjar
            Enumeration<URL> queryDefinitionsFiles = kieContainer.getClassLoader().getResources("query-definitions.json");
            while (queryDefinitionsFiles.hasMoreElements()) {
                try (InputStream qdStream = queryDefinitionsFiles.nextElement().openStream()) {
                    loadAndRegisterQueryDefinitions(qdStream, kieContainerInstance.getMarshaller(MarshallingFormat.JSON), id);
                }
            }

            logger.debug("Container {} created successfully by extension {}", id, this);
        } catch (Exception e) {
            Throwable root = ExceptionUtils.getRootCause(e);
            if (root == null) {
                root = e;
            }
            messages.add(new Message(Severity.ERROR, "Error when creating container " + id + " by extension " + this + " due to " + root.getMessage()));
            logger.error("Error when creating container {} by extension {}", id, this, e);
        }
    }

    @Override
    public boolean isUpdateContainerAllowed(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {

        // Allowing container updates when Kie Server runs in DEVELOPMENT mode.
        if (isDevelopmentMode()) {
            return true;
        }

        // first check if there are any active process instances
        List<Integer> states = new ArrayList<Integer>();
        states.add(ProcessInstance.STATE_ACTIVE);
        states.add(ProcessInstance.STATE_PENDING);
        states.add(ProcessInstance.STATE_SUSPENDED);
        Collection<ProcessInstanceDesc> activeProcesses = runtimeDataService.getProcessInstancesByDeploymentId(id, states, new QueryContext());
        if (!activeProcesses.isEmpty()) {
            parameters.put(KieServerConstants.FAILURE_REASON_PROP, "Update of container forbidden - there are active process instances for container " + id);
            return false;
        }

        return true;
    }

    @Override
    public void prepareContainerUpdate(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
        if (!deploymentService.isDeployed(id)) {
            logger.info("No container with id {} found", id);
            return;
        }

        KModuleDeploymentUnit unit = (KModuleDeploymentUnit) deploymentService.getDeployedUnit(id).getDeploymentUnit();

        Boolean abortInstances = (Boolean) parameters.getOrDefault(KieServerConstants.KIE_SERVER_PARAM_RESET_BEFORE_UPDATE, Boolean.FALSE);

        // Aborting active process instances only if the server runs in DEVELOPMENT mode & we are on a redeployment
        if (isDevelopmentMode() && abortInstances) {
            PreUndeployOperations.abortUnitActiveProcessInstances(runtimeDataService, deploymentService).apply(unit);
        }
    }

    @Override
    public void updateContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
        // essentially it's a redeploy to make sure all components are up to date,
        // though update of kie base is done only once on kie server level and KieContainer is reused across all extensions
        parameters.put(IS_DISPOSE_CONTAINER_PARAM, Boolean.FALSE);

        disposeContainer(id, kieContainerInstance, parameters);

        createContainer(id, kieContainerInstance, parameters);
    }

    @Override
    public void activateContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
        deploymentService.activate(id);
    }

    @Override
    public void deactivateContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
        deploymentService.deactivate(id);
    }

    @Override
    public void disposeContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
        if (!deploymentService.isDeployed(id)) {
            logger.info("No container with id {} found", id);
            return;
        }

        KModuleDeploymentUnit unit = (KModuleDeploymentUnit) deploymentService.getDeployedUnit(id).getDeploymentUnit();

        if (kieServer.getInfo().getResult().getMode().equals(KieServerMode.PRODUCTION)) {
            deploymentService.undeploy(new CustomIdKmoduleDeploymentUnit(id, unit.getGroupId(), unit.getArtifactId(), unit.getVersion()));
        } else {
            // Checking if we are disposing or updating the container. We must only keep process instances only when updating.
            Boolean isDispose = (Boolean) parameters.getOrDefault(IS_DISPOSE_CONTAINER_PARAM, Boolean.TRUE);

            if (isDispose) {
                deploymentService.undeploy(new CustomIdKmoduleDeploymentUnit(id, unit.getGroupId(), unit.getArtifactId(), unit.getVersion()), PreUndeployOperations.abortUnitActiveProcessInstances(runtimeDataService, deploymentService));
            } else {
                deploymentService.undeploy(new CustomIdKmoduleDeploymentUnit(id, unit.getGroupId(), unit.getArtifactId(), unit.getVersion()), PreUndeployOperations.doNothing());
            }
        }

        // remove any query result mappers for container
        List<String> addedMappers = containerMappers.get(id);
        if (addedMappers != null && !addedMappers.isEmpty()) {

            for (String mapper : addedMappers) {
                QueryMapperRegistry.get().removeMapper(mapper);
            }
        }
        // remove any query param builder factories
        QueryParamBuilderManager.get().removeQueryFactories(id);
        logger.debug("Container {} disposed successfully by extension {}", id, this);

        // remove any container specific queries
        List<String> queries = containerQueries.remove(id);
        if (queries != null) {
            logger.debug("Removing queries {} that comes from container {} that is being disposed", queries, id);
            queries.forEach(q -> {
                try {
                    queryService.unregisterQuery(q);
                } catch (QueryNotFoundException e) {
                    logger.debug("Query {} not found when being removed on container dispose", q);
                }
            });
        }
    }

    @Override
    public List<Object> getAppComponents(SupportedTransports type) {
        ServiceLoader<KieServerApplicationComponentsService> appComponentsServices
                = ServiceLoader.load(KieServerApplicationComponentsService.class);
        List<Object> appComponentsList = new ArrayList<Object>();
        Object[] services = {
                deploymentService,
                definitionService,
                processService,
                userTaskService,
                runtimeDataService,
                executorService,
                formManagerService,
                queryService,
                processInstanceMigrationService,
                processInstanceAdminService,
                userTaskAdminService,
                context
        };
        for (KieServerApplicationComponentsService appComponentsService : appComponentsServices) {
            appComponentsList.addAll(appComponentsService.getAppComponents(EXTENSION_NAME, type, services));
        }
        return appComponentsList;
    }

    @Override
    public <T> T getAppComponents(Class<T> serviceType) {
        if (serviceType.isAssignableFrom(kieContainerCommandService.getClass())) {
            return (T) kieContainerCommandService;
        }

        Object[] services = {
                deploymentService,
                definitionService,
                processService,
                userTaskService,
                runtimeDataService,
                executorService,
                formManagerService,
                queryService,
                processInstanceMigrationService,
                processInstanceAdminService,
                userTaskAdminService,
                context
        };

        for (Object service : services) {
            if (service != null && serviceType.isAssignableFrom(service.getClass())) {
                return (T) service;
            }
        }

        return null;
    }

    @Override
    public String getImplementedCapability() {
        return KieServerConstants.CAPABILITY_BPM;
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
        return 0;
    }

    @Override
    public String toString() {
        return EXTENSION_NAME + " KIE Server extension";
    }

    private boolean isDevelopmentMode() {
        return kieServer.getInfo().getResult().getMode().equals(KieServerMode.DEVELOPMENT);
    }

    private static class CustomIdKmoduleDeploymentUnit extends KModuleDeploymentUnit {

        private String id;

        public CustomIdKmoduleDeploymentUnit(String id, String groupId, String artifactId, String version) {
            super(groupId, artifactId, version);
            this.id = id;
        }

        @Override
        public String getIdentifier() {
            return this.id;
        }
    }

    protected void addAsyncHandler(final KModuleDeploymentUnit unit, final InternalKieContainer kieContainer) {
        // add async only when the executor component is not disabled
        if (isExecutorAvailable && executorService != null) {
            final DeploymentDescriptor descriptor = getDeploymentDescriptor(unit, kieContainer);
            descriptor.getBuilder()
                    .addWorkItemHandler(new NamedObjectModel("mvel", "async",
                                                             "new org.jbpm.executor.impl.wih.AsyncWorkItemHandler(org.jbpm.executor.ExecutorServiceFactory.newExecutorService(null),\"org.jbpm.executor.commands.PrintOutCommand\")"));

            unit.setDeploymentDescriptor(descriptor);
        }
    }

    protected void addTaskBAMEventListener(final KModuleDeploymentUnit unit, final InternalKieContainer kieContainer) {
        final DeploymentDescriptor descriptor = getDeploymentDescriptor(unit, kieContainer);
        if (descriptor.getAuditMode() != AuditMode.NONE) {
            descriptor.getBuilder().addTaskEventListener(
                    new ObjectModel(
                            "mvel",
                            "new org.jbpm.services.task.lifecycle.listeners.BAMTaskEventListener(false)"
                    )
            );
            unit.setDeploymentDescriptor(descriptor);
        }
    }

    protected void addTaskCleanUpProcessListener(final KModuleDeploymentUnit unit, final InternalKieContainer kieContainer) {
        final DeploymentDescriptor descriptor = getDeploymentDescriptor(unit, kieContainer);
        descriptor.getBuilder().addEventListener(
                new ObjectModel(
                        "mvel",
                        "new org.jbpm.services.task.admin.listener.TaskCleanUpProcessEventListener(taskService)"
                )
        );
        unit.setDeploymentDescriptor(descriptor);
    }

    protected void addProcessIdentityProcessListener(final KModuleDeploymentUnit unit, final InternalKieContainer kieContainer) {
        final DeploymentDescriptor descriptor = getDeploymentDescriptor(unit, kieContainer);
        descriptor.getBuilder().addEventListener(
                new ObjectModel(
                        "mvel",
                        "new org.jbpm.kie.services.impl.IdentityProviderAwareProcessListener(ksession)"
                )
        );
        unit.setDeploymentDescriptor(descriptor);
    }

    protected DeploymentDescriptor getDeploymentDescriptor(KModuleDeploymentUnit unit, InternalKieContainer kieContainer) {
        DeploymentDescriptor descriptor = unit.getDeploymentDescriptor();
        if (descriptor == null) {
            List<DeploymentDescriptor> descriptorHierarchy = DeploymentDescriptorManagerUtil.getDeploymentDescriptorHierarchy(deploymentDescriptorManager, kieContainer);
            descriptor = merger.merge(descriptorHierarchy, MergeMode.MERGE_COLLECTIONS);
        }
        return descriptor;
    }

    protected boolean isExecutorOnClasspath() {
        try {
            Class.forName("org.jbpm.executor.impl.wih.AsyncWorkItemHandler");

            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    protected Map<String, String> getPersistenceProperties(KieServerConfig config) {
        Map<String, String> persistenceProperties = new HashMap<String, String>();

        persistenceProperties.put("hibernate.dialect", config.getConfigItemValue(KieServerConstants.CFG_PERSISTANCE_DIALECT, "org.hibernate.dialect.H2Dialect"));
        persistenceProperties.put("hibernate.default_schema", config.getConfigItemValue(KieServerConstants.CFG_PERSISTANCE_DEFAULT_SCHEMA));
        persistenceProperties.put("hibernate.transaction.jta.platform", config.getConfigItemValue(KieServerConstants.CFG_PERSISTANCE_TM, "JBossAS"));
        persistenceProperties.put("javax.persistence.jtaDataSource", config.getConfigItemValue(KieServerConstants.CFG_PERSISTANCE_DS, "java:jboss/datasources/ExampleDS"));

        System.getProperties().stringPropertyNames()
                .stream()
                .filter(PersistenceUnitInfoLoader::isValidPersistenceKey)
                .forEach(name -> persistenceProperties.put(name, System.getProperty(name)));

        return persistenceProperties;
    }

    protected EntityManagerFactory build(Map<String, String> properties) {
        try {
            InitialContext ctx = new InitialContext();
            InputStream inputStream = PersistenceUnitInfoLoader.class.getResourceAsStream(PERSISTENCE_XML_LOCATION);
            PersistenceUnitInfo info = PersistenceUnitInfoLoader.load(inputStream, ctx, this.getClass().getClassLoader());
            // prepare persistence unit root location
            URL root = PersistenceUnitInfoLoader.class.getResource(PERSISTENCE_XML_LOCATION);
            String jarLocation = root.toExternalForm().split("!")[0].replace(PERSISTENCE_XML_LOCATION, "");
            try {
                ((PersistenceUnitInfoImpl) info).setPersistenceUnitRootUrl(new URL(jarLocation));
            } catch (Exception e) {
                // in case setting URL to jar file location only fails, fallback to complete URL
                ((PersistenceUnitInfoImpl) info).setPersistenceUnitRootUrl(root);
            }
            checkAndAddTaskAssigningEntities(info);
            // Need to explicitly set jtaDataSource here, its value is fetched in Hibernate logger before configuration
            ((PersistenceUnitInfoImpl) info).setJtaDataSource(properties.get("javax.persistence.jtaDataSource"));
            List<PersistenceProvider> persistenceProviders = PersistenceProviderResolverHolder.getPersistenceProviderResolver().getPersistenceProviders();
            PersistenceProvider selectedProvider = null;
            if (persistenceProviders != null) {
                for (PersistenceProvider provider : persistenceProviders) {
                    if (provider.getClass().getName().equals(info.getPersistenceProviderClassName())) {
                        selectedProvider = provider;
                        break;
                    }
                }
            }

            return selectedProvider.createContainerEntityManagerFactory(info, properties);
        } catch (Exception e) {
            throw new RuntimeException("Unable to create EntityManagerFactory due to " + e.getMessage(), e);
        }
    }

    private void checkAndAddTaskAssigningEntities(PersistenceUnitInfo info) {
        if ("false".equals(System.getProperty(KIE_TASK_ASSIGNING_RUNTIME_EXT_DISABLED))) {
            logger.debug("Adding the task assigning entities to the current jBPM persistent unit info.");
            Class planningTask = null;
            try {
                planningTask = Class.forName("org.kie.server.services.taskassigning.runtime.persistence.PlanningTaskImpl");
            } catch (ClassNotFoundException e) {
                logger.error("PlanningTask implementation for the task assigning api was not found. " +
                                     "Please check that the task assigning runtime jars were properly packaged.", e);
            }

            if (planningTask != null) {
                final String classResource = "/" + planningTask.getName().replaceAll("[.]", "/") + ".class";
                final URL classURL = planningTask.getClassLoader().getResource(classResource);
                if (classURL != null) {
                    info.getManagedClassNames().add(planningTask.getName());
                    final String classJarLocation = classURL.toExternalForm().split("!")[0].replace(classResource, "");
                    try {
                        info.getJarFileUrls().add(new URL(classJarLocation));
                    } catch (Exception e) {
                        // in case setting URL to jar file location only fails, fallback to complete URL
                        info.getJarFileUrls().add(classURL);
                    }
                    logger.debug("Task assigning entities where successfully added.");
                } else {
                    logger.error("Unexpected error, it was not possible to get resource for: {}", classResource);
                }
            }
        }
    }

    protected void loadAndRegisterQueryDefinitions(InputStream qdStream, org.kie.server.api.marshalling.Marshaller marshaller, String containerId) throws IOException {
        if (qdStream != null) {
            String qdString = IOUtils.toString(qdStream, Charset.forName("UTF-8"));

            try {
                QueryDefinition[] queryDefinitionList = marshaller.unmarshall(qdString, QueryDefinition[].class);
                List<String> queries = new ArrayList<>();
                Arrays.asList(queryDefinitionList).forEach(qd ->
                                                           {
                                                               queryService.replaceQuery(QueryDataServiceBase.build(context, qd));
                                                               queries.add(qd.getName());
                                                               logger.debug("Registered '{}' query from container '{}' successfully", qd.getName(), containerId);
                                                           });

                if (containerId != null) {
                    containerQueries.put(containerId, queries);
                }
            } catch (MarshallingException e) {
                logger.error("Error when unmarshalling query definitions from stream.", e);
            }
        }
    }

    protected void registerDefaultQueryDefinitions() {
        try (InputStream qdStream = getDefaultQueryDefinitionsInputStream()) {
            loadAndRegisterQueryDefinitions(qdStream, MarshallerFactory.getMarshaller(MarshallingFormat.JSON, this.getClass().getClassLoader()), null);
        } catch (Exception e) {
            logger.error("Error when loading default query definitions from default-query-definitions.json", e);
        }
    }

    private InputStream getDefaultQueryDefinitionsInputStream() throws FileNotFoundException {
        // load any default query definitions
        InputStream qdStream = this.getClass().getResourceAsStream("/default-query-definitions.json");
        if (qdStream == null) {
            String externalLocationQueryDefinitions = System.getProperty(KieServerConstants.CFG_DEFAULT_QUERY_DEFS_LOCATION);
            if (externalLocationQueryDefinitions != null) {
                qdStream = new FileInputStream(externalLocationQueryDefinitions);
            }
        }
        return qdStream;
    }

    // just for tests
    void setQueryService(QueryService queryService) {
        this.queryService = queryService;
    }

    void setContext(KieServerRegistry context) {
        this.context = context;
    }

    @Override
    public List<Message> healthCheck(boolean report) {
        List<Message> messages = KieServerExtension.super.healthCheck(report);

        try {
            // run base query to make sure data access layer is available
            runtimeDataService.getProcessInstanceById(-99999);
            if (report) {
                messages.add(new Message(Severity.INFO, getExtensionName() + " is alive"));
            }
        } catch (Exception e) {
            messages.add(new Message(Severity.ERROR, getExtensionName() + " failed due to " + e.getMessage()));
        }

        return messages;
    }
}
