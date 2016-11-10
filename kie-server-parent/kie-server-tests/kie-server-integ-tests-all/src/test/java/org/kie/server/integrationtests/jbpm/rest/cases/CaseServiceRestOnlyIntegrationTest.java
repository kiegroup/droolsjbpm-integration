/*
 * Copyright 2016 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.server.integrationtests.jbpm.rest.cases;

import static org.junit.Assert.assertNotNull;
import static org.kie.server.api.rest.RestURI.build;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;

import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.cases.CaseFile;
import org.kie.server.api.model.type.JaxbString;
import org.kie.server.api.rest.RestURI;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.integrationtests.jbpm.rest.RestJbpmBaseIntegrationTest;
import org.kie.server.integrationtests.shared.KieServerDeployer;

public class CaseServiceRestOnlyIntegrationTest extends RestJbpmBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "case-insurance",
            "1.0.0.Final");

    private static final String CONTAINER_ID = "insurance";

    private static final String CASE_OWNER_ROLE = "owner";
    private static final String CASE_CONTACT_ROLE = "contact";

    private static final String CASE_HR_DEF_ID = "UserTaskCase";

    private Response response = null;

    @BeforeClass
    public static void buildAndDeployArtifacts() {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/case-insurance").getFile());

        createContainer(CONTAINER_ID, releaseId);
    }

    @After
    public void releaseConnection() {
        if (response != null) {
            response.close();
        }
    }

    @Test
    public void testCreateCase() {
        Entity<CaseFile> caseFile = Entity.entity(new CaseFile(), getMediaType());
        Map<String, Object> valuesMap = new HashMap<String, Object>();
        valuesMap.put(RestURI.CONTAINER_ID, CONTAINER_ID);
        valuesMap.put(RestURI.CASE_DEF_ID, CASE_HR_DEF_ID);

        WebTarget clientRequest = newRequest(build(TestConfig.getKieServerHttpUrl(), RestURI.CASE_URI + "/" + RestURI.START_CASE_POST_URI, valuesMap));
        logger.debug("Start case: [POST] " + clientRequest.getUri());

        response = clientRequest.request(getMediaType()).post(caseFile);
        Assert.assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());

        String caseId = response.readEntity(JaxbString.class).getValue();
        assertNotNull(caseId);
    }

    @Test
    public void testUpdateNotExistingCaseComment() {
        String caseId = startUserTaskCase(USER_YODA, USER_JOHN);

        Entity<String> newComment = Entity.entity("", getMediaType());
        Map<String, Object> valuesMap = new HashMap<String, Object>();
        valuesMap.put(RestURI.CONTAINER_ID, CONTAINER_ID);
        valuesMap.put(RestURI.CASE_ID, caseId);
        valuesMap.put(RestURI.CASE_COMMENT_ID, "not-existing-id");

        WebTarget clientRequest = newRequest(build(TestConfig.getKieServerHttpUrl(), RestURI.CASE_URI + "/" + RestURI.CASE_COMMENTS_PUT_URI, valuesMap));
        logger.debug("Update case: [PUT] " + clientRequest.getUri());

        response = clientRequest.request(getMediaType()).put(newComment);
        Assert.assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    @Test
    public void testRemoveNotExistingCaseComment() {
        String caseId = startUserTaskCase(USER_YODA, USER_JOHN);

        Map<String, Object> valuesMap = new HashMap<String, Object>();
        valuesMap.put(RestURI.CONTAINER_ID, CONTAINER_ID);
        valuesMap.put(RestURI.CASE_ID, caseId);
        valuesMap.put(RestURI.CASE_COMMENT_ID, "not-existing-id");

        WebTarget clientRequest = newRequest(build(TestConfig.getKieServerHttpUrl(), RestURI.CASE_URI + "/" + RestURI.CASE_COMMENTS_DELETE_URI, valuesMap));
        logger.debug("Remove case: [DELETE] " + clientRequest.getUri());

        response = clientRequest.request(getMediaType()).delete();
        Assert.assertEquals(Response.Status.NOT_FOUND.getStatusCode(), response.getStatus());
    }

    private String startUserTaskCase(String owner, String contact) {
        Map<String, Object> data = new HashMap<>();
        data.put("s", "first case started");
        CaseFile caseFile = CaseFile.builder()
                .addUserAssignments(CASE_OWNER_ROLE, owner)
                .addUserAssignments(CASE_CONTACT_ROLE, contact)
                .data(data)
                .build();

        String caseId = caseClient.startCase(CONTAINER_ID, CASE_HR_DEF_ID, caseFile);
        assertNotNull(caseId);
        return caseId;
    }
}
