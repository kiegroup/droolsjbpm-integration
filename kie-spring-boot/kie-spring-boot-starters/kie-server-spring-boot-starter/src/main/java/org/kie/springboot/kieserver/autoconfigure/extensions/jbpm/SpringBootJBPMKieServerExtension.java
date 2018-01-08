package org.kie.springboot.kieserver.autoconfigure.extensions.jbpm;

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
import org.kie.api.executor.ExecutorService;
import org.kie.server.services.api.KieServerRegistry;
import org.kie.server.services.impl.KieServerImpl;
import org.kie.server.services.jbpm.JbpmKieServerExtension;

public class SpringBootJBPMKieServerExtension extends JbpmKieServerExtension {

    public SpringBootJBPMKieServerExtension(DeploymentService deploymentService,
                                            DefinitionService definitionService,
                                            ProcessService processService,
                                            UserTaskService userTaskService,
                                            RuntimeDataService runtimeDataService,
                                            FormManagerService formManagerService,

                                            ProcessInstanceMigrationService processInstanceMigrationService,
                                            ProcessInstanceAdminService processInstanceAdminService,
                                            UserTaskAdminService userTaskAdminService,

                                            ExecutorService executorService,
                                            QueryService queryService) {
        
        this.deploymentService = deploymentService;
        this.definitionService = definitionService;
        this.processService = processService;
        this.userTaskService = userTaskService;
        this.runtimeDataService = runtimeDataService;
        this.formManagerService = formManagerService;

        this.processInstanceMigrationService = processInstanceMigrationService;
        this.processInstanceAdminService = processInstanceAdminService;
        this.userTaskAdminService = userTaskAdminService;

        this.executorService = executorService;
        this.queryService = queryService;
    }

    @Override
    protected void configureServices(KieServerImpl kieServer, KieServerRegistry registry) {
        // all services are injected from JBPMAutoConfigure
    }

}
