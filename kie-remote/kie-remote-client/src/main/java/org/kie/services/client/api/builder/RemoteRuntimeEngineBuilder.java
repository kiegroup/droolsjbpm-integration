package org.kie.services.client.api.builder;

import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.services.client.api.RemoteRuntimeEngineFactory;
import org.kie.services.client.api.builder.exception.InsufficientInfoToBuildException;
import org.kie.services.client.api.command.RemoteRuntimeEngine;


public interface RemoteRuntimeEngineBuilder<B, R> {

    /**
     * Adds the deployment id to the configuration.
     * @param deploymentId The deployment id
     * @return The builder instance
     */
    B addDeploymentId(String deploymentId);
    
    /**
     * Adds the process instance id, which may be necessary when interacting
     * with deployments that employ the {@link RuntimeStrategy#PER_PROCESS_INSTANCE}.
     * @param processInstanceId The process instance id
     * @return The builder instance
     */
    B addProcessInstanceId(long processInstanceId);
    
    /**
     * Adds the user name used. If no other user name is specified, the user id
     * specified is used for all purposes.
     * 
     * @param userName The user name
     * @return The builder instance
     */
    B addUserName(String userName);
    
    /**
     * Adds the password used. If no other password is specified, the password 
     * specified is used for all purposes.
     * 
     * @param userName The password
     * @return The builder instance
     */
    B addPassword(String password);
    
    /**
     * The timeout (or otherwise the quality-of-service threshold when sending JMS msgs).
     * @param timeoutInSeconds The timeout in seconds
     * @return The builder instance
     */
    B addTimeout(int timeoutInSeconds);
    
    /**
     * When sending non-primitive class instances, it's necessary to add the class instances
     * beforehand to the configuration so that the class instances can be serialized correctly
     * in requests
     * @param classes One or more class instances
     * @return The builder instance
     */
    B addExtraJaxbClasses(Class... classes);
   
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
    R buildFactory() throws InsufficientInfoToBuildException;
   
    /**
     * Creates a {@link RuntimeEngine} instance, using the 
     * configuration built up to this point. 
     * </p>
     * 
     * @return The {@link RuntimeEngine} instance
     * @throws @{link InsufficientInfoToBuildException} when insufficient information 
     * is provided to build the {@link RuntimeEngine}
     */
    RemoteRuntimeEngine build();

}