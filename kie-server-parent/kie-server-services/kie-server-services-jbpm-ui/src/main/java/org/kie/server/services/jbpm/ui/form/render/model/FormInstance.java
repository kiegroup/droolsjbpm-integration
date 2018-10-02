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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import com.fasterxml.jackson.annotation.JsonProperty;

public class FormInstance {

    private String id;
    private String name;

    private FormModel model;

    private List<FormField> fields;

    @JsonProperty("layoutTemplate")
    private FormLayout layout;
    
    private Map<String, FormInstance> nestedForms = new HashMap<>();
    private Function<String, FormInstance> nestedFormsLookup = formId -> null;

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

    public List<FormField> getFields() {
        return fields;
    }

    public void setFields(List<FormField> fields) {
        this.fields = fields;
    }

    public FormLayout getLayout() {
        return layout;
    }

    public void setLayout(FormLayout layout) {
        this.layout = layout;
    }

    public FormField getField(String fieldId) {
        return fields.stream().filter(f -> f.getId().equals(fieldId)).findFirst().get();
    }

    public FormModel getModel() {
        return model;
    }

    public void setModel(FormModel model) {
        this.model = model;
    }
    
    public void addNestedForm(FormInstance nestedForm) {
        this.nestedForms.put(nestedForm.getId(), nestedForm);
    }
    
    public FormInstance getNestedForm(String formId) {
        FormInstance nestedForm = this.nestedForms.get(formId);
        if (nestedForm != null) {
            return nestedForm;
        }
        
        return nestedFormsLookup.apply(formId);
    }

    public void setNestedFormsLookup(Function<String, FormInstance> nestedFormsLookup) {
        this.nestedFormsLookup = nestedFormsLookup;
    }
}
