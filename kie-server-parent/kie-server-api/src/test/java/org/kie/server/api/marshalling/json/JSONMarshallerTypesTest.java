/*
 * Copyright 2024 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.api.marshalling.json;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;
import org.kie.server.api.marshalling.test.types.FreeFormItemType;
import org.kie.server.api.marshalling.test.types.ItemsType;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;

public class JSONMarshallerTypesTest {

    private Logger logger = LoggerFactory.getLogger(JSONMarshallerTypesTest.class);

    private static ItemsType createTestObject() {
        FreeFormItemType freeFormItemType = new FreeFormItemType();
        freeFormItemType.setItemValue("test");
        List<Serializable> standardItemsAndFreeformItems = new ArrayList<Serializable>();
        standardItemsAndFreeformItems.add(freeFormItemType);
        ItemsType itemsType = new ItemsType();
        itemsType.setStandardItemsAndFreeformItems(standardItemsAndFreeformItems);
        return itemsType;
    }
        
    @Test
    public void testMarshallingTypes() throws IOException {
        Reflections reflections = new Reflections("org.kie.server.api.marshalling.test.types", new SubTypesScanner(false));
        Set<Class<?>> clazzes = reflections.getSubTypesOf(Object.class).stream().collect(Collectors.toSet());

        JSONMarshaller marshaller = new JSONMarshaller(clazzes, JSONMarshallerTypesTest.class.getClassLoader());
        String rawContent = "{\n"
            + "  \"org.kie.server.api.marshalling.test.types.ItemsType\" : {\n"
            + "    \"standardItemsAndFreeformItems\" : [ {\n"
            + "      \"FreeFormItemType\" : {\n"
            + "        \"itemValue\" : \"test\"\n"
            + "      }\n"
            + "    } ]\n"
            + "  }\n"
            + "}";
        String marshalledContent = marshaller.marshall(createTestObject());
        assertEquals(rawContent, marshalledContent);
    }

}
