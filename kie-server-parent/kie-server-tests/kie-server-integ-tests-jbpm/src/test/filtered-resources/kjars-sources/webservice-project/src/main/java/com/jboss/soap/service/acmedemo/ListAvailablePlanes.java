
package com.jboss.soap.service.acmedemo;

import java.io.Serializable;


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
