
package com.jboss.soap.service.acmedemo;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for listAvailablePlanesResponse complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="listAvailablePlanesResponse"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="return" type="{http://service.soap.jboss.com/AcmeDemo/}flight" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "listAvailablePlanesResponse", propOrder = {
    "_return"
})
public class ListAvailablePlanesResponse
    implements Serializable
{

    private final static long serialVersionUID = 1L;
    @XmlElement(name = "return")
    protected Flight _return;

    /**
     * Gets the value of the return property.
     * 
     * @return
     *     possible object is
     *     {@link Flight }
     *     
     */
    public Flight getReturn() {
        return _return;
    }

    /**
     * Sets the value of the return property.
     * 
     * @param value
     *     allowed object is
     *     {@link Flight }
     *     
     */
    public void setReturn(Flight value) {
        this._return = value;
    }

}
