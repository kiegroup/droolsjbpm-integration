package org.kie.remote.services.ws.sei.process;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

import org.kie.remote.services.ws.common.SerializableServiceObject;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WorkItemIdRequest", propOrder = {
    "deploymentId",
    "workItemId"
})
public class WorkItemIdRequest extends SerializableServiceObject {

    /** generated Serial Version UID */
    private static final long serialVersionUID = -9207348385851998251L;

    @XmlElement(required=true)
    @XmlSchemaType(name="string")
    private String deploymentId;
    
    @XmlElement(required=true)
    @XmlSchemaType(name="int")
    private Integer workItemId;
    
}
