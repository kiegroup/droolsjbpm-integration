package org.kie.server.api.model.dmn;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "dmn-node-stub")
public class DMNNodeStub {

    @XmlElement(name="value")
    private String value;
    
    public DMNNodeStub() {
        // empty constructor for marshalling
    }
    
    static DMNNodeStub of( Object value ) {
        DMNNodeStub res = new DMNNodeStub();
        res.value = value.toString();
        return res;
    }

    @Override
    public String toString() {
        return value;
    }
    
}
