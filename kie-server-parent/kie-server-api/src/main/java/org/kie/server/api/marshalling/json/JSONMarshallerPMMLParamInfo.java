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

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.kie.api.pmml.ParameterInfo;


public class JSONMarshallerPMMLParamInfo implements JSONMarshallerExtension {

    private static final PMMLParamDeserializer DESERIALIZER = new PMMLParamDeserializer();
    private static final PMMLParamSerializer SERIALIZER = new PMMLParamSerializer();

    @Override
    public void extend(JSONMarshaller marshaller, ObjectMapper serializer, ObjectMapper deserializer) {
        registerModule(serializer);
        registerModule(deserializer);
    }

    private void registerModule(ObjectMapper objectMapper) {
        SimpleModule paramInfoModule = new SimpleModule("pmml-param-info-module", Version.unknownVersion());
        paramInfoModule.addDeserializer(ParameterInfo.class, DESERIALIZER);
        paramInfoModule.addSerializer(ParameterInfo.class, SERIALIZER);
        objectMapper.registerModule(paramInfoModule);
    }


    private static class PMMLParamDeserializer extends JsonDeserializer<ParameterInfo> {

        @Override
        public ParameterInfo deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            ParameterInfo pi = new ParameterInfo<>();
            String fldName = p.nextFieldName();
            String value = p.nextTextValue();
            return pi;
        }

    }

    private static class PMMLParamSerializer extends JsonSerializer<ParameterInfo> {

        @Override
        public void serialize(ParameterInfo value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
            gen.writeFieldName("correlationId");
            gen.writeString(value.getCorrelationId());
            gen.writeFieldName("name");
            gen.writeString(value.getName());
            gen.writeFieldName("type");
            gen.writeString(value.getType().getName());
            gen.writeFieldName("value");
            gen.writeString(value.getValue().toString());
        }

    }
}
