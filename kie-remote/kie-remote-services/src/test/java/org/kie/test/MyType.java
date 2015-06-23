/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.test;

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
    private String text;
    
    @XmlElement
    @XmlSchemaType(name="int") 
    private Integer data;
    
    public MyType() {
       // default constructor 
    }
    
    public MyType(String text) {
       this.text = text;
       this.data = text.length();
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
        return "{" + this.text + "," + this.data + "}";
    }
    
}
