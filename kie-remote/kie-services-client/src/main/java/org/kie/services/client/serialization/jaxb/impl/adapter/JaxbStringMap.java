package org.kie.services.client.serialization.jaxb.impl.adapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import javax.xml.bind.annotation.XmlElement;

public class JaxbStringMap {

    @XmlElement(name="entry")
    public List<JaxbStringMapEntry> entries = new ArrayList<JaxbStringMapEntry>();
    
    public void addEntry(Entry<String, String> entry) { 
       this.entries.add(new JaxbStringMapEntry(entry)); 
    }
    
}
