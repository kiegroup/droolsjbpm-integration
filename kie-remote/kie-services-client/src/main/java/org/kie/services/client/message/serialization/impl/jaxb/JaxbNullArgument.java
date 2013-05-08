package org.kie.services.client.message.serialization.impl.jaxb;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "null")
@XmlAccessorType(XmlAccessType.FIELD)
public class JaxbNullArgument extends JaxbArgument {

    public JaxbNullArgument() {
        // Default constructor
    }
    
    public JaxbNullArgument(int i) { 
       this.setIndex(i); 
    }
}
