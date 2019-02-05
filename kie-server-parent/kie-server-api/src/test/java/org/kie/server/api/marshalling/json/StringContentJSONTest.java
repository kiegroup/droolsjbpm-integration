/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.api.marshalling.json;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.marshalling.MarshallingFormat;

import static org.junit.Assert.assertEquals;

public class StringContentJSONTest {

    @Test
    public void testStringContentMap() {
        Set<Class<?>> extraClasses = new HashSet<Class<?>>();
        Marshaller marshaller = MarshallerFactory.getMarshaller(extraClasses, MarshallingFormat.JSON, this.getClass().getClassLoader());
        String jsonMap = "{\"name\" : \"value\"}";
        StringContentMap map = new StringContentMap(jsonMap);

        // content must be of exact value as given in constructor
        String marshall = marshaller.marshall(map);
        assertEquals(jsonMap, marshall);
    }

    @Test
    public void testStringContentCaseFile() {
        Set<Class<?>> extraClasses = new HashSet<Class<?>>();
        Marshaller marshaller = MarshallerFactory.getMarshaller(extraClasses, MarshallingFormat.JSON, this.getClass().getClassLoader());
        String jsonMap = "{\n" +
                "  \"case-data\" : {\n" +
                "    \"yearsOfService\" : 1\n" +
                "  },\n" +
                "  \"case-user-assignments\" : {\n" +
                "  },\n" +
                "  \"case-group-assignments\" : {\n" +
                " }\n" +
                "}";
        StringContentCaseFile map = new StringContentCaseFile(jsonMap);

        // content must be of exact value as given in constructor
        String marshall = marshaller.marshall(map);
        assertEquals(jsonMap, marshall);
    }
}
