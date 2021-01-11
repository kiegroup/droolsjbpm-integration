/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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
package org.kie.server.services.jbpm.kafka;

import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;

import com.fasterxml.jackson.databind.JsonNode;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.marshalling.MarshallingFormat;

import static org.kie.server.services.jbpm.kafka.KafkaJsonUtils.mapper;

class CloudEventReader implements KafkaEventReader {
    private ClassLoader cl;

    private Map<MarshallingFormat, Marshaller> marshallers = new EnumMap<>(MarshallingFormat.class);

    public CloudEventReader(ClassLoader cl) {
        this.cl = cl;
    }

    @Override
    public <T> T readEvent(byte[] value, Class<T> valueType) throws IOException {
        JsonNode node = mapper.readTree(value);
        MarshallingFormat contentType = MarshallingFormat.JSON;
        if (node.has("datacontenttype")) {
            contentType = MarshallingFormat.fromType(node.get("datacontenttype").asText());
        }
        if (node.has("data")) {
            Marshaller marshaller = marshallers.computeIfAbsent(contentType, c -> MarshallerFactory.getMarshaller(c,
                    cl));
            return marshaller.unmarshall(node.get("data").toString(), valueType);
        }
        throw new IOException("Missing data field in cloud event " + new String(value));
    }
}
