package org.kie.services.client.message.serialization.impl.jaxb;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;

@XmlRootElement(name = "map")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbMap extends JaxbArgument {

    @XmlElement(name="entry")
    private List<JaxbMapEntry> entryList = new ArrayList<JaxbMapEntry>();
    
    public JaxbMap() { 
        // Default constructor
    }
    
    public JaxbMap(Map<String, Object> inputMap, int i) {
        this.setIndex(i);
        for(Map.Entry<String, Object> entry : inputMap.entrySet() ) { 
            entryList.add(new JaxbMapEntry(entry));
        }
        this.setIndex(i);
    }
   
    public List<JaxbMapEntry> getEntryList() {
        return entryList;
    }

    @XmlRootElement(name = "entry")
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class JaxbMapEntry { 
       
        @XmlAttribute(name = "key")
        @XmlSchemaType(name = "string")
        private String key;
        
        @XmlElement(name="value")
        @XmlSchemaType(name="base64Binary")
        private Object value;
        
        public JaxbMapEntry() { 
            // Default constructor
        }
        
        public JaxbMapEntry(Map.Entry<String, Object> entry) { 
            this.key = entry.getKey();
            this.value = entry.getValue();
        }

        public String getKey() {
            return key;
        }

        public Object getValue() {
            return value;
        }

    }


}
