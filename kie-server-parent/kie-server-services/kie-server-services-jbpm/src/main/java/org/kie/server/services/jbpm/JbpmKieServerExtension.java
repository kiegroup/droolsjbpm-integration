package org.kie.server.services.jbpm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import javax.persistence.EntityManagerFactory;

import org.drools.compiler.kie.builder.impl.InternalKieContainer;
import org.jbpm.executor.ExecutorServiceFactory;
import org.jbpm.kie.services.impl.AbstractDeploymentService;
import org.jbpm.kie.services.impl.FormManagerServiceImpl;
import org.jbpm.kie.services.impl.KModuleDeploymentService;
import org.jbpm.kie.services.impl.KModuleDeploymentUnit;
import org.jbpm.kie.services.impl.ProcessServiceImpl;
import org.jbpm.kie.services.impl.RuntimeDataServiceImpl;
import org.jbpm.kie.services.impl.UserTaskServiceImpl;
import org.jbpm.kie.services.impl.bpmn2.BPMN2DataServiceImpl;
import org.jbpm.runtime.manager.impl.RuntimeManagerFactoryImpl;
import org.jbpm.runtime.manager.impl.deploy.DeploymentDescriptorImpl;
import org.jbpm.runtime.manager.impl.jpa.EntityManagerFactoryManager;
import org.jbpm.services.api.DefinitionService;
import org.jbpm.services.api.DeploymentService;
import org.jbpm.services.api.ProcessService;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.UserTaskService;
import org.jbpm.services.api.model.DeployedUnit;
import org.jbpm.services.task.HumanTaskServiceFactory;
import org.jbpm.services.task.identity.JAASUserGroupCallbackImpl;
import org.jbpm.shared.services.impl.TransactionalCommandService;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.model.KieSessionModel;
import org.kie.internal.executor.api.ExecutorService;
import org.kie.internal.runtime.conf.DeploymentDescriptor;
import org.kie.internal.runtime.conf.NamedObjectModel;
import org.kie.server.services.api.KieContainerInstance;
import org.kie.server.services.api.KieServerApplicationComponentsService;
import org.kie.server.services.api.KieServerExtension;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.api.SupportedTransports;
import org.kie.server.services.impl.KieServerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JbpmKieServerExtension implements KieServerExtension {

    public static final String EXTENSION_NAME = "jBPM";

    private static final Logger logger = LoggerFactory.getLogger(JbpmKieServerExtension.class);

    private static final Boolean disabled = Boolean.parseBoolean(System.getProperty("org.jbpm.server.ext.disabled", "false"));


    private boolean isExecutorAvailable = false;

    private String persistenceUnitName = "org.jbpm.domain";

    private KieServerImpl kieServer;
    private KieServerRegistry context;

    private DeploymentService deploymentService;
    private DefinitionService definitionService;
    private ProcessService processService;
    private UserTaskService userTaskService;
    private RuntimeDataService runtimeDataService;

    private ExecutorService executorService;

    @Override
    public boolean isActive() {
        return disabled == false;
    }

    @Override
    public void init(KieServerImpl kieServer, KieServerRegistry registry) {
        this.isExecutorAvailable = isExecutorOnClasspath();

        this.kieServer = kieServer;
        this.context = registry;

        EntityManagerFactory emf = EntityManagerFactoryManager.get().getOrCreate(persistenceUnitName);

        // build definition service
        definitionService = new BPMN2DataServiceImpl();

        // build deployment service
        deploymentService = new KModuleDeploymentService();
        ((KModuleDeploymentService)deploymentService).setBpmn2Service(definitionService);
        ((KModuleDeploymentService)deploymentService).setEmf(emf);
        ((KModuleDeploymentService)deploymentService).setIdentityProvider(registry.getIdentityProvider());
        ((KModuleDeploymentService)deploymentService).setManagerFactory(new RuntimeManagerFactoryImpl());
        ((KModuleDeploymentService)deploymentService).setFormManagerService(new FormManagerServiceImpl());

        // build runtime data service
        runtimeDataService = new RuntimeDataServiceImpl();
        ((RuntimeDataServiceImpl) runtimeDataService).setCommandService(new TransactionalCommandService(emf));
        ((RuntimeDataServiceImpl) runtimeDataService).setIdentityProvider(registry.getIdentityProvider());
        ((RuntimeDataServiceImpl) runtimeDataService).setTaskService(HumanTaskServiceFactory.newTaskServiceConfigurator()
                .entityManagerFactory(emf)
                .userGroupCallback(new JAASUserGroupCallbackImpl(true))
                .getTaskService());
        ((KModuleDeploymentService)deploymentService).setRuntimeDataService(runtimeDataService);

        // set runtime data service as listener on deployment service
        ((KModuleDeploymentService)deploymentService).addListener(((RuntimeDataServiceImpl) runtimeDataService));
        ((KModuleDeploymentService)deploymentService).addListener(((BPMN2DataServiceImpl) definitionService));

        // build process service
        processService = new ProcessServiceImpl();
        ((ProcessServiceImpl) processService).setDataService(runtimeDataService);
        ((ProcessServiceImpl) processService).setDeploymentService(deploymentService);

        // build user task service
        userTaskService = new UserTaskServiceImpl();
        ((UserTaskServiceImpl) userTaskService).setDataService(runtimeDataService);
        ((UserTaskServiceImpl) userTaskService).setDeploymentService(deploymentService);

        // build executor service
        executorService = ExecutorServiceFactory.newExecutorService(emf);
        executorService.init();
    }

    @Override
    public void destroy(KieServerImpl kieServer, KieServerRegistry registry) {
        ((AbstractDeploymentService)deploymentService).shutdown();

        executorService.destroy();
    }

    @Override
    public void createContainer(String id, KieContainerInstance kieContainerInstance, Map<String, Object> parameters) {
        try {
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
            // reuse kieContainer to avoid unneeded bootstrap
            unit.setKieContainer(kieContainer);

            addAsyncHandler(unit);

            deploymentService.deploy(unit);
            // in case it was deployed successfully pass all known classes to marshallers (jaxb, json etc)
            DeployedUnit deployedUnit = deploymentService.getDeployedUnit(unit.getIdentifier());
            kieContainerInstance.addJaxbClasses(new HashSet<Class<?>>(deployedUnit.getDeployedClasses()));

            kieContainerInstance.addService(deploymentService);
            kieContainerInstance.addService(definitionService);
            kieContainerInstance.addService(processService);
            kieContainerInstance.addService(userTaskService);
            kieContainerInstance.addService(runtimeDataService);
            kieContainerInstance.addService(executorService);

            logger.info("Container {} created successfully", id);
        } catch (Exception e) {
            logger.error("Error when creating container {} by extension {}", id, this);
        }
    }

    @Override
    public void disposeContainer(String id, Map<String, Object> parameters) {
        if (!deploymentService.isDeployed(id)) {
            logger.warn("No container with id {} found", id);
            return;
        }
        KModuleDeploymentUnit unit = (KModuleDeploymentUnit) deploymentService.getDeployedUnit(id).getDeploymentUnit();
        deploymentService.undeploy(new CustomIdKmoduleDeploymentUnit(id, unit.getGroupId(), unit.getArtifactId(), unit.getVersion()));
        logger.info("Container {} disposed successfully", id);
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
                context
        };
        for( KieServerApplicationComponentsService appComponentsService : appComponentsServices ) {
           appComponentsList.addAll(appComponentsService.getAppComponents(EXTENSION_NAME, type, services));
        }
        return appComponentsList;
    }

    @Override
    public <T> T getAppComponents(Class<T> serviceType) {
        return null;
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

    protected void addAsyncHandler(KModuleDeploymentUnit unit) {
        // add async only when the executor component is not disabled
        if (isExecutorAvailable) {
            DeploymentDescriptor descriptor = unit.getDeploymentDescriptor();
            if (descriptor == null) {
                descriptor = new DeploymentDescriptorImpl(persistenceUnitName);
            }
            descriptor.getBuilder()
                    .addWorkItemHandler(new NamedObjectModel("mvel", "async",
                            "new org.jbpm.executor.impl.wih.AsyncWorkItemHandler(org.jbpm.executor.ExecutorServiceFactory.newExecutorService(),\"org.jbpm.executor.commands.PrintOutCommand\")"));

            unit.setDeploymentDescriptor(descriptor);
        }
    }

    protected boolean isExecutorOnClasspath() {
        try {
            Class.forName("org.jbpm.executor.impl.wih.AsyncWorkItemHandler");

            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
