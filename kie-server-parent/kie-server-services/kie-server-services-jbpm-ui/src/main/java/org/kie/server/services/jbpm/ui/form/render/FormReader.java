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

package org.kie.server.services.jbpm.ui.form.render;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.Map;

import org.kie.server.services.jbpm.ui.form.render.model.FormInstance;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class FormReader {

    private final ObjectMapper mapper;
    
    
    public FormReader() {
        this.mapper = new ObjectMapper();
        this.mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
    
    public FormInstance readFromString(String formStructure) {
        if (formStructure == null) {
            return null;
        }
        try {
            return this.mapper.readValue(formStructure, FormInstance.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public FormInstance readFromStream(InputStream formStructure) {
        
        try {
            return this.mapper.readValue(formStructure, FormInstance.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    public Map<String, Object> extractValues(Object data) {
        if (data == null) {
            return Collections.emptyMap();
        }
        return mapper.convertValue(data, Map.class);
    }
}
