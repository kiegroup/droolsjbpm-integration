package org.kie.server.services.api;

import org.kie.server.api.commands.CommandScript;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.ServiceResponse;
import org.kie.server.api.model.ServiceResponsesList;

public interface KieContainerCommandService {

    ServiceResponse<String> callContainer(String containerId, String payload, MarshallingFormat marshallingFormat, String classType);
    
    ServiceResponsesList executeScript(CommandScript commands, MarshallingFormat marshallingFormat, String classType);
}
