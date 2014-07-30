package org.kie.remote.services.rest.jaxb.user;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

@XmlType(name="user-d")
@XmlAccessorType(XmlAccessType.FIELD)
public class EClass {

    public Integer why;

    public Integer getWhy() {
        return why;
    }

    public void setWhy(Integer why) {
        this.why = why;
    }
}
