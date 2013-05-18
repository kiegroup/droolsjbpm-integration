package org.kie.services.remote.rest.jaxb.general;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;

public class JaxbGenericValue {

    @XmlAttribute(name="name")
    @XmlSchemaType(name="string")
    private String valueName; 
    
    @XmlAttribute(name="type")
    @XmlSchemaType(name="string")
    private Class<?> type;
    
    @XmlElement(name="content")
    @XmlSchemaType(name="base64Binary")
    private Object content;
    
    public String getValueName() {
        return valueName;
    }

    public void setValueName(String valueName) {
        this.valueName = valueName;
    }

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public Object getContent() {
        return content;
    }

    public void setContent(Object content) {
        this.content = content;
    }
    
}
