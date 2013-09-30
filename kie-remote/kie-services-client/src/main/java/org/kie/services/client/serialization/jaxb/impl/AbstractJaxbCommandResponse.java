package org.kie.services.client.serialization.jaxb.impl;

import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.kie.api.command.Command;
import org.kie.services.client.serialization.jaxb.rest.JaxbRequestStatus;


@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties({"result"})
public abstract class AbstractJaxbCommandResponse<T> implements JaxbCommandResponse<T> {

    @XmlAttribute
    @XmlSchemaType(name="int")
    private Integer index;
    
    @XmlElement(name="command-name")
    @XmlSchemaType(name="string")
    protected String commandName;

    @XmlElement
    protected JaxbRequestStatus status;
    
    @XmlElement
    @XmlSchemaType(name="anyURI")
    protected String url;
    
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

    protected String getUrl(HttpServletRequest request) { 
        String url = request.getRequestURI();
        if( request.getQueryString() != null ) { 
            url += "?" + request.getQueryString();
        }
        return url;
    }
    
    public String prettyPrint() throws JAXBException {
        StringWriter writer = new StringWriter();

        JAXBContext jc = JAXBContext.newInstance(this.getClass());
        Marshaller marshaller = jc.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        marshaller.marshal(this, writer);
        return writer.toString();
    }
    
}
