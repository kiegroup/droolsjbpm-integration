package org.kie.server.api.model.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;

@XmlRootElement(name = "byte-type")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbByte {

    @XmlElement
    @XmlSchemaType(name = "byte")
    private byte value;

    public JaxbByte() {

    }

    public JaxbByte(Byte value) {
        if (value != null) {
            this.value = value;
        }
    }

    public byte getValue() {
        return value;
    }

    public void setValue(Byte value) {
        if (value != null) {
            this.value = value;
        }
    }
}
