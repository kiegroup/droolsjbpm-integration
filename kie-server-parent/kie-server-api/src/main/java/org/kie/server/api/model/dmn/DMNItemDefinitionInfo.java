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
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "dmn-itemdefinition-info")
@XStreamAlias("dmn-itemdefinition-info")
public class DMNItemDefinitionInfo {

    @XmlElement(name = "itemdefinition-id")
    @XStreamAlias("itemdefinition-id")
    private String id;

    @XmlElement(name = "itemdefinition-name")
    @XStreamAlias("itemdefinition-name")
    private String name;

    @XmlElement(name = "itemdefinition-typeRef")
    @XStreamAlias("itemdefinition-typeRef")
    private DMNQNameInfo typeRef;

    @XmlElement(name = "itemdefinition-allowedValues")
    @XStreamAlias("itemdefinition-allowedValues")
    @JsonInclude(Include.NON_NULL)
    private DMNUnaryTestsInfo allowedValues;

    // note Jackson annotation is needed on this field and getter, in order for Jackson to NOT use JAXB annotation but proper Jackson annotation
    @XmlElementWrapper(name = "itemdefinition-itemComponent")
    @XmlElement(name = "dmn-itemdefinition-info")
    @JsonIgnore
    @XStreamAlias("itemdefinition-itemComponent")
    private List<DMNItemDefinitionInfo> itemComponent = new ArrayList<>();

    @XmlElement(name = "itemdefinition-typeLanguage")
    @XStreamAlias("itemdefinition-typeLanguage")
    @JsonInclude(Include.NON_NULL)
    private String typeLanguage;

    @XmlElement(name = "itemdefinition-isCollection")
    @XStreamAlias("itemdefinition-isCollection")
    private Boolean isCollection;

    public DMNItemDefinitionInfo() {
        // To avoid the need for kie-server-api to depend on kie-dmn-backend, in order to access DMN's Definitions and DMN's inputdata element
        // build this as DTO and only on server-side leverage setters to populate data as needed.
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

    public DMNQNameInfo getTypeRef() {
        return typeRef;
    }

    public void setTypeRef(DMNQNameInfo typeRef) {
        this.typeRef = typeRef;
    }

    public DMNUnaryTestsInfo getAllowedValues() {
        return allowedValues;
    }

    public void setAllowedValues(DMNUnaryTestsInfo allowedValues) {
        this.allowedValues = allowedValues;
    }

    @JsonProperty("itemdefinition-itemComponent")
    public List<DMNItemDefinitionInfo> getItemComponent() {
        return itemComponent;
    }

    public void setItemComponent(List<DMNItemDefinitionInfo> itemComponent) {
        this.itemComponent = itemComponent;
    }

    public String getTypeLanguage() {
        return typeLanguage;
    }

    public void setTypeLanguage(String typeLanguage) {
        this.typeLanguage = typeLanguage;
    }

    public Boolean getIsCollection() {
        return isCollection;
    }

    public void setIsCollection(Boolean isCollection) {
        this.isCollection = isCollection;
    }
}
