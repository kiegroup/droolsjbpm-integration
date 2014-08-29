package org.kie.remote.services.ws.sei;

import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * Only used for initial WSDL generation
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "stringObjectEntryList", propOrder = {
    "entries"
})
public class StringObjectEntryList {

    @XmlElement(name = "entry", nillable = true)
    protected List<StringObjectEntry> entries;

}