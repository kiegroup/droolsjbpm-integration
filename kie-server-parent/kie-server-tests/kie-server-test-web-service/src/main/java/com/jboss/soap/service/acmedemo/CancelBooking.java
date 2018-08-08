
package com.jboss.soap.service.acmedemo;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>Java class for anonymous complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType>
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="in" type="{http://www.w3.org/2001/XMLSchema}string"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
                                 "in"
})
@XmlRootElement(name = "cancelBooking")
public class CancelBooking implements Serializable {

    @XmlElement(required = true)
    protected String in;

    /**
     * Gets the value of the in property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getIn() {
        return in;
    }

    /**
     * Sets the value of the in property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setIn(String value) {
        this.in = value;
    }

}
