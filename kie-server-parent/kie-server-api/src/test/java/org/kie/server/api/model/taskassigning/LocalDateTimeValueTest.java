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

package org.kie.server.api.model.taskassigning;

import java.time.LocalDateTime;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LocalDateTimeValueTest {

    private static final LocalDateTime LOCAL_DATE_TIME = LocalDateTime.now();

    @Test
    public void getValue() {
        LocalDateTimeValue value = new LocalDateTimeValue();
        value.setValue(LOCAL_DATE_TIME);
        assertEquals(LOCAL_DATE_TIME, value.getValue());
    }

    @Test
    public void from() {
        LocalDateTimeValue value = LocalDateTimeValue.from(LOCAL_DATE_TIME);
        assertEquals(LOCAL_DATE_TIME, value.getValue());
    }

    @Test
    public void equals() {
        LocalDateTimeValue value1 = LocalDateTimeValue.from(LOCAL_DATE_TIME);
        LocalDateTimeValue value2 = LocalDateTimeValue.from(LOCAL_DATE_TIME);
        assertEquals(value1, value2);
    }

    @Test
    public void hashCodeTest() {
        LocalDateTimeValue value1 = LocalDateTimeValue.from(LOCAL_DATE_TIME);
        LocalDateTimeValue value2 = LocalDateTimeValue.from(LOCAL_DATE_TIME);
        assertEquals(value1.hashCode(), value2.hashCode(), 0);
    }
}
