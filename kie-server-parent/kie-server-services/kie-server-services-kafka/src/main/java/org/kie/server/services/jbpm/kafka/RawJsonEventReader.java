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

import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.marshalling.MarshallingFormat;


public class RawJsonEventReader implements KafkaEventReader {

    private Marshaller marshaller;

    public RawJsonEventReader(ClassLoader cl) {
        marshaller = MarshallerFactory.getMarshaller(MarshallingFormat.JSON, cl);
    }

    @Override
    public <T> T readEvent(byte[] value, Class<T> valueType) {
        return marshaller.unmarshall(value, valueType);
    }
}
