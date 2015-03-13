package org.kie.server.api.model.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;

@XmlRootElement(name = "short-type")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbShort {

    @XmlElement
    @XmlSchemaType(name = "short")
    private short value;

    public JaxbShort() {
    }

    public JaxbShort(Short value) {
        if (value != null) {
            this.value = value;
        }
    }

    public short getValue() {
        return value;
    }

    public void setValue(Short value) {
        if (value != null) {
            this.value = value;
        }
    }
}
