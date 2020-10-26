
package com.jboss.soap.service.acmedemo;

import java.io.Serializable;

public class CancelBookingResponse
    implements Serializable
{

    private final static long serialVersionUID = 1L;
    protected int out;

    /**
     * Gets the value of the out property.
     * 
     */
    public int getOut() {
        return out;
    }

    /**
     * Sets the value of the out property.
     * 
     */
    public void setOut(int value) {
        this.out = value;
    }

}
