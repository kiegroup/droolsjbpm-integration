package org.kie.remote.services.ws.sei.task;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TaskQueryRequest", propOrder = {
    "workItemId",
    "taskId",
    "procInstId",
    "busAdmin",
    "potOwner",
    "taskOwner",
    "status",
    "union",
    "language",
    "maxResults",
    "page"
})
public class TaskQueryRequest {

    @XmlElement(required=false)
    @XmlSchemaType(name="long")
    private List<Long> workItemId;
    
    @XmlElement(required=false)
    @XmlSchemaType(name="long")
    private List<Long> taskId;
    
    @XmlElement(required=false)
    @XmlSchemaType(name="long")
    private List<Long> procInstId;
    
    @XmlElement(required=false)
    @XmlSchemaType(name="string")
    private List<String> busAdmin;
    
    @XmlElement(required=false)
    @XmlSchemaType(name="string")
    private List<String> potOwner;
    
    @XmlElement(required=false)
    @XmlSchemaType(name="string")
    private List<String> taskOwner;
    
    @XmlElement(required=false)
    @XmlSchemaType(name="string")
    private List<String> status;
    
    @XmlElement(required=false)
    @XmlSchemaType(name="boolean")
    private Boolean union;
    
    @XmlElement(required=false)
    @XmlSchemaType(name="string")
    private List<String> language;
    
    @XmlElement(required=false)
    @XmlSchemaType(name="int")
    private Integer maxResults = 10;
    
    @XmlElement(required=false)
    @XmlSchemaType(name="int")
    private Integer page;
}
