package org.kie.server.services.api;

import org.kie.server.api.commands.CommandScript;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.ServiceResponsesList;

public interface KieContainerCommandService {

    ServiceResponse<String> callContainer(String containerId, String payload);
    
    ServiceResponsesList executeScript(CommandScript commands);
}
