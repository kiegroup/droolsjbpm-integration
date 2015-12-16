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
package org.kie.aries.blueprint.factorybeans;

import org.kie.api.io.ResourceType;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeEnvironment;
import org.kie.api.runtime.manager.RuntimeEnvironmentBuilder;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.manager.RuntimeManagerFactory;
import org.kie.internal.io.ResourceFactory;
import org.kie.internal.runtime.manager.context.EmptyContext;

public class KieRuntimeManagerFactoryBean {

    public static Object createRuntime(String type, String asset, String assetType){

        RuntimeEnvironmentBuilder builder;
        RuntimeEnvironment environment;
        RuntimeManager manager;

        if ("empty".equalsIgnoreCase(type)) {
            builder = RuntimeEnvironmentBuilder.Factory.get().newEmptyBuilder();
        } else if ("default".equalsIgnoreCase(type)) {
            builder = RuntimeEnvironmentBuilder.Factory.get().newDefaultBuilder();
        } else if ("defaultInMemory".equalsIgnoreCase(type)) {
            builder = RuntimeEnvironmentBuilder.Factory.get().newDefaultInMemoryBuilder();
        } else {
            throw new IllegalArgumentException("Could not find a RuntimeManager for the type : " + type);
        }

        // Add asset(s)
        if (assetType.equals(ResourceType.BPMN2.getName())){
        builder.addAsset(ResourceFactory.newClassPathResource(asset), ResourceType.BPMN2);
        } else {
            throw new IllegalArgumentException("Asset is not of type BPMN2");
        }

        // Get RuntimeEnvironment
        environment = builder.get();

        // Create Singleton RuntimeManager
        // TODO Allow to create Singleton, PerProcess or PerRequest
        manager = RuntimeManagerFactory.Factory.get().newSingletonRuntimeManager(environment);

        return manager;
    }

    public static Object createSession(RuntimeManager manager) {
        RuntimeEngine engine = manager.getRuntimeEngine(EmptyContext.get());
        return engine.getKieSession();
    }
}
