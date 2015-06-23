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

import org.kie.internal.runtime.conf.MergeMode;
import org.kie.internal.runtime.conf.RuntimeStrategy;
import org.kie.services.client.serialization.jaxb.impl.deploy.JaxbDeploymentDescriptor;

/**
 * Only used for initial WSDL generation
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DeploymentIdRequest", propOrder = {
    "deploymentId",
    "operation",
    "descriptor",
    "strategy",
    "mergeMode",
    "pageNumber",
    "pageSize"
})
public class DeploymentIdRequest {

    @XmlElement(required=true)
    @XmlSchemaType(name="string")
    private String deploymentId;
   
    @XmlElement(required=true)
    private DeploymentOperationType operation;
    
    @XmlElement(required=false)
    private JaxbDeploymentDescriptor descriptor;

    @XmlElement(required=false)
    private RuntimeStrategy strategy;

    @XmlElement(required=false)
    private MergeMode mergeMode;

    @XmlElement(required=false)
    private Integer pageNumber;

    @XmlElement(required=false)
    private Integer pageSize;
}
