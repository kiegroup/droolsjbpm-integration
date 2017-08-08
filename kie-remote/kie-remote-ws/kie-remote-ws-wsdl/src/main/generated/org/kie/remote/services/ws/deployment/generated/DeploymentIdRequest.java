
package org.kie.remote.services.ws.deployment.generated;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;
import org.kie.internal.runtime.conf.MergeMode;
import org.kie.internal.runtime.conf.RuntimeStrategy;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentDescriptor;


/**
 * <p>Java class for DeploymentIdRequest complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="DeploymentIdRequest">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="deploymentId" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *         &lt;element name="operation" type="{http://services.remote.kie.org/6.5.1.1/deployment}deploymentOperationType"/>
 *         &lt;element name="descriptor" type="{http://services.remote.kie.org/6.5.1.1/deployment}DeploymentDescriptor" minOccurs="0"/>
 *         &lt;element name="strategy" type="{http://services.remote.kie.org/6.5.1.1/deployment}RuntimeStrategy" minOccurs="0"/>
 *         &lt;element name="mergeMode" type="{http://services.remote.kie.org/6.5.1.1/deployment}MergeMode" minOccurs="0"/>
 *         &lt;element name="pageNumber" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *         &lt;element name="pageSize" type="{http://www.w3.org/2001/XMLSchema}int" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
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
public class DeploymentIdRequest
    implements Serializable
{

    private final static long serialVersionUID = 1L;
    @XmlElement(required = true)
    protected String deploymentId;
    @XmlElement(required = true)
    @XmlSchemaType(name = "string")
    protected DeploymentOperationType operation;
    protected JaxbDeploymentDescriptor descriptor;
    protected RuntimeStrategy strategy;
    protected MergeMode mergeMode;
    protected Integer pageNumber;
    protected Integer pageSize;

    /**
     * Gets the value of the deploymentId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getDeploymentId() {
        return deploymentId;
    }

    /**
     * Sets the value of the deploymentId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDeploymentId(String value) {
        this.deploymentId = value;
    }

    /**
     * Gets the value of the operation property.
     * 
     * @return
     *     possible object is
     *     {@link DeploymentOperationType }
     *     
     */
    public DeploymentOperationType getOperation() {
        return operation;
    }

    /**
     * Sets the value of the operation property.
     * 
     * @param value
     *     allowed object is
     *     {@link DeploymentOperationType }
     *     
     */
    public void setOperation(DeploymentOperationType value) {
        this.operation = value;
    }

    /**
     * Gets the value of the descriptor property.
     * 
     * @return
     *     possible object is
     *     {@link JaxbDeploymentDescriptor }
     *     
     */
    public JaxbDeploymentDescriptor getDescriptor() {
        return descriptor;
    }

    /**
     * Sets the value of the descriptor property.
     * 
     * @param value
     *     allowed object is
     *     {@link JaxbDeploymentDescriptor }
     *     
     */
    public void setDescriptor(JaxbDeploymentDescriptor value) {
        this.descriptor = value;
    }

    /**
     * Gets the value of the strategy property.
     * 
     * @return
     *     possible object is
     *     {@link RuntimeStrategy }
     *     
     */
    public RuntimeStrategy getStrategy() {
        return strategy;
    }

    /**
     * Sets the value of the strategy property.
     * 
     * @param value
     *     allowed object is
     *     {@link RuntimeStrategy }
     *     
     */
    public void setStrategy(RuntimeStrategy value) {
        this.strategy = value;
    }

    /**
     * Gets the value of the mergeMode property.
     * 
     * @return
     *     possible object is
     *     {@link MergeMode }
     *     
     */
    public MergeMode getMergeMode() {
        return mergeMode;
    }

    /**
     * Sets the value of the mergeMode property.
     * 
     * @param value
     *     allowed object is
     *     {@link MergeMode }
     *     
     */
    public void setMergeMode(MergeMode value) {
        this.mergeMode = value;
    }

    /**
     * Gets the value of the pageNumber property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getPageNumber() {
        return pageNumber;
    }

    /**
     * Sets the value of the pageNumber property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setPageNumber(Integer value) {
        this.pageNumber = value;
    }

    /**
     * Gets the value of the pageSize property.
     * 
     * @return
     *     possible object is
     *     {@link Integer }
     *     
     */
    public Integer getPageSize() {
        return pageSize;
    }

    /**
     * Sets the value of the pageSize property.
     * 
     * @param value
     *     allowed object is
     *     {@link Integer }
     *     
     */
    public void setPageSize(Integer value) {
        this.pageSize = value;
    }

}
