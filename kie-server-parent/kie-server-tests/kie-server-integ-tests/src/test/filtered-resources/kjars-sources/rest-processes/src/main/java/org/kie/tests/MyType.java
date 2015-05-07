package org.kie.tests;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;

@XmlRootElement(name="my-type")
@XmlAccessorType(XmlAccessType.FIELD)
public class MyType implements Serializable {

    /**
     * Default ID.
     */
    private static final long serialVersionUID = 1L;
    
    @XmlElement
    @XmlSchemaType(name="string")
    private String text;
    
    @XmlElement
    @XmlSchemaType(name="int") 
    private Integer data;
    
    public MyType() {
       // default constructor 
    }
    
    public MyType(String text, int data) {
        this.text = text;
        this.data = data;
    }
    
    public String getText() {
        return text;
    }
    
    public void setText(String text) {
        this.text = text;
    }

    public Integer getData() {
        return data;
    }
    
    public void setData(Integer data) {
        this.data = data;
    }

    @Override
    public String toString() {
        return "MyType{" + "text=" + text + ", data=" + data + "}";
    }
}
