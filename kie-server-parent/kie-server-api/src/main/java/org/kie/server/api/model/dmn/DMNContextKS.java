/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.api.model.dmn;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;
import org.drools.core.xml.jaxb.util.JaxbUnknownAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "dmn-evaluation-context")
@XStreamAlias("dmn-evaluation-context")
public class DMNContextKS {

    @XmlElement(name = "model-namespace")
    @XStreamAlias("model-namespace")
    private String namespace;

    @XmlElement(name = "model-name")
    @XStreamAlias("model-name")
    private String modelName;

    @XmlElement(name = "decision-name")
    @XStreamImplicit(itemFieldName = "decision-name")
    @JsonFormat(with = {JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY, JsonFormat.Feature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED})
    private List<String> decisionNames = new ArrayList<>();

    @XmlElement(name = "decision-id")
    @XStreamImplicit(itemFieldName = "decision-id")
    @JsonFormat(with = {JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY, JsonFormat.Feature.WRITE_SINGLE_ELEM_ARRAYS_UNWRAPPED})
    private List<String> decisionIds = new ArrayList<>();

    @XmlElement(name = "decision-service-name")
    @XStreamAlias("decision-service-name")
    private String decisionServiceName;

    @XmlElement(name = "dmn-context")
    @XStreamAlias("dmn-context")
    @XmlJavaTypeAdapter(JaxbUnknownAdapter.class)
    private Map<String, Object> dmnContext = new HashMap<>();

    public DMNContextKS() {
        // no-arg constructor for marshalling
    }

    public DMNContextKS(Map<String, Object> dmnContext) {
        this.dmnContext.putAll(dmnContext);
    }

    public DMNContextKS(String namespace, String modelName, Map<String, Object> dmnContext) {
        this(dmnContext);
        this.namespace = namespace;
        this.modelName = modelName;
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

    public List<String> getDecisionNames() {
        return decisionNames;
    }

    public void setDecisionNames(List<String> decisionName) {
        this.decisionNames = decisionName;
    }

    public List<String> getDecisionIds() {
        return decisionIds;
    }

    public void setDecisionIds(List<String> decisionId) {
        this.decisionIds = decisionId;
    }

    public Map<String, Object> getDmnContext() {
        return dmnContext;
    }

    public void setDmnContext(Map<String, Object> dmnContext) {
        this.dmnContext = dmnContext;
    }

    @Override
    public String toString() {
        return new StringBuilder("DMNContextKS [")
                .append("namespace=").append(namespace)
                .append(", modelName=").append(modelName)
                .append(", decisionServiceName=").append(decisionServiceName)
                .append(", decisionNames=").append(decisionNames)
                .append(", decisionIds=").append(decisionIds)
                .append(", dmnContext=").append(dmnContext)
                .append("]").toString();
    }

    public String getDecisionServiceName() {
        return decisionServiceName;
    }

    public void setDecisionServiceName(String decisionServiceName) {
        this.decisionServiceName = decisionServiceName;
    }
}
