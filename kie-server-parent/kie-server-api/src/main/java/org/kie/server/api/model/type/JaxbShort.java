package org.kie.server.api.model.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;

import org.kie.server.api.model.Wrapped;

@XmlRootElement(name = "short-type")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbShort implements Wrapped<Short> {

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

    @Override
    public Short unwrap() {
        return value;
    }
}
