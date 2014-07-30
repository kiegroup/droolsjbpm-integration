package org.kie.remote.services.rest.jaxb.user;

import javax.xml.bind.annotation.XmlType;

@XmlType(namespace="user")
public class BClass {

    private Integer why;

    public Integer getWhy() {
        return why;
    }

    public void setWhy(Integer why) {
        this.why = why;
    }
}
