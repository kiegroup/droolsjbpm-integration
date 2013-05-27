package org.kie.services.client.serialization.jaxb.rest;

import java.io.PrintWriter;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;

import org.jboss.resteasy.spi.BadRequestException;

@XmlRootElement(name = "response")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbGenericResponse {

    @XmlElement
    private JaxbRequestStatus status;
    
    @XmlElement
    @XmlSchemaType(name="anyURI")
    private String url;
    
    @XmlElement
    @XmlSchemaType(name="string")
    private String error;
    
    @XmlElement
    @XmlSchemaType(name="string")
    private String stackTrace;
    
    public JaxbGenericResponse() { 
       // Default constructor 
    }
    
    public JaxbGenericResponse(HttpServletRequest request ) { 
        this.url = getUrl(request);
        this.status = JaxbRequestStatus.SUCCESS;
    }
    
    /**
     * Exception constructor
     * @param request
     * @param e
     */
    public JaxbGenericResponse(HttpServletRequest request, Exception e) {
        this.url = getUrl(request);
        this.error = e.getMessage();
        if( ! (e instanceof BadRequestException) ) { 
            this.status = JaxbRequestStatus.FAILURE;
            StringWriter stringWriter = new StringWriter();
            PrintWriter writer = new PrintWriter(stringWriter);
            e.printStackTrace(writer);
            stackTrace = stringWriter.toString();
        } else { 
            this.status = JaxbRequestStatus.BAD_REQUEST;
        }
    }
    
    private String getUrl(HttpServletRequest request) { 
        String url = request.getRequestURI();
        if( request.getQueryString() != null ) { 
            url += "?" + request.getQueryString();
        }
        return url;
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
    
    public JaxbRequestStatus getStatus() {
        return status;
    }

    public String getUrl() {
        return url;
    }

    public String getError() {
        return error;
    }

    public String getStackTrace() {
        return stackTrace;
    }

}
