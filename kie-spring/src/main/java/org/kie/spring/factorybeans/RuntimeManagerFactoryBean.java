/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.jbpm.runtime.manager.impl.SimpleRuntimeEnvironment;
import org.kie.api.runtime.manager.RuntimeEnvironment;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.manager.RuntimeManagerFactory;
import org.kie.internal.runtime.conf.ObjectModelResolver;
import org.kie.internal.runtime.conf.ObjectModelResolverProvider;
import org.kie.internal.runtime.manager.RuntimeManagerRegistry;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * FactoryBean responsible to create instances of <code>RuntimeManager</code> of given type based on provided
 * runtimeEnvironment (which is a mandatory property).
 * Supported types:
 * <ul>
 *     <li>SINGLETON</li>
 *     <li>PER_REQUEST</li>
 *     <li>PER_PROCESS_INSTANCE</li>
 * </ul>
 * where default is SINGLETON when no type is specified. <br/>
 * Every runtime manager must be uniquely identified thus <code>identifier</code> is a mandatory property.
 * <br/>
 * All instances created by this factory are cached to be able to properly dispose them using destroy method (close()).
 */
public class RuntimeManagerFactoryBean implements FactoryBean, InitializingBean, ApplicationContextAware {

    private ApplicationContext applicationContext;

    private Set<RuntimeManager> runtimeManagerSet = new HashSet<RuntimeManager>();

    private RuntimeEnvironment runtimeEnvironment;
    private String identifier;
    private String type = "SINGLETON";

    private RuntimeManagerFactory factory = RuntimeManagerFactory.Factory.get();

    @Override
    public Object getObject() throws Exception {
        if(RuntimeManagerRegistry.get().isRegistered(getIdentifier())) {
            // get the runtime manager from the registry
            return RuntimeManagerRegistry.get().getManager(getIdentifier());
        }
        else {
            RuntimeManager manager = null;
            if ("PER_REQUEST".equalsIgnoreCase(type)) {
                disallowSharedTaskService(runtimeEnvironment);
                manager = factory.newPerRequestRuntimeManager(runtimeEnvironment, identifier);
            } else if ("PER_PROCESS_INSTANCE".equalsIgnoreCase(type)) {
                disallowSharedTaskService(runtimeEnvironment);
                manager = factory.newPerProcessInstanceRuntimeManager(runtimeEnvironment, identifier);
            } else {
                manager = factory.newSingletonRuntimeManager(runtimeEnvironment, identifier);
            }

            runtimeManagerSet.add(manager);
            return manager;
        }
    }

    @Override
    public Class<?> getObjectType() {
        return RuntimeManager.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        if (runtimeEnvironment == null && identifier == null) {
            throw new IllegalArgumentException("RuntimeEnvironment and identifier needs to be set");
        }

    }

    public RuntimeEnvironment getRuntimeEnvironment() {
        return runtimeEnvironment;
    }

    public void setRuntimeEnvironment(RuntimeEnvironment runtimeEnvironment) {
        this.runtimeEnvironment = runtimeEnvironment;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void close() {
        for (RuntimeManager manager : runtimeManagerSet) {
            manager.close();
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;

        // init any spring based ObjectModelResolvers
        List<ObjectModelResolver> resolvers = ObjectModelResolverProvider.getResolvers();
        if (resolvers != null) {
            for (ObjectModelResolver resolver : resolvers) {
                if (resolver instanceof ApplicationContextAware) {
                    ((ApplicationContextAware) resolver).setApplicationContext(applicationContext);
                }
            }
        }
    }

    protected void disallowSharedTaskService(RuntimeEnvironment environment) {

        if (((SimpleRuntimeEnvironment)environment).getEnvironmentTemplate().get("org.kie.api.task.TaskService") != null) {
            throw new IllegalStateException("Per process instance and per request runtime manager do not support shared task service");
        }
    }
}
