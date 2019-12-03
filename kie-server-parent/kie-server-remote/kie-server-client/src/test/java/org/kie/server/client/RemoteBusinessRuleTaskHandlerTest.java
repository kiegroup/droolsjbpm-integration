package org.kie.server.client;

import java.io.Serializable;
import java.util.Collections;

import org.junit.Test;

import static org.mvel2.MVEL.compileExpression;
import static org.mvel2.MVEL.executeExpression;

public class RemoteBusinessRuleTaskHandlerTest {

    // invoking a constructor when the endpoint is not available causes not being able to deploy
    @Test
    public void testNoEndpointAvailable() {
        Serializable compiled = compileExpression(
                                                  "new org.kie.server.client.integration.RemoteBusinessRuleTaskHandler(\"https://localhost:8443/kie-server/services/rest/server\", \"rhpamAdmin\", \"Password1!\", classLoader)");
        executeExpression(compiled, Collections.singletonMap("classLoader", Thread.currentThread().getContextClassLoader()));
    }
}
