package org.kie.remote.services.ws.sei.knowledge;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RepositoriesRequest", propOrder = {
    "deploymentId",
    "processInstanceId"
})
public class RepositoriesRequest {

    @XmlElement(required=true)
    @XmlSchemaType(name="string")
    private String deploymentId;
    
    @XmlElement(required=true)
    @XmlSchemaType(name="string")
    private Long processInstanceId;

}
