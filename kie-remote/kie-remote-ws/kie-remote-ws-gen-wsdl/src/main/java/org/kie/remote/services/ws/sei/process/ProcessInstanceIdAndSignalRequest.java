package org.kie.remote.services.ws.sei.process;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAnyElement;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProcessInstanceIdAndSignalRequest", propOrder = {
    "deploymentId",
    "processInstanceId",
    "type",
    "event"
})
public class ProcessInstanceIdAndSignalRequest {

    @XmlElement(required=true)
    @XmlSchemaType(name="string")
    private String deploymentId;
    
    @XmlElement(required=false)
    @XmlSchemaType(name="string")
    private Long processInstanceId;

    @XmlElement(required=true)
    @XmlSchemaType(name="string")
    private String type;

    @XmlAnyElement
    private Object event;

}
