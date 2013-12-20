/*
 * Copyright 2013 JBoss Inc
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

package org.kie.spring.factorybeans;

import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.jbpm.process.core.timer.GlobalSchedulerService;
import org.kie.api.KieBase;
import org.kie.api.builder.ReleaseId;
import org.kie.api.io.Resource;
import org.kie.api.io.ResourceType;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.manager.RegisterableItemsFactory;
import org.kie.api.runtime.manager.RuntimeEnvironment;
import org.kie.api.runtime.manager.RuntimeEnvironmentBuilder;
import org.kie.api.runtime.manager.RuntimeEnvironmentBuilderFactory;
import org.kie.api.task.TaskService;
import org.kie.api.task.UserGroupCallback;
import org.kie.internal.task.api.UserInfo;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Factory responsible for producing instances of RuntimeEnvironment that are consumed by <code>RuntimeManager</code>
 * upon creation. It allows to create following types of <code>RuntimeEnvironment</code> (that mainly means what is
 * configured by default):
 * <ul>
 *     <li>DEFAULT - default (most common) configuration for RuntimeManager</li>
 *     <li>EMPTY - completely empty environment to be manually populated</li>
 *     <li>DEFAULT_IN_MEMORY - same as DEFAULT but without persistence of the runtime engine</li>
 *     <li>DEFAULT_KJAR - same as DEFAULT but knowledge asset are taken from KJAR identified by releaseid or GAV</li>
 *     <li>DEFAULT_KJAR_CL - build directly from classpath that consists kmodule.xml descriptor</li>
 * </ul>
 *
 * Mandatory properties depends on the type selected but knowledge information must be given for all types. That means
 * that one of the following is provided:
 * <ul>
 *     <li>knowledgeBase</li>
 *     <li>assets</li>
 *     <li>releaseId</li>
 *     <li>groupId, artifactId, version</li>
 * </ul>
 * Next for DEFAULT, DEFAULT_KJAR, DEFAULT_KJAR_CL persistence needs to be configured:
 * <ul>
 *     <li>entity manager factory</li>
 *     <li>transaction manager</li>
 * </ul>
 * Transaction Manager must be Spring transaction manager as based on its presence entire
 * persistence and transaction support is configured. <br/>
 * Optionally <code>entityManager</code> can be provided to be used instead of always
 * creating new one from EntityManagerFactory - e.g. when using shared entity manager from Spring.
 * <br/>
 * All other properties are optional and are meant to override the default given by type of the environment selected.
 * @see RuntimeManagerFactoryBean
 */
public class RuntimeEnvironmentFactoryBean implements FactoryBean, InitializingBean {

    public static final String TYPE_DEFAULT = "DEFAULT";
    public static final String TYPE_EMPTY = "EMPTY";
    public static final String TYPE_DEFAULT_IN_MEMORY = "DEFAULT_IN_MEMORY";
    public static final String TYPE_DEFAULT_KJAR = "DEFAULT_KJAR";
    public static final String TYPE_DEFAULT_KJAR_CL = "DEFAULT_KJAR_CL";

    private String type = TYPE_DEFAULT;
    // persistence
    private EntityManagerFactory entityManagerFactory;
    private EntityManager entityManager;
    // transaction
    private PlatformTransactionManager transactionManager;

    // knowledge base and assets
    private KieBase knowledgeBase;
    private Map<Resource, ResourceType> assets;

    // kjar
    private ReleaseId releaseId;
    private String groupId;
    private String artifactId;
    private String version;
    private String kbaseName;
    private String ksessionName;

    // environment entries
    private Map<String, Object> environmentEntries;

    // configuration
    private Map<String, String> configuration;

    // human task
    private UserGroupCallback userGroupCallback;
    private UserInfo userInfo;
    private TaskService taskService;

    private RegisterableItemsFactory registerableItemsFactory;

    // misc
    private ClassLoader classLoader;
    private GlobalSchedulerService schedulerService;

