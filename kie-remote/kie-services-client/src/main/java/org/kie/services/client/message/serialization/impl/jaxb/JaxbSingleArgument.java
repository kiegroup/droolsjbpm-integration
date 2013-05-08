package org.kie.services.client.message.serialization.impl.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;

@XmlRootElement(name="argument")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbSingleArgument extends JaxbArgument {

    @XmlAttribute(name="type")
    @XmlSchemaType(name="string")
    private Class<?> type;
    
    @XmlElement(name="content")
    @XmlSchemaType(name="base64Binary")
    private Object content;
    
    public JaxbSingleArgument() { 
        // Default constructor
    }
    
    public JaxbSingleArgument(Object arg, int i) { 
        this.setIndex(i);
        this.type = arg.getClass();
        this.content = arg;
    }


    public Class<?> getType() {
        return type;
    }

    public Object getContent() {
        return content;
    }
}
