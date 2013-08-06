package org.kie.services.client.serialization.jaxb.impl;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;

import org.kie.api.command.Command;

@XmlRootElement(name="exception")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbExceptionResponse extends AbstractJaxbCommandResponse<String> {

    @XmlElement
    @XmlSchemaType(name="string")
    private String message;
    
    @XmlElement
    @XmlSchemaType(name="string")
    private String causeMessage;
    
    @XmlTransient
    public Exception cause;
    
    public JaxbExceptionResponse() {
    }
    
    public JaxbExceptionResponse(Exception e, Command<?> cmd) {
       super();
       this.cause = e;
       this.commandName = cmd.getClass().getSimpleName();
       initializeExceptionInfo(e);
    }
    
    public JaxbExceptionResponse(Exception e, int i, Command<?> cmd) {
       super(i, cmd);
       initializeExceptionInfo(e);
    }
    
    private void initializeExceptionInfo(Exception e) { 
       this.message = e.getClass().getSimpleName() + " thrown with message '" + e.getMessage() + "'";
       if( e.getCause() != null ) { 
           Throwable t = e.getCause();
           this.causeMessage = t.getClass().getSimpleName() + " thrown with message '" + t.getMessage() + "'";
       }
    }
    
    public String getMessage() {
    	return message;
    }

    public String getCauseMessage() {
        return causeMessage;
    }

    @Override
    public String getResult() {
        return message;
    }

    public Exception getCause() {
        return cause;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }

    public void setCauseMessage(String causeMessage) {
        this.causeMessage = causeMessage;
    }

    @Override
    public void setResult(String result) {
        this.message = result;
    }
    

}
