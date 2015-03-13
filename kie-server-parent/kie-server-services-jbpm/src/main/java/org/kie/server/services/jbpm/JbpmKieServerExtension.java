package org.kie.server.services.jbpm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManagerFactory;

import org.drools.compiler.kie.builder.impl.InternalKieContainer;
import org.jbpm.kie.services.impl.KModuleDeploymentService;
import org.jbpm.kie.services.impl.KModuleDeploymentUnit;
import org.jbpm.kie.services.impl.ProcessServiceImpl;
import org.jbpm.kie.services.impl.RuntimeDataServiceImpl;
import org.jbpm.kie.services.impl.UserTaskServiceImpl;
import org.jbpm.kie.services.impl.bpmn2.BPMN2DataServiceImpl;
import org.jbpm.runtime.manager.impl.RuntimeManagerFactoryImpl;
import org.jbpm.runtime.manager.impl.jpa.EntityManagerFactoryManager;
import org.jbpm.services.api.DefinitionService;
import org.jbpm.services.api.DeploymentService;
import org.jbpm.services.api.ProcessService;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.UserTaskService;
import org.jbpm.services.task.HumanTaskServiceFactory;
import org.jbpm.services.task.identity.JAASUserGroupCallbackImpl;
import org.jbpm.shared.services.impl.TransactionalCommandService;
import org.kie.api.builder.ReleaseId;
import org.kie.api.builder.model.KieSessionModel;
import org.kie.api.runtime.KieContainer;
import org.kie.server.services.api.KieContainerInstance;
import org.kie.server.services.api.KieServerExtension;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.api.SupportedTransports;
import org.kie.server.services.impl.KieServerImpl;
import org.kie.server.services.jbpm.rest.ProcessServiceResource;
import org.kie.server.services.jbpm.rest.RuntimeDataServiceResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JbpmKieServerExtension implements KieServerExtension {

    private static final Logger logger = LoggerFactory.getLogger(JbpmKieServerExtension.class);

    private static final Boolean disabled = Boolean.parseBoolean(System.getProperty("org.jbpm.server.ext.disabled", "false"));

    private KieServerImpl kieServer;

    private DeploymentService deploymentService;
    private DefinitionService definitionService;
    private ProcessService processService;
    private UserTaskService userTaskService;
    private RuntimeDataService runtimeDataService;

    @Override
    public boolean isActive() {
        return disabled == false;
    }

    @Override
    public void init(KieServerImpl kieServer, KieServerRegistry registry) {
        this.kieServer = kieServer;
        EntityManagerFactory emf = EntityManagerFactoryManager.get().getOrCreate("org.jbpm.domain");

        // build definition service
        definitionService = new BPMN2DataServiceImpl();

        // build deployment service
        deploymentService = new KModuleDeploymentService();
        ((KModuleDeploymentService)deploymentService).setBpmn2Service(definitionService);
        ((KModuleDeploymentService)deploymentService).setEmf(emf);
        ((KModuleDeploymentService)deploymentService).setIdentityProvider(registry.getIdentityProvider());
        ((KModuleDeploymentService)deploymentService).setManagerFactory(new RuntimeManagerFactoryImpl());

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

            deploymentService.deploy(unit);

            kieContainerInstance.addService(deploymentService);
            kieContainerInstance.addService(definitionService);
            kieContainerInstance.addService(processService);
            kieContainerInstance.addService(userTaskService);
            kieContainerInstance.addService(runtimeDataService);

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
        List<Object> applicationComponents = new ArrayList<Object>();
        if (type.equals(SupportedTransports.REST)) {
            applicationComponents.add(new ProcessServiceResource(processService));
            applicationComponents.add(new RuntimeDataServiceResource(runtimeDataService));
        }
        return applicationComponents;
    }

    @Override
    public <T> T getAppComponents(Class<T> serviceType) {
        return null;
    }

    @Override
    public String toString() {
        return "jBPM KIE Server extension";
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
}
