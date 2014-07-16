package org.kie.server.api.commands;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.kie.server.api.model.KieServerCommand;

@XmlRootElement(name="dispose-container")
@XmlAccessorType(XmlAccessType.NONE)
public class DisposeContainerCommand implements KieServerCommand {
    private static final long serialVersionUID = -1803374525440238478L;
    
    @XmlAttribute(name="container-id")
    private String    containerId;
    
    public DisposeContainerCommand() {
        super();
    }
    
    public DisposeContainerCommand(String containerId) {
        this.containerId = containerId;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

}
