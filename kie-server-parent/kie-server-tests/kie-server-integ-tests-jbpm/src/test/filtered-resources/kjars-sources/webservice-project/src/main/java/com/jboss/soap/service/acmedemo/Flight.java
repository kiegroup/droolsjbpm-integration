
package com.jboss.soap.service.acmedemo;

import java.io.Serializable;
import java.math.BigDecimal;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for flight complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="flight"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;element name="company" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="planeId" type="{http://www.w3.org/2001/XMLSchema}int"/&gt;
 *         &lt;element name="ratePerPerson" type="{http://www.w3.org/2001/XMLSchema}decimal"/&gt;
 *         &lt;element name="startCity" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="targetCity" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *         &lt;element name="travelDate" type="{http://www.w3.org/2001/XMLSchema}string"/&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "flight", propOrder = {
    "company",
    "planeId",
    "ratePerPerson",
    "startCity",
    "targetCity",
    "travelDate"
})
public class Flight
    implements Serializable
{

    private final static long serialVersionUID = 1L;
    @XmlElement(required = true)
    protected String company;
    protected int planeId;
    @XmlElement(required = true)
    protected BigDecimal ratePerPerson;
    @XmlElement(required = true)
    protected String startCity;
    @XmlElement(required = true)
    protected String targetCity;
    @XmlElement(required = true)
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
