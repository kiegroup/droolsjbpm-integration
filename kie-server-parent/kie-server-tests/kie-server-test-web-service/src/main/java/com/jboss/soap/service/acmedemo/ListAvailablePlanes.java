
package com.jboss.soap.service.acmedemo;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>Java class for listAvailablePlanes complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="listAvailablePlanes">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="in" type="{http://service.soap.jboss.com/AcmeDemo/}flightRequest" minOccurs="0"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "listAvailablePlanes", propOrder = {
                                                    "in"
})
public class ListAvailablePlanes {

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
