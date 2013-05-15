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

import static org.junit.Assert.assertEquals;

import java.net.URL;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.arquillian.api.ServerSetup;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.services.remote.setup.ArquillianJbossServerSetupTask;

@RunAsClient
@RunWith(Arquillian.class)
@ServerSetup(ArquillianJbossServerSetupTask.class)
public class RestIntegrationTest extends IntegrationBase {

    @Deployment(testable = false)
    public static WebArchive createTestWar() {
         return createWebArchive();
    }

    @ArquillianResource
    URL deploymentUrl;

    @Ignore
    @Test
    public void shouldBeAbleToDeployAndProcessSimpleRestRequest() throws Exception { 
        // create REST request
        String urlString = new URL(deploymentUrl, "/arquillian-test/rest/runtime/test/process/org.jbpm.humantask/start").toExternalForm();
        System.out.println( ">> " + urlString );
        
        ClientRequest restRequest = new ClientRequest(urlString);

        // Get response
        ClientResponse responseObj = restRequest.post();

        // Check response
//        assertEquals(200, responseObj.getStatus());
//        Object result = responseObj.getEntity();
//        System.out.println(result);
        
        urlString = new URL(deploymentUrl, "/arquillian-test/rest/task/1/start?userId=salaboy").toExternalForm();
        System.out.println( ">> " + urlString );
        
        restRequest = new ClientRequest(urlString);

        // Get response
        responseObj = restRequest.post();

        // Check response
//        assertEquals(200, responseObj.getStatus());
//        Object result = responseObj.getEntity();
//        System.out.println(result);

    }
    
//    @Test
//    public void shouldBeAbleToDeployAndProcessSimpleRestXmlRequest() throws Exception { 
//        SameApiRequestHandler requestFactory = getSameApiRequestFactory();
//
//        // create service request
//        RuntimeEngine remoteRuntimeEngine = requestFactory.getRemoteRuntimeEngine("test");
//        KieSession serviceRequest =  remoteRuntimeEngine.getKieSession();
//        HashMap<String, Object> params = new HashMap<String, Object>();
//        params.put("user-id", "Lin Dze");
//        serviceRequest.startProcess("org.jbpm.scripttask", params);
//        
//        // send REST request
//        // requestFactory.sendRestRequest(serviceRequest, deploymentUrl);
//        
//        String msgXmlString = ((MessageHolder) serviceRequest).getMessageXmlString();
//
//        // create REST request
//        String urlString = new URL(deploymentUrl, "test" + "/session/startProcess").toExternalForm();
//        System.out.println( ">> " + urlString );
//        
//        ClientRequest restRequest = new ClientRequest(urlString);
//        restRequest.body(MediaType.APPLICATION_XML, msgXmlString);
//
//        // Get response
//        ClientResponse<String> responseObj = restRequest.post(String.class);
//
//        // Check response
//        assertEquals(200, responseObj.getStatus());
//        String result = responseObj.getEntity();
//    }

//    private SameApiRequestHandler getSameApiRequestFactory() { 
//        SameApiRequestHandler factory = ApiRequestFactoryProvider.createNewSameApiInstance();
//        factory.setSerialization(Type.JAXB);
//        return factory;
//    }
}
