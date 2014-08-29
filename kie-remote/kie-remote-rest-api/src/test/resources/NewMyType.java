package org.kie.remote.services.rest.jaxb;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;

@XmlRootElement(name="my-type")
@XmlAccessorType(XmlAccessType.FIELD)
public class MyType implements Serializable {

    @XmlElement
    @XmlSchemaType(name="string")
    private String notText;

    public MyType() {
       // default constructor
    }

    public MyType(String text) {
        this.notText = text;
    }

    public String getNotText() {
        return notText;
    }

    public void setNotText(String text) {
        this.notText = text;
    }

}