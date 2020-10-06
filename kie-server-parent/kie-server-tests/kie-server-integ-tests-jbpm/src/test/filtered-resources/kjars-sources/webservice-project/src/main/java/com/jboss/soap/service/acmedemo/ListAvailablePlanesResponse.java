
package com.jboss.soap.service.acmedemo;

import java.io.Serializable;


public class ListAvailablePlanesResponse
    implements Serializable
{

    private final static long serialVersionUID = 1L;
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