    @Override
    public Object getObject() throws Exception {
        RuntimeEnvironmentBuilder builder = null;

        RuntimeEnvironmentBuilderFactory factory = RuntimeEnvironmentBuilder.Factory.get();

        if (type.equalsIgnoreCase(TYPE_EMPTY)) {
            builder = factory.newEmptyBuilder();
        } else if (type.equalsIgnoreCase(TYPE_DEFAULT_IN_MEMORY)) {
            builder = factory.newDefaultInMemoryBuilder();
        } else if (type.equalsIgnoreCase(TYPE_DEFAULT)) {
            builder = factory.newDefaultBuilder();
        } else if (type.equalsIgnoreCase(TYPE_DEFAULT_KJAR)) {
            if (releaseId != null) {
                builder = factory.newDefaultBuilder(releaseId, kbaseName, ksessionName);
            } else {
                builder = factory.newDefaultBuilder(groupId, artifactId, version, kbaseName, ksessionName);
            }
        } else if (type.equalsIgnoreCase(TYPE_DEFAULT_KJAR_CL)) {
            builder = factory.newClasspathKmoduleDefaultBuilder(kbaseName, ksessionName);
        } else {
            throw new IllegalArgumentException("Unknown type of environment");
        }

        // apply all known properties
        builder
        .entityManagerFactory(entityManagerFactory)
        .knowledgeBase(knowledgeBase)
        .classLoader(classLoader)
        .schedulerService(schedulerService)
        .userGroupCallback(userGroupCallback)
        .registerableItemsFactory(registerableItemsFactory);

        // common environment entries
        builder.addEnvironmentEntry("org.kie.api.task.TaskService", taskService);
        builder.addEnvironmentEntry(EnvironmentName.TRANSACTION_MANAGER, transactionManager);
        builder.addEnvironmentEntry(EnvironmentName.TASK_USER_GROUP_CALLBASK, userGroupCallback);
        builder.addEnvironmentEntry(EnvironmentName.TASK_USER_INFO, userInfo);
        if (entityManager != null) {
            builder.addEnvironmentEntry(EnvironmentName.APP_SCOPED_ENTITY_MANAGER, entityManager)
            .addEnvironmentEntry(EnvironmentName.CMD_SCOPED_ENTITY_MANAGER, entityManager)
            .addEnvironmentEntry("IS_JTA_TRANSACTION", false)
            .addEnvironmentEntry("IS_SHARED_ENTITY_MANAGER", true);
        }

        // apply configuration if any
        if (configuration != null) {
            for (Map.Entry<String, String> entry : configuration.entrySet()) {
                builder.addConfiguration(entry.getKey(), entry.getValue());
            }
        }

        // apply environment entries if any
        if (environmentEntries != null) {
            for (Map.Entry<String, Object> entry : environmentEntries.entrySet()) {
                builder.addEnvironmentEntry(entry.getKey(), entry.getValue());
            }
        }

        // apply assets if kbase was not given
        if (knowledgeBase == null && assets != null) {
            for (Map.Entry<Resource, ResourceType> entry : assets.entrySet()) {
                builder.addAsset(entry.getKey(), entry.getValue());
            }
        }

        return builder.get();
    }

    @Override
    public Class<?> getObjectType() {
        return RuntimeEnvironment.class;
    }

    @Override
    public boolean isSingleton() {
        return false;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        // check it always
        checkKnowledge();
        // check persistence and transaction for all except empty and in memory
        if (!type.equalsIgnoreCase(TYPE_DEFAULT_IN_MEMORY) && !type.equalsIgnoreCase(TYPE_EMPTY)) {
            checkPersistence();
        }
        // for kjar based ensure that either release id or GAV is given
        if (type.equalsIgnoreCase(TYPE_DEFAULT_KJAR) || type.equalsIgnoreCase(TYPE_DEFAULT_KJAR_CL)) {
            checkKjar();
        }
    }

    protected void checkPersistence() {
        if (entityManagerFactory == null && entityManager == null) {
            throw new IllegalArgumentException("Entity Manager or EntityManagerFactory must be provided");
        }
        if (transactionManager == null) {
            throw new IllegalArgumentException("TransactionManager must be provided");
        }
    }

    protected void checkKnowledge() {
        if (knowledgeBase == null && assets == null && releaseId == null && groupId == null ) {
            throw new IllegalArgumentException("Knowledge is not provided, set one of knowledgeBase, assets, releaseId or GAV");
        }
    }

    protected void checkKjar() {
        if (releaseId == null && groupId == null && artifactId == null && version == null) {
            throw new IllegalArgumentException("For Kjar environment either ReleaseId or GAV must be provided");
        }
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public EntityManagerFactory getEntityManagerFactory() {
        return entityManagerFactory;
    }

    public void setEntityManagerFactory(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = entityManagerFactory;
    }

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public PlatformTransactionManager getTransactionManager() {
        return transactionManager;
    }

    public void setTransactionManager(PlatformTransactionManager transactionManager) {
        this.transactionManager = transactionManager;
    }

    public KieBase getKnowledgeBase() {
        return knowledgeBase;
    }

    public void setKnowledgeBase(KieBase knowledgeBase) {
        this.knowledgeBase = knowledgeBase;
    }

    public Map<Resource, ResourceType> getAssets() {
        return assets;
    }

    public void setAssets(Map<Resource, ResourceType> assets) {
        this.assets = assets;
    }

    public ReleaseId getReleaseId() {
        return releaseId;
    }

    public void setReleaseId(ReleaseId releaseId) {
        this.releaseId = releaseId;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getKbaseName() {
        return kbaseName;
    }

    public void setKbaseName(String kbaseName) {
        this.kbaseName = kbaseName;
    }

    public String getKsessionName() {
        return ksessionName;
    }

    public void setKsessionName(String ksessionName) {
        this.ksessionName = ksessionName;
    }

    public Map<String, Object> getEnvironmentEntries() {
        return environmentEntries;
    }

    public void setEnvironmentEntries(Map<String, Object> environmentEntries) {
        this.environmentEntries = environmentEntries;
    }

    public Map<String, String> getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Map<String, String> configuration) {
        this.configuration = configuration;
    }

    public UserGroupCallback getUserGroupCallback() {
        return userGroupCallback;
    }

    public void setUserGroupCallback(UserGroupCallback userGroupCallback) {
        this.userGroupCallback = userGroupCallback;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public TaskService getTaskService() {
        return taskService;
    }

    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    public RegisterableItemsFactory getRegisterableItemsFactory() {
        return registerableItemsFactory;
    }

    public void setRegisterableItemsFactory(RegisterableItemsFactory registerableItemsFactory) {
        this.registerableItemsFactory = registerableItemsFactory;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public void setClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    public GlobalSchedulerService getSchedulerService() {
        return schedulerService;
    }

    public void setSchedulerService(GlobalSchedulerService schedulerService) {
        this.schedulerService = schedulerService;
    }
}
