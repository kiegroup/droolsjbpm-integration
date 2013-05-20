package org.kie.services.client.serialization.jaxb.impl;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;

import org.kie.api.command.Command;


public abstract class AbstractJaxbCommandResponse<T> implements JaxbCommandResponse<T> {

    @XmlAttribute
    @XmlSchemaType(name="int")
    private Integer index;
    
    @XmlElement(name="command-name")
    @XmlSchemaType(name="string")
    private String commandName;

    public AbstractJaxbCommandResponse() { 
       // Default constructor 
    }
   
    public AbstractJaxbCommandResponse(int i, Command<?> cmd) { 
        this.index = i;
        this.commandName = cmd.getClass().getSimpleName();
    }
    
    /* (non-Javadoc)
     * @see org.kie.services.client.serialization.jaxb.impl.JaxbCommandResponse#getIndex()
     */
    @Override
    public Integer getIndex() {
        return index;
    }

    /* (non-Javadoc)
     * @see org.kie.services.client.serialization.jaxb.impl.JaxbCommandResponse#getCommandName()
     */
    @Override
    public String getCommandName() {
        return commandName;
    }

}
