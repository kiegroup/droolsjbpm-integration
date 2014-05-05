package org.kie.remote.services.ws.sei.deployment;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ProcessDefinitionResponse", propOrder = {
    "id",
    "name",
    "version",
    "packageName",
    "deploymentId",
    "encodedProcessSource"
})
public class ProcessDefinitionResponse {

    @XmlElement
    @XmlSchemaType(name="string") 
    private String id;
   
    @XmlElement
    @XmlSchemaType(name="string") 
    private String name;
   
    @XmlElement
    @XmlSchemaType(name="string") 
    private String version;
   
    @XmlElement(name="package-name")
    @XmlSchemaType(name="string") 
    private String packageName;
   
    @XmlElement(name="deployment-id")
    @XmlSchemaType(name="string") 
    private String deploymentId;
   
    @XmlElement(name="encoded-process-source")
    @XmlSchemaType(name="string") 
    private String encodedProcessSource;

}
