package org.kie.remote.services.rest.jaxb;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;

@XmlRootElement(name="my-type")
@XmlAccessorType(XmlAccessType.FIELD)
public class MyType implements Externalizable {

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
    public void writeExternal(ObjectOutput out) throws IOException {
        if( this.text != null ) { 
            out.writeBoolean(true);
            out.writeUTF(this.text);
        } else { 
            out.writeBoolean(false);
        }
        if( this.data != null) { 
            out.writeBoolean(true);
            out.write(this.data);
        } else { 
            out.writeBoolean(false);
        }
    }

    @Override
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        if( in.readBoolean() ) { 
            this.text = in.readUTF();
        }
        if( in.readBoolean() ) { 
            this.data = in.read();
        }
    }
    
}