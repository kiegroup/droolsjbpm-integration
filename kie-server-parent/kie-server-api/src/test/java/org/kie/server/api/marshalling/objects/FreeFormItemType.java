package org.kie.server.api.marshalling.objects;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FreeFormItemType", propOrder = {
    "itemValue"
})
public class FreeFormItemType implements Serializable, Cloneable
{

    private final static long serialVersionUID = 1L;

    @XmlElement(required = true)
    protected String itemValue;

    /**
     * Gets the value of the itemValue property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getItemValue() {
        return itemValue;
    }

    /**
     * Sets the value of the itemValue property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setItemValue(String value) {
        this.itemValue = value;
    }

}