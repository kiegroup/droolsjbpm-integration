package org.kie.server.api.model.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;

@XmlRootElement(name = "double-type")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbDouble {

    @XmlElement
    @XmlSchemaType(name = "double")
    private double value;

    public JaxbDouble() {
    }

    public JaxbDouble(Double value) {
        if (value != null) {
            this.value = value;
        }
    }

    public double getValue() {
        return value;
    }

    public void setValue(Double value) {
        if (value != null) {
            this.value = value;
        }
    }
}
