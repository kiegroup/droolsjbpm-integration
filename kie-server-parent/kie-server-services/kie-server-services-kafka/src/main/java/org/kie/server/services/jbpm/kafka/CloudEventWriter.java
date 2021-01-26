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

import java.util.Date;
import java.util.UUID;

import org.kie.api.runtime.process.ProcessInstance;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.json.JSONMarshaller;

class CloudEventWriter implements KafkaEventWriter {

    private static final String SOURCE_FORMATTER = "/process/%s/%s";

    private Marshaller marshaller = new JSONMarshaller(true);

    public CloudEventWriter() {
        // need public constructor 
    }

    @Override
    public byte[] writeEvent(ProcessInstance processInstance,
                             Object value) {
        CloudEvent<Object> event = new CloudEvent<>();
        event.setType(value != null ? value.getClass().getTypeName() : "empty");
        event.setSource(String.format(SOURCE_FORMATTER, processInstance.getProcessId(), processInstance.getId()));
        event.setSpecversion("1.0");
        event.setTime(new Date());
        event.setId(UUID.randomUUID().toString());
        event.setData(value);
        return marshaller.marshallAsBytes(event);
    }
}
