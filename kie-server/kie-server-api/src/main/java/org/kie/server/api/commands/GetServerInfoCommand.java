package org.kie.server.api.commands;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import org.kie.server.api.model.KieServerCommand;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="get-server-info")
@XStreamAlias( "get-server-info" )
@XmlAccessorType(XmlAccessType.NONE)
public class GetServerInfoCommand
        implements KieServerCommand {
    private static final long serialVersionUID = -1803374525440238478L;

    public GetServerInfoCommand() {
        super();
    }
    
}
