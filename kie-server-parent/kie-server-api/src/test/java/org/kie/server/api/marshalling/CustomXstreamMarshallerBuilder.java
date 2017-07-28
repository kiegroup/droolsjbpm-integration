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

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.reflection.PureJavaReflectionProvider;
import com.thoughtworks.xstream.io.xml.DomDriver;
import com.thoughtworks.xstream.io.xml.XmlFriendlyNameCoder;
import org.kie.server.api.marshalling.xstream.XStreamMarshaller;

public class CustomXstreamMarshallerBuilder extends BaseMarshallerBuilder {

    @Override
    public Marshaller build(Set<Class<?>> classes, MarshallingFormat format, ClassLoader classLoader) {

        if (format.equals(MarshallingFormat.XSTREAM)) {

            return new XStreamMarshaller(classes, classLoader) {
                @Override
                protected void buildMarshaller(Set<Class<?>> classes, ClassLoader classLoader) {
                    xstream = new XStream(new PureJavaReflectionProvider(), new DomDriver("UTF-8", new XmlFriendlyNameCoder("_-", "_")));
                    String[] voidDeny = {"void.class", "Void.class"};
                    xstream.denyTypes(voidDeny);
                }
            };
        }

        return super.build(classes, format, classLoader);
    }
}
