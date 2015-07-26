/*
 * Copyright 2015 JBoss Inc
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

package org.kie.remote.client.api;

import java.util.Map;

import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.internal.runtime.conf.RuntimeStrategy;
import org.kie.remote.client.api.exception.InsufficientInfoToBuildException;


/**
 * This interface defines the fluent builder methods that can be used when either configuring a remote REST or remote JMS
 * runtime engine instance. 
 * 
 * @param <B> The builder instance type
 * @param <F> The factory instance type
 */
public interface RemoteRuntimeEngineBuilder<B, F> extends RemoteClientBuilder<B> {

    /**
     * Adds the process instance id, which may be necessary when interacting
     * with deployments that employ the {@link RuntimeStrategy#PER_PROCESS_INSTANCE}.
     * @param processInstanceId The process instance id
     * @return The builder instance
     */
    B addProcessInstanceId(long processInstanceId);
    
    /**
     * Adds correlation key properties, which are necessary when interacting
     * with a correlation-key identitied {@link KieSession}.
     * @param correlationKeyNameValueProperties a {@link Map} of the correlation key properties,
     * where each entry key is a property name, and the entry value is the property value
     * 
     * @return The builder instance
     */
    B addCorrelationProperties(String... correlationProperty);
   
    /**
     * Adds correlation key properties, which are necessary when interacting
     * with a correlation-key identitied {@link KieSession}.
     * @param correlationKeyNameValueProperties a {@link Map} of the correlation key properties,
     * where each entry key is a property name, and the entry value is the property value
     * 
     * @return The builder instance
     */
    B clearCorrelationProperties();
    
    /**
     * If the {@link RemoteRuntimeEngineBuilder} is being reused (in order to build
     * multiple {@link RemoteRuntimeEngineFactory}'s, then this method can be called between 
     * {@ RemoteRuntimeEngineFactoryBuilder#build()} methods to reset the list of user-defined
     * classes being used by the builder. 
     * @return The builder instance
     */
    B clearJaxbClasses();
    
    /**
     * Creates a {@link RemoteRuntimeEngineFactory} instance, using the 
     * configuration given.
     * @return The {@link RemoteRuntimeEngineFactory} instance
     * @throws InsufficientInfoToBuildException when insufficient information 
     * is provided to build the {@link RemoteRuntimeEngineFactory}
     * @see {@link RemoteRuntimeEngineBuilder#build()}.
     */
    F buildFactory() throws InsufficientInfoToBuildException;
   
    /**
     * Creates a {@link RuntimeEngine} instance, using the 
     * configuration built up to this point. 
     * </p>
     * 
     * @return The {@link RuntimeEngine} instance
     * @throws @{link InsufficientInfoToBuildException} when insufficient information 
     * is provided to build the {@link RuntimeEngine}
     */
    RuntimeEngine build();

}