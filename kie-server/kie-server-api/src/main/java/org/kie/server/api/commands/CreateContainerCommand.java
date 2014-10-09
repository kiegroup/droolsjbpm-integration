package org.kie.server.api.commands;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.wordnik.swagger.annotations.ApiModel;
import com.wordnik.swagger.annotations.ApiModelProperty;
import org.kie.server.api.model.KieContainerResource;
import org.kie.server.api.model.KieServerCommand;

@XmlRootElement(name = "create-container")
@XmlAccessorType(XmlAccessType.NONE)
@ApiModel(value = "create-container",
          description = "Creates a new Kie Container on the server",
          parent = KieServerCommand.class)
public class CreateContainerCommand implements KieServerCommand {

    private static final long    serialVersionUID = -1803374525440238478L;

    @XmlElement
    @ApiModelProperty(value="The container resource description", required = true)
    private KieContainerResource container;

    public CreateContainerCommand() {
        super();
    }

    public CreateContainerCommand(KieContainerResource container) {
        this.container = container;
    }

    public KieContainerResource getContainer() {
        return container;
    }

    public void setContainer(KieContainerResource container) {
        this.container = container;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((container == null) ? 0 : container.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        CreateContainerCommand other = (CreateContainerCommand) obj;
        if (container == null) {
            if (other.container != null)
                return false;
        } else if (!container.equals(other.container))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "CreateContainerCommand [container=" + container + "]";
    }

}
