package org.kie.services.client.serialization.jaxb.impl.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;

@XmlRootElement(name = "float-type")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbFloat {

    @XmlElement
    @XmlSchemaType(name = "float")
    private float value;

    public JaxbFloat() {
    }

    public JaxbFloat(Float value) {
        if (value != null) {
            this.value = value;
        }
    }

    public float getValue() {
        return value;
    }

    public void setValue(Float value) {
        if (value != null) {
            this.value = value;
        }
    }
}
