package org.kie.services.client.message.serialization.impl.jaxb;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;

@XmlSeeAlso({JaxbMap.class, JaxbNullArgument.class, JaxbSingleArgument.class})
public abstract class JaxbArgument {

    @XmlAttribute(name="index")
    @XmlSchemaType(name="int")
    private Integer index;
    
    @XmlAttribute(name="type")
    @XmlSchemaType(name="int")
    private String type;
    
    public Integer getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
    
}
