package org.kie.services.client.serialization.jaxb.rest;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.kie.api.command.Command;
import org.kie.services.client.serialization.jaxb.impl.AbstractJaxbCommandResponse;
import org.kie.services.client.serialization.jaxb.impl.JaxbRequestStatus;

@XmlRootElement(name="exception-response")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties({"cause"})
public class JaxbExceptionResponse extends AbstractJaxbCommandResponse<String> {

    @XmlTransient
    public Exception cause;
   
    @XmlElement
    @XmlSchemaType(name="string")
    private String message;
    
    @XmlElement
    @XmlSchemaType(name="string")
    private String stackTrace;
    
    public JaxbExceptionResponse() {
        // JAXB default constructor
    }
    
    public JaxbExceptionResponse(Exception e, int i, Command<?> cmd, JaxbRequestStatus status) {
       super(i, cmd);
       initializeExceptionAndMessage(e);
       this.status = status;
    }
    
    public JaxbExceptionResponse(String requestUrl, Exception e, JaxbRequestStatus status) { 
        this.url = requestUrl;
        this.status = status;
        initializeExceptionAndMessage(e);
    }
   
    private void initializeExceptionAndMessage(Exception e) { 
       this.cause = e;
       this.message = e.getClass().getSimpleName() + " thrown with message '" + e.getMessage() + "'";
       if( e.getCause() != null ) { 
           Throwable t = e.getCause();
           this.message = t.getClass().getSimpleName() + " thrown with message '" + t.getMessage() + "'";
       }
       this.stackTrace = convertStackTraceToString(e);
    }
    
    public static String convertStackTraceToString(Throwable t) { 
        StringWriter stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        t.printStackTrace(writer);
        return stringWriter.toString();
    }

    public String prettyPrint() throws JAXBException {
        StringWriter writer = new StringWriter();

        JAXBContext jc = JAXBContext.newInstance(this.getClass());
        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.marshal(this, writer);
        return writer.toString();
    }
    
    // GETTER/SETTTERS ------------------------------------------------------------------------------------------------------------
    
    @Override
    public String getResult() {
        return message;
    }

    @Override
    public void setResult(String result) {
        this.message = result;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Exception getCause() {
        return cause;
    }

    public void setCause(Exception cause) {
        this.cause = cause;
        if( cause != null ) { 
            this.stackTrace = convertStackTraceToString(cause);
        }
    }

    public String getStackTrace() {
        return stackTrace;
    }

    public void setStackTrace(String stackTrace) {
        this.stackTrace = stackTrace;
    }

}
