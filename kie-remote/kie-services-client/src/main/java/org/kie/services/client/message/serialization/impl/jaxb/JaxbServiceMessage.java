package org.kie.services.client.message.serialization.impl.jaxb;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;

import org.kie.services.client.api.command.serialization.jaxb.impl.JaxbCommandMessage;
import org.kie.services.client.message.OperationMessage;
import org.kie.services.client.message.ServiceMessage;

@XmlRootElement(name="message")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbServiceMessage {

    @XmlElement
    @XmlSchemaType(name="string")
    private String domain; 
    
    @XmlElement(name="ver")
    @XmlSchemaType(name="int")
    private Integer version; 
    
    @XmlElement(name="command")
    private List<JaxbCommandMessage> commands = new ArrayList<JaxbCommandMessage>();
    
    public JaxbServiceMessage() { 
        // Default constructor
    }
    
    public JaxbServiceMessage(ServiceMessage origRequest) { 
       this.domain = origRequest.getDomainName();
       this.version = origRequest.getVersion();
       this.commands = new ArrayList<JaxbCommandMessage>();
       
       for( OperationMessage oper : origRequest.getOperations() ) { 
           JaxbCommandMessage jaxbCmd = new JaxbCommandMessage(oper);
           commands.add(jaxbCmd);
       }
    }
    
    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public void addOperation(OperationMessage origOper) {
        this.commands.add(new JaxbCommandMessage(origOper));
    }

    public List<JaxbCommandMessage> getOperations() { 
        return this.commands;
    }
    
}
