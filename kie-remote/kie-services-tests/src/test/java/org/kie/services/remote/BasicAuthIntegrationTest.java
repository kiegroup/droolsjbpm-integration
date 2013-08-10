/*
 * JBoss, Home of Professional Open Source
 * 
 * Copyright 2012, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.services.remote;

import static org.kie.services.remote.setup.TestConstants.*;

import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.resteasy.client.ClientRequestFactory;
import org.jboss.shrinkwrap.api.Archive;
import org.junit.AfterClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.services.client.api.RemoteConfiguration.AuthenticationType;
import org.kie.services.remote.tests.JmsIntegrationTestMethods;
import org.kie.services.remote.tests.RestIntegrationTestMethods;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RunAsClient
@RunWith(Arquillian.class)
public class BasicAuthIntegrationTest extends BasicAuthIntegrationTestBase {

    private static Logger logger = LoggerFactory.getLogger(BasicAuthIntegrationTest.class);

    @Deployment(testable = false)
    public static Archive<?> createWar() {
       return createWarWithTestDeploymentLoader(true);
    }

    @ArquillianResource
    URL deploymentUrl;

    private RestIntegrationTestMethods restTests = new RestIntegrationTestMethods(KJAR_DEPLOYMENT_ID);
    private JmsIntegrationTestMethods jmsTests = new JmsIntegrationTestMethods(KJAR_DEPLOYMENT_ID);
    
    @AfterClass
    public static void waitForTxOnServer() throws InterruptedException { 
        Thread.sleep(1000);
    }
   
    @Test
    @Ignore("fix me!")
    public void testJmsStartProcess() throws Exception {
       jmsTests.startProcess(getRemoteInitialContext(), USER, PASSWORD); 
    }
    
    
    @Test
    @Ignore("fix me!")
    public void testJmsRemoteApiHumanTaskProcess() throws Exception {
        jmsTests.remoteApiHumanTaskProcess(getRemoteInitialContext(), USER, PASSWORD);
    }

    @Test
    public void testRestUrlStartHumanTaskProcess() throws Exception {
        ClientRequestFactory requestFactory = createBasicAuthRequestFactory(deploymentUrl, USER, PASSWORD);
        restTests.urlStartHumanTaskProcessTest(deploymentUrl, requestFactory);
    }
    
    @Test
    public void testRestExecuteStartProcess() throws Exception { 
        ClientRequestFactory requestFactory = createBasicAuthRequestFactory(deploymentUrl, USER, PASSWORD);
        restTests.executeStartProcess(deploymentUrl, requestFactory);
    }
    
    @Test
    public void testRestRemoteApiHumanTaskProcess() throws Exception {
        restTests.remoteApiHumanTaskProcess(deploymentUrl, AuthenticationType.BASIC, USER, PASSWORD);
    }
    
    @Test
    public void testRestExecuteTaskCommands() throws Exception  {
        ClientRequestFactory requestFactory = createBasicAuthRequestFactory(deploymentUrl, USER, PASSWORD);
        restTests.executeTaskCommands(deploymentUrl, requestFactory);
    }
    
    @Test
    public void testRestHistoryLogs() throws Exception {
        ClientRequestFactory requestFactory = createBasicAuthRequestFactory(deploymentUrl, USER, PASSWORD);
        restTests.restHistoryLogs(deploymentUrl, requestFactory);
    }
}
