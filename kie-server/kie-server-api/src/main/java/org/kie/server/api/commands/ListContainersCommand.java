package org.kie.server.api.commands;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

import org.kie.server.api.model.KieServerCommand;

@XmlRootElement(name="list-containers")
@XmlAccessorType(XmlAccessType.NONE)
public class ListContainersCommand implements KieServerCommand {
    private static final long serialVersionUID = -1803374525440238478L;
    
    public ListContainersCommand() {
        super();
    }
    
}
