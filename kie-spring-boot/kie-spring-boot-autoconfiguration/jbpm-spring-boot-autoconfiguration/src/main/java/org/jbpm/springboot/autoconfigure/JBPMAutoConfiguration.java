/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.jbpm.springboot.autoconfigure;

import java.io.IOException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

import org.dashbuilder.dataprovider.sql.SQLDataSetProvider;
import org.dashbuilder.dataprovider.sql.SQLDataSourceLocator;
import org.dashbuilder.dataset.def.SQLDataSetDef;
import org.drools.core.impl.EnvironmentFactory;
import org.drools.persistence.api.TransactionManager;
import org.jbpm.casemgmt.api.CaseRuntimeDataService;
import org.jbpm.casemgmt.api.CaseService;
import org.jbpm.casemgmt.api.generator.CaseIdGenerator;
import org.jbpm.casemgmt.impl.AuthorizationManagerImpl;
import org.jbpm.casemgmt.impl.CaseRuntimeDataServiceImpl;
import org.jbpm.casemgmt.impl.CaseServiceImpl;
import org.jbpm.casemgmt.impl.event.CaseConfigurationDeploymentListener;
import org.jbpm.casemgmt.impl.generator.TableCaseIdGenerator;
import org.jbpm.executor.ExecutorServiceFactory;
import org.jbpm.executor.impl.event.ExecutorEventSupportImpl;
import org.jbpm.kie.services.impl.FormManagerService;
import org.jbpm.kie.services.impl.FormManagerServiceImpl;
import org.jbpm.kie.services.impl.KModuleDeploymentService;
import org.jbpm.kie.services.impl.ProcessServiceImpl;
import org.jbpm.kie.services.impl.RuntimeDataServiceImpl;
import org.jbpm.kie.services.impl.UserTaskServiceImpl;
import org.jbpm.kie.services.impl.admin.ProcessInstanceAdminServiceImpl;
import org.jbpm.kie.services.impl.admin.ProcessInstanceMigrationServiceImpl;
import org.jbpm.kie.services.impl.admin.UserTaskAdminServiceImpl;
import org.jbpm.kie.services.impl.bpmn2.BPMN2DataServiceImpl;
import org.jbpm.kie.services.impl.query.QueryServiceImpl;
import org.jbpm.runtime.manager.impl.jpa.EntityManagerFactoryManager;
import org.jbpm.services.api.DefinitionService;
import org.jbpm.services.api.DeploymentService;
import org.jbpm.services.api.ProcessService;
import org.jbpm.services.api.RuntimeDataService;
import org.jbpm.services.api.UserTaskService;
import org.jbpm.services.api.admin.ProcessInstanceAdminService;
import org.jbpm.services.api.admin.ProcessInstanceMigrationService;
import org.jbpm.services.api.admin.UserTaskAdminService;
import org.jbpm.services.api.query.QueryService;
import org.jbpm.services.task.HumanTaskServiceFactory;
import org.jbpm.services.task.audit.TaskAuditServiceFactory;
import org.jbpm.services.task.identity.DefaultUserInfo;
import org.jbpm.shared.services.impl.TransactionalCommandService;
import org.jbpm.springboot.security.SpringSecurityIdentityProvider;
import org.jbpm.springboot.security.SpringSecurityUserGroupCallback;
import org.kie.api.executor.ExecutorService;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.manager.RuntimeManagerFactory;
import org.kie.api.task.TaskService;
import org.kie.api.task.UserGroupCallback;
import org.kie.internal.identity.IdentityProvider;
import org.kie.internal.task.api.UserInfo;
import org.kie.spring.jbpm.services.SpringTransactionalCommandService;
import org.kie.spring.manager.SpringRuntimeManagerFactoryImpl;
import org.kie.spring.persistence.KieSpringTransactionManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;

@Configuration
@ConditionalOnClass({ KModuleDeploymentService.class })
@EnableConfigurationProperties(JBPMProperties.class)
public class JBPMAutoConfiguration {
    
    protected static final String PERSISTENCE_UNIT_NAME = "org.jbpm.domain";
    protected static final String PERSISTENCE_XML_LOCATION = "classpath:/META-INF/jbpm-persistence.xml";

    private JBPMProperties properties;
    private DataSource dataSource;
    private PlatformTransactionManager transactionManager;
 
