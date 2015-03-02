package org.kie.remote.client.api;

import java.util.Properties;

import javax.jms.ConnectionFactory;
import javax.jms.Queue;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.remote.client.api.exception.RemoteCommunicationException;
import org.kie.remote.client.api.order.OrderedRemoteJmsRuntimeEngineBuilder;
import org.kie.remote.client.api.order.OrderedRemoteRestRuntimeEngineBuilder;
import org.kie.remote.services.ws.command.generated.CommandWebService;

/**
 * This factory class is the starting point for building and configuring {@link RuntimeEngine} instances
 * that can interact with the remote API. 
 * </p> 
 * The main use for this class will be to create builder instances (see 
 * {@link #newJmsBuilder()} and {@link #newRestBuilder()}). These builder instances 
 * can then be used to directly configure and create a {@link RuntimeEngine} instance that will 
 * act as a client to the remote (REST or JMS) API.
 */
public abstract class RemoteRuntimeEngineFactory {

    /**
     * Create a new {@link RemoteJmsRuntimeEngineBuilder} instance 
     * to configure and buid a remote API client {@link RuntimeEngine} instance.
     * @return A {@link RemoteJmsRuntimeEngineBuilder} instance
     */
    public static RemoteJmsRuntimeEngineBuilder newJmsBuilder() { 
       return org.kie.services.client.api.RemoteRuntimeEngineFactory.newJmsBuilder();
    }
   
    /**
     * Create a new {@link RemoteRestRuntimeEngineBuilder} instance 
     * to configure and buid a remote API client {@link RuntimeEngine} instance.
     * @return A {@link RemoteRestRuntimeEngineBuilder} instance
     */
    public static RemoteRestRuntimeEngineBuilder newRestBuilder() { 
       return org.kie.services.client.api.RemoteRuntimeEngineFactory.newRestBuilder();
    }
    
    /**
     * Create a new {@link RemoteWebserviceClientBuilder} instance 
     * to configure and buid a remote client for the {@link CommandWebService}.
     * @return A {@link RemoteWebserviceClientBuilder} instance
     */
    public static RemoteWebserviceClientBuilder<RemoteWebserviceClientBuilder, CommandWebService> newCommandWebServiceClientBuilder() { 
       return org.kie.services.client.api.RemoteRuntimeEngineFactory.newCommandWebServiceClientBuilder();
    }
   
    /**
     * Create a new {@link RemoteJmsRuntimeEngineBuilder} instance 
     * to configure and buid a remote API client {@link RuntimeEngine} instance.
     * @return A {@link RemoteJmsRuntimeEngineBuilder} instance
     */
    public static OrderedRemoteJmsRuntimeEngineBuilder newOrderedJmsBuilder() { 
       return org.kie.services.client.api.RemoteRuntimeEngineFactory.newOrderedJmsBuilder();
    }
    
    /**
     * Create a new {@link RemoteRestRuntimeEngineBuilder} instance 
     * to configure and buid a remote API client {@link RuntimeEngine} instance.
     * @return A {@link RemoteRestRuntimeEngineBuilder} instance
     */
    public static OrderedRemoteRestRuntimeEngineBuilder newOrderedRestBuilder() { 
       return org.kie.services.client.api.RemoteRuntimeEngineFactory.newOrderedRestBuilder();
    }
    
    /**
     * Create a new {@link RemoteWebserviceClientBuilder} instance 
     * to configure and buid a remote client for the {@link CommandWebService}.
     * @return A {@link RemoteWebserviceClientBuilder} instance
     */
    public static RemoteWebserviceClientBuilder<RemoteWebserviceClientBuilder, CommandWebService> newOrderedCommandWebServiceClientBuilder() { 
       return org.kie.services.client.api.RemoteRuntimeEngineFactory.newCommandWebServiceClientBuilder();
    }
    
    /**
     * @return a new (remote client) {@link RuntimeEngine} instance.
     * @see {@link RemoteRuntimeEngineBuilder#buildRuntimeEngine()}
     */
    abstract public RuntimeEngine newRuntimeEngine();
    
    /**
     * Retrieves the (remote) {@link InitialContext} from the JBoss AS server instance in order 
     * to be able to retrieve the {@link ConnectionFactory} and {@link Queue} instances to communicate 
     * with the workbench, console or BPMS instance.
     * 
     * @param jbossServerHostName The hostname of the jboss server instance
     * @param user A user permitted to retrieve the remote {@link InitialContext}
     * @param password The password for the user specified
     * @return a remote {@link InitialContext} instance
     */
    public static InitialContext getRemoteJbossInitialContext(String jbossServerHostName, String user, String password) { 
        Properties initialProps = new Properties();
        initialProps.setProperty(InitialContext.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.remote.client.InitialContextFactory");
        initialProps.setProperty(InitialContext.PROVIDER_URL, "remote://"+ jbossServerHostName + ":4447");
        initialProps.setProperty(InitialContext.SECURITY_PRINCIPAL, user);
        initialProps.setProperty(InitialContext.SECURITY_CREDENTIALS, password);

        for (Object keyObj : initialProps.keySet()) {
            String key = (String) keyObj;
            System.setProperty(key, (String) initialProps.get(key));
        }
        try {
            return new InitialContext(initialProps);
        } catch (NamingException e) {
            throw new RemoteCommunicationException("Unable to create " + InitialContext.class.getSimpleName(), e);
        }
    }
}