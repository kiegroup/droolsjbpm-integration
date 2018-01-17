/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.remote.rest.common.resource;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.Response;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.KieServerEnvironment;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.KieServiceResponse.ResponseType;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.services.impl.KieServerImpl;
import org.kie.server.services.impl.storage.file.KieServerStateFileRepository;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class KieServerRestImplTest {

    private static final File REPOSITORY_DIR = new File("target/repository-dir");
    private static final String KIE_SERVER_ID = "kie-server-impl-test";
    
    private KieServerImpl kieServer;
    private String origServerId = null;
    
    private Marshaller marshaller = MarshallerFactory.getMarshaller(MarshallingFormat.JSON, this.getClass().getClassLoader());
    
    @Mock
    private HttpHeaders headers;

    @Before
    public void setupKieServerImpl() throws Exception {
        System.setProperty(KieServerConstants.KIE_SERVER_MGMT_API_DISABLED, "true");
        origServerId = KieServerEnvironment.getServerId();
        System.setProperty("org.kie.server.id", KIE_SERVER_ID);
        KieServerEnvironment.setServerId(KIE_SERVER_ID);

        FileUtils.deleteDirectory(REPOSITORY_DIR);
        FileUtils.forceMkdir(REPOSITORY_DIR);
        kieServer = new KieServerImpl(new KieServerStateFileRepository(REPOSITORY_DIR));
        
        MultivaluedHashMap<String, String> mockedRequestHeaders = new MultivaluedHashMap<>();
        mockedRequestHeaders.add("Accept", "application/json");
        when(headers.getRequestHeaders()).thenReturn(mockedRequestHeaders);
    }

    @After
    public void cleanUp() {
        if (kieServer != null) {
            kieServer.destroy();
        }
        KieServerEnvironment.setServerId(origServerId);
        System.clearProperty(KieServerConstants.KIE_SERVER_MGMT_API_DISABLED);
    }
    
    @Test
    public void testCreateContainerWithManagementDisabled() {
  
        KieServerRestImpl restServer = new KieServerRestImpl(kieServer);
        
        Response response = restServer.createContainer(headers, "test", "");        
        assertForbiddenResponse(response);
    }
    
    @Test
    public void testDisposeContainerWithManagementDisabled() {
  
        KieServerRestImpl restServer = new KieServerRestImpl(kieServer);
        
        Response response = restServer.disposeContainer(headers, "test");        
        assertForbiddenResponse(response);
    }
    
    @Test
    public void testUpdateReleaseIdWithManagementDisabled() {
  
        KieServerRestImpl restServer = new KieServerRestImpl(kieServer);
        
        Response response = restServer.updateReleaseId(headers, "test", "");        
        assertForbiddenResponse(response);
    }
    
    @Test
    public void testUpdateScannerWithManagementDisabled() {
  
        KieServerRestImpl restServer = new KieServerRestImpl(kieServer);
        
        Response response = restServer.updateScanner(headers, "test", "");        
        assertForbiddenResponse(response);
    }
    
    private void assertForbiddenResponse(Response response) {   
        assertNotNull(response);        
        assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus());
        ServiceResponse<?> serviceResponse = marshaller.unmarshall((String) response.getEntity(), ServiceResponse.class);
        assertNotNull(serviceResponse);
        
        assertEquals(ResponseType.FAILURE, serviceResponse.getType());
        assertEquals("KIE Server management api is disabled", serviceResponse.getMsg());
    }
}
