package org.kie.server.integrationtests.drools;

import java.util.ArrayList;
import java.util.List;

import org.drools.core.command.runtime.rule.ClearAgendaCommand;
import org.junit.BeforeClass;
import org.junit.Test;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.command.Command;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.ReleaseId;
import org.kie.server.api.model.ServiceResponse;

public class AgendaIntegrationTest extends DroolsKieServerBaseIntegrationTest {

    private static ReleaseId releaseId = new ReleaseId("org.kie.server.testing", "agenda-group",
            "1.0.0.Final");

    private static final String CONTAINER_ID = "activation";
    private static final String LIST_NAME = "list";
    private static final String LIST_OUTPUT_NAME = "output-list";
    private static final String KIE_SESSION = "defaultKieSession";

    @BeforeClass
    public static void buildAndDeployArtifacts() {
        buildAndDeployCommonMavenParent();
        buildAndDeployMavenProject(ClassLoader.class.getResource("/kjars-sources/agenda-group").getFile());
    }

    /**
     * If clear-agenda is used then there are no rules to be executed so command will return empty list.
     */
    @Test
    public void testClearAgenda() {
        assertSuccess(client.createContainer(CONTAINER_ID, new KieContainerResource(CONTAINER_ID, releaseId)));

        List<Command<?>> commands = new ArrayList<Command<?>>();

        BatchExecutionCommand batchExecution = commandsFactory.newBatchExecution(commands, KIE_SESSION);

        commands.add(commandsFactory.newSetGlobal(LIST_NAME, new ArrayList<String>(), LIST_OUTPUT_NAME));
        // Replace if/after Clear command is added to command factory.
        // commands.add(commandsFactory.newClearAgenda());
        commands.add(new ClearAgendaCommand());
        commands.add(commandsFactory.newFireAllRules());
        commands.add(commandsFactory.newGetGlobal(LIST_NAME, LIST_OUTPUT_NAME));

        ServiceResponse<String> response = ruleClient.executeCommands(CONTAINER_ID, batchExecution);
        assertSuccess(response);
        String result = response.getResult();
        assertResultNotContainingStringRegex(result,
            ".*Rule without agenda group executed.*");
        assertResultNotContainingStringRegex(result,
            ".*Rule in first agenda group executed.*");
    }
}
