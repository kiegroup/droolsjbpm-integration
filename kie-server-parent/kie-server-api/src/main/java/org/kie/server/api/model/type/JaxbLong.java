package org.kie.server.api.model.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;

import org.kie.server.api.model.Wrapped;

@XmlRootElement(name = "long-type")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbLong implements Wrapped<Long> {

    @XmlElement
    @XmlSchemaType(name="long")
    private long value;

    public JaxbLong() {

    }

    public JaxbLong(Long value) {
        if (value != null) {
            this.value = value;
        }
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        if (value != null) {
            this.value = value;
        }
    }

    @Override
    public Long unwrap() {
        return value;
    }
}
