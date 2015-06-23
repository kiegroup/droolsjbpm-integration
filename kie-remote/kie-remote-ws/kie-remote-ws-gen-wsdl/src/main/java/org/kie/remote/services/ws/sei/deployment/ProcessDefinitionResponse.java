/*
 * Copyright 2015 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.remote.services.ws.sei.deployment;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlType;

/**
 * Only used for initial WSDL generation
 */
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
