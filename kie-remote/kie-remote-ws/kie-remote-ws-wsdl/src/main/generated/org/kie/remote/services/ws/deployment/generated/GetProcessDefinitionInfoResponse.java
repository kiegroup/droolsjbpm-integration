
package org.kie.remote.services.ws.deployment.generated;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import org.kie.services.client.serialization.jaxb.impl.process.JaxbProcessDefinitionList;


/**
 * <p>Java class for getProcessDefinitionInfoResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="getProcessDefinitionInfoResponse">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="return" type="{http://services.remote.kie.org/6.5.1.1/deployment}ProcessDefinitionList" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "getProcessDefinitionInfoResponse", propOrder = {
    "_return"
})
public class GetProcessDefinitionInfoResponse
    implements Serializable
{

    private final static long serialVersionUID = 1L;
    @XmlElement(name = "return")
    protected JaxbProcessDefinitionList _return;

    /**
     * Gets the value of the return property.
     * 
     * @return
     *     possible object is
     *     {@link JaxbProcessDefinitionList }
     *     
     */
    public JaxbProcessDefinitionList getReturn() {
        return _return;
    }

    /**
     * Sets the value of the return property.
     * 
     * @param value
     *     allowed object is
     *     {@link JaxbProcessDefinitionList }
     *     
     */
    public void setReturn(JaxbProcessDefinitionList value) {
        this._return = value;
    }

}
