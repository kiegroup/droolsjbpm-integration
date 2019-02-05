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

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.assertEquals;

public class MarshallingFormatTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void testEmptyMarshallingFormat() {
        exception.expectMessage("Invalid marshalling format []");
        MarshallingFormat.fromType("");
    }

    @Test
    public void testNullMarshallingFormat() {
        exception.expectMessage("Invalid marshalling format [null]");
        MarshallingFormat.fromType(null);
    }

    @Test
    public void testNonNullEmptyInvalidMarshallingFormat() {
        exception.expectMessage("Invalid marshalling format [JAX]");
        MarshallingFormat.fromType("JAX");
    }

    @Test
    public void testExpectedMarshallingFormats() {
        assertEquals(MarshallingFormat.JSON, MarshallingFormat.fromType("json"));
        assertEquals(MarshallingFormat.JAXB, MarshallingFormat.fromType("xml"));
        assertEquals(MarshallingFormat.XSTREAM, MarshallingFormat.fromType("xstream"));

        assertEquals(MarshallingFormat.JSON, MarshallingFormat.fromType("application/json"));
        assertEquals(MarshallingFormat.JAXB, MarshallingFormat.fromType("application/xml"));
        assertEquals(MarshallingFormat.XSTREAM, MarshallingFormat.fromType("application/xstream"));
    }

    @Test
    public void testMarshallingFormatsWithExtraneousParameters() {
        assertEquals(MarshallingFormat.JSON, MarshallingFormat.fromType("application/json;"));
        assertEquals(MarshallingFormat.JAXB, MarshallingFormat.fromType("application/xml;"));
        assertEquals(MarshallingFormat.XSTREAM, MarshallingFormat.fromType("application/xstream;"));
        assertEquals(MarshallingFormat.JSON, MarshallingFormat.fromType("application/json;encode="));
        assertEquals(MarshallingFormat.JAXB, MarshallingFormat.fromType("application/xml;encode=utf-8"));
        assertEquals(MarshallingFormat.XSTREAM, MarshallingFormat.fromType("application/xstream;utf-8"));
    }

    @Test
    public void testMarshallingFormatCase() {
        assertEquals(MarshallingFormat.JSON, MarshallingFormat.fromType("JSON"));
    }

    @Test
    public void testEdgeCaseWithJaxb() {
        assertEquals(MarshallingFormat.JAXB, MarshallingFormat.fromType("jaxb"));
        assertEquals(MarshallingFormat.JAXB, MarshallingFormat.fromType("JAXB"));
    }
}