    public JBPMAutoConfiguration(DataSource dataSource, 
                                 PlatformTransactionManager transactionManager,
                                 JBPMProperties properties) {
        this.dataSource = dataSource;
        this.transactionManager = transactionManager;
        this.properties = properties;
    }
    
    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(JpaProperties jpaProperties){
        LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
        factoryBean.setPersistenceUnitName(PERSISTENCE_UNIT_NAME);
        factoryBean.setPersistenceXmlLocation(PERSISTENCE_XML_LOCATION);
        factoryBean.setJtaDataSource(dataSource);
        factoryBean.setJpaPropertyMap(jpaProperties.getHibernateProperties(dataSource));
                
   
        return factoryBean;
    }
    
    @Bean
    @ConditionalOnMissingBean(name = "identityProvider")
    public IdentityProvider identityProvider() {
        
        return new SpringSecurityIdentityProvider();
    }
    
    @Bean
    @ConditionalOnMissingBean(name = "kieTransactionManager")
    public TransactionManager kieTransactionManager() {
        
        return new KieSpringTransactionManager((AbstractPlatformTransactionManager) transactionManager);
    }
    
    @Bean
    @ConditionalOnMissingBean(name = "userGroupCallback")
    public UserGroupCallback userGroupCallback(IdentityProvider identityProvider) throws IOException {
        return new SpringSecurityUserGroupCallback(identityProvider);
    }
    
    @Bean
    @ConditionalOnMissingBean(name = "userInfo")
    public UserInfo userInfo() throws IOException {
        Resource resource = new ClassPathResource("/userinfo.properties");
        Properties userInfo = PropertiesLoaderUtils.loadProperties(resource);
        return new DefaultUserInfo(userInfo);
    }
    
    @Bean
    @ConditionalOnMissingBean(name = "definitionService")
    public DefinitionService definitionService() {
        
        return new BPMN2DataServiceImpl();
    }
    
    @Bean
    @ConditionalOnMissingBean(name = "formService")
    public FormManagerService formService() {
        
        return new FormManagerServiceImpl();
    }
    
    @Bean
    @ConditionalOnMissingBean(name = "runtimeManagerFactory")
    public RuntimeManagerFactory runtimeManagerFactory(UserGroupCallback userGroupCallback, UserInfo userInfo) {
        
        SpringRuntimeManagerFactoryImpl runtimeManager = new SpringRuntimeManagerFactoryImpl();
        runtimeManager.setTransactionManager((AbstractPlatformTransactionManager) transactionManager);
        runtimeManager.setUserGroupCallback(userGroupCallback);
        runtimeManager.setUserInfo(userInfo);
        return runtimeManager;
    }
    
    @Bean
    @ConditionalOnMissingBean(name = "transactionalCommandService")
    public TransactionalCommandService transactionalCommandService(EntityManagerFactory entityManagerFactory, TransactionManager kieTransactionManager) {
        
        return new SpringTransactionalCommandService(entityManagerFactory, kieTransactionManager, (AbstractPlatformTransactionManager) transactionManager);
    }
    
    @Bean
    @ConditionalOnMissingBean(name = "deploymentService")
    public DeploymentService deploymentService(DefinitionService definitionService, RuntimeManagerFactory runtimeManagerFactory, FormManagerService formService, EntityManagerFactory entityManagerFactory, IdentityProvider identityProvider) {
        EntityManagerFactoryManager.get().addEntityManagerFactory(PERSISTENCE_UNIT_NAME, entityManagerFactory);
        
        KModuleDeploymentService deploymentService = new KModuleDeploymentService();
        ((KModuleDeploymentService) deploymentService).setBpmn2Service(definitionService);
        ((KModuleDeploymentService) deploymentService).setEmf(entityManagerFactory);
        ((KModuleDeploymentService) deploymentService).setIdentityProvider(identityProvider);
        ((KModuleDeploymentService) deploymentService).setManagerFactory(runtimeManagerFactory);
        ((KModuleDeploymentService) deploymentService).setFormManagerService(formService);
        
        ((KModuleDeploymentService) deploymentService).addListener(((BPMN2DataServiceImpl) definitionService));
        
        return deploymentService;
    }
    
