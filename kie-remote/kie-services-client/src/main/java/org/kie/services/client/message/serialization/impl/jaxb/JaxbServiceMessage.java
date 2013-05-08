package org.kie.services.client.message.serialization.impl.jaxb;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;

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
    
    @XmlElement(name="operation")
    private List<JaxbOperation> operations = new ArrayList<JaxbOperation>();
    
    public JaxbServiceMessage() { 
        // Default constructor
    }
    
    public JaxbServiceMessage(ServiceMessage origRequest) { 
       this.domain = origRequest.getDomainName();
       this.version = origRequest.getVersion();
       this.operations = new ArrayList<JaxbOperation>();
       
       for( OperationMessage oper : origRequest.getOperations() ) { 
           JaxbOperation jaxbOper = new JaxbOperation(oper);
           operations.add(jaxbOper);
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
        this.operations.add(new JaxbOperation(origOper));
    }

    public List<JaxbOperation> getOperations() { 
        return this.operations;
    }
    
}
