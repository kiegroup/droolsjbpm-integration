package org.kie.remote.services.rest.jaxb.user.sub;

import javax.xml.bind.annotation.XmlType;

@XmlType(name="sub-c")
public class CClass {

    private Integer why;

    public Integer getWhy() {
        return why;
    }

    public void setWhy(Integer why) {
        this.why = why;
    }
}
