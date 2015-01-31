package org.kie.services.client.serialization.jaxb.impl.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;

@XmlRootElement(name = "char-type")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbCharacter {

    @XmlElement
    @XmlSchemaType(name = "string")
    private char value;

    public JaxbCharacter() {
    }

    public JaxbCharacter(Character value) {
        if (value != null) {
            this.value = value;
        }
    }

    public char getValue() {
        return value;
    }

    public void setValue(Character value) {
        if (value != null) {
            this.value = value;
        }
    }
}
