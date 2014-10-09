package org.kie.server.api.model;

import javax.xml.bind.annotation.XmlRootElement;

import com.wordnik.swagger.annotations.ApiModel;
import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.kie.api.command.Command;
import org.kie.server.api.commands.CallContainerCommand;
import org.kie.server.api.commands.CreateContainerCommand;
import org.kie.server.api.commands.DisposeContainerCommand;
import org.kie.server.api.commands.ListContainersCommand;

@XmlRootElement
@ApiModel(description = "A command to be executed on the server",
          subTypes = {ListContainersCommand.class,
                      CreateContainerCommand.class,
                      CallContainerCommand.class,
                      DisposeContainerCommand.class})
public interface KieServerCommand extends Command<ServiceResponse> {

}
