/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.services.taskassigning.planning.util;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class PropertyUtilTest {

    private static final String TEST_PROPERTY = "TEST_PROPERTY";
    private static final Integer VALUE = 1234;
    private static final Integer DEFAULT_VALUE = 5678;

    @Before
    public void setUp() {
        System.clearProperty(TEST_PROPERTY);
    }

    @Test
    public void readySystemPropertyWithValueSetAndParser() {
        System.setProperty(TEST_PROPERTY, VALUE.toString());
        Integer result = PropertyUtil.readSystemProperty(TEST_PROPERTY, DEFAULT_VALUE, Integer::parseInt);
        assertEquals(VALUE, result, 0);

        result = PropertyUtil.readSystemProperty(TEST_PROPERTY, null, Integer::parseInt);
        assertEquals(VALUE, result, 0);
    }

    @Test
    public void readySystemPropertyWithNoValueButDefaultSet() {
        Integer result = PropertyUtil.readSystemProperty(TEST_PROPERTY, DEFAULT_VALUE, Integer::parseInt);
        assertEquals(DEFAULT_VALUE, result, 0);

        result = PropertyUtil.readSystemProperty(TEST_PROPERTY, DEFAULT_VALUE, Integer::parseInt);
        assertEquals(DEFAULT_VALUE, result, 0);
    }

    @Test
    public void readySystemPropertyWithValueSetButParsingError() {
        System.setProperty(TEST_PROPERTY, "VALUE");
        Integer result = PropertyUtil.readSystemProperty(TEST_PROPERTY, DEFAULT_VALUE, Integer::parseInt);
        assertEquals(DEFAULT_VALUE, result, 0);

        result = PropertyUtil.readSystemProperty(TEST_PROPERTY, null, Integer::parseInt);
        assertNull(result);
    }

    @Test
    public void readySystemPropertyWithNoParser() {
        Integer result = PropertyUtil.readSystemProperty(TEST_PROPERTY, DEFAULT_VALUE, null);
        assertEquals(DEFAULT_VALUE, result, 0);

        System.setProperty(TEST_PROPERTY, VALUE.toString());
        result = PropertyUtil.readSystemProperty(TEST_PROPERTY, null, null);
        assertNull(result);
    }
}
