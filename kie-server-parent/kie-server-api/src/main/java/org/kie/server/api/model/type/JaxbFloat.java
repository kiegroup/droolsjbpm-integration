package org.kie.server.api.model.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;

import org.kie.server.api.model.Wrapped;

@XmlRootElement(name = "float-type")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbFloat implements Wrapped<Float> {

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

    @Override
    public Float unwrap() {
        return value;
    }
}
