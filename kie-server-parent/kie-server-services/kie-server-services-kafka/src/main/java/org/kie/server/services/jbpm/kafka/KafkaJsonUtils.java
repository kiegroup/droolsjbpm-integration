/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

import java.text.SimpleDateFormat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import static org.kie.server.services.jbpm.kafka.KafkaServerUtils.KAFKA_EXTENSION_PREFIX;

class KafkaJsonUtils {
    
    private KafkaJsonUtils() {}

    public static final ObjectMapper mapper = new ObjectMapper()
            .setDateFormat(new SimpleDateFormat(System.getProperty(
                    KAFKA_EXTENSION_PREFIX + "json.date_format", System.getProperty(
                            "org.kie.server.json.date_format",
                            "yyyy-MM-dd'T'HH:mm:ss.SSSZ"))))
            .configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
}
