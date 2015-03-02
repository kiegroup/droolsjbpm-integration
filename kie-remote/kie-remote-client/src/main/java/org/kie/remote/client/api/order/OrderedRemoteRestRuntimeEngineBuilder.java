package org.kie.remote.client.api.order;

import java.net.MalformedURLException;
import java.net.URL;

import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.internal.runtime.conf.RuntimeStrategy;

public interface OrderedRemoteRestRuntimeEngineBuilder {

    /**
     * Adds the user name used. If no other user name is specified, the user id
     * specified is used for all purposes.
     * 
     * @param userName The user name
     * @return The builder instance
     */
    RemoteRestRuntimeEngineBuilder1 addUserName(String userName);

    public static interface RemoteRestRuntimeEngineBuilder1 {

        /**
         * Adds the password used. If no other password is specified, the password 
         * specified is used for all purposes.
         * 
         * @param userName The password
         * @return The builder instance
         */
        RemoteRestRuntimeEngineBuilder2 addPassword(String password);
    } 

    public static interface RemoteRestRuntimeEngineBuilder2 {

        /**
         * The URL used here should be in the following form:
         * <code>http://HOST:PORT/INSTANCE/</code>
         * The different parts of the URL are:<ul>
         *   <li><code>HOST</code>: the hostname or ip address</li>
         *   <li><code>PORT</code>: the port number that the application is available on (often 8080)</li>
         *   <li><code>INSTANCE</code>: the name of the application, often one of the following:<ul>
         *     <li>business-central</li>
         *     <li>kie-wb</li>
         *     <li>jbpm-console</li></ul></li>
         * </ul>
         * 
         * @param instanceUrl The URL of the application
         * @return The builder instance
         */
        RemoteRestRuntimeEngineBuilderOpt addUrl(URL instanceUrl);

        /**
         * The URL used here should be in the following form:
         * <code>http://HOST:PORT/INSTANCE/</code>
         * The different parts of the URL are:<ul>
         *   <li><code>HOST</code>: the hostname or ip address</li>
         *   <li><code>PORT</code>: the port number that the application is available on (often 8080)</li>
         *   <li><code>INSTANCE</code>: the name of the application, often one of the following:<ul>
         *     <li>business-central</li>
         *     <li>kie-wb</li>
         *     <li>jbpm-console</li></ul></li>
         * </ul>
         * 
         * @param instanceUrl The URL of the application
         * @return The builder instance
         * @throws MalformedURLException if the string is not a proper URL
         */
        RemoteRestRuntimeEngineBuilderOpt addUrl(String instanceUrlString) throws MalformedURLException;
    }

    public static interface RemoteRestRuntimeEngineBuilderOpt {

        /**
         * The timeout (or otherwise the quality-of-service threshold when sending JMS msgs).
         * For HTTP related services (REST or webservices), this timeout is used for both 
         * the time it takes to connect as well as the time it takes to receive the request.
         * @param timeoutInSeconds The timeout in seconds
         * @return The builder instance
         */
        RemoteRestRuntimeEngineBuilderOpt addTimeout(int timeoutInSeconds);

        /**
         * Adds the deployment id to the configuration.
         * @param deploymentId The deployment id
         * @return The builder instance
         */
        RemoteRestRuntimeEngineBuilderOpt addDeploymentId(String deploymentId);

        /**
         * Adds the process instance id, which may be necessary when interacting
         * with deployments that employ the {@link RuntimeStrategy#PER_PROCESS_INSTANCE}.
         * @param processInstanceId The process instance id
         * @return The builder instance
         */
        RemoteRestRuntimeEngineBuilderOpt addProcessInstanceId(long processInstanceId);

        /**
         * When sending non-primitive class instances, it's necessary to add the class instances
         * beforehand to the configuration so that the class instances can be serialized correctly
         * in requests
         * @param classes One or more class instances
         * @return The builder instance
         */
        RemoteRestRuntimeEngineBuilderOpt addExtraJaxbClasses(Class... classes); 

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

    public static interface OrderedRemoteRestRuntimeEngineBuilderAll 
        extends OrderedRemoteRestRuntimeEngineBuilder,
        RemoteRestRuntimeEngineBuilder1, RemoteRestRuntimeEngineBuilder2,
        RemoteRestRuntimeEngineBuilderOpt {
        
    }
}
