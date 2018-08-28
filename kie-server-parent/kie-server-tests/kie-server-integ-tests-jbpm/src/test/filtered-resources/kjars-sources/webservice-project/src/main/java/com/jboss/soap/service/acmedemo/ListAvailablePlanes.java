
package com.jboss.soap.service.acmedemo;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for listAvailablePlanes complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="listAvailablePlanes"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="in" type="{http://service.soap.jboss.com/AcmeDemo/}flightRequest" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "listAvailablePlanes", propOrder = {
    "in"
})
public class ListAvailablePlanes
    implements Serializable
{

    private final static long serialVersionUID = 1L;
    protected FlightRequest in;

    /**
     * Gets the value of the in property.
     * 
     * @return
     *     possible object is
     *     {@link FlightRequest }
     *     
     */
    public FlightRequest getIn() {
        return in;
    }

    /**
     * Sets the value of the in property.
     * 
     * @param value
     *     allowed object is
     *     {@link FlightRequest }
     *     
     */
    public void setIn(FlightRequest value) {
        this.in = value;
    }

}
