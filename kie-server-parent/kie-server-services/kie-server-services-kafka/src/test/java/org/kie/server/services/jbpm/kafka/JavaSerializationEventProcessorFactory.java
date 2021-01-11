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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;

import org.kie.api.runtime.process.ProcessInstance;

public class JavaSerializationEventProcessorFactory implements KafkaEventProcessorFactory, KafkaEventWriter {

    @Override
    public KafkaEventReader getEventReader(String topic, ClassLoader cl) {
        return new KafkaEventReader() {
            @Override
            public <T> T readEvent(byte[] value, Class<T> valueClazz) throws IOException, ClassNotFoundException {
                try (ByteArrayInputStream input = new ByteArrayInputStream(value);
                        ObjectInputStream stream = new ObjectInputStream(input) {
                            @Override
                            public Class<?> resolveClass(ObjectStreamClass desc) throws ClassNotFoundException {
                                return Class.forName(desc.getName(), true, cl);
                            }
                        }) {
                    return valueClazz.cast(stream.readObject());
                }
            }
        };
    }

    @Override
    public KafkaEventWriter getEventWriter(String topic) {
        return this;
    }

    @Override
    public byte[] writeEvent(ProcessInstance processInstance, Object value) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
                ObjectOutputStream stream = new ObjectOutputStream(out)) {
            stream.writeObject(value);
            stream.flush();
            return out.toByteArray();
        }
    }
}
