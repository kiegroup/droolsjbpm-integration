package org.kie.remote.services.ws.sei.deployment;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DeploymentIdRequest", propOrder = {
    "deploymentId"
})
public class DeploymentIdRequest {

    @XmlElement(required=true)
    @XmlSchemaType(name="string")
    private String deploymentId;
    
}
