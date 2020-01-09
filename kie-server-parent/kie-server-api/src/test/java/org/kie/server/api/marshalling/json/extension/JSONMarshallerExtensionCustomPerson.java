/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.api.marshalling.json.extension;

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
import org.kie.server.api.marshalling.json.JSONMarshaller;
import org.kie.server.api.marshalling.json.JSONMarshallerExtension;
import org.kie.server.api.marshalling.objects.CustomPerson;

public class JSONMarshallerExtensionCustomPerson implements JSONMarshallerExtension {

    private static final CustomPersonDeser DESERIALIZER = new CustomPersonDeser();
    private static final CustomPersonSer SERIALIZER = new CustomPersonSer();

    @Override
    public void extend(JSONMarshaller marshaller, ObjectMapper serializer, ObjectMapper deserializer) {
        registerModule(serializer);
        registerModule(deserializer);
    }

    private void registerModule(ObjectMapper objectMapper) {
        SimpleModule CustomPersonModule = new SimpleModule("custom-person-module", Version.unknownVersion());
        CustomPersonModule.addDeserializer(CustomPerson.class, DESERIALIZER);
        CustomPersonModule.addSerializer(CustomPerson.class, SERIALIZER);
        objectMapper.registerModule(CustomPersonModule);
    }

    private static class CustomPersonDeser extends JsonDeserializer<CustomPerson> {

        @Override
        public CustomPerson deserialize(JsonParser jp, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            // Regardless of the payload, create a CustomPerson with age=50
            CustomPerson customPerson = new CustomPerson("John", 50);
            return customPerson;
        }
    }

    private static class CustomPersonSer extends JsonSerializer<CustomPerson> {

        @Override
        public void serialize(CustomPerson value, JsonGenerator jgen, SerializerProvider provider) throws IOException, JsonProcessingException {
            // Regardless of the Object, write a constant String
            jgen.writeRaw("{ \"name\" : \"John is CustomPerson\",  \"age\" : 20 }");
        }
    }
}
