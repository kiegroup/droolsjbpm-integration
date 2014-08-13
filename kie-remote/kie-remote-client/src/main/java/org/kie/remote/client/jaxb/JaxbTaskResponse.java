package org.kie.remote.client.jaxb;

import static org.kie.services.client.api.command.AbstractRemoteCommandObject.unsupported;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;

import org.kie.api.command.Command;
import org.kie.api.task.model.Task;
import org.kie.remote.client.jaxb.JaxbWrapper.JaxbTaskWrapper;
import org.kie.services.client.serialization.jaxb.impl.JaxbCommandResponse;

@XmlRootElement(name = "task-response")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbTaskResponse extends org.kie.remote.jaxb.gen.Task implements JaxbCommandResponse<Task> {

    @XmlAttribute
    @XmlSchemaType(name = "int")
    private Integer index;

    @XmlElement(name = "command-name")
    @XmlSchemaType(name = "string")
    private String commandName;

    public JaxbTaskResponse() {
        // Default constructor
    }

    public JaxbTaskResponse(int i, Command<?> cmd) {
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
    
    public JaxbTaskResponse(Task task, int i, Command<?> cmd) {
        this.index = i;
        this.commandName = cmd.getClass().getSimpleName();
    }

    public Task getResult() {
        return new JaxbTaskWrapper(this);
   }

    @Override
    public void setResult( Task result ) {
        unsupported(JaxbTaskResponse.class, Void.class);
    }
    
}