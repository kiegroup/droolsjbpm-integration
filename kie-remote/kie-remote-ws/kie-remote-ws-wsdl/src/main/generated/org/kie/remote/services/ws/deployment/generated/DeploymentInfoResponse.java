
package org.kie.remote.services.ws.deployment.generated;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentUnit;


/**
 * <p>Java class for DeploymentInfoResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DeploymentInfoResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="deploymentUnit" type="{http://services.remote.kie.org/6.5.1.1/deployment}DeploymentUnit"/>
 *         &lt;element name="operationRequested" type="{http://services.remote.kie.org/6.5.1.1/deployment}deploymentOperationType"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DeploymentInfoResponse", propOrder = {
    "deploymentUnit",
    "operationRequested"
})
public class DeploymentInfoResponse
    implements Serializable
{

    private final static long serialVersionUID = 1L;
    @XmlElement(required = true)
    protected JaxbDeploymentUnit deploymentUnit;
    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected DeploymentOperationType operationRequested;

    /**
     * Gets the value of the deploymentUnit property.
     * 
     * @return
     *     possible object is
     *     {@link JaxbDeploymentUnit }
     *     
     */
    public JaxbDeploymentUnit getDeploymentUnit() {
        return deploymentUnit;
    }

    /**
     * Sets the value of the deploymentUnit property.
     * 
     * @param value
     *     allowed object is
     *     {@link JaxbDeploymentUnit }
     *     
     */
    public void setDeploymentUnit(JaxbDeploymentUnit value) {
        this.deploymentUnit = value;
    }

    /**
     * Gets the value of the operationRequested property.
     * 
     * @return
     *     possible object is
     *     {@link DeploymentOperationType }
     *     
     */
    public DeploymentOperationType getOperationRequested() {
        return operationRequested;
    }

    /**
     * Sets the value of the operationRequested property.
     * 
     * @param value
     *     allowed object is
     *     {@link DeploymentOperationType }
     *     
     */
    public void setOperationRequested(DeploymentOperationType value) {
        this.operationRequested = value;
    }

}
