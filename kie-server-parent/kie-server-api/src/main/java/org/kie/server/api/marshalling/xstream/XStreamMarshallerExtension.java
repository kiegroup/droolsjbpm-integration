/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.api.marshalling.xstream;

import com.thoughtworks.xstream.mapper.MapperWrapper;

/**
 * Represents custom extension for the XStream marshaller.
 */
public interface XStreamMarshallerExtension {

    /**
     * Extends the provided marshaller, with e.g. custom converters.
     * @param xStreamMarshaller the marshaller which should be extended
     */
    void extend(XStreamMarshaller xStreamMarshaller);

    /**
     * Enables additional {@link MapperWrapper}s by chaining them to the provided one.
     * <p>
     * Example: {@code return new HibernateWrapper(next)}
     * - this will add the HibernateWrapper into the chain. Note that it is important to pass the {@code next}
     * MapperWrapper into the constructor to make sure the chain does not get broken
     * <p>
     * Default implementation: identity
     * @param next the next {@link MapperWrapper} in the chain
     */
    default MapperWrapper chainMapperWrapper(MapperWrapper next) {
        return next;
    }
}
