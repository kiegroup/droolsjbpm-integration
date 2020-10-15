/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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
package org.kie.server.services.jbpm.kafka;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

class CloudEvent<T> {

    private static ObjectMapper mapper = new ObjectMapper()
            .setDateFormat(new SimpleDateFormat(System.getProperty(
                    KafkaServerExtension.KAFKA_EXTENSION_PREFIX + "json.date_format", System.getProperty(
                            "org.kie.server.json.date_format",
                            "yyyy-MM-dd'T'HH:mm:ss.SSSZ"))));

    private String specVersion;
    private Date time;
    private String id;
    private String type;
    private String source;
    private T data;

    public static <T> CloudEvent<T> read(byte[] bytes, Class<T> type) throws IOException, ParseException {
        JsonNode node = mapper.readTree(bytes);
        CloudEvent<T> cloudEvent = new CloudEvent<>();
        if (node.has("id")) {
            cloudEvent.id = node.get("id").asText();
        }
        if (node.has("source")) {
            cloudEvent.source = node.get("source").asText();
        }
        if (node.has("type")) {
            cloudEvent.type = node.get("type").asText();
        }
        if (node.has("specversion")) {
            cloudEvent.specVersion = node.get("specversion").asText();
        }
        if (node.has("time")) {
            cloudEvent.time = mapper.getDateFormat().parse(node.get("time").asText());
        }
        if (node.has("data")) {
            cloudEvent.data = mapper.treeToValue(node.get("data"), type);
        }
        return cloudEvent;
    }

    private CloudEvent() {}

    public String getSpecVersion() {
        return specVersion;
    }

    public Date getTime() {
        return time;
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getSource() {
        return source;
    }

    public T getData() {
        return data;
    }

    @Override
    public String toString() {
        return "CloudEvent [specversion=" + specVersion + ", time=" + time + ", id=" + id + ", type=" + type +
               ", source=" + source + ", data=" + data + "]";
    }
}
