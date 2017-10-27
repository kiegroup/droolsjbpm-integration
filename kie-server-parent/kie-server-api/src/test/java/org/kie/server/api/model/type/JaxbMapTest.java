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

package org.kie.server.api.model.type;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

public class JaxbMapTest {

    private static final String KEY = "variable1";
    private static final Object VALUE = new Date();

    @Test
    public void testImmutableMap() {
        Map<String, Object> entries = Collections.singletonMap(KEY, VALUE);
        // used to throw UnsupportedOperationException here
        JaxbMap jaxbMap = new JaxbMap(entries);
        assertThat(jaxbMap.getEntries()).containsKey(KEY);
    }

    @Test
    public void testEntryNotModified() {
        Map<String, Object> entries = new HashMap<>();
        entries.put(KEY, VALUE);
        JaxbMap jaxbMap = new JaxbMap(entries);
        assertThat(jaxbMap.getEntries()).containsKey(KEY);
        assertThat(entries).containsEntry(KEY, VALUE);
    }

}
