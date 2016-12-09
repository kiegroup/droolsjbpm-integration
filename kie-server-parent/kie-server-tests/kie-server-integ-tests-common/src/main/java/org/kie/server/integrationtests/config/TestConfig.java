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

package org.kie.server.integrationtests.config;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.ServerSocket;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.TreeMap;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class contains all test configurations and its basic handling.
 */
public class TestConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestConfig.class);

    static {
        LOGGER.info("----- Initializing TestConfig -----");
    }

    /**
     * Property holding port number of embedded REST server.
     */
    private static Integer ALLOCATED_PORT;
    /**
     * Property holding port number of embedded REST server controller.
     */
    private static Integer CONTROLLER_ALLOCATED_PORT;
    /**
     * Property holding port number of embedded REST server router.
     */
    private static Integer ROUTER_ALLOCATED_PORT;
    private static final StringTestParameter PROVIDED_HTTP_URL = new StringTestParameter("kie.server.base.http.url");
    private static final StringTestParameter PROVIDED_CONTEXT = new StringTestParameter("kie.server.context");
    private static final StringTestParameter PROVIDED_CONTROLLER_HTTP_URL = new StringTestParameter("kie.server.controller.base.http.url");

    private static final StringTestParameter USERNAME = new StringTestParameter("username", "yoda");
    private static final StringTestParameter PASSWORD = new StringTestParameter("password", "usetheforce123@");

    private static final StringTestParameter INITIAL_CONTEXT_FACTORY = new StringTestParameter("kie.server.context.factory", "org.jboss.naming.remote.client.InitialContextFactory");
    private static final StringTestParameter CONNECTION_FACTORY = new StringTestParameter("kie.server.connection.factory", "jms/RemoteConnectionFactory");
    private static final StringTestParameter REMOTING_URL = new StringTestParameter("kie.server.remoting.url");
    private static final StringTestParameter REQUEST_QUEUE_JNDI = new StringTestParameter("kie.server.jndi.request.queue", "jms/queue/KIE.SERVER.REQUEST");
    private static final StringTestParameter RESPONSE_QUEUE_JNDI = new StringTestParameter("kie.server.jndi.response.queue", "jms/queue/KIE.SERVER.RESPONSE");

    private static final StringTestParameter KJARS_BUILD_SETTINGS_XML = new StringTestParameter("kie.server.testing.kjars.build.settings.xml");
    private static final StringTestParameter KIE_CLIENT_DEPLOYMENT_SETTINGS = new StringTestParameter("kie.server.client.deployment.settings.xml");

    private static final StringTestParameter CONTAINER_ID = new StringTestParameter("cargo.container.id");
    private static final StringTestParameter CONTAINER_PORT = new StringTestParameter("cargo.servlet.port");
    private static final StringTestParameter KIE_SERVER_WAR_PATH = new StringTestParameter("kie.server.war.path");

    private static final StringTestParameter WEBLOGIC_HOME = new StringTestParameter("weblogic.home");

    /**
     * Get kie-server URL for HTTP services - like REST.
     *
     * @return HTTP URL.
     */
    public static String getKieServerHttpUrl() {
        String httpUrl = "";

        if(PROVIDED_HTTP_URL.isParameterConfigured()) {
            // If HTTP URL is provided by system property then it is returned.
            httpUrl = PROVIDED_HTTP_URL.getParameterValue();
        } else {
            // If HTTP URL is not provided by system property then we run tests locally on embedded server and URL is generated.
            httpUrl = getEmbeddedKieServerHttpUrl();
        }

        return httpUrl;
    }

    /**
     * Get embedded kie-server URL for HTTP services - like REST.
     *
     * @return HTTP URL.
     */
    public static String getEmbeddedKieServerHttpUrl() {
        return "http://localhost:" + getKieServerAllocatedPort() + "/server";
    }

    /**
     * Get kie-servers controller URL for HTTP services - like REST.
     *
     * @return controller HTTP URL.
     */
    public static String getControllerHttpUrl() {
        String httpUrl = "";

        if(PROVIDED_CONTROLLER_HTTP_URL.isParameterConfigured()) {
            // If HTTP URL is provided by system property then it is returned.
            httpUrl = PROVIDED_CONTROLLER_HTTP_URL.getParameterValue();
        } else {
            // If HTTP URL is not provided by system property then we run tests locally on embedded server and URL is generated.
            httpUrl = "http://localhost:" + getControllerAllocatedPort() + "/controller";
        }

        return httpUrl;
    }

    /**
     * Check if controller should be present.
     *
     * @return controller HTTP URL.
     */
    public static boolean isControllerProvided() {
        if(PROVIDED_CONTROLLER_HTTP_URL.isParameterConfigured()) {
            return true;
        }
        return false;
    }

    /**
     * Get allocated port of embedded REST server.
     *
     * @return HTTP port number.
     */
    public static Integer getKieServerAllocatedPort() {
        if(ALLOCATED_PORT == null) {
            try {
                ServerSocket server = new ServerSocket(0);
                ALLOCATED_PORT = server.getLocalPort();
                server.close();
            } catch (IOException e) {
                // failed to dynamically allocate port, try to use hard coded one
                ALLOCATED_PORT = 9789;
            }
            LOGGER.debug("Allocating port {}.", +ALLOCATED_PORT);
        }

        return ALLOCATED_PORT;
    }

    /**
     * Get allocated port of embedded REST server.
     *
     * @return HTTP port number.
     */
    public static Integer getControllerAllocatedPort() {
        if(CONTROLLER_ALLOCATED_PORT == null) {
            try {
                ServerSocket server = new ServerSocket(0);
                CONTROLLER_ALLOCATED_PORT = server.getLocalPort();
                server.close();
            } catch (IOException e) {
                // failed to dynamically allocate port, try to use hard coded one
                CONTROLLER_ALLOCATED_PORT = 9689;
            }
            LOGGER.debug("Allocating port {}.", +CONTROLLER_ALLOCATED_PORT);
        }

        return CONTROLLER_ALLOCATED_PORT;
    }

    /**
     * Get allocated port of embedded REST server.
     *
     * @return HTTP port number.
     */
    public static Integer getRouterAllocatedPort() {
        if(ROUTER_ALLOCATED_PORT == null) {
            try {
                ServerSocket server = new ServerSocket(0);
                ROUTER_ALLOCATED_PORT = server.getLocalPort();
                server.close();
            } catch (IOException e) {
                // failed to dynamically allocate port, try to use hard coded one
                ROUTER_ALLOCATED_PORT = 9765;
            }
            LOGGER.debug("Allocating port for router {}.", +ROUTER_ALLOCATED_PORT);
        }

        return ROUTER_ALLOCATED_PORT;
    }

    /**
     * Allows to skip JMS tests by placing on the classpath empty file 'jms.skip'
     * @return
     */
    public static boolean skipJMS() {
        if (TestConfig.class.getResource("/jms.skip") != null) {
            return true;
        }

        return false;
    }

    /**
     * Allows to start router by placing on the classpath empty file 'router.start'
     * @return
     */
    public static boolean startRouter() {
        if (TestConfig.class.getResource("/router.start") != null) {
            return true;
        }

        return false;
    }

    /**
     * Used for detecting if we run test with local embedded server.
     *
     * @return True if local embedded server is used.
     */
    public static boolean isLocalServer() {
        boolean isLocalServer = true;

        // If there is configured HTTP URL or remoting URL then tests are run against it, otherwise local embedded server is used.
        if(PROVIDED_HTTP_URL.isParameterConfigured() || REMOTING_URL.isParameterConfigured() || PROVIDED_CONTROLLER_HTTP_URL.isParameterConfigured()) {
            isLocalServer = false;
        }

        return isLocalServer;
    }

    /**
     * Get username of user registered in container.
     *
     * @return username.
     */
    public static String getUsername() {
        return TestConfig.USERNAME.getParameterValue();
    }

    /**
     * Get password of user registered in container.
     *
     * @return password.
     */
    public static String getPassword() {
        return TestConfig.PASSWORD.getParameterValue();
    }

    /**
     * Get initial context factory class name for creating context factory used in JMS.
     *
     * @return Initial context factory class name.
     */
    public static String getInitialContextFactory() {
        return TestConfig.INITIAL_CONTEXT_FACTORY.getParameterValue();
    }

    /**
     * Get connection factory JNDI name defined in container to create JMS messages.
     *
     * @return Connection factory JNDI name.
     */
    public static String getConnectionFactory() {
        return TestConfig.CONNECTION_FACTORY.getParameterValue();
    }

    /**
     * Get URL which is used for remoting services like JMS.
     *
     * @return URL for remoting service.
     */
    public static String getRemotingUrl() {
        return TestConfig.REMOTING_URL.getParameterValue();
    }

    /**
     * Get JNDI name of request queue for kie server.
     *
     * @return Request queue JNDI name.
     */
    public static String getRequestQueueJndi() {
        return TestConfig.REQUEST_QUEUE_JNDI.getParameterValue();
    }

    /**
     * Get JNDI name of response queue for kie server.
     *
     * @return Response queue JNDI name.
     */
    public static String getResponseQueueJndi() {
        return TestConfig.RESPONSE_QUEUE_JNDI.getParameterValue();
    }

    /**
     * @return Initial context for connecting to remote server.
     */
    public static InitialContext getInitialRemoteContext() {
        InitialContext context = null;
        try {
            final Properties env = new Properties();
            env.put(Context.INITIAL_CONTEXT_FACTORY, getInitialContextFactory());
            env.put(Context.PROVIDER_URL, getRemotingUrl());
            env.put(Context.SECURITY_PRINCIPAL, getUsername());
            env.put(Context.SECURITY_CREDENTIALS, getPassword());
            context = new InitialContext(env);
        } catch (NamingException e) {
            throw new RuntimeException("Failed to create initial context!", e);
        }
        return context;
    }

    /**
     * @return location of the settings.xml file that should be used when building testing kjars
     */
    public static String getKjarsBuildSettingsXml() {
        return TestConfig.KJARS_BUILD_SETTINGS_XML.getParameterValue();
    }

    /**
     * @return Cargo container ID.
     */
    public static String getContainerId() {
        return TestConfig.CONTAINER_ID.getParameterValue();
    }

    /**
     * @return Kie server context value.
     */
    public static String getKieServerContext() {
        return TestConfig.PROVIDED_CONTEXT.getParameterValue();
    }

    /**
     * @return Path to Kie server WAR file.
     */
    public static String getKieServerWarPath() {
        return TestConfig.KIE_SERVER_WAR_PATH.getParameterValue();
    }

    /**
     * @return Servlet port to container.
     */
    public static String getContainerPort() {
        return TestConfig.CONTAINER_PORT.getParameterValue();
    }

    /**
     * @return Servlet port to container.
     */
    public static String getWebLogicHome() {
        return TestConfig.WEBLOGIC_HOME.getParameterValue();
    }

    /**
     * @return Servlet port to container.
     */
    public static boolean isWebLogicHomeProvided() {
        return TestConfig.WEBLOGIC_HOME.isParameterConfigured();
    }

    /**
     * @return location of custom kie-server-testing-client-deployment-settings.xml
     */
    public static String getKieClientDeploymentSettings() {
        return TestConfig.KIE_CLIENT_DEPLOYMENT_SETTINGS.getParameterValue();
    }

    // Used for printing all configuration values at the beginning of first test run.
    static {
        TreeMap<String, String> params = new TreeMap<String, String>();
        int maxKeyLength = 0;
        for (Field f : TestConfig.class.getDeclaredFields()) {
            if (TestParameter.class.isAssignableFrom(f.getType())) {
                try {
                    String paramName = f.getName();
                    TestParameter<?> paramValue = (TestParameter<?>) f.get(null);
                    maxKeyLength = Math.max(maxKeyLength, paramName.length());
                    if (paramValue.isParameterConfigured()) {
                        params.put(paramName, paramValue.getParameterValue().toString());
                    }
                } catch (IllegalAccessException ex) {
                    LOGGER.error("Cannot read field '{}'.", f.getName(), ex);
                }
            }
        }
        for (Entry<String, String> entry : params.entrySet()) {
            String paramName = entry.getKey();
            String value = entry.getValue();

            LOGGER.info("{} = {}",
                    String.format("%" + maxKeyLength + "s", paramName),
                    value
            );
        }
    }

    private abstract static class TestParameter<T> {

        private String key;
        private T defaultValue;

        private TestParameter(String key, T defaultValue) {
            this.key = key;
            this.defaultValue = defaultValue;
        }

        /**
         * @return Parameter value.
         */
        public T getParameterValue() {
            T parameterValue = convert(key);
            return parameterValue != null ? parameterValue : defaultValue;
        }

        /**
         * @return True if parameter is configured.
         */
        public boolean isParameterConfigured() {
            T parameterValue = convert(key);
            return parameterValue != null ? true : false;
        }

        /**
         * Convert provided key to value object.
         *
         * @param key Key for conversion.
         * @return Object generated using provided key.
         */
        protected abstract T convert(String key);
    }

    private static class StringTestParameter extends TestParameter<String> {

        private StringTestParameter(String key) {
            super(key, null);
        }

        private StringTestParameter(String key, String defaultValue) {
            super(key, defaultValue);
        }

        @Override
        protected String convert(String key) {
            String systemPropertyValue = System.getProperty(key);
            if (systemPropertyValue == null || systemPropertyValue.isEmpty()) {
                return null;
            }
            return systemPropertyValue;
        }
    }
}
