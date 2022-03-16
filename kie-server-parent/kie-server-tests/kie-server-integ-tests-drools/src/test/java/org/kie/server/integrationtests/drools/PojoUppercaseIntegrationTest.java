package org.kie.server.integrationtests.drools;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.command.Command;
import org.kie.api.runtime.ExecutionResults;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.integrationtests.shared.KieServerAssert;
import org.kie.server.integrationtests.shared.KieServerDeployer;
import org.kie.server.integrationtests.shared.KieServerReflections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

public class PojoUppercaseIntegrationTest extends DroolsKieServerBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "pojo-uppercase", "1.0.0.Final");

    private static ClassLoader kjarClassLoader;

    private static final String CONTAINER_ID = "pojouppercase";

    private static final String APPLICATION_FQCN = "com.myspace.demo20210908applicant.Application";
    private static final String APPLICANT_FQCN = "com.myspace.demo20210908applicant.Applicant";
    private static final String KIE_SESSION = "ksession1";

    @BeforeClass
    public static void buildAndDeployArtifacts() {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/pojo-uppercase");
        
        kjarClassLoader = KieServices.Factory.get().newKieContainer(releaseId).getClassLoader();
    }
    
    @Before
    public void cleanupContainers() {
        disposeAllContainers();
        createContainer(CONTAINER_ID, releaseId);
    }
    
    @Override
    protected void addExtraCustomClasses(Map<String, Class<?>> extraClasses) throws Exception {
        extraClasses.put(APPLICATION_FQCN, Class.forName(APPLICATION_FQCN, true, kjarClassLoader));
        extraClasses.put(APPLICANT_FQCN, Class.forName(APPLICANT_FQCN, true, kjarClassLoader));
    }
    
    public Object buildApplicant(long applicantID, String name, String address, String SSN, boolean USCitizen) {
        Object applicant = KieServerReflections.createInstance(APPLICANT_FQCN, 
                                                               kjarClassLoader, 
                                                               applicantID, 
                                                               name, 
                                                               address, 
                                                               SSN,
                                                               USCitizen);
        return applicant;
    }
    
    public Object buildApplication(List<Object> family, String programName) {
        Object applicant = KieServerReflections.createInstance(APPLICATION_FQCN, 
                                                               kjarClassLoader);
        KieServerReflections.setValue(applicant, "family", family);
        KieServerReflections.setValue(applicant, "programName", programName);
        return applicant;
    }

    @Test
    public void testPassThrough() {
        // DROOLS-6592 Kie Server Marshalling changes Pojo field UpperCase begin and JSON
        List<Object> family = Arrays.asList(buildApplicant(1, "Jim", "444 St", "SSNJim", true));
        Object application = buildApplication(family, "pojouppercase");
        
        List<Command<?>> commands = new ArrayList<Command<?>>();

        BatchExecutionCommand batchExecution = commandsFactory.newBatchExecution(commands, KIE_SESSION);

        commands.add(commandsFactory.newInsert(application, "application"));
        commands.add(commandsFactory.newFireAllRules("numberOfFiredRules"));

        ServiceResponse<ExecutionResults> response = ruleClient.executeCommandsWithResults(CONTAINER_ID, batchExecution);
        KieServerAssert.assertSuccess(response);
        ExecutionResults result = response.getResult();

        Object outcome = result.getValue("application");
        assertNotNull(outcome);
        assertThat(outcome).hasFieldOrProperty("family");
        Object out_family = KieServerReflections.valueOf(outcome, "family");
        assertThat(out_family).asList().element(0).hasFieldOrPropertyWithValue("name", "Jim")
                .hasFieldOrPropertyWithValue("address", "444 St")
                .hasFieldOrPropertyWithValue("SSN", "SSNJim")
                .hasFieldOrPropertyWithValue("USCitizen", true)
                ;
    }
}
