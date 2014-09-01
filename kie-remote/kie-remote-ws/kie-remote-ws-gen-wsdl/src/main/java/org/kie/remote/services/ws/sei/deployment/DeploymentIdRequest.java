package org.kie.remote.services.ws.sei.deployment;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.kie.internal.runtime.conf.MergeMode;
import org.kie.internal.runtime.conf.RuntimeStrategy;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentDescriptor;

/**
 * Only used for initial WSDL generation
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DeploymentIdRequest", propOrder = {
    "deploymentId",
    "operation",
    "descriptor",
    "strategy",
    "mergeMode",
    "pageNumber",
    "pageSize"
})
public class DeploymentIdRequest {

    @XmlElement(required=true)
    @XmlSchemaType(name="string")
    private String deploymentId;
   
    @XmlElement(required=true)
    private DeploymentOperationType operation;
    
    @XmlElement(required=false)
    private JaxbDeploymentDescriptor descriptor;

    @XmlElement(required=false)
    private RuntimeStrategy strategy;

    @XmlElement(required=false)
    private MergeMode mergeMode;

    @XmlElement(required=false)
    private Integer pageNumber;

    @XmlElement(required=false)
    private Integer pageSize;
}
