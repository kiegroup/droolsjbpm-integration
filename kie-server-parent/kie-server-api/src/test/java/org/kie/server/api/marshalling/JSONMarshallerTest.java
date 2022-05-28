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
import org.apache.commons.io.IOUtils;
import org.assertj.core.util.Arrays;
import org.assertj.core.util.Files;
import org.drools.core.command.runtime.BatchExecutionCommandImpl;
import org.drools.core.command.runtime.rule.InsertObjectCommand;
import org.drools.core.xml.jaxb.util.JaxbUnknownAdapter;
import org.junit.After;
import org.junit.Test;
import org.kie.server.api.marshalling.json.JSONMarshaller;
import org.kie.server.api.marshalling.objects.DateObject;
import org.kie.server.api.marshalling.objects.DateObjectUnannotated;
import org.kie.server.api.model.definition.QueryParam;
import org.skyscreamer.jsonassert.JSONAssert;

import static org.hamcrest.CoreMatchers.everyItem;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.skyscreamer.jsonassert.JSONCompareMode.STRICT;

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
    
    public static class Order {

        private String ORDER_ID;
        
        public Order() {}
        
        public Order(String o){
            this.ORDER_ID = o;
        }

        public String getORDER_ID() {
            return ORDER_ID;
        }

        public void setORDER_ID(String o) {
            this.ORDER_ID = o;
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
    
    @Test
    public void testCapitalizedFieldnames() throws Exception {
        Marshaller marshaller = new JSONMarshaller(new HashSet<>(),getClass().getClassLoader(), false, true); 

        Order order = new Order("all");
        String converted = marshaller.marshall(order);
        String expectedMarshalled = "{\"ORDER_ID\" : \"all\"}";

        JSONAssert.assertEquals(expectedMarshalled, converted, STRICT);
        
        Order unconverted = marshaller.unmarshall(converted, Order.class);
        assertEquals("all", unconverted.getORDER_ID());
    }

    @Test
    public void testLegacyFieldnames() throws Exception {
        Marshaller marshaller = new JSONMarshaller(new HashSet<>(),getClass().getClassLoader(), false, false); 

        Order order = new Order("all");
        String converted = marshaller.marshall(order);
        String expectedMarshalled = "{\"order_ID\" : \"all\"}";

        JSONAssert.assertEquals(expectedMarshalled, converted, STRICT);
        
        Order unconverted = marshaller.unmarshall(converted, Order.class);
        assertEquals("all", unconverted.getORDER_ID());
    }

    @Test
    public void testCapitalizedWrapObjectFieldnames() throws Exception {
        Marshaller marshaller = new JSONMarshaller(new HashSet<>(),getClass().getClassLoader(), false, true); 

        BatchExecutionCommandImpl batch = new BatchExecutionCommandImpl();
        batch.addCommand(new InsertObjectCommand(new Order("all")));

        String converted = marshaller.marshall(batch);
        String expectedMarshalled = "{ \"lookup\" : null, \"commands\" : [ { \"insert\" : " +
                                    "{ \"object\" : {\"org.kie.server.api.marshalling.JSONMarshallerTest$Order\":{ \"ORDER_ID\" : \"all\" }}, " +
                                    "\"out-identifier\" : null, \"return-object\" : true, \"entry-point\" : \"DEFAULT\", \"disconnected\" : false } } ] }";
        
        JSONAssert.assertEquals(expectedMarshalled, converted, STRICT);

        BatchExecutionCommandImpl unconverted = marshaller.unmarshall(converted, BatchExecutionCommandImpl.class);
        assertEquals("all", ((Order) ((InsertObjectCommand) unconverted.getCommands().get(0)).getObject()).getORDER_ID());
    }

    @Test
    public void testLegalizeWrapObjectFieldnames() throws Exception {
        Marshaller marshaller = new JSONMarshaller(new HashSet<>(),getClass().getClassLoader(), false, false); 

        BatchExecutionCommandImpl batch = new BatchExecutionCommandImpl();
        batch.addCommand(new InsertObjectCommand(new Order("all")));

        String converted = marshaller.marshall(batch);
        String expectedMarshalled = "{ \"lookup\" : null, \"commands\" : [ { \"insert\" : " +
                "{ \"object\" : {\"org.kie.server.api.marshalling.JSONMarshallerTest$Order\":{ \"order_ID\" : \"all\" }}, " +
                "\"out-identifier\" : null, \"return-object\" : true, \"entry-point\" : \"DEFAULT\", \"disconnected\" : false } } ] }";

        JSONAssert.assertEquals(expectedMarshalled, converted, STRICT);

        BatchExecutionCommandImpl unconverted = marshaller.unmarshall(converted, BatchExecutionCommandImpl.class);
        assertEquals("all", ((Order) ((InsertObjectCommand) unconverted.getCommands().get(0)).getObject()).getORDER_ID());
    }

    @Test
    public void testLocalDateTimeWithClasses() throws Exception {
        HashSet hs = new HashSet<>();
        hs.add(org.kie.server.api.marshalling.Person.class);
        hs.add(org.kie.server.api.marshalling.SupportedlDate.class);
        Marshaller marshaller = new JSONMarshaller(hs, getClass().getClassLoader(), false, false);

        String wrapLocalDateTimeWithType = "{\"person\":{\"org.kie.server.api.marshalling.Person\":{\"fullname\":\"123\",\"dateBirth\":{\"java.time.LocalDateTime\":\"2022-05-19T00:00\"},\"age\":\"21\"}}}";
        Map converted = marshaller.unmarshall(wrapLocalDateTimeWithType, Map.class);
        assertEquals(org.kie.server.api.marshalling.Person.class, converted.get("person").getClass());
        assertEquals(java.time.LocalDateTime.class, ((Person) converted.get("person")).getDateBirth().getClass());

        String wrapLocalDateTimeWithoutType = "{\"person\":{\"org.kie.server.api.marshalling.Person\":{\"fullname\":\"123\",\"dateBirth\":\"2022-05-19T00:00\",\"age\":\"21\"}}}";
        Map converted1 = marshaller.unmarshall(wrapLocalDateTimeWithoutType, Map.class);
        assertEquals(org.kie.server.api.marshalling.Person.class, converted1.get("person").getClass());
        assertEquals(java.time.LocalDateTime.class, ((Person) converted1.get("person")).getDateBirth().getClass());

        String localDateTimeStringWithType = "{\n" +
                "    \"bdate\":{\"java.time.LocalDateTime\":\"2022-05-17T14:54\"},\n" +
                "      \"name\":\"123\",\n" +
                "       \"bbdate\":{\"java.time.LocalDateTime\":\"2022-05-18T00:00\"}\n" +
                "}";
        Map converted2 = marshaller.unmarshall(localDateTimeStringWithType, Map.class);
        assertEquals(java.time.LocalDateTime.class, converted2.get("bdate").getClass());

        String localDateTimeStringWithoutType = "{\n" +
                "    \"bdate\":\"2022-05-17T14:54\",\n" +
                "      \"name\":\"123\",\n" +
                "       \"bbdate\":\"2022-05-18T00:00\"}\n" +
                "}";
        Map converted3 = marshaller.unmarshall(localDateTimeStringWithoutType, Map.class);
        assertEquals(String.class, converted3.get("bdate").getClass());

        Map convertedSupportedDateType = marshaller.unmarshall(IOUtils.toString(this.getClass().getResourceAsStream("/supportedDateType.json")), Map.class);
        assertEquals(java.time.LocalDateTime.class, convertedSupportedDateType.get("bdate").getClass());
        assertEquals(java.time.LocalDateTime.class, convertedSupportedDateType.get("bbdate").getClass());
        assertEquals(java.time.LocalDate.class, convertedSupportedDateType.get("localdate").getClass());
        assertEquals(java.time.LocalTime.class, convertedSupportedDateType.get("localtime").getClass());
        assertEquals(java.time.OffsetDateTime.class, convertedSupportedDateType.get("offsetDateTime").getClass());
        assertEquals(java.util.Date.class, convertedSupportedDateType.get("utildate").getClass());

        assertEquals(SupportedlDate.class, convertedSupportedDateType.get("sqldate").getClass());

        SupportedlDate supportedDate = (SupportedlDate) convertedSupportedDateType.get("sqldate");
        assertEquals(java.util.Date.class, supportedDate.getUtildate().getClass());
        assertEquals(java.time.LocalDateTime.class, supportedDate.getLocalDateTime().getClass());
        assertEquals(java.time.LocalDate.class, supportedDate.getLocalDate().getClass());
        assertEquals(java.time.LocalTime.class, supportedDate.getLocalTime().getClass());
        assertEquals(java.time.OffsetDateTime.class, supportedDate.getOffsetDateTime().getClass());
    }
}

class SupportedlDate {

    private java.util.Date utildate;
    private java.time.LocalDate localDate;
    private java.time.LocalDateTime localDateTime;
    private java.time.LocalTime localTime;
    private java.time.OffsetDateTime offsetDateTime;

    public SupportedlDate() {
    }

    public java.util.Date getUtildate() {
        return this.utildate;
    }

    public void setUtildate(java.util.Date utildate) {
        this.utildate = utildate;
    }

    public java.time.LocalDate getLocalDate() {
        return this.localDate;
    }

    public void setLocalDate(java.time.LocalDate localDate) {
        this.localDate = localDate;
    }

    public java.time.LocalDateTime getLocalDateTime() {
        return this.localDateTime;
    }

    public void setLocalDateTime(java.time.LocalDateTime localDateTime) {
        this.localDateTime = localDateTime;
    }

    public java.time.LocalTime getLocalTime() {
        return this.localTime;
    }

    public void setLocalTime(java.time.LocalTime localTime) {
        this.localTime = localTime;
    }

    public java.time.OffsetDateTime getOffsetDateTime() {
        return this.offsetDateTime;
    }

    public void setOffsetDateTime(java.time.OffsetDateTime offsetDateTime) {
        this.offsetDateTime = offsetDateTime;
    }
}

class Person {

    private java.lang.String fullname;
    private java.lang.Integer age;
    private java.time.LocalDateTime dateBirth;
    private java.util.Date date;
    private java.time.LocalTime localTime;
    private java.time.OffsetDateTime offsetDateTime;

    public LocalTime getLocalTime() {
        return localTime;
    }

    public void setLocalTime(LocalTime localTime) {
        this.localTime = localTime;
    }

    public OffsetDateTime getOffsetDateTime() {
        return offsetDateTime;
    }

    public void setOffsetDateTime(OffsetDateTime offsetDateTime) {
        this.offsetDateTime = offsetDateTime;
    }

    public Person() {
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public java.lang.String getFullname() {
        return this.fullname;
    }

    public void setFullname(java.lang.String fullname) {
        this.fullname = fullname;
    }

    public java.lang.Integer getAge() {
        return this.age;
    }

    public void setAge(java.lang.Integer age) {
        this.age = age;
    }

    public java.time.LocalDateTime getDateBirth() {
        return this.dateBirth;
    }

    public void setDateBirth(java.time.LocalDateTime dateBirth) {
        this.dateBirth = dateBirth;
    }
}


