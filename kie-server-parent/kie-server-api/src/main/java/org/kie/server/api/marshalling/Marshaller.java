/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.api.marshalling;

import java.util.Map;

/**
 * These Marshallers implementations must be thread-safe
 */
public interface Marshaller {

    static String MARSHALLER_PARAMETER_STRICT = "strict";

    default String marshall(Object input, Map<String, Object> parameters) {
        return marshall(input);
    }

    String marshall(Object input);

    <T> T unmarshall(String input, Class<T> type);

    default <T> T unmarshall(String input, Class<T> type, Map<String, Object> parameters) {
        return unmarshall(input, type);
    }

    void dispose();

    MarshallingFormat getFormat();

    void setClassLoader(ClassLoader classloader);

    ClassLoader getClassLoader();

    default byte[] marshallAsBytes(Object input) {
        return marshall(input).getBytes();
    }

    default <T> T unmarshall(byte[] input, Class<T> type) {
        return unmarshall(new String(input), type);
    }
}
