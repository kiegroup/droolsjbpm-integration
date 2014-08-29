package org.kie.remote.services.ws.sei;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * Only used for initial WSDL generation
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "stringObjectEntry", propOrder = {
    "value"
})
public class StringObjectEntry {

    protected Object value;
    
    @XmlAttribute(name = "key")
    protected String key;

}