    @Bean
    @ConditionalOnMissingBean(name = "runtimeDataService")
    public RuntimeDataService runtimeDataService(EntityManagerFactory entityManagerFactory, UserGroupCallback userGroupCallback, UserInfo userInfo, TransactionalCommandService transactionalCommandService, IdentityProvider identityProvider, DeploymentService deploymentService) {
        
        Environment environment = EnvironmentFactory.newEnvironment();
        environment.set(EnvironmentName.TRANSACTION_MANAGER, transactionManager);
        environment.set(EnvironmentName.ENTITY_MANAGER_FACTORY, entityManagerFactory);
        
        TaskService taskService = HumanTaskServiceFactory.newTaskServiceConfigurator()
                .entityManagerFactory(entityManagerFactory)
                .userGroupCallback(userGroupCallback)
                .userInfo(userInfo)
                .environment(environment)
                .getTaskService();

        // build runtime data service
        RuntimeDataServiceImpl runtimeDataService = new RuntimeDataServiceImpl();
        runtimeDataService.setCommandService(transactionalCommandService);
        runtimeDataService.setIdentityProvider(identityProvider);
        runtimeDataService.setUserGroupCallback(userGroupCallback);
        runtimeDataService.setTaskService(taskService);
        runtimeDataService.setTaskAuditService(TaskAuditServiceFactory.newTaskAuditServiceConfigurator()
                .setTaskService(taskService)
                .getTaskAuditService());
        
        ((KModuleDeploymentService) deploymentService).setRuntimeDataService(runtimeDataService);
        ((KModuleDeploymentService) deploymentService).addListener(runtimeDataService);
        
        return runtimeDataService;
    }
    
    @Bean
    @ConditionalOnMissingBean(name = "processService")
    public ProcessService processService(RuntimeDataService runtimeDataService, DeploymentService deploymentService) {
        
        ProcessServiceImpl processService = new ProcessServiceImpl();
        processService.setDataService(runtimeDataService);
        processService.setDeploymentService(deploymentService);
        
        return processService;
    }
    
    @Bean
    @ConditionalOnMissingBean(name = "userTaskService")
    public UserTaskService userTaskService(RuntimeDataService runtimeDataService, DeploymentService deploymentService) {
        
        UserTaskServiceImpl userTaskService = new UserTaskServiceImpl();
        ((UserTaskServiceImpl) userTaskService).setDataService(runtimeDataService);
        ((UserTaskServiceImpl) userTaskService).setDeploymentService(deploymentService);
        
        return userTaskService;
    }
    
    @Bean
    @ConditionalOnMissingBean(name = "queryService")
    public QueryService queryService(TransactionalCommandService transactionalCommandService, IdentityProvider identityProvider) {
        
        QueryServiceImpl queryService = new QueryServiceImpl();
        queryService.setIdentityProvider(identityProvider);
        queryService.setCommandService(transactionalCommandService);        
        // override data source locator to not use JNDI
        SQLDataSetProvider sqlDataSetProvider = SQLDataSetProvider.get();
        sqlDataSetProvider.setDataSourceLocator(new SQLDataSourceLocator() {
                        
            @Override
            public DataSource lookup(SQLDataSetDef def) throws Exception {
                return dataSource;
            }
        });
        
        queryService.init();
        
        return queryService;
    }
    
    @Bean
    @ConditionalOnMissingBean(name = "processInstanceMigrationService")
    public ProcessInstanceMigrationService processInstanceMigrationService() {
        
        return new ProcessInstanceMigrationServiceImpl();
    }
    
    @Bean
    @ConditionalOnMissingBean(name = "processInstanceAdminService")
    public ProcessInstanceAdminService processInstanceAdminService(RuntimeDataService runtimeDataService, ProcessService processService, TransactionalCommandService transactionalCommandService, IdentityProvider identityProvider) {
        ProcessInstanceAdminServiceImpl processInstanceAdminService = new ProcessInstanceAdminServiceImpl();
        processInstanceAdminService.setProcessService(processService);
        processInstanceAdminService.setRuntimeDataService(runtimeDataService);
        processInstanceAdminService.setCommandService(transactionalCommandService);
        processInstanceAdminService.setIdentityProvider(identityProvider);
        
        return processInstanceAdminService;
    }
    
