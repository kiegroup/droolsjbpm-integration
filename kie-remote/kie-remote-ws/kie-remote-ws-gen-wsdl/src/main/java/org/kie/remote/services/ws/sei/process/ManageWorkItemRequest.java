package org.kie.remote.services.ws.sei.process;

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
@XmlType(name = "ManageWorkItemRequest", propOrder = {
    "operation",
    "deploymentId",
    "workItemId",
    "results"
})
public class ManageWorkItemRequest {

    @XmlElement
    private WorkItemOperationType operation;
    
    @XmlElement(required=true)
    @XmlSchemaType(name="string")
    private String deploymentId;
    
    @XmlElement(required=true)
    @XmlSchemaType(name="long")
    private Long workItemId;

    @XmlElement
    private StringObjectEntryList results;
    
}
