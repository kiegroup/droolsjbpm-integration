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

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;

import org.drools.core.impl.EnvironmentFactory;
import org.jbpm.services.task.HumanTaskConfigurator;
import org.jbpm.services.task.HumanTaskServiceFactory;
import org.jbpm.services.task.impl.TaskDeadlinesServiceImpl;
import org.jbpm.services.task.lifecycle.listeners.TaskLifeCycleEventListener;
import org.kie.api.runtime.Environment;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.task.TaskService;
import org.kie.api.task.UserGroupCallback;
import org.kie.internal.task.api.UserInfo;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * Creates instance of <code>TaskService</code> based on set properties.
 * Following are mandatory properties that must be provided:
 * <ul>
 *     <li>entity manager factory</li>
 *     <li>transaction manager</li>
 * </ul>
 * Transaction Manager must be Spring transaction manager as based on its presence entire
 * persistence and transaction support is configured. <br/>
 * Optionally <code>EntityManager</code> can be provided to be used instead of always
 * creating new one from EntityManagerFactory - e.g. when using shared entity manager from Spring.
 * <br/>
 * In addition to above there are optional properties that can be set on task service instance:
 * <ul>
 *     <li>userGroupCallback - implementation of <code>UserGroupCallback</code> to be used, defaults to MVELUserGroupCallbackImpl</li>
 *     <li>userInfo - implementation of <code>UserInfo</code> to be used, defaults to DefaultUserInfo</li>
 *     <li>listener - list of <code>TaskLifeCycleEventListener</code> that will be notified upon various operations on tasks</li>
 * </ul>
 * This factory creates single instance of task service only as it's intended to be shared across all other beans in the system.
 */
public class TaskServiceFactoryBean implements FactoryBean, InitializingBean {

    private TaskService instance;

    // persistence
    private EntityManagerFactory entityManagerFactory;
    private EntityManager entityManager;
    // transaction
    private PlatformTransactionManager transactionManager;
    // callback and user info
    private UserGroupCallback userGroupCallback;
    private UserInfo userInfo;
    // listeners to be registered
    private List<TaskLifeCycleEventListener> listeners;
    @Override
    public Object getObject() throws Exception {
        if (instance == null) {
            HumanTaskConfigurator configurator = HumanTaskServiceFactory.newTaskServiceConfigurator();
            configurator
            .entityManagerFactory(entityManagerFactory)
            .userGroupCallback(userGroupCallback)
            .userInfo(userInfo);

            if (listeners != null) {
                for(TaskLifeCycleEventListener listener : listeners) {
                    configurator.listener(listener);
                }
            }
            Environment environment = EnvironmentFactory.newEnvironment();
            environment.set(EnvironmentName.TRANSACTION_MANAGER, transactionManager);
            environment.set(EnvironmentName.ENTITY_MANAGER_FACTORY, entityManagerFactory);
            if (entityManager != null) {
                environment.set(EnvironmentName.APP_SCOPED_ENTITY_MANAGER, entityManager);
                environment.set(EnvironmentName.CMD_SCOPED_ENTITY_MANAGER, entityManager);
                environment.set("IS_JTA_TRANSACTION", false);
                environment.set("IS_SHARED_ENTITY_MANAGER", true);
            }
            configurator.environment(environment);

            instance = configurator.getTaskService();
        }

        return instance;
    }

    @Override
    public Class<?> getObjectType() {
        return TaskService.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        checkPersistence();
    }

    public void close() {
        TaskDeadlinesServiceImpl.dispose();
    }

    protected void checkPersistence() {
        if (entityManagerFactory == null && entityManager == null) {
            throw new IllegalArgumentException("Entity Manager or EntityManagerFactory must be provided");
        }
        if (transactionManager == null) {
            throw new IllegalArgumentException("TransactionManager must be provided");
        }
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

    public List<TaskLifeCycleEventListener> getListeners() {
        return listeners;
    }

    public void setListeners(List<TaskLifeCycleEventListener> listeners) {
        this.listeners = listeners;
    }
}
