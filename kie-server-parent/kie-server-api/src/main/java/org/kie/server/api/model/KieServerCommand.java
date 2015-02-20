package org.kie.server.api.model;

import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonTypeInfo;
import org.kie.api.command.Command;

@XmlRootElement
public interface KieServerCommand extends Command<ServiceResponse> {

}
