package org.kie.remote.services.ws.sei.deployment;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentUnit;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentUnit.JaxbDeploymentStatus;

/**
 * Only used for initial WSDL generation
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DeploymentInfoResponse", propOrder = {
    "deploymentUnit",
    "operationRequested"
})
public class DeploymentInfoResponse {

    @XmlElement
    private JaxbDeploymentUnit deploymentUnit;
    
    @XmlElement
    @XmlSchemaType(name="string")
    private DeploymentOperationType operationRequested;
    
}
