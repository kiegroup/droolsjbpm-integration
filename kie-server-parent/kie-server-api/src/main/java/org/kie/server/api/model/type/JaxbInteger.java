package org.kie.server.api.model.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;

import org.kie.server.api.model.Wrapped;

@XmlRootElement(name = "int-type")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbInteger implements Wrapped<Integer> {


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

    @Override
    public Integer unwrap() {
        return value;
    }
}
