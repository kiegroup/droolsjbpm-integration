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

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import javax.naming.InitialContext;
import javax.persistence.EntityManagerFactory;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceProviderResolverHolder;
import javax.persistence.spi.PersistenceUnitInfo;

import org.drools.compiler.kie.builder.impl.InternalKieContainer;
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
import org.jbpm.kie.services.impl.admin.ProcessInstanceMigrationServiceImpl;
import org.jbpm.kie.services.impl.bpmn2.BPMN2DataServiceImpl;
import org.jbpm.kie.services.impl.query.QueryServiceImpl;
import org.jbpm.runtime.manager.impl.RuntimeManagerFactoryImpl;
import org.jbpm.runtime.manager.impl.deploy.DeploymentDescriptorManager;
import org.jbpm.runtime.manager.impl.deploy.DeploymentDescriptorMerger;
import org.jbpm.runtime.manager.impl.identity.UserDataServiceProvider;
import org.jbpm.runtime.manager.impl.jpa.EntityManagerFactoryManager;
import org.jbpm.services.api.DefinitionService;
import org.jbpm.services.api.DeploymentService;
import org.jbpm.services.api.ProcessService;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.UserTaskService;
import org.jbpm.services.api.admin.ProcessInstanceMigrationService;
import org.jbpm.services.api.model.DeployedUnit;
import org.jbpm.services.api.model.ProcessInstanceDesc;
import org.jbpm.services.api.query.QueryMapperRegistry;
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
import org.kie.internal.runtime.conf.DeploymentDescriptor;
import org.kie.internal.runtime.conf.MergeMode;
import org.kie.internal.runtime.conf.NamedObjectModel;
import org.kie.internal.runtime.conf.ObjectModel;
import org.kie.internal.runtime.conf.RuntimeStrategy;
import org.kie.internal.task.api.UserInfo;
import org.kie.scanner.KieModuleMetaData;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.model.KieServerConfig;
import org.kie.server.services.api.KieContainerCommandService;
import org.kie.server.services.api.KieContainerInstance;
import org.kie.server.services.api.KieServerApplicationComponentsService;
import org.kie.server.services.api.KieServerExtension;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.api.SupportedTransports;
import org.kie.server.services.impl.KieServerImpl;
import org.kie.server.services.jbpm.admin.ProcessAdminServiceBase;
import org.kie.server.services.jbpm.jpa.PersistenceUnitInfoImpl;
import org.kie.server.services.jbpm.jpa.PersistenceUnitInfoLoader;
import org.kie.server.services.jbpm.security.JMSUserGroupAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JbpmKieServerExtension implements KieServerExtension {

    public static final String EXTENSION_NAME = "jBPM";
    private static final String PERSISTENCE_XML_LOCATION = "/jpa/META-INF/persistence.xml";

    private static final Logger logger = LoggerFactory.getLogger(JbpmKieServerExtension.class);

    private static final Boolean disabled = Boolean.parseBoolean(System.getProperty(KieServerConstants.KIE_JBPM_SERVER_EXT_DISABLED, "false"));


    private boolean isExecutorAvailable = false;

    private String persistenceUnitName = KieServerConstants.KIE_SERVER_PERSISTENCE_UNIT_NAME;

    private KieServerImpl kieServer;
    private KieServerRegistry context;

    private DeploymentService deploymentService;
    private DefinitionService definitionService;
    private ProcessService processService;
    private UserTaskService userTaskService;
    private RuntimeDataService runtimeDataService;
    private FormManagerService formManagerService;

    private ProcessInstanceMigrationService processInstanceMigrationService;

    private ExecutorService executorService;

    private QueryService queryService;

    private KieContainerCommandService kieContainerCommandService;

    private DeploymentDescriptorManager deploymentDescriptorManager = new DeploymentDescriptorManager(persistenceUnitName);
    private DeploymentDescriptorMerger merger = new DeploymentDescriptorMerger();

    private List<Object> services = new ArrayList<Object>();

    private Map<String, List<String>> containerMappers = new ConcurrentHashMap<String, List<String>>();

    @Override
    public boolean isActive() {
        return disabled == false;
    }

    @Override
    public void init(KieServerImpl kieServer, KieServerRegistry registry) {

        KieServerConfig config = registry.getConfig();

        // loaded from system property as callback info isn't stored as configuration in kie server repository
        String callbackConfig = System.getProperty(KieServerConstants.CFG_HT_CALLBACK);

        // if no other callback set, use jaas by default
        if (callbackConfig == null || callbackConfig.isEmpty()) {
            System.setProperty(KieServerConstants.CFG_HT_CALLBACK, "jaas");
            JAASUserGroupCallbackImpl.addExternalUserGroupAdapter(new JMSUserGroupAdapter());
        }

        this.isExecutorAvailable = isExecutorOnClasspath();

        this.kieServer = kieServer;
        this.context = registry;


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
        ((QueryServiceImpl)queryService).setIdentityProvider(registry.getIdentityProvider());
        ((QueryServiceImpl)queryService).setCommandService(new TransactionalCommandService(emf));
        ((QueryServiceImpl)queryService).init();

        // set runtime data service as listener on deployment service
        ((KModuleDeploymentService) deploymentService).addListener(((RuntimeDataServiceImpl) runtimeDataService));
        ((KModuleDeploymentService) deploymentService).addListener(((BPMN2DataServiceImpl) definitionService));
        ((KModuleDeploymentService) deploymentService).addListener(((QueryServiceImpl) queryService));

        if (config.getConfigItemValue(KieServerConstants.CFG_EXECUTOR_DISABLED, "false").equalsIgnoreCase("false")) {
            String executorQueueName = System.getProperty("org.kie.executor.jms.queue", "queue/KIE.SERVER.EXECUTOR");

            // build executor service
            executorService = ExecutorServiceFactory.newExecutorService(emf);
            executorService.setInterval(Integer.parseInt(config.getConfigItemValue(KieServerConstants.CFG_EXECUTOR_INTERVAL, "3")));
            executorService.setRetries(Integer.parseInt(config.getConfigItemValue(KieServerConstants.CFG_EXECUTOR_RETRIES, "3")));
            executorService.setThreadPoolSize(Integer.parseInt(config.getConfigItemValue(KieServerConstants.CFG_EXECUTOR_POOL, "1")));
            executorService.setTimeunit(TimeUnit.valueOf(config.getConfigItemValue(KieServerConstants.CFG_EXECUTOR_TIME_UNIT, "SECONDS")));

            ((ExecutorImpl) ((ExecutorServiceImpl) executorService).getExecutor()).setQueueName(executorQueueName);

            executorService.init();

            ((KModuleDeploymentService) deploymentService).setExecutorService(executorService);
        }

        // admin services
        this.processInstanceMigrationService = new ProcessInstanceMigrationServiceImpl();

        this.kieContainerCommandService = new JBPMKieContainerCommandServiceImpl(context, deploymentService, new DefinitionServiceBase(definitionService),
                new ProcessServiceBase(processService, definitionService, runtimeDataService, context), new UserTaskServiceBase(userTaskService, context),
                new RuntimeDataServiceBase(runtimeDataService, context), new ExecutorServiceBase(executorService, context), new QueryDataServiceBase(queryService, context),
                new DocumentServiceBase(context), new ProcessAdminServiceBase(processInstanceMigrationService, context));

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
    }

    @Override
    public void destroy(KieServerImpl kieServer, KieServerRegistry registry) {
        ((AbstractDeploymentService)deploymentService).shutdown();

        if (executorService != null) {
            executorService.destroy();
        }

        EntityManagerFactory emf = EntityManagerFactoryManager.get().remove(persistenceUnitName);
        if (emf != null && emf.isOpen()) {
            emf.close();
        }
    }

    @Override
    public void createContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
        try {
            KieModuleMetaData metaData = (KieModuleMetaData) parameters.get(KieServerConstants.KIE_SERVER_PARAM_MODULE_METADATA);
            if (metaData.getProcesses() == null || metaData.getProcesses().isEmpty()) {
                logger.info("Container {} does not include processes, {} skipped", id, this);
                return;
            }

            boolean hasStatefulSession = false;
            boolean hasDefaultSession = false;
            // let validate if they are any stateful sessions defined and in case there are not, skip this container
            InternalKieContainer kieContainer = (InternalKieContainer)kieContainerInstance.getKieContainer();
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

            String runtimeStrategy = config.getConfigItemValue("RuntimeStrategy");
            if (runtimeStrategy != null && !runtimeStrategy.isEmpty()) {
                unit.setStrategy(RuntimeStrategy.valueOf(runtimeStrategy));
            }
            String mergeMode = config.getConfigItemValue("MergeMode");
            if (mergeMode != null && !mergeMode.isEmpty()) {
                unit.setMergeMode(MergeMode.valueOf(mergeMode));
            }
            String ksession = config.getConfigItemValue("KSession");
            unit.setKsessionName(ksession);
            String kbase = config.getConfigItemValue("KBase");
            unit.setKbaseName(kbase);

            // reuse kieContainer to avoid unneeded bootstrap
            unit.setKieContainer(kieContainer);

            addAsyncHandler(unit, kieContainer);

            if (config.getConfigItemValue(KieServerConstants.CFG_JBPM_TASK_CLEANUP_LISTENER, "true").equalsIgnoreCase("true")) {
                logger.debug("Registering TaskCleanUpProcessEventListener");
                addTaskCleanUpProcessListener(unit, kieContainer);
            }

            if (config.getConfigItemValue(KieServerConstants.CFG_JBPM_TASK_BAM_LISTENER, "true").equalsIgnoreCase("true")) {
                logger.debug("Registering BAMTaskEventListener");
                addTaskBAMEventListener(unit, kieContainer);
            }

            if (config.getConfigItemValue(KieServerConstants.CFG_JBPM_PROCESS_IDENTITY_LISTENER, "true").equalsIgnoreCase("true")) {
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

            logger.debug("Container {} created successfully by extension {}", id, this);
        } catch (Exception e) {
            logger.error("Error when creating container {} by extension {}", id, this);
        }
    }

    @Override
    public boolean isUpdateContainerAllowed(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
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
    public void updateContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
        // essentially it's a redeploy to make sure all components are up to date,
        // though update of kie base is done only once on kie server level and KieContainer is reused across all extensions
        disposeContainer(id, kieContainerInstance, parameters);

        createContainer(id, kieContainerInstance, parameters);
    }

    @Override
    public void disposeContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
        if (!deploymentService.isDeployed(id)) {
            logger.info("No container with id {} found", id);
            return;
        }
        List<Integer> states = new ArrayList<Integer>();
        states.add(ProcessInstance.STATE_ACTIVE);
        states.add(ProcessInstance.STATE_PENDING);
        states.add(ProcessInstance.STATE_SUSPENDED);

        KModuleDeploymentUnit unit = (KModuleDeploymentUnit) deploymentService.getDeployedUnit(id).getDeploymentUnit();
        deploymentService.undeploy(new CustomIdKmoduleDeploymentUnit(id, unit.getGroupId(), unit.getArtifactId(), unit.getVersion()));

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
    }

    @Override
    public List<Object> getAppComponents(SupportedTransports type) {
        ServiceLoader<KieServerApplicationComponentsService> appComponentsServices
            = ServiceLoader.load(KieServerApplicationComponentsService.class);
        List<Object> appComponentsList = new ArrayList<Object>();
        Object [] services = {
                deploymentService,
                definitionService,
                processService,
                userTaskService,
                runtimeDataService,
                executorService,
                formManagerService,
                queryService,
                processInstanceMigrationService,
                context
        };
        for( KieServerApplicationComponentsService appComponentsService : appComponentsServices ) {
           appComponentsList.addAll(appComponentsService.getAppComponents(EXTENSION_NAME, type, services));
        }
        return appComponentsList;
    }

    @Override
    public <T> T getAppComponents(Class<T> serviceType) {
        if (serviceType.isAssignableFrom(kieContainerCommandService.getClass())) {
            return (T) kieContainerCommandService;
        }

        Object [] services = {
                deploymentService,
                definitionService,
                processService,
                userTaskService,
                runtimeDataService,
                executorService,
                formManagerService,
                queryService,
                processInstanceMigrationService,
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
                            "new org.jbpm.executor.impl.wih.AsyncWorkItemHandler(org.jbpm.executor.ExecutorServiceFactory.newExecutorService(),\"org.jbpm.executor.commands.PrintOutCommand\")"));

            unit.setDeploymentDescriptor(descriptor);
        }
    }

    protected void addTaskBAMEventListener(final KModuleDeploymentUnit unit, final InternalKieContainer kieContainer) {
        final DeploymentDescriptor descriptor = getDeploymentDescriptor(unit, kieContainer);
        descriptor.getBuilder().addTaskEventListener(
                new ObjectModel(
                        "mvel",
                        "new org.jbpm.services.task.lifecycle.listeners.BAMTaskEventListener(false)"
                )
        );
        unit.setDeploymentDescriptor(descriptor);
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
            List<DeploymentDescriptor> descriptorHierarchy = deploymentDescriptorManager.getDeploymentDescriptorHierarchy(kieContainer);
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
        persistenceProperties.put("hibernate.transaction.jta.platform", config.getConfigItemValue(KieServerConstants.CFG_PERSISTANCE_TM, "org.hibernate.service.jta.platform.internal.JBossAppServerJtaPlatform"));
        persistenceProperties.put("javax.persistence.jtaDataSource", config.getConfigItemValue(KieServerConstants.CFG_PERSISTANCE_DS, "java:jboss/datasources/ExampleDS"));

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

}
