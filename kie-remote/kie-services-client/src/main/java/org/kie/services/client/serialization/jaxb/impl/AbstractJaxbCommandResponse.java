package org.kie.services.client.serialization.jaxb.impl;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;

import org.kie.api.command.Command;


@XmlAccessorType(XmlAccessType.FIELD)
public abstract class AbstractJaxbCommandResponse<T> implements JaxbCommandResponse<T> {

    @XmlAttribute
    @XmlSchemaType(name="int")
    private Integer index;
    
    @XmlElement(name="command-name")
    @XmlSchemaType(name="string")
    protected String commandName;

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

    /*
     * (non-Javadoc)
     * @see org.kie.services.client.serialization.jaxb.impl.JaxbCommandResponse#setIndex(java.lang.Integer)
     */
    @Override
    public void setIndex(Integer index) {
        this.index = index;
    }

    /* (non-Javadoc)
     * @see org.kie.services.client.serialization.jaxb.impl.JaxbCommandResponse#getCommandName()
     */
    @Override
    public String getCommandName() {
        return commandName;
    }

    /*
     * (non-Javadoc)
     * @see org.kie.services.client.serialization.jaxb.impl.JaxbCommandResponse#setCommandName(java.lang.String)
     */
    @Override
    public void setCommandName(String cmdName) { 
        this.commandName = cmdName;
    }

}
