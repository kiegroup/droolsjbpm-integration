package org.kie.remote.services.ws.sei.process;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.kie.remote.services.ws.sei.StringObjectEntryList;

/**
 * Only used for initial WSDL generation
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProcessInstanceResponse", propOrder = {
    "id",
    "processId",
    "state",
    "eventTypes",
    "variables"
})
public class ProcessInstanceResponse {
    
    @XmlElement(required=true)
    @XmlSchemaType(name="long")
    private Long id;

    @XmlElement(required=true)
    @XmlSchemaType(name="string")
    private String processId;

    @XmlElement(required=true)
    @XmlSchemaType(name="int")
    private ProcessInstanceState state; 

    @XmlElement
    private List<String> eventTypes = new ArrayList<String>();
    
    @XmlElement
    private StringObjectEntryList variables;
    
}
