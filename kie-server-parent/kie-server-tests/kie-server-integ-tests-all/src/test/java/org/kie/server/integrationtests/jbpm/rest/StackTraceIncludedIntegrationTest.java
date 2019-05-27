/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.integrationtests.jbpm.rest;

import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;
import static org.kie.server.api.rest.RestURI.DOCUMENT_URI;
import static org.kie.server.api.rest.RestURI.build;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.kie.server.integrationtests.shared.KieServerUtil;
import org.kie.server.integrationtests.shared.basetests.RestOnlyBaseIntegrationTest;


public class StackTraceIncludedIntegrationTest extends RestOnlyBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "definition-project", "1.0.0.Final");
   
    private static Map<MarshallingFormat, String> acceptHeadersByFormat = new HashMap<MarshallingFormat, String>();
    
    private static final String CONTAINER = "definition-project";
    
    
    @BeforeClass
    public static void buildAndDeployArtifacts() {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/definition-project");
        // set the accepted formats with quality param to express preference
        acceptHeadersByFormat.put(MarshallingFormat.JAXB, "application/xml;q=0.9,application/json;q=0.3");// xml is preferred over json
        acceptHeadersByFormat.put(MarshallingFormat.JSON, "application/json;q=0.9,application/xml;q=0.3");// json is preferred over xml

        createContainer(CONTAINER, releaseId);
    }
  
    @Test
    public void testStoreDocumentError() throws Exception {
        KieServerUtil.deleteDocumentStorageFolder();

        String documentEntity="Bad-formed document";

        Map<String, Object> empty = new HashMap<>();
        Response response = null;
        try {
            // create document
            WebTarget clientRequest = newRequest(build(TestConfig.getKieServerHttpUrl(), DOCUMENT_URI, empty));
            response = clientRequest.request(acceptHeadersByFormat.get(marshallingFormat)).post(createEntity(documentEntity));
            String responseBody = response.readEntity(String.class);
            Assert.assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus()); 
            Assert.assertThat(responseBody, 
            		    allOf(containsString("at org.kie.server.remote.rest.jbpm.DocumentResource.createDocument"),
            		          containsString("at org.kie.server.services.impl.marshal.MarshallerHelper.unmarshal")));    
        } finally {
            if(response != null) {
                response.close();
            }
        }
    }

}
