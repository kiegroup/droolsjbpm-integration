package org.kie.services.client.serialization.jaxb.impl.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;

@XmlRootElement(name = "int-type")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbInteger {


    @XmlElement
    @XmlSchemaType(name = "int")
    private int value;

    public JaxbInteger() {
    }

    public JaxbInteger(Integer value) {
        if (value != null) {
            this.value = value;
        }
    }

    public int getValue() {
        return value;
    }

    public void setValue(Integer value) {
        if (value != null) {
            this.value = value;
        }
    }
}
