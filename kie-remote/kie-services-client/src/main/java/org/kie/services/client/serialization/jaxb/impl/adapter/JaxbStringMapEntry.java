package org.kie.services.client.serialization.jaxb.impl.adapter;

import java.util.Map.Entry;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;

public class JaxbStringMapEntry {

    @XmlAttribute
    public String key;
  
    @XmlValue
    public String value;
    
    public JaxbStringMapEntry() { 
       // default 
    }
    
    public JaxbStringMapEntry(Entry<String, String> entry) {
        this.key = entry.getKey();
        this.value = entry.getValue();
    }
    
}
