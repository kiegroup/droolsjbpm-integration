
package org.kie.remote.services.ws.history.generated;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for findVariableInstanceLogs complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="findVariableInstanceLogs">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="request" type="{http://services.remote.kie.org/6.5.1.1/history}HistoryInstanceLogRequest" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "findVariableInstanceLogs", propOrder = {
    "request"
})
public class FindVariableInstanceLogs
    implements Serializable
{

    private final static long serialVersionUID = 1L;
    protected HistoryInstanceLogRequest request;

    /**
     * Gets the value of the request property.
     * 
     * @return
     *     possible object is
     *     {@link HistoryInstanceLogRequest }
     *     
     */
    public HistoryInstanceLogRequest getRequest() {
        return request;
    }

    /**
     * Sets the value of the request property.
     * 
     * @param value
     *     allowed object is
     *     {@link HistoryInstanceLogRequest }
     *     
     */
    public void setRequest(HistoryInstanceLogRequest value) {
        this.request = value;
    }

}
