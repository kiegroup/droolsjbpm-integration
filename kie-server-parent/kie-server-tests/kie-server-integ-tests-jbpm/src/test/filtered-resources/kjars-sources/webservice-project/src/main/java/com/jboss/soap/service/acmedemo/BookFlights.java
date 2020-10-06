
package com.jboss.soap.service.acmedemo;

import java.io.Serializable;


public class BookFlights
    implements Serializable
{

    private final static long serialVersionUID = 1L;
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
