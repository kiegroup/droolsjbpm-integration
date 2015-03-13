package org.kie.server.services.drools;

import org.kie.server.api.commands.CommandScript;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.ServiceResponsesList;

public interface RuleService {

    ServiceResponsesList executeScript(CommandScript commands);

    ServiceResponse<String> callContainer(String containerId, String payload);
}
