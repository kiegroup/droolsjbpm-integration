package org.kie.remote.services.rest.jaxb.user;

import javax.xml.bind.annotation.XmlType;

@XmlType(name="user-d")
public class DClass {

    public Integer why;

    public Integer getWhy() {
        return why;
    }

    public void setWhy(Integer why) {
        this.why = why;
    }
}
