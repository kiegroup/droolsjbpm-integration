/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.api.marshalling;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.marshalling.objects.DateObject;
import org.kie.server.api.model.definition.QueryParam;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;

public class JAXBMarshallerTest {

    @Test
    public void testMarshallDateObject() {
        String expectedString = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<date-object>\n" +
                "    <localDate>2017-01-01</localDate>\n" +
                "    <localDateTime>2017-01-01T10:10:10</localDateTime>\n" +
                "    <localTime>10:10:10</localTime>\n" +
                "    <offsetDateTime>2017-01-01T10:10:10+01:00</offsetDateTime>\n" +
                "</date-object>\n";

        Set<Class<?>> extraClasses = new HashSet<Class<?>>();
        extraClasses.add(DateObject.class);
        Marshaller marshaller = MarshallerFactory.getMarshaller(extraClasses, MarshallingFormat.JAXB, getClass().getClassLoader());

        DateObject dateObject = new DateObject();
        dateObject.setLocalDate(LocalDate.of(2017, 1, 1));
        dateObject.setLocalDateTime(LocalDateTime.of(2017, 1, 1, 10, 10, 10));
        dateObject.setLocalTime(LocalTime.of(10, 10, 10));
        dateObject.setOffsetDateTime(OffsetDateTime.of(LocalDateTime.of(2017, 1, 1, 10, 10, 10), ZoneOffset.ofHours(1)));

        String dateObjectString = marshaller.marshall(dateObject);
        assertNotNull(dateObjectString);

        assertEquals(expectedString, dateObjectString);
    }

    @Test
    public void testUnmarshallDateObject() {
        String expectedString = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                "<date-object>\n" +
                "    <localDate>2017-01-01</localDate>\n" +
                "    <localDateTime>2017-01-01T10:10:10</localDateTime>\n" +
                "    <localTime>10:10:10</localTime>\n" +
                "    <offsetDateTime>2017-01-01T10:10:10+01:00</offsetDateTime>\n" +
                "</date-object>\n";

        Set<Class<?>> extraClasses = new HashSet<Class<?>>();
        extraClasses.add(DateObject.class);
        Marshaller marshaller = MarshallerFactory.getMarshaller(extraClasses, MarshallingFormat.JAXB, getClass().getClassLoader());

        DateObject dateObject = marshaller.unmarshall(expectedString, DateObject.class);
        assertNotNull(dateObject);

        assertEquals(LocalDate.of(2017, 1, 1), dateObject.getLocalDate());
        assertEquals(LocalDateTime.of(2017, 1, 1, 10, 10, 10), dateObject.getLocalDateTime());
        assertEquals(LocalTime.of(10, 10, 10), dateObject.getLocalTime());
        assertEquals(OffsetDateTime.of(LocalDateTime.of(2017, 1, 1, 10, 10, 10), ZoneOffset.ofHours(1)), dateObject.getOffsetDateTime());
    }

    @Test
    public void testMarshallQueryParam() {
        Marshaller marshaller = MarshallerFactory.getMarshaller(new HashSet<>(), MarshallingFormat.JAXB, getClass().getClassLoader());
        QueryParam subParam = new QueryParam("col2", "EQUALS_TO", Collections.singletonList("XXX"));

        QueryParam param = new QueryParam("hola", "OR", Collections.singletonList(subParam));
        String converted = marshaller.marshall(param);
        QueryParam param2 = marshaller.unmarshall(converted, QueryParam.class);
        assertTrue(param2.getValue().get(0) instanceof QueryParam);
    }

    @Test
    public void testMarshallError() {
        Marshaller marshaller = MarshallerFactory.getMarshaller(new HashSet<>(), MarshallingFormat.JAXB, getClass()
                .getClassLoader());
        assertEquals(13, marshaller.unmarshall("<int-type><value>13</value></int-type>", int.class).intValue());
        assertEquals(0, marshaller.unmarshall("<int-type><value>2kkbk</value></int-type>", int.class).intValue());
        System.setProperty(KieServerConstants.KIE_SERVER_STRICT_JAXB_FORMAT, "true");
        try {
            assertThrows(MarshallingException.class, () -> marshaller.unmarshall(
                    "<int-type><value>13ab</value></int-type>", int.class));
        } finally {
            System.clearProperty(KieServerConstants.KIE_SERVER_STRICT_JAXB_FORMAT);
        }
    }

}
