package org.kie.remote.services.ws.sei.deployment;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DeploymentInfoResponse", propOrder = {
    "groupId",
    "artifactId",
    "version",
    "kbaseName",
    "ksessionName",
    "strategy",
    "identifier",
    "status",
    "operation"
})
public class DeploymentInfoResponse {

    @XmlElement
    @XmlSchemaType(name="string")
    private String groupId;
    
    @XmlElement
    @XmlSchemaType(name="string")
    private String artifactId;
    
    @XmlElement
    @XmlSchemaType(name="string")
    private String version;
    
    @XmlElement
    @XmlSchemaType(name="string")
    private String kbaseName;
    
    @XmlElement
    @XmlSchemaType(name="string")
    private String ksessionName;

    @XmlElement
    @XmlSchemaType(name="string")
    private String strategy;
    
    @XmlElement
    @XmlSchemaType(name="string")
    private String identifier;
    
    @XmlElement
    @XmlSchemaType(name="string")
    private String status;
   
    @XmlElement
    @XmlSchemaType(name="string")
    private String operation;
    
}
