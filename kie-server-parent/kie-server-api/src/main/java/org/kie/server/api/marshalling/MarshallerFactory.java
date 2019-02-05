/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MarshallerFactory {

    private static final Logger logger = LoggerFactory.getLogger(MarshallerFactory.class);

    private static MarshallerBuilder builder = getMarshallerBuilder();

    /**
     * Builds new marshaller for given format and class loader
     * @param format marshaller format that marshaller should be built for
     * @param classLoader classloader to be used by the marshaller
     * @return new instance of the marshaller
     */
    public static Marshaller getMarshaller(MarshallingFormat format, ClassLoader classLoader) {
        return getMarshaller(null, format, classLoader);
    }

    /**
     * Builds new marshaller for given format and class loader
     * @param classes optional custom classes to be added to marshaller - might be null
     * @param format marshaller format that marshaller should be built for
     * @param classLoader classloader to be used by the marshaller
     * @return new instance of the marshaller
     */
    public static Marshaller getMarshaller(Set<Class<?>> classes, MarshallingFormat format, ClassLoader classLoader) {
        return builder.build(classes, format, classLoader);
    }

    /*
     * Helper methods
     */

    private static synchronized MarshallerBuilder getMarshallerBuilder() {
        ServiceLoader<MarshallerBuilder> builders = ServiceLoader.load(MarshallerBuilder.class);
        Iterator<MarshallerBuilder> it = builders.iterator();

        if (it.hasNext()) {
            MarshallerBuilder marshallerBuilder = it.next();
            logger.debug("Found custom marshaller builder {} that is going to be used instead of the default", marshallerBuilder);
            return marshallerBuilder;
        }

        return new BaseMarshallerBuilder();
    }
}
