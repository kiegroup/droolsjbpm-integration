package org.kie.camel.container.integration.tests.remote;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.kie.camel.container.api.ExecutionServerCommand;
import org.kie.dmn.api.core.DMNContext;
import org.kie.dmn.api.core.DMNResult;

import static org.junit.Assert.assertThat;

public class DMNClientIntegrationTest extends AbstractRemoteIntegrationTest {

    @Test
    public void testEvaluateAll() {
        final DMNContext dmnContext = createDMNContext();
        dmnContext.set( "a", 10 );
        dmnContext.set( "b", 5 );

        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("containerId", CONTAINER_ID);
        final ExecutionServerCommand executionServerCommand = new ExecutionServerCommand();
        executionServerCommand.setClient("dmn");
        executionServerCommand.setOperation("evaluateAll");
        executionServerCommand.setParameters(parameters);
        executionServerCommand.setBody(dmnContext);
        final Object results = runOnExecutionServer(executionServerCommand);
        Assertions.assertThat(results).isNotNull();
        Assertions.assertThat(results).isInstanceOf(DMNResult.class);

        final DMNResult dmnResult = (DMNResult) results;
        Map<String, Object> map = (Map<String, Object>) dmnResult.getContext().get("Math");
        Assertions.assertThat(map.get("Sum")).isEqualTo(BigDecimal.valueOf(15));
    }

    private DMNContext createDMNContext() {
        final ExecutionServerCommand executionServerCommand = new ExecutionServerCommand();
        executionServerCommand.setClient("dmn");
        executionServerCommand.setOperation("newContext");
        final Object response = runOnExecutionServer(executionServerCommand);

        return (DMNContext) response;
    }
}
