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

import java.io.File;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.assertj.core.util.Arrays;
import org.assertj.core.util.Files;
import org.drools.core.xml.jaxb.util.JaxbUnknownAdapter;
import org.junit.After;
import org.junit.Test;
import org.kie.server.api.marshalling.json.JSONMarshaller;
import org.kie.server.api.marshalling.objects.DateObject;
import org.kie.server.api.marshalling.objects.DateObjectUnannotated;
import org.kie.server.api.model.definition.QueryParam;

import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class JSONMarshallerTest {

    @After
    public void cleanup() {
        System.clearProperty("org.kie.server.json.format.date");
    }

    @Test
    public void testMarshallDateObject() {
        String expectedString = String.format("{%n" +
                "  \"localDate\" : \"2017-01-01\",%n" +
                "  \"localDateTime\" : \"2017-01-01T10:10:10\",%n" +
                "  \"localTime\" : \"10:10:10\",%n" +
                "  \"offsetDateTime\" : \"2017-01-01T10:10:10+01:00\"%n" +
                "}");

        Marshaller marshaller = MarshallerFactory.getMarshaller(MarshallingFormat.JSON, getClass().getClassLoader());

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
        String expectedString = "{\n" +
                "  \"localDate\" : \"2017-01-01\",\n" +
                "  \"localDateTime\" : \"2017-01-01T10:10:10\",\n" +
                "  \"localTime\" : \"10:10:10\",\n" +
                "  \"offsetDateTime\" : \"2017-01-01T10:10:10+01:00\"\n" +
                "}";

        Marshaller marshaller = MarshallerFactory.getMarshaller(MarshallingFormat.JSON, getClass().getClassLoader());

        DateObject dateObject = marshaller.unmarshall(expectedString, DateObject.class);
        assertNotNull(dateObject);

        assertEquals(LocalDate.of(2017, 1, 1), dateObject.getLocalDate());
        assertEquals(LocalDateTime.of(2017, 1, 1, 10, 10, 10), dateObject.getLocalDateTime());
        assertEquals(LocalTime.of(10, 10, 10), dateObject.getLocalTime());
        assertEquals(OffsetDateTime.of(LocalDateTime.of(2017, 1, 1, 10, 10, 10), ZoneOffset.ofHours(1)), dateObject.getOffsetDateTime());
    }

    @Test
    public void testMarshallDateObjectUnannotated() {
        String expectedString = String.format("{%n" +
                "  \"localDate\" : \"2017-01-01\",%n" +
                "  \"localDateTime\" : \"2017-01-01T10:10:10\",%n" +
                "  \"localTime\" : \"10:10:10\",%n" +
                "  \"offsetDateTime\" : \"2017-01-01T10:10:10+0100\"%n" +
                "}");

        Marshaller marshaller = MarshallerFactory.getMarshaller(MarshallingFormat.JSON, getClass().getClassLoader());

        DateObjectUnannotated dateObject = new DateObjectUnannotated();
        dateObject.setLocalDate(LocalDate.of(2017, 1, 1));
        dateObject.setLocalDateTime(LocalDateTime.of(2017, 1, 1, 10, 10, 10));
        dateObject.setLocalTime(LocalTime.of(10, 10, 10));
        dateObject.setOffsetDateTime(OffsetDateTime.of(2017, 1, 1, 10, 10, 10, 0, ZoneOffset.ofHours(1)));

        String dateObjectString = marshaller.marshall(dateObject);
        assertNotNull(dateObjectString);

        assertEquals(expectedString, dateObjectString);
    }

    @Test
    public void testUnmarshallDateObjectUnannotated() {
        String expectedString = "{\n" +
                "  \"localDate\" : \"2017-01-01\",\n" +
                "  \"localDateTime\" : \"2017-01-01T10:10:10\",\n" +
                "  \"localTime\" : \"10:10:10\",\n" +
                "  \"offsetDateTime\" : \"2017-01-01T10:10:10+0100\"\n" +
                "}";

        Marshaller marshaller = MarshallerFactory.getMarshaller(MarshallingFormat.JSON, getClass().getClassLoader());

        DateObjectUnannotated dateObject = marshaller.unmarshall(expectedString, DateObjectUnannotated.class);
        assertNotNull(dateObject);

        assertEquals(LocalDate.of(2017, 1, 1), dateObject.getLocalDate());
        assertEquals(LocalDateTime.of(2017, 1, 1, 10, 10, 10), dateObject.getLocalDateTime());
        assertEquals(LocalTime.of(10, 10, 10), dateObject.getLocalTime());
        assertEquals(OffsetDateTime.of(2017, 1, 1, 10, 10, 10, 0, ZoneOffset.ofHours(1)), dateObject.getOffsetDateTime());
    }

    public static class Holder {

        private String h;

        public String getH() {
            return h;
        }

        public void setH(String h) {
            this.h = h;
        }
    }

    public static class Ref {

        @XmlJavaTypeAdapter(JaxbUnknownAdapter.class)
        @JsonSerialize(using = JSONMarshaller.PassThruSerializer.class)
        private Object r;

        public Object getR() {
            return r;
        }

        public void setR(Object r) {
            this.r = r;
        }
    }

    @Test
    public void testRecursiveMap() {
        Map<String, Map<String, Holder>> outerMap = new HashMap<>();
        Map<String, Holder> innerMap = new HashMap<>();
        Holder holder = new Holder();
        holder.setH("myValueInH");

        innerMap.put("level2", holder);
        outerMap.put("level1", innerMap);

        Marshaller marshaller = MarshallerFactory.getMarshaller(MarshallingFormat.JSON, getClass().getClassLoader());

        Map mu_outerMap = marshaller.unmarshall(marshaller.marshall(outerMap), Map.class);
        Map mu_innerMap = marshaller.unmarshall(marshaller.marshall(innerMap), Map.class);

        Ref ref = new Ref();
        ref.setR(innerMap);

        Ref mu_ref = marshaller.unmarshall(marshaller.marshall(ref), Ref.class);

        assertEquals("verify that Ref.r is not being serialized with JSONMarshaller.WrappingObjectSerializer, but with the specified one in @JsonSerialize",
                mu_innerMap.entrySet(), ((Map) mu_ref.getR()).entrySet());
    }

    @Test
    public void testMarshallFormatDateObject() throws ParseException {
        System.setProperty("org.kie.server.json.format.date", "true");
        Date date = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss.SSSZ").parse("2018-01-01T10:00:00.000+0100");
        String expectedString = "\"2018-01-01";

        Marshaller marshaller = MarshallerFactory.getMarshaller(MarshallingFormat.JSON, getClass().getClassLoader());

        String dateObjectString = marshaller.marshall(date);
        assertNotNull(dateObjectString);

        assertTrue(dateObjectString.startsWith(expectedString));
    }

    @Test
    public void testMarshallQueryParam() {
        Marshaller marshaller = MarshallerFactory.getMarshaller(new HashSet<>(), MarshallingFormat.JSON, getClass().getClassLoader());
        QueryParam subParam = new QueryParam("col2", "EQUALS_TO", Collections.singletonList("XXX"));

        QueryParam param = new QueryParam("hola", "OR", Collections.singletonList(subParam));
        String converted = marshaller.marshall(param);
        QueryParam param2 = marshaller.unmarshall(converted, QueryParam.class);
        assertTrue(param2.getValue().get(0) instanceof QueryParam);
    }

    @Test
    public void testParam() throws Exception {
        Marshaller marshaller = MarshallerFactory.getMarshaller(new HashSet<>(), MarshallingFormat.JSON, getClass().getClassLoader());
        URL url = Thread.currentThread().getContextClassLoader().getResource("marshaller.json");
        String input = Files.contentOf(new File(url.toURI()), "UTF-8");
        QueryParam[] params = marshaller.unmarshall(input, QueryParam[].class);
        assertEquals(1, params.length);
        assertThat(Arrays.asList(params), everyItem(instanceOf(QueryParam.class)));
    }

}
