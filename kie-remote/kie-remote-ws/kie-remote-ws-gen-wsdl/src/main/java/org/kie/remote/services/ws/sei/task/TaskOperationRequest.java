package org.kie.remote.services.ws.sei.task;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TaskOperationRequest", propOrder = {
    "type",
    "targetEntityId",
    "language",
    "user",
    "group"
})
public class TaskOperationRequest {

    @XmlElement(required=true)
    private TaskOperationType type;
    
    @XmlElement(required=false)
    @XmlSchemaType(name="string")
    private String targetEntityId;
    
    @XmlElement(required=false)
    @XmlSchemaType(name="string")
    private String language;
   
    // For nominate
    @XmlElement(required=false)
    private List<String> user;
    
    @XmlElement(required=false)
    private List<String> group;
    
}
