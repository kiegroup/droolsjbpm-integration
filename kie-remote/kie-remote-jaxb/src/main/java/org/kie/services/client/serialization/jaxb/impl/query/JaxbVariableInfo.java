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

package org.kie.services.client.serialization.jaxb.impl.query;

import java.util.Date;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.namespace.QName;

import org.kie.api.runtime.manager.audit.VariableInstanceLog;
import org.w3c.dom.Element;

@XmlRootElement(name="variable-info")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbVariableInfo {

    @XmlElement(required=true)
    @XmlSchemaType(name="string")
    private String name;

    @XmlAnyElement
    private Object value;
    
    @XmlElement
    @XmlSchemaType(name="dateTime")
    private Date lastModificationDate;

    public JaxbVariableInfo() { 
        // default for JAXB
    }
    
    public JaxbVariableInfo(String name, Object value) { 
        this.name = name;
        if( value instanceof String ) {
            this.value = getStringJaxbElement((String) value);
        } else {
            this.value = value;
        }
    }
    
    public JaxbVariableInfo(String name, Object value, Date date) { 
        this(name, value);
        this.lastModificationDate = date;
    }
    
    public JaxbVariableInfo(VariableInstanceLog varLog) {
        this(varLog.getVariableId(), getStringJaxbElement(varLog.getValue()), varLog.getDate());
    }
    
    public String getName() {
        return name;
    }

    public void setName( String name ) {
        this.name = name;
    }

    public Object getValue() {
        if( value == null ) { 
            return value;
        }
        if( value instanceof JAXBElement ) { 
            return ((JAXBElement) value).getValue();
        }
        // name of the dom element ('string') must match the one defined in QName of getStringJaxbElement method
        if( value instanceof Element && "string".equals(((Element) value).getNodeName())) {
            return ((Element) value).getTextContent();
        }
        return value;
    }

    public void setValue( Object value ) {
        if( value instanceof String ) {
           this.value = getStringJaxbElement((String) value);
        }
        this.value = value;
    }

    private static JAXBElement<String> getStringJaxbElement(String value) { 
        return new JAXBElement<String>(new QName("string"), String.class, value);
    }
    
    public Date getModificationDate() {
        return lastModificationDate;
    }

    public void setModificationDate( Date modificationDate ) {
        this.lastModificationDate = modificationDate;
    }
}
