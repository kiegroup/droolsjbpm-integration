/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.api.model.dmn;

import java.util.Collection;
import java.util.HashSet;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "dmn-model-info")
@XStreamAlias("dmn-model-info")
public class DMNModelInfo {

    @XmlElement(name="model-namespace")
    @XStreamAlias("model-namespace")
    private String namespace;

    @XmlElement(name="model-name")
    @XStreamAlias("model-name")
    private String name;
    
    @XmlElement(name="model-id")
    @XStreamAlias("model-id")
    private String id;
    
    // note Jackson annotation is needed on this field and getter, in order for Jackson to NOT use JAXB annotation but proper Jackson annotation
    @XmlElementWrapper(name="decisions")
    @XmlElement(name="dmn-decision-info")
    @JsonIgnore
    @XStreamAlias("decisions")
    private Collection<DMNDecisionInfo> decisions = new HashSet<>();
    
    // note Jackson annotation is needed on this field and getter, in order for Jackson to NOT use JAXB annotation but proper Jackson annotation
    @XmlElementWrapper(name="inputs")
    @XmlElement(name="dmn-inputdata-info")
    @JsonIgnore
    @XStreamAlias("inputs")
    private Collection<DMNInputDataInfo> inputs = new HashSet<>();
    
    public DMNModelInfo() {
        // To avoid the need for kie-server-api to depend on kie-dmn-backend, in order to access DMN's Definitions and DMN's Decision element
        // build this as DTO and only on server-side leverage setters to populate data as needed.
    }
    
    public String getNamespace() {
        return namespace;
    }
    
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getId() {
        return id;
    }
    
    public void setId(String id) {
        this.id = id;
    }
    
    @JsonProperty("decisions")
    public Collection<DMNDecisionInfo> getDecisions() {
        return decisions;
    }
    
    public void setDecisions(Collection<DMNDecisionInfo> decisions) {
        this.decisions = decisions;
    }
    
    @JsonProperty("inputs")
    public Collection<DMNInputDataInfo> getInputs() {
        return inputs;
    }
    
    public void setInputs(Collection<DMNInputDataInfo> inputs) {
        this.inputs = inputs;
    }

}
