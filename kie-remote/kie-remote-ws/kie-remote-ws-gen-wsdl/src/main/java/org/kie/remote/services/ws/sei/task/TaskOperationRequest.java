package org.kie.remote.services.ws.sei.task;

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
@XmlType(name = "TaskOperationRequest", propOrder = {
    "type",
    "taskId", 
    "userId",
    "targetEntityId",
    "language",
    "user",
    "group",
    "data"
})
public class TaskOperationRequest {

    @XmlElement(required=true)
    private TaskOperationType type;
    
    @XmlElement(required=true)
    @XmlSchemaType(name="long")
    private Long taskId;
    
    @XmlElement(required=true)
    @XmlSchemaType(name="string")
    private String userId;
    
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
   
    // For complete
    @XmlElement(required=false)
    private StringObjectEntryList data;
    
}
