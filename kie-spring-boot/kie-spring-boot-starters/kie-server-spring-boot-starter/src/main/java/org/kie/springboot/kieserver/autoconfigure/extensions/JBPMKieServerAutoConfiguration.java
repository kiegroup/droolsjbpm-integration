package org.kie.springboot.kieserver.autoconfigure.extensions;

import java.util.Optional;

import org.jbpm.kie.services.impl.FormManagerService;
import org.jbpm.services.api.DefinitionService;
import org.jbpm.services.api.DeploymentService;
import org.jbpm.services.api.ProcessService;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.UserTaskService;
import org.jbpm.services.api.admin.ProcessInstanceAdminService;
import org.jbpm.services.api.admin.ProcessInstanceMigrationService;
import org.jbpm.services.api.admin.UserTaskAdminService;
import org.jbpm.services.api.query.QueryService;
import org.jbpm.springboot.autoconfigure.JBPMAutoConfiguration;
import org.kie.api.executor.ExecutorService;
import org.kie.server.services.api.KieServerExtension;
import org.kie.server.services.drools.DroolsKieServerExtension;
import org.kie.server.services.impl.KieServerImpl;
import org.kie.springboot.kieserver.autoconfigure.KieServerProperties;
import org.kie.springboot.kieserver.autoconfigure.extensions.jbpm.SpringBootJBPMKieServerExtension;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass({KieServerImpl.class})
@AutoConfigureAfter({JBPMAutoConfiguration.class})
@EnableConfigurationProperties(KieServerProperties.class)
public class JBPMKieServerAutoConfiguration {

    private KieServerProperties properties;

    private DeploymentService deploymentService;
    private DefinitionService definitionService;
    private ProcessService processService;
    private UserTaskService userTaskService;
    private RuntimeDataService runtimeDataService;
    private FormManagerService formManagerService;

    private ProcessInstanceMigrationService processInstanceMigrationService;
    private ProcessInstanceAdminService processInstanceAdminService;
    private UserTaskAdminService userTaskAdminService;

    private ExecutorService executorService;

    private QueryService queryService;

    public JBPMKieServerAutoConfiguration(KieServerProperties properties,
                                          DeploymentService deploymentService,
                                          DefinitionService definitionService,
                                          ProcessService processService,
                                          UserTaskService userTaskService,
                                          RuntimeDataService runtimeDataService,
                                          FormManagerService formManagerService,

                                          ProcessInstanceMigrationService processInstanceMigrationService,
                                          ProcessInstanceAdminService processInstanceAdminService,
                                          UserTaskAdminService userTaskAdminService,

                                          Optional<ExecutorService> executorService,
                                          QueryService queryService) {
        this.properties = properties;
        this.deploymentService = deploymentService;
        this.definitionService = definitionService;
        this.processService = processService;
        this.userTaskService = userTaskService;
        this.runtimeDataService = runtimeDataService;
        this.formManagerService = formManagerService;

        this.processInstanceMigrationService = processInstanceMigrationService;
        this.processInstanceAdminService = processInstanceAdminService;
        this.userTaskAdminService = userTaskAdminService;

        if (executorService.isPresent()) {
            this.executorService = executorService.get();
        }
        this.queryService = queryService;
    }

    @Bean
    @ConditionalOnMissingBean(name = "jBPMServerExtension")
    @ConditionalOnProperty(name = "kieserver.jbpm.enabled")
    public KieServerExtension jbpmServerExtension() {

        return new SpringBootJBPMKieServerExtension(deploymentService, definitionService, processService, userTaskService, runtimeDataService, formManagerService, processInstanceMigrationService,
                                                    processInstanceAdminService, userTaskAdminService, executorService, queryService);

    }
}
