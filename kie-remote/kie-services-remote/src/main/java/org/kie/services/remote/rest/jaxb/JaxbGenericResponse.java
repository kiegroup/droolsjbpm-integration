package org.kie.services.remote.rest.jaxb;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "message")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbGenericResponse {

    @XmlElement(name = "value")
    private List<JaxbGenericValue> values = new ArrayList<JaxbGenericValue>();

    public List<JaxbGenericValue> getValues() {
        return values;
    }

    public void setValues(List<JaxbGenericValue> values) {
        this.values = values;
    }
}
