package org.kie.services.client.serialization.jaxb.impl;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;

import org.kie.api.command.Command;

public class JaxbExceptionResponse extends AbstractJaxbCommandResponse<Object> {

    @XmlElement
    @XmlSchemaType(name="string")
    private String message;
    
    @XmlElement
    @XmlSchemaType(name="string")
    private String causeMessage;
    
    public JaxbExceptionResponse() {
    }
    
    public JaxbExceptionResponse(Exception e, int i, Command<?> cmd) {
       super(i, cmd);
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
    public Object getResult() {
        return message;
    }

}
