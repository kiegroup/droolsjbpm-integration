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
import java.io.InputStream;
import java.util.Set;
import java.util.stream.Collectors;

import org.drools.core.util.IoUtils;
import org.junit.Test;
import org.kie.server.api.marshalling.test.model.Fact;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class JSONMarshallerLargeTest {

    private Logger logger = LoggerFactory.getLogger(JSONMarshallerLargeTest.class);

    @Test(timeout = 5000L)
    public void testLargeNumberOfClasses() throws IOException {
        try (InputStream is = JSONMarshallerLargeTest.class.getClassLoader().getResourceAsStream("complex_payload.json")) {
            String content = new String(IoUtils.readBytesFromInputStream(is));

            Reflections reflections = new Reflections("org.kie.server.api.marshalling.test.model", new SubTypesScanner(false));
            Set<Class<?>> clazzes = reflections.getSubTypesOf(Object.class).stream().collect(Collectors.toSet());
            clazzes.remove(Fact.class);

            JSONMarshaller marshaller = new JSONMarshaller(clazzes, JSONMarshallerLargeTest.class.getClassLoader());
            Fact object = marshaller.unmarshall(content, Fact.class);
            assertThat(object).isNotNull();
            logger.info("object captured {}", object);
        }
    }

}
