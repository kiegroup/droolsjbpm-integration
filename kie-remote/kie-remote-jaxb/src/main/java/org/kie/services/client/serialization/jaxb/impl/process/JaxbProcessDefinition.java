/*
 * Copyright 2014 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.services.client.serialization.jaxb.impl.process;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.kie.internal.jaxb.StringKeyObjectValueMapXmlAdapter;
import org.kie.internal.jaxb.StringKeyStringValueMapXmlAdapter;

@XmlRootElement(name="process-definition")
@XmlAccessorType(XmlAccessType.FIELD)
@JsonIgnoreProperties({"processSource"})
public class JaxbProcessDefinition implements Serializable {
    
    /** Generated serial version UID */
    private static final long serialVersionUID = -3957871508798793000L;
   
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
   
    @XmlElement
    @XmlJavaTypeAdapter(value=StringKeyObjectValueMapXmlAdapter.class)
    private Map<String, String> forms = new HashMap<String, String>();

    @XmlElement
    @XmlJavaTypeAdapter(value=StringKeyStringValueMapXmlAdapter.class)
    private Map<String, String> variables = new HashMap<String, String>();
    
    public JaxbProcessDefinition() { 
        // default constructor for JAXB
    }
  
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getDeploymentId() {
        return deploymentId;
    }

    public void setDeploymentId(String deploymentId) {
        this.deploymentId = deploymentId;
    }

    public Map<String, String> getForms() {
        return forms;
    }

    public void setForms(Map<String, String> forms) {
        this.forms = forms;
    }

    public void setVariables(Map<String, String> variables) {
        this.variables = variables;
    }
    
    public Map<String, String> getVariables() { 
        return variables;
    }

}
