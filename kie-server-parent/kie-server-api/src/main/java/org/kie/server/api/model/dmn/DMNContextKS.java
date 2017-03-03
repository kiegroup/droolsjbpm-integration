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

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "dmn-evaluation-context")
public class DMNContextKS {

    @XmlElement(name="model-namespace")
    private String namespace;

    @XmlElement(name="model-name")
    private String modelName;

    @XmlElement(name="decision-name")
    private String decisionName;

    @XmlElementWrapper(name="dmn-context")
    private Map<String, Object> dmnContext = new HashMap<>();
    
    public DMNContextKS() {
        // no-arg constructor for marshalling
    }
    
    public DMNContextKS(Map<String, Object> dmnContext) {
        this.dmnContext.putAll( dmnContext );
    }
    
    public DMNContextKS(String namespace, String modelName, Map<String, Object> dmnContext) {
        this(dmnContext);
        this.namespace = namespace;
        this.modelName = modelName;
    }
    
    public DMNContextKS(String namespace, String modelName, String decisionName, Map<String, Object> dmnContext) {
        this(namespace, modelName, dmnContext);
        this.decisionName = decisionName;
    }

    
    public String getNamespace() {
        return namespace;
    }

    
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    
    public String getModelName() {
        return modelName;
    }

    
    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    
    public String getDecisionName() {
        return decisionName;
    }

    
    public void setDecisionName(String decisionName) {
        this.decisionName = decisionName;
    }

    
    public Map<String, Object> getDmnContext() {
        return dmnContext;
    }

    
    public void setDmnContext(Map<String, Object> dmnContext) {
        this.dmnContext = dmnContext;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("DMNContextKS [namespace=").append(namespace).append(", modelName=").append(modelName).append(", decisionName=").append(decisionName).append(", dmnContext=").append(dmnContext).append("]");
        return builder.toString();
    }
    
    
}
