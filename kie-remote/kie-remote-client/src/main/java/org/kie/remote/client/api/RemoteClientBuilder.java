package org.kie.remote.client.api;

import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.internal.runtime.conf.RuntimeStrategy;
import org.kie.remote.client.api.exception.InsufficientInfoToBuildException;


/**
 * This interface defines the fluent builder methods that can be used when either configuring a remote REST or remote JMS
 * runtime engine instance, or a remote web service client instance.
 * 
 * @param <B> The builder instance type
 */
public interface RemoteClientBuilder<B> {
    
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
     * For HTTP related services (REST or webservices), this timeout is used for both 
     * the time it takes to connect as well as the time it takes to receive the request.
     * @param timeoutInSeconds The timeout in seconds
     * @return The builder instance
     */
    B addTimeout(int timeoutInSeconds);

}