    @Bean
    @ConditionalOnMissingBean(name = "userTaskAdminService")
    public UserTaskAdminService userTaskAdminService(RuntimeDataService runtimeDataService, UserTaskService userTaskService, TransactionalCommandService transactionalCommandService, IdentityProvider identityProvider) {
        UserTaskAdminServiceImpl userTaskAdminService = new UserTaskAdminServiceImpl();
        userTaskAdminService.setRuntimeDataService(runtimeDataService);
        userTaskAdminService.setUserTaskService(userTaskService);
        userTaskAdminService.setIdentityProvider(identityProvider);
        userTaskAdminService.setCommandService(transactionalCommandService);        
        
        return userTaskAdminService;
    }
    
    @Bean
    @ConditionalOnMissingBean(name = "executorService")
    @ConditionalOnProperty(name = "jbpm.executor.enabled")
    public ExecutorService executorService(EntityManagerFactory entityManagerFactory, TransactionalCommandService transactionalCommandService, DeploymentService deploymentService) {
        
        ExecutorEventSupportImpl eventSupport = new ExecutorEventSupportImpl();
        // configure services
        ExecutorService service = ExecutorServiceFactory.newExecutorService(entityManagerFactory, transactionalCommandService, eventSupport);
        
        service.setInterval(properties.getExecutor().getInterval());
        service.setRetries(properties.getExecutor().getRetries());
        service.setThreadPoolSize(properties.getExecutor().getThreadPoolSize());
        service.setTimeunit(TimeUnit.valueOf(properties.getExecutor().getTimeUnit()));

        ((KModuleDeploymentService) deploymentService).setExecutorService(service);
        
        return service;
    }
    
    @Bean
    @ConditionalOnMissingBean(name = "caseIdGenerator")
    public CaseIdGenerator caseIdGenerator(TransactionalCommandService transactionalCommandService) {
        
        return new TableCaseIdGenerator(transactionalCommandService);
    }
    
    @Bean
    @ConditionalOnClass({ CaseRuntimeDataServiceImpl.class })
    @ConditionalOnMissingBean(name = "caseRuntimeService")
    public CaseRuntimeDataService caseRuntimeService(CaseIdGenerator caseIdGenerator, RuntimeDataService runtimeDataService, DeploymentService deploymentService, TransactionalCommandService transactionalCommandService, IdentityProvider identityProvider) {
        
        CaseRuntimeDataServiceImpl caseRuntimeDataService = new CaseRuntimeDataServiceImpl();
        caseRuntimeDataService.setCaseIdGenerator(caseIdGenerator);
        caseRuntimeDataService.setRuntimeDataService(runtimeDataService);
        caseRuntimeDataService.setCommandService(transactionalCommandService);
        caseRuntimeDataService.setIdentityProvider(identityProvider);
        
        // configure case mgmt services as listeners
        ((KModuleDeploymentService)deploymentService).addListener(caseRuntimeDataService);
        
        return caseRuntimeDataService;
    }
    
    @Bean
    @ConditionalOnClass({ CaseServiceImpl.class })
    @ConditionalOnMissingBean(name = "caseService")
    public CaseService caseService(CaseIdGenerator caseIdGenerator, CaseRuntimeDataService caseRuntimeDataService, RuntimeDataService runtimeDataService, ProcessService processService, DeploymentService deploymentService, TransactionalCommandService transactionalCommandService, IdentityProvider identityProvider) {
        CaseServiceImpl caseService = new CaseServiceImpl();
        caseService.setCaseIdGenerator(caseIdGenerator);
        caseService.setCaseRuntimeDataService(caseRuntimeDataService);
        caseService.setProcessService(processService);
        caseService.setDeploymentService(deploymentService);
        caseService.setRuntimeDataService(runtimeDataService);
        caseService.setCommandService(transactionalCommandService);
        caseService.setAuthorizationManager(new AuthorizationManagerImpl(identityProvider, transactionalCommandService));
        caseService.setIdentityProvider(identityProvider);
        
        // build case configuration on deployment listener
        CaseConfigurationDeploymentListener configurationListener = new CaseConfigurationDeploymentListener(identityProvider, transactionalCommandService);

        // configure case mgmt services as listeners        
        ((KModuleDeploymentService)deploymentService).addListener(configurationListener);
        
        return caseService;
    }
}
