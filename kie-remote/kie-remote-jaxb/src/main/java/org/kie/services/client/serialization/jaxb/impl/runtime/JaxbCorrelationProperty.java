package org.kie.services.client.serialization.jaxb.impl.runtime;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.kie.internal.process.CorrelationProperty;

@XmlRootElement(name="correlation-property")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbCorrelationProperty implements CorrelationProperty<String> {

        @XmlElement
        @XmlSchemaType(name="string")
        private String name;

        @XmlElement(required=true)
        @XmlSchemaType(name="string")
        private String value;

        public JaxbCorrelationProperty() {
            // JAXB default constructor 
        }
       
        public JaxbCorrelationProperty(String value) { 
           this.value = value; 
        }
        
        public JaxbCorrelationProperty(String name, String value) { 
            this(value);
            this.name = name;
        }
        
        @Override
        public String getName() {
            return this.name;
        }

        public void setName( String name ) {
            this.name = name;
        }

        @Override
        @JsonIgnore
        public String getType() {
            return String.class.getName();
        }

        @Override
        public String getValue() {
            return this.value;
        }

        public void setValue( String value ) {
            this.value = value;
        }
        
}
