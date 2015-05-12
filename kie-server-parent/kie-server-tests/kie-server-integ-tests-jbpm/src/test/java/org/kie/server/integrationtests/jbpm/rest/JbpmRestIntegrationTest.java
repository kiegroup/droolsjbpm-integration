package org.kie.server.integrationtests.jbpm.rest;

import java.io.File;
import java.io.FilenameFilter;
import javax.ws.rs.core.Response;

import bitronix.tm.resource.jdbc.PoolingDataSource;
import org.jboss.resteasy.client.ClientRequest;
import org.jboss.resteasy.client.ClientResponse;
import org.jboss.resteasy.client.ClientResponseFailure;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.ExternalResource;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.type.JaxbLong;
import org.kie.server.integrationtests.config.TestConfig;
import org.kie.server.integrationtests.jbpm.DBExternalResource;
import org.kie.server.integrationtests.shared.RestOnlyBaseIntegrationTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JbpmRestIntegrationTest extends RestOnlyBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "rest-processes", "1.0.0.Final");
   
    private static Logger logger = LoggerFactory.getLogger(JbpmRestIntegrationTest.class);


    @ClassRule
    public static ExternalResource StaticResource = new DBExternalResource();
    
    @BeforeClass
    public static void buildAndDeployArtifacts() {
        buildAndDeployCommonMavenParent();
        buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/rest-processes").getFile());
    }


    @Before
    public void cleanup() {
        cleanupSingletonSessionId();
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
        
        ClientResponse<JaxbLong> response = null;
        try {
            ClientRequest clientRequest = newRequest(TestConfig.getHttpUrl() + "/containers/" + resource.getContainerId()
                    + "/process/" + HUMAN_TASK_OWN_TYPE_ID ).header("Content-Type", getMediaType().toString());
            logger.info( "[PUT] " + clientRequest.getUri());
            response = clientRequest.put();
            Assert.assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus());

            clientRequest = newRequest(TestConfig.getHttpUrl() + "/containers/" + resource.getContainerId()
                    + "/process/instance/" + response.getEntity(JaxbLong.class).getValue() ).header("Content-Type", getMediaType().toString());
            logger.info( "[DELETE] " + clientRequest.getUri());
            response = clientRequest.delete();
            Assert.assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus());
        } catch (Exception e) {
            throw new ClientResponseFailure(e, response);
        }
        
    }
    
}
