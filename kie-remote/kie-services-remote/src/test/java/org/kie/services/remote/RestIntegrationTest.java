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

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.ws.rs.core.MediaType;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.container.test.api.RunAsClient;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.as.arquillian.api.ServerSetup;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.commons.java.nio.file.spi.FileSystemProvider;
import org.kie.commons.java.nio.fs.file.SimpleFileSystemProvider;
import org.kie.services.remote.setup.ArquillianJbossServerSetupTask;

import com.sun.tools.internal.ws.processor.model.Request;

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

    @Test
    @Ignore
    public void shouldBeAbleToDeployAndProcessSimpleRestRequest() throws Exception { 
        // create REST request
        String urlString = new URL(deploymentUrl, "test" + "/session/startProcess").toExternalForm();
        System.out.println( ">> " + urlString );
        
        ClientRequest restRequest = new ClientRequest(urlString);
        restRequest.queryParameter("processId", "org.jbpm.scripttask");

        // Get response
        ClientResponse<String> responseObj = restRequest.post(String.class);

        // Check response
        assertEquals(200, responseObj.getStatus());
        String result = responseObj.getEntity();
    }
    
    @Test
    public void shouldBeAbleToDeployAndProcessSimpleRestXmlRequest() throws Exception { 
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
    }

//    private SameApiRequestHandler getSameApiRequestFactory() { 
//        SameApiRequestHandler factory = ApiRequestFactoryProvider.createNewSameApiInstance();
//        factory.setSerialization(Type.JAXB);
//        return factory;
//    }
}
