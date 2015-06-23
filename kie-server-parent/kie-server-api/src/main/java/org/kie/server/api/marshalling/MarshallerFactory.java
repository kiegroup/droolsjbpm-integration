/*
 * Copyright 2015 JBoss Inc
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

import org.kie.server.api.marshalling.jaxb.JaxbMarshaller;
import org.kie.server.api.marshalling.json.JSONMarshaller;
import org.kie.server.api.marshalling.xstream.XStreamMarshaller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MarshallerFactory {

    private static final Logger logger = LoggerFactory.getLogger( MarshallerFactory.class );

    public static Marshaller getMarshaller(MarshallingFormat format, ClassLoader classLoader) {
        return getMarshaller(null, format, classLoader);
    }

    public static Marshaller getMarshaller(Set<Class<?>> classes, MarshallingFormat format, ClassLoader classLoader) {
        switch ( format ) {
            case XSTREAM:
                return new XStreamMarshaller( classLoader );
            case JAXB:
                return new JaxbMarshaller(classes, classLoader); // has to be implemented
            case JSON:
                return new JSONMarshaller(classes, classLoader); // has to be implemented
            default:
                logger.error( "Unsupported marshalling format: " + format );
        }
        return null;
    }
}
