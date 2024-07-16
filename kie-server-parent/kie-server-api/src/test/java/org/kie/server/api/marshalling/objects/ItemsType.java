package org.kie.server.api.marshalling.objects;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElements;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ItemsType", propOrder = {
    "standardItemsAndFreeformItems"
})
public class ItemsType
    implements Serializable, Cloneable
{

    private final static long serialVersionUID = 1L;
    @XmlElements({
        @XmlElement(name = "standardItem", type = StandardItemType.class),
        @XmlElement(name = "freeformItem", type = FreeFormItemType.class)
    })
    protected List<Serializable> standardItemsAndFreeformItems;

    public List<Serializable> getStandardItemsAndFreeformItems() {
        if (standardItemsAndFreeformItems == null) {
            standardItemsAndFreeformItems = new ArrayList<Serializable>();
        }
        return this.standardItemsAndFreeformItems;
    }


}