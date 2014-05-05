package org.kie.remote.services.ws.sei.process;

import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProcessDefIdAndParametersRequest", propOrder = {
    "deploymentId",
    "processDefinitionId",
    "parameters"
})
public class ProcessDefIdAndParametersRequest {

    @XmlElement(required=true)
    @XmlSchemaType(name="string")
    private String deploymentId;
    
    @XmlElement(required=true)
    @XmlSchemaType(name="string")
    private String processDefinitionId;

    @XmlElement
    private Map<String, Object> parameters;
    
}
