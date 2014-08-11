package org.kie.services.client.api;

import java.net.URL;
import java.util.Properties;

import javax.jms.ConnectionFactory;
import javax.jms.Queue;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.services.client.api.builder.RemoteJmsRuntimeEngineBuilder;
import org.kie.services.client.api.command.RemoteConfiguration;
import org.kie.services.client.api.command.RemoteRuntimeEngine;
import org.kie.services.client.api.command.exception.RemoteCommunicationException;

/**
 * A factory for creating JMS remote API client instances of the {@link RuntimeEngine}.
 * @see {@link RemoteRuntimeEngineFactory}
 */
public class RemoteJmsRuntimeEngineFactory extends RemoteRuntimeEngineFactory {

    protected RemoteConfiguration config;
   
    protected RemoteJmsRuntimeEngineFactory() {
        // private constructor 
    }
    
    RemoteJmsRuntimeEngineFactory(RemoteConfiguration config) { 
        this.config = config;
    }

    /**
     * Retrieves the (remote) {@link InitialContext} from the JBoss AS server instance in order 
     * to be able to retrieve the {@link ConnectionFactory} and {@link Queue} instances to communicate 
     * with the workbench, console or BPMS instance.
     * </p>
     * This method is deprecated in favor of the {@link RemoteJmsRuntimeEngineFactory#getRemoteJbossInitialContext(String, String, String)}
     * instance, which takes a {@link String} hostname parameter instead of a {@link URL} parameter.
     * 
     * @param url The URL of the server instance
     * @param user A user permitted to retrieve the remote {@link InitialContext}
     * @param password The password for the user specified
     * @return an {@link InitialContext} that contains the {@link ConnectionFactory} and {@link Queue} instances to communicate
     * with the workbench, console or BPMS instance.
     */
    @Deprecated
    public static InitialContext getRemoteJbossInitialContext(URL url, String user, String password) { 
        return getRemoteJbossInitialContext(url.getHost(), user, password);
    }
    
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
    
    public RemoteRuntimeEngine newRuntimeEngine() {
    	return new RemoteRuntimeEngine(config);
    }

    public static RemoteJmsRuntimeEngineBuilder newBuilder()  { 
       return newJmsBuilder();
    }

}