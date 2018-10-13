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

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;


public class LayoutItemDeserializer extends JsonDeserializer<LayoutItem> {


    @Override
    public LayoutItem deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        ObjectCodec oc = p.getCodec();
        JsonNode node = oc.readTree(p);
        
        JsonNode propertiesNode = node.get("properties");
        
        LayoutItem item = new LayoutItem();
        
        if (propertiesNode.has("field_id")) {
            item.setFieldId(propertiesNode.get("field_id").asText());
            item.setFormId(propertiesNode.get("form_id").asText());
        } else if (propertiesNode.has("HTML_CODE")) {
            item.setValue(propertiesNode.get("HTML_CODE").asText());
        }
        return item;
    }

}
