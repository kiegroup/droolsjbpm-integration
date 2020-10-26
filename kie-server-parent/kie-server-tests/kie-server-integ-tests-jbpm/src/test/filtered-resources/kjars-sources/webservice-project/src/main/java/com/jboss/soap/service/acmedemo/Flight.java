
package com.jboss.soap.service.acmedemo;

import java.io.Serializable;
import java.math.BigDecimal;


public class Flight
    implements Serializable
{

    private final static long serialVersionUID = 1L;
    protected String company;
    protected int planeId;
    protected BigDecimal ratePerPerson;
    protected String startCity;
    protected String targetCity;
    protected String travelDate;

    /**
     * Gets the value of the company property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCompany() {
        return company;
    }

    /**
     * Sets the value of the company property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCompany(String value) {
        this.company = value;
    }

    /**
     * Gets the value of the planeId property.
     * 
     */
    public int getPlaneId() {
        return planeId;
    }

    /**
     * Sets the value of the planeId property.
     * 
     */
    public void setPlaneId(int value) {
        this.planeId = value;
    }

    /**
     * Gets the value of the ratePerPerson property.
     * 
     * @return
     *     possible object is
     *     {@link BigDecimal }
     *     
     */
    public BigDecimal getRatePerPerson() {
        return ratePerPerson;
    }

    /**
     * Sets the value of the ratePerPerson property.
     * 
     * @param value
     *     allowed object is
     *     {@link BigDecimal }
     *     
     */
    public void setRatePerPerson(BigDecimal value) {
        this.ratePerPerson = value;
    }

    /**
     * Gets the value of the startCity property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getStartCity() {
        return startCity;
    }

    /**
     * Sets the value of the startCity property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStartCity(String value) {
        this.startCity = value;
    }

    /**
     * Gets the value of the targetCity property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTargetCity() {
        return targetCity;
    }

    /**
     * Sets the value of the targetCity property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTargetCity(String value) {
        this.targetCity = value;
    }

    /**
     * Gets the value of the travelDate property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTravelDate() {
        return travelDate;
    }

    /**
     * Sets the value of the travelDate property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTravelDate(String value) {
        this.travelDate = value;
    }

}
