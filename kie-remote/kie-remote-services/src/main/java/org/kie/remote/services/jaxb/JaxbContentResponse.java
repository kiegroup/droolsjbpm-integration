package org.kie.remote.services.jaxb;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;

import org.jbpm.services.task.impl.model.xml.JaxbContent;
import org.kie.api.command.Command;
import org.kie.api.task.model.Content;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandResponse;

@XmlRootElement(name = "content-response")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbContentResponse extends JaxbContent implements JaxbCommandResponse<Content> {

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
    
    public JaxbContentResponse(JaxbContent content, int i, Command<?> cmd) {
        super(content);
        this.index = i;
        this.commandName = cmd.getClass().getSimpleName();
    }

    public Content getResult() {
        return this;
   }

    @Override
    public void setResult(Content result) {
        initialize(result);
    }

}