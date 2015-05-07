package org.kie.server.integrationtests.jbpm.rest;

import javax.ws.rs.core.Response;

import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.ClientResponseFailure;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.integrationtests.shared.RestOnlyBaseIntegrationTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JbpmRestIntegrationTest extends RestOnlyBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "rest-processes", "1.0.0.Final");
   
    private static Logger logger = LoggerFactory.getLogger(JbpmRestIntegrationTest.class);
    
    
    @BeforeClass
    public static void buildAndDeployArtifacts() {
        buildAndDeployCommonMavenParent();
        buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/rest-processes").getFile());
    }

    /**
     * Process ids
     */
    
//    private static final String HUMAN_TASK_PROCESS_ID        = "org.test.kjar.writedocument";
//    private static final String HUMAN_TASK_VAR_PROCESS_ID    = "org.test.kjar.HumanTaskWithForm";
//    private static final String SCRIPT_TASK_PROCESS_ID       = "org.test.kjar.scripttask";
//    private static final String SCRIPT_TASK_VAR_PROCESS_ID   = "org.test.kjar.scripttask.var";
//    private static final String SINGLE_HUMAN_TASK_PROCESS_ID = "org.test.kjar.singleHumanTask";
//    private static final String OBJECT_VARIABLE_PROCESS_ID   = "org.test.kjar.ObjectVariableProcess";
//    private static final String RULE_TASK_PROCESS_ID         = "org.test.kjar.RuleTask";
//    private static final String TASK_CONTENT_PROCESS_ID      = "org.test.kjar.UserTask";
//    private static final String EVALUTAION_PROCESS_ID        = "org.test.kjar.evaluation";
//    private static final String GROUP_ASSSIGNMENT_PROCESS_ID = "org.test.kjar.GroupAssignmentHumanTask";
//    private static final String GROUP_ASSSIGN_VAR_PROCESS_ID = "org.test.kjar.groupAssign";
//    private static final String CLASSPATH_OBJECT_PROCESS_ID  = "org.test.kjar.classpath.process";
    
    private static final String HUMAN_TASK_OWN_TYPE_ID       = "org.test.kjar.HumanTaskWithOwnType";
    
    @Test
    public void testBasicJbpmRequest() throws Exception {
        KieContainerResource resource = new KieContainerResource("rest-processes", releaseId);
        assertSuccess(client.createContainer("rest-processes", resource));
        
        ClientResponse<ServiceResponse<KieContainerResource>> response = null;
        try {
            ClientRequest clientRequest = newRequest(BASE_HTTP_URL + "/containers/" + resource.getContainerId()
                    + "/process/" + HUMAN_TASK_OWN_TYPE_ID );
            logger.info( "[PUT] " + clientRequest.getUri());
            response = clientRequest.put();
            Assert.assertEquals(Response.Status.OK.getStatusCode(), response.getStatus());
        } catch (Exception e) {
            throw new ClientResponseFailure(
                    e, response);
        }
        
    }
    
}
