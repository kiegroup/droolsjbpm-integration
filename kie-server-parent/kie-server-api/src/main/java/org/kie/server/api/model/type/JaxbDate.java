package org.kie.server.api.model.type;

import java.util.Date;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;

import org.kie.server.api.model.Wrapped;

@XmlRootElement(name = "date-type")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbDate implements Wrapped<Date> {

    @XmlElement
    @XmlSchemaType(name = "dateTime")
    private Date value;

    public JaxbDate() {
    }

    public JaxbDate(Date value) {
        this.value = value;
    }

    public Date getValue() {
        return value;
    }

    public void setValue(Date value) {
        this.value = value;
    }

    @Override
    public Date unwrap() {
        return value;
    }
}
