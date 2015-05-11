package org.kie.server.api.model.type;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;

import org.kie.server.api.model.Wrapped;

@XmlRootElement(name = "string-type")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbString implements Wrapped<String> {

    @XmlElement
    @XmlSchemaType(name="string")
    private String value;

    public JaxbString() {

    }

    public JaxbString(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String unwrap() {
        return value;
    }
}
