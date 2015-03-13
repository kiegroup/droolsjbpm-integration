package org.kie.server.api.model.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;

@XmlRootElement(name = "boolean-type")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbBoolean {

    @XmlElement
    @XmlSchemaType(name = "boolean")
    private boolean value;

    public JaxbBoolean() {

    }

    public JaxbBoolean(Boolean value) {
        if (value != null) {
            this.value = value;
        }
    }

    public Boolean getValue() {
        return value;
    }

    public void setValue(Boolean value) {
        if (value != null) {
            this.value = value;
        }
    }
}
