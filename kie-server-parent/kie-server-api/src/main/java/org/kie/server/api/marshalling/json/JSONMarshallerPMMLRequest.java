/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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
package org.kie.server.api.marshalling.json;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.kie.api.pmml.PMMLRequestData;
import org.kie.api.pmml.ParameterInfo;


public class JSONMarshallerPMMLRequest implements JSONMarshallerExtension {

    private static final PMMLRequestDeserializer DESERIALIZER = new PMMLRequestDeserializer();
    private static final PMMLRequestSerializer SERIALIZER = new PMMLRequestSerializer();
    private static final String CORRELATION_ID = "correlationId";
    private static final String MODEL_NAME = "modelName";
    private static final String SOURCE = "source";

    @Override
    public void extend(JSONMarshaller marshaller, ObjectMapper serializer, ObjectMapper deserializer) {
        registerModule(serializer);
        registerModule(deserializer);
    }

    private void registerModule(ObjectMapper objectMapper) {
        SimpleModule pmmlRequestModule = new SimpleModule("pmml-module", Version.unknownVersion());
        pmmlRequestModule.addDeserializer(PMMLRequestData.class, DESERIALIZER);
        pmmlRequestModule.addSerializer(PMMLRequestData.class, SERIALIZER);
        objectMapper.registerModule(pmmlRequestModule);
    }

    private static class PMMLRequestDeserializer extends JsonDeserializer<PMMLRequestData> {

        @Override
        public PMMLRequestData deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            PMMLRequestData request = new PMMLRequestData();
            if (p.isExpectedStartObjectToken()) {
                JsonToken token;
                String fldName = null;
                String fldVal = null;
                do {
                    token = p.nextToken();
                    switch (token) {
                        case FIELD_NAME:
                            fldName = p.getText();
                            break;
                        case VALUE_STRING:
                            fldVal = p.getText();
                            break;
                    }
                    if (fldName != null && fldVal != null) {
                        if (fldName.equals(CORRELATION_ID)) {
                            request.setCorrelationId(fldVal);
                        } else if (fldName.equals(MODEL_NAME)) {
                            request.setModelName(fldVal);
                        } else if (fldName.equals(SOURCE)) {
                            request.setSource(fldVal);
                        }
                        fldName = null;
                        fldVal = null;
                    }
                } while (!p.isExpectedStartArrayToken());
            }
            if (p.isExpectedStartArrayToken()) {
                p.nextToken();
                while (p.isExpectedStartObjectToken()) {
                    Map<String, String> values = new HashMap<>();
                    JsonToken tok;
                    String fieldName = null;
                    String fieldVal = null;
                    do {
                        tok = p.nextToken();
                        switch (tok) {
                            case FIELD_NAME:
                                fieldName = p.getText();
                                break;
                            case VALUE_STRING:
                                fieldVal = p.getText();
                                break;
                        }
                        if (fieldName != null && fieldVal != null) {
                            values.put(fieldName, fieldVal);
                            fieldName = null;
                            fieldVal = null;
                        }
                    } while (tok != JsonToken.END_OBJECT);
                    p.nextToken();
                    values.entrySet().forEach(e -> {
                        System.out.println(e.getKey() + ": " + e.getValue());
                    });
                    String pitype = values.get("type");
                    if (pitype != null && !pitype.trim().isEmpty()) {
                        if (pitype.equals(String.class.getName())) {
                            ParameterInfo<String> pi = new ParameterInfo<>(values.get("correlationId"), values.get("name"), String.class, (String) values.get("value"));
                            request.addRequestParam(pi);
                        } else if (pitype.equals(Integer.class.getName())) {
                            ParameterInfo<Integer> pi = new ParameterInfo<>();
                            pi.setCorrelationId(values.get("correlationId"));
                            pi.setName(values.get("name"));
                            pi.setType(Integer.class);
                            pi.setValue(Integer.valueOf(values.get("value")));
                            request.addRequestParam(pi);
                        } else if (pitype.equals(Long.class.getName())) {
                            ParameterInfo<Long> pi = new ParameterInfo<>();
                            pi.setCorrelationId(values.get("correlationId"));
                            pi.setName(values.get("name"));
                            pi.setType(Long.class);
                            pi.setValue(Long.valueOf(values.get("value")));
                            request.addRequestParam(pi);
                        } else if (pitype.equals(Float.class.getName()) || pitype.equals(Double.class.getName())) {
                            ParameterInfo<Double> pi = new ParameterInfo<>();
                            pi.setCorrelationId(values.get("correlationId"));
                            pi.setName(values.get("name"));
                            pi.setType(Double.class);
                            pi.setValue(Double.valueOf(values.get("value")));
                            request.addRequestParam(pi);
                        }
                    }
                }
            }
            return request;
        }

    }

    private static class PMMLRequestSerializer extends JsonSerializer<PMMLRequestData> {

        @Override
        public void serialize(PMMLRequestData value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeStartObject(value);
            gen.writeFieldName("correlationId");
            gen.writeString(value.getCorrelationId());
            gen.writeFieldName("modelName");
            gen.writeString(value.getModelName());
            gen.writeFieldName("source");
            gen.writeString(value.getSource());
            Collection<ParameterInfo> params = value.getRequestParams();
            if (params != null && !params.isEmpty()) {
                gen.writeFieldName("requestParams");
                gen.writeStartArray(params.size());
                for (ParameterInfo<?> p : params) {
                    gen.writeStartObject(p);
                    serializers.defaultSerializeValue(p, gen);
                    gen.writeEndObject();
                }
                gen.writeEndArray();
            }
            gen.writeEndObject();
        }

    }

}
