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

package org.kie.spring.manager;

import javax.persistence.EntityManager;

import org.jbpm.process.core.timer.GlobalSchedulerService;
import org.jbpm.runtime.manager.impl.RuntimeManagerFactoryImpl;
import org.jbpm.runtime.manager.impl.SimpleRuntimeEnvironment;
import org.kie.api.runtime.EnvironmentName;
import org.kie.api.runtime.manager.RuntimeEnvironment;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.task.UserGroupCallback;
import org.kie.internal.task.api.UserInfo;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;

public class SpringRuntimeManagerFactoryImpl extends RuntimeManagerFactoryImpl {
	
	private AbstractPlatformTransactionManager transactionManager;
	
	private UserGroupCallback userGroupCallback;

    private EntityManager entityManager;

    private UserInfo userInfo;

    private boolean pessimisticLocking = false;

    private GlobalSchedulerService schedulerService;


	@Override
	public RuntimeManager newSingletonRuntimeManager(RuntimeEnvironment environment, String identifier) {
		adjustEnvironment(environment);
		return super.newSingletonRuntimeManager(environment, identifier);
	}

	@Override
	public RuntimeManager newPerRequestRuntimeManager(RuntimeEnvironment environment, String identifier) {
        disallowSharedTaskService(environment);
        adjustEnvironment(environment);
		return super.newPerRequestRuntimeManager(environment, identifier);
	}

	@Override
	public RuntimeManager newPerProcessInstanceRuntimeManager(RuntimeEnvironment environment, String identifier) {
        disallowSharedTaskService(environment);
        adjustEnvironment(environment);
		return super.newPerProcessInstanceRuntimeManager(environment, identifier);
	}

	public UserGroupCallback getUserGroupCallback() {
		return userGroupCallback;
	}

	public void setUserGroupCallback(UserGroupCallback userGroupCallback) {
		this.userGroupCallback = userGroupCallback;
	}
	

	public AbstractPlatformTransactionManager getTransactionManager() {
		return transactionManager;
	}

	public void setTransactionManager(AbstractPlatformTransactionManager transactionManager) {
		this.transactionManager = transactionManager;
	}

    public EntityManager getEntityManager() {
        return entityManager;
    }

    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public UserInfo getUserInfo() {
        return userInfo;
    }

    public void setUserInfo(UserInfo userInfo) {
        this.userInfo = userInfo;
    }

    public boolean isPessimisticLocking() {
        return pessimisticLocking;
    }

    public void setPessimisticLocking(boolean pessimisticLocking) {
        this.pessimisticLocking = pessimisticLocking;
    }

    public GlobalSchedulerService getSchedulerService() {
        return schedulerService;
    }

    public void setSchedulerService(GlobalSchedulerService schedulerService) {
        this.schedulerService = schedulerService;
    }

    protected void adjustEnvironment(RuntimeEnvironment environment) {
        if (userGroupCallback != null) {
            ((SimpleRuntimeEnvironment)environment).setUserGroupCallback(userGroupCallback);
        }
        if (schedulerService != null) {
            ((SimpleRuntimeEnvironment)environment).setSchedulerService(schedulerService);
        }
        ((SimpleRuntimeEnvironment)environment).getEnvironmentTemplate().set(EnvironmentName.TRANSACTION_MANAGER, transactionManager);
		((SimpleRuntimeEnvironment)environment).getEnvironmentTemplate().set(EnvironmentName.TASK_USER_GROUP_CALLBACK, userGroupCallback);
        ((SimpleRuntimeEnvironment)environment).getEnvironmentTemplate().set(EnvironmentName.TASK_USER_INFO, userInfo);

        if (entityManager != null) {
            ((SimpleRuntimeEnvironment)environment).getEnvironmentTemplate().set(EnvironmentName.APP_SCOPED_ENTITY_MANAGER, entityManager);
            ((SimpleRuntimeEnvironment)environment).getEnvironmentTemplate().set(EnvironmentName.CMD_SCOPED_ENTITY_MANAGER, entityManager);
            ((SimpleRuntimeEnvironment)environment).getEnvironmentTemplate().set("IS_JTA_TRANSACTION", false);
            ((SimpleRuntimeEnvironment)environment).getEnvironmentTemplate().set("IS_SHARED_ENTITY_MANAGER", true);
        }
        if (pessimisticLocking) {
            ((SimpleRuntimeEnvironment)environment).getEnvironmentTemplate().set(EnvironmentName.USE_PESSIMISTIC_LOCKING, true);
        }
	}

    protected void disallowSharedTaskService(RuntimeEnvironment environment) {

        if (((SimpleRuntimeEnvironment)environment).getEnvironmentTemplate().get("org.kie.api.task.TaskService") != null) {
            throw new IllegalStateException("Per process instance and per request runtime manager do not support shared task service");
        }
    }

}
