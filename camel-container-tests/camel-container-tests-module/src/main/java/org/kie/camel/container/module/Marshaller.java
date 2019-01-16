/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.camel.container.module;

import java.util.HashSet;

import com.thoughtworks.xstream.security.AnyTypePermission;
import com.thoughtworks.xstream.security.NoTypePermission;
import org.kie.server.api.marshalling.xstream.XStreamMarshaller;

public class Marshaller {

    private final XStreamMarshaller xStreamMarshaller;

    public Marshaller() {
        xStreamMarshaller = new XStreamMarshaller(new HashSet<>(), Marshaller.class.getClassLoader());
        xStreamMarshaller.getXstream().addPermission(NoTypePermission.NONE);
        xStreamMarshaller.getXstream().addPermission(AnyTypePermission.ANY);
    }

    public String marshall(Object input) {
        return xStreamMarshaller.marshall(input);
    }

    public Object unmarshall(String input) {
        return xStreamMarshaller.unmarshall(input, Object.class);
    }
}
