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

/**
 * Implements the logic to convert the byte[] contained in the value of a Kafka Record to a java object
 */
public interface KafkaEventReader {

    /**
     * Converts a byte[] to an object
     * @param <T> object type
     * @param value byte[] to be converted to object
     * @param valueClazz the class of the result object
     * @return object of specified type 
     * @throws IOException if any input output exception class
     * @throws ClassNotFoundException if any class contained in the object being built cannot be found
     */
    <T> T readEvent(byte[] value, Class<T> valueClazz) throws IOException, ClassNotFoundException;
}
