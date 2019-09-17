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

    public default String marshall(Object input, Map<String, Object> parameters) {
        return marshall(input);
    }

    public String marshall(Object input);

    public <T> T unmarshall(String input, Class<T> type);

    public void dispose();

    public MarshallingFormat getFormat();

    public void setClassLoader(ClassLoader classloader);

    public ClassLoader getClassLoader();
}
