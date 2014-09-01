package org.kie.services.client.serialization.jaxb.impl.query;

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;

@XmlRootElement(name="variable-info")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbVariableInfo {

    @XmlElement(required=true)
    @XmlSchemaType(name="string")
    private String name;

    @XmlElement(required=true)
    @XmlSchemaType(name="string")
    private String value;
    
    @XmlElement
    @XmlSchemaType(name="dateTime")
    private Date modificationDate;

    public JaxbVariableInfo() { 
        // default for JAXB
    }
    
    public JaxbVariableInfo(String name, String value) { 
        this.name = name;
        this.value = value;
    }
    
    public JaxbVariableInfo(String name, String value, Date date) { 
        this(name, value);
        this.modificationDate = date;
    }
    
    public String getName() {
        return name;
    }

    public void setName( String name ) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue( String value ) {
        this.value = value;
    }

    public Date getModificationDate() {
        return modificationDate;
    }

    public void setModificationDate( Date modificationDate ) {
        this.modificationDate = modificationDate;
    }
}
