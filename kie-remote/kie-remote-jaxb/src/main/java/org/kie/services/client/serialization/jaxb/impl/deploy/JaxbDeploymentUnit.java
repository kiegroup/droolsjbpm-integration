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

package org.kie.services.client.serialization.jaxb.impl.deploy;

import java.io.Serializable;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.kie.internal.runtime.conf.RuntimeStrategy;

@XmlRootElement(name="deployment-unit")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties({"identifier"})
public class JaxbDeploymentUnit implements Serializable {

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

    @XmlElement(type = RuntimeStrategy.class)
    private RuntimeStrategy strategy;
    
    @XmlElement
    @XmlSchemaType(name="string")
    private String identifier;
    
    @XmlElement(type = JaxbDeploymentStatus.class)
    private volatile JaxbDeploymentStatus status;
    
    @XmlEnum
    public static enum JaxbDeploymentStatus { 
        NONEXISTENT,
        ACCEPTED,
        DEPLOYING,
        DEPLOYED,
        DEPLOY_FAILED,
        UNDEPLOYING,
        UNDEPLOY_FAILED,
        UNDEPLOYED;
    }
    
    public JaxbDeploymentUnit() { 
        // default for JAXB
    }
    
    public JaxbDeploymentUnit(String groupId, String artifactId, String version) { 
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
    }
    
    public JaxbDeploymentUnit(String groupId, String artifactId, String version, String kbaseName, String ksessionName) { 
        this(groupId, artifactId, version);
        this.kbaseName = kbaseName;
        this.ksessionName = ksessionName;
    }
    
    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getKbaseName() {
        return kbaseName;
    }

    public void setKbaseName(String kbaseName) {
        this.kbaseName = kbaseName;
    }

    public String getKsessionName() {
        return ksessionName;
    }

    public void setKsessionName(String ksessionName) {
        this.ksessionName = ksessionName;
    }

    public RuntimeStrategy getStrategy() {
        return strategy;
    }

    public void setStrategy(RuntimeStrategy strategy) {
        this.strategy = strategy;
    }
    
    public JaxbDeploymentStatus getStatus() {
        return status;
    }

    public void setStatus(JaxbDeploymentStatus status) {
        this.status = status;
    }

    public String getIdentifier() {
        String id = getGroupId() + ":" + getArtifactId() + ":" + getVersion();
        boolean kbaseFilled = kbaseName != null  && !kbaseName.isEmpty();
        boolean ksessionFilled = ksessionName != null && !ksessionName.isEmpty();
        if( kbaseFilled || ksessionFilled) {
            id = id.concat(":");
            if( kbaseFilled ) {
                id = id.concat(kbaseName);
            }
            if( ksessionFilled ) {
                id = id.concat(":" + ksessionName);
            }
        }
        return id;
    }
    
    public void setIdentifier(String newId) { 
        // no op
    }
}
