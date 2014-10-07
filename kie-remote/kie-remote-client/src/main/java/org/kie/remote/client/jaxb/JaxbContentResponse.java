package org.kie.remote.client.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;

import org.kie.api.command.Command;
import org.kie.api.task.model.Content;
import org.kie.remote.client.jaxb.JaxbWrapper.JaxbContentWrapper;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandResponse;

@XmlRootElement(name = "content-response")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbContentResponse extends org.kie.remote.jaxb.gen.Content implements JaxbCommandResponse<Content> {

    @XmlAttribute
    @XmlSchemaType(name = "int")
    private Integer index;

    @XmlElement(name = "command-name")
    @XmlSchemaType(name = "string")
    private String commandName;

    public JaxbContentResponse() {
        // Default constructor
    }

    public JaxbContentResponse(int i, Command<?> cmd) {
        this.index = i;
        this.commandName = cmd.getClass().getSimpleName();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kie.services.client.serialization.jaxb.impl.JaxbCommandResponse#getIndex()
     */
    @Override
    public Integer getIndex() {
        return index;
    }

    @Override
    public void setIndex(Integer index) {
        this.index = index;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.kie.services.client.serialization.jaxb.impl.JaxbCommandResponse#getCommandName()
     */
    @Override
    public String getCommandName() {
        return commandName;
    }

    @Override
    public void setCommandName(String cmdName) {
        this.commandName = cmdName;
    }
    
    public JaxbContentResponse(Content content, int i, Command<?> cmd) {
        this.id = content.getId();
        this.content = content.getContent();
        this.index = i;
        this.commandName = cmd.getClass().getSimpleName();
    }

    public Content getResult() {
        return new JaxbContentWrapper(this);
   }

    @Override
    public void setResult(Content result) {
        this.id = result.getId();
        this.content = result.getContent();
    }

}