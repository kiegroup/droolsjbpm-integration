package org.kie.services.client.serialization.jaxb.impl.runtime;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonProperty;
import org.kie.internal.jaxb.CorrelationKeyXmlAdapter;
import org.kie.internal.process.CorrelationKey;
import org.kie.internal.process.CorrelationProperty;

@XmlRootElement(name="correlation-key")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbCorrelationKey implements CorrelationKey {

        @XmlElement
        @XmlSchemaType(name="string")
        private String name;

        @XmlElement(name="properties")
        @JsonProperty("properties")
        private List<JaxbCorrelationProperty> jaxbProperties;
        
        public void setName( String name ) {
            this.name = name;
        }

        @Override
        public String getName() {
            return this.name;
        }

        @Override
        @JsonIgnore
        public List<CorrelationProperty<?>> getProperties() {
            List properties = getJaxbProperties();
            return (List<CorrelationProperty<?>>) properties;
        }
        
        public List<JaxbCorrelationProperty> getJaxbProperties() { 
            if( this.jaxbProperties == null ) { 
                this.jaxbProperties = new ArrayList<JaxbCorrelationProperty>();
            }
            return this.jaxbProperties;
        }

        public void setJaxbProperties( List<JaxbCorrelationProperty> properties ) {
            this.jaxbProperties = properties;
        }

        @Override
        public String toExternalForm() {
            return CorrelationKeyXmlAdapter.marshalCorrelationKey(this);
        }
        
}
