package org.kie.server.integrationtests.drools;

import java.util.ArrayList;
import java.util.List;

import org.drools.core.command.runtime.rule.ClearAgendaGroupCommand;
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

public class AgendaGroupIntegrationTest extends DroolsKieServerBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "agenda-group",
            "1.0.0.Final");

    private static final String CONTAINER_ID = "agenda";
    private static final String LIST_NAME = "list";
    private static final String LIST_OUTPUT_NAME = "output-list";
    private static final String KIE_SESSION = "ksession1";
    private static final String AGENDA_GROUP = "first-agenda";

    @BeforeClass
    public static void buildAndDeployArtifacts() {
        KieServerDeployer.buildAndDeployCommonMavenParent();
        KieServerDeployer.buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/agenda-group").getFile());

        createContainer(CONTAINER_ID, releaseId);
    }

    /**
     * Rule in agenda group "First agenda" is executed first as it gets focus, then focus is returned to main agenda.
     */
    @Test
    public void testAgendaGroup() {
        List<Command<?>> commands = new ArrayList<Command<?>>();

        BatchExecutionCommand batchExecution = commandsFactory.newBatchExecution(commands, KIE_SESSION);

        commands.add(commandsFactory.newSetGlobal(LIST_NAME, new ArrayList<String>(), LIST_OUTPUT_NAME));
        commands.add(commandsFactory.newAgendaGroupSetFocus(AGENDA_GROUP));
        commands.add(commandsFactory.newFireAllRules());
        commands.add(commandsFactory.newGetGlobal(LIST_NAME, LIST_OUTPUT_NAME));

        ServiceResponse<ExecutionResults> response = ruleClient.executeCommandsWithResults(CONTAINER_ID, batchExecution);
        KieServerAssert.assertSuccess(response);
        ExecutionResults result = response.getResult();

        List<?> outcome = (List<?>) result.getValue(LIST_OUTPUT_NAME);
        assertNotNull(outcome);
        assertEquals(2, outcome.size());

        assertEquals("Rule in first agenda group executed", outcome.get(0));
        assertEquals("Rule without agenda group executed", outcome.get(1));
    }

    /**
     * Agenda group "First agenda" is cleared, so it isn't executed.
     */
    @Test
    public void testClearAgendaGroup() {
        List<Command<?>> commands = new ArrayList<Command<?>>();

        BatchExecutionCommand batchExecution = commandsFactory.newBatchExecution(commands, KIE_SESSION);

        commands.add(commandsFactory.newSetGlobal(LIST_NAME, new ArrayList<String>(), LIST_OUTPUT_NAME));
        commands.add(commandsFactory.newAgendaGroupSetFocus(AGENDA_GROUP));
        // Replace if/after Clear command is added to command factory.
        // commands.add(commandsFactory.newClearAgendaGroup(AGENDA_GROUP));
        commands.add(new ClearAgendaGroupCommand(AGENDA_GROUP));
        commands.add(commandsFactory.newFireAllRules());
        commands.add(commandsFactory.newGetGlobal(LIST_NAME, LIST_OUTPUT_NAME));

        ServiceResponse<ExecutionResults> response = ruleClient.executeCommandsWithResults(CONTAINER_ID, batchExecution);
        KieServerAssert.assertSuccess(response);
        ExecutionResults result = response.getResult();

        List<?> outcome = (List<?>) result.getValue(LIST_OUTPUT_NAME);
        assertNotNull(outcome);
        assertEquals(1, outcome.size());

        assertEquals("Rule without agenda group executed", outcome.get(0));

    }
}
