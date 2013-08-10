package org.kie.services.remote.setup;

import java.util.Properties;

import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.jbpm.kie.services.impl.KModuleDeploymentUnit;
import org.kie.services.remote.BasicAuthIntegrationTestBase;

public class TestConstants {
    
    public static final String USER = "test";
    public static final String PASSWORD = "1234asdf@";
    
    public final static String projectVersion;
    static { 
        Properties testProps = new Properties();
        try {
            testProps.load(BasicAuthIntegrationTestBase.class.getResourceAsStream("/test.properties"));
        } catch (Exception e) {
            throw new RuntimeException("Unable to initialize projectVersion property: " + e.getMessage(), e);
        }
        projectVersion = testProps.getProperty("project.version");
    }
    
    /**
     * Vfs deployment
     */
    
    public static final String VFS_DEPLOYMENT_ID = "test";

    /**
     * Kjar deployment
     */
    
    public static final String GROUP_ID = "org.kie.test";
    public static final String ARTIFACT_ID = "kie-wb";
    public static final String VERSION = "1.0";
    public static final String KBASE_NAME = "defaultKieBase";
    public static final String KSESSION_NAME = "defaultKieSession";
 
    public static final String KJAR_DEPLOYMENT_ID;
    static { 
        KModuleDeploymentUnit deploymentUnit = new KModuleDeploymentUnit(GROUP_ID, ARTIFACT_ID, VERSION, KBASE_NAME, KSESSION_NAME);
        KJAR_DEPLOYMENT_ID = deploymentUnit.getIdentifier();
    }
    
    /**
     * JMS initial context
     */
    
    /**
     * Initializes a (remote) IntialContext instance.
     * 
     * @return a remote {@link InitialContext} instance
     */
    public static InitialContext getRemoteInitialContext() {
        Properties initialProps = new Properties();
        initialProps.setProperty(InitialContext.INITIAL_CONTEXT_FACTORY, "org.jboss.naming.remote.client.InitialContextFactory");
        initialProps.setProperty(InitialContext.PROVIDER_URL, "remote://localhost:4447");
        initialProps.setProperty(InitialContext.SECURITY_PRINCIPAL, USER );
        initialProps.setProperty(InitialContext.SECURITY_CREDENTIALS, PASSWORD );
        
        for (Object keyObj : initialProps.keySet()) {
            String key = (String) keyObj;
            System.setProperty(key, (String) initialProps.get(key));
        }
        try {
            return new InitialContext(initialProps);
        } catch (NamingException e) {
            throw new RuntimeException("Unable to create " + InitialContext.class.getSimpleName(), e);
        }
    }
}
