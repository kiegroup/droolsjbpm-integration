package org.kie.server.api.commands;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.kie.server.api.model.KieServerCommand;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="get-scanner-info")
@XStreamAlias("get-scanner-info")
@XmlAccessorType(XmlAccessType.NONE)
public class GetScannerInfoCommand
        implements KieServerCommand {
    private static final long serialVersionUID = -1803374525440238478L;

    @XmlAttribute(name="container-id")
    @XStreamAlias( "container-id" )
    private String    containerId;

    public GetScannerInfoCommand() {
        super();
    }

    public GetScannerInfoCommand(String containerId) {
        this.containerId = containerId;
    }

    public String getContainerId() {
        return containerId;
    }

    public void setContainerId(String containerId) {
        this.containerId = containerId;
    }

}
