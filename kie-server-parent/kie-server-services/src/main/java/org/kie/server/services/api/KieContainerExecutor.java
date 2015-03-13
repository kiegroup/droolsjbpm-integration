package org.kie.server.services.api;

import org.kie.server.api.commands.CommandScript;
import org.kie.server.api.model.ServiceResponsesList;

public interface KieContainerExecutor {

    ServiceResponsesList executeScript(CommandScript commands);
}
