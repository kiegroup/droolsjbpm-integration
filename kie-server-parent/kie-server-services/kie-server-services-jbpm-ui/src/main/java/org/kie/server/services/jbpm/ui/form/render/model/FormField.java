/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.services.jbpm.ui.form.render.model;

import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FormField {

    private String id;
    private String name;
    
    private Set<String> tags;
    
    private String code;
    private String label;
    @JsonProperty("standaloneClassName")
    private String type;

    private String binding;

    private String placeHolder;
    private long maxLength;
    private boolean required;
    @JsonProperty("readOnly")
    private boolean readOnly;


    private List<ItemOption> options;

    @JsonProperty("listOfValues")
    private List<String> listOfValues;

    private Number min;

    private Number max;

    private Number precision;

    private Number step;

    // subform related
    private String nestedForm;

    // multi subform related
    @JsonProperty("columnMetas")
    private List<TableInfo> tableInfo;

    private String creationForm;
    private String editionForm;

    private boolean showTime;

    public boolean isShowTime() {
        return showTime;
    }

    public void setShowTime(boolean showTime) {
        this.showTime = showTime;
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

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getBinding() {
        return binding;
    }

    public void setBinding(String binding) {
        this.binding = binding;
    }

    public String getPlaceHolder() {
        return placeHolder;
    }

    public void setPlaceHolder(String placeHolder) {
        this.placeHolder = placeHolder;
    }

    public long getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(long maxLeangth) {
        this.maxLength = maxLeangth;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public String getNestedForm() {
        return nestedForm;
    }

    public void setNestedForm(String nestedForm) {
        this.nestedForm = nestedForm;
    }

    public List<ItemOption> getOptions() {
        return options;
    }

    public void setOptions(List<ItemOption> options) {
        this.options = options;
    }

    public List<TableInfo> getTableInfo() {
        return tableInfo;
    }

    public void setTableInfo(List<TableInfo> tableInfo) {
        this.tableInfo = tableInfo;
    }

    public String getCreationForm() {
        return creationForm;
    }

    public void setCreationForm(String creationForm) {
        this.creationForm = creationForm;
    }

    public String getEditionForm() {
        return editionForm;
    }

    public void setEditionForm(String editionForm) {
        this.editionForm = editionForm;
    }

    public Number getMin() {
        return min;
    }

    public void setMin(Number min) {
        this.min = min;
    }

    public Number getMax() {
        return max;
    }

    public void setMax(Number max) {
        this.max = max;
    }

    public Number getPrecision() {
        return precision;
    }

    public void setPrecision(Number precision) {
        this.precision = precision;
    }

    public Number getStep() {
        return step;
    }

    public void setStep(Number step) {
        this.step = step;
    }

    public List<String> getListOfValues() {
        return listOfValues;
    }

    public void setListOfValues(List<String> listOfValues) {
        this.listOfValues = listOfValues;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public Set<String> getTags() {
        return tags;
    }

}
