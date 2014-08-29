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
@XmlType(name = "ManageProcessInstanceRequest", propOrder = {
    "operation",
    "deploymentId",
    "processDefinitionId",
    "parameters",
    "getVariables"
})
public class ManageProcessInstanceRequest {

    @XmlElement
    private ProcessInstanceOperationType operation;
    
    @XmlElement(required=true)
    @XmlSchemaType(name="string")
    private String deploymentId;
    
    @XmlElement(required=true)
    @XmlSchemaType(name="string")
    private String processDefinitionId;

    @XmlElement
    private StringObjectEntryList parameters;
    
    @XmlElement
    private Boolean getVariables;
}
