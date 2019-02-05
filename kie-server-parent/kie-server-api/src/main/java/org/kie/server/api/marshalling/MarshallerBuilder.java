/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.api.marshalling;

import java.util.Set;

/**
 * Allows plugable builders for various marshallers so out of the box functionality can be enhanced
 */
public interface MarshallerBuilder {

    /**
     * Based on given parameters builds marshaller instance that matches given format (MarshallingFormat)
     * @param classes optional set of custom classes - can be null
     * @param format expected type of the marshaller
     * @param classLoader class loader to be used for this marshaller
     * @return new instance of marshaller
     */
    Marshaller build(Set<Class<?>> classes, MarshallingFormat format, ClassLoader classLoader);
}
