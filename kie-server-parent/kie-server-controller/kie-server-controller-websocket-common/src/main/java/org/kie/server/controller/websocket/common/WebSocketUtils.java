/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.controller.websocket.common;

import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.model.Wrapped;

public class WebSocketUtils {

    private static Marshaller jsonMarshaller = MarshallerFactory.getMarshaller(null, MarshallingFormat.JSON, WebSocketUtils.class.getClassLoader());

    @SuppressWarnings("unchecked")
    public static <T> T unmarshal(String data, Class<T> unmarshalType) {
        if (data == null || data.isEmpty()) {
            return null;
        }

        Object instance = jsonMarshaller.unmarshall(data, unmarshalType);

        if (instance instanceof Wrapped) {
            return (T) ((Wrapped<?>) instance).unwrap();
        }

        return (T) instance;
    }

    public static String marshal(Object entity) {
        return jsonMarshaller.marshall(entity);
    }
    
}
