package org.kie.server.integrationtests.drools;

import java.util.ArrayList;
import java.util.List;

import org.drools.core.command.runtime.rule.ClearAgendaCommand;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.command.Command;
import org.kie.api.runtime.ExecutionResults;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;

import static org.junit.Assert.*;
import org.kie.server.integrationtests.shared.KieServerAssert;
import org.kie.server.integrationtests.shared.KieServerDeployer;

public class AgendaIntegrationTest extends DroolsKieServerBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "agenda-group",
            "1.0.0.Final");

    private static final String CONTAINER_ID = "agenda";
    private static final String LIST_NAME = "list";
    private static final String LIST_OUTPUT_NAME = "output-list";
    private static final String KIE_SESSION = "ksession1";

    @BeforeClass
    public static void buildAndDeployArtifacts() {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProjectFromResource("/kjars-sources/agenda-group");

        createContainer(CONTAINER_ID, releaseId);
    }

    /**
     * If clear-agenda is used then there are no rules to be executed so command will return empty list.
     */
    @Test
    public void testClearAgenda() {
        List<Command<?>> commands = new ArrayList<Command<?>>();

        BatchExecutionCommand batchExecution = commandsFactory.newBatchExecution(commands, KIE_SESSION);

        commands.add(commandsFactory.newSetGlobal(LIST_NAME, new ArrayList<String>(), LIST_OUTPUT_NAME));
        // Replace if/after Clear command is added to command factory.
        // commands.add(commandsFactory.newClearAgenda());
        commands.add(new ClearAgendaCommand());
        commands.add(commandsFactory.newFireAllRules());
        commands.add(commandsFactory.newGetGlobal(LIST_NAME, LIST_OUTPUT_NAME));

        ServiceResponse<ExecutionResults> response = ruleClient.executeCommandsWithResults(CONTAINER_ID, batchExecution);
        KieServerAssert.assertSuccess(response);
        ExecutionResults result = response.getResult();

        List<?> outcome = (List<?>) result.getValue(LIST_OUTPUT_NAME);
        assertNotNull(outcome);
        assertEquals(0, outcome.size());
    }
}
