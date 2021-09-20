/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

import com.thoughtworks.xstream.converters.reflection.AbstractReflectionConverter.UnknownFieldException;
import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.kie.api.pmml.PMMLRequestData;
import org.kie.api.pmml.ParameterInfo;
import org.kie.server.api.commands.ListContainersCommand;
import org.kie.server.api.marshalling.objects.AnotherMessage;
import org.kie.server.api.marshalling.objects.DateObject;
import org.kie.server.api.marshalling.objects.Message;
import org.kie.server.api.marshalling.xstream.XStreamMarshaller;
import org.kie.server.api.model.KieContainerResourceFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

public class XStreamMarshallerTest {

    private static final Logger logger = LoggerFactory.getLogger(XStreamMarshallerTest.class);

    @Test
    public void testXstreamMarshalWithAnnotation() {
        String expectedXml = "<org.kie.server.api.marshalling.objects.Message>\n" +
                "  <nameWithAlias>test content</nameWithAlias>\n" +
                "</org.kie.server.api.marshalling.objects.Message>";

        Set<Class<?>> extraClasses = new HashSet<Class<?>>();
        extraClasses.add(Message.class);
        Marshaller marshaller = MarshallerFactory.getMarshaller(extraClasses, MarshallingFormat.XSTREAM, this.getClass().getClassLoader());

        Message testMessage = new Message("test content");

        String testMessageString = marshaller.marshall(testMessage);
        assertNotNull(testMessageString);

        assertEquals(expectedXml, testMessageString);
    }

    @Test
    public void testXstreamMarshalWithCustomMarshallerBuilder() {
        String expectedXml = "<org.kie.server.api.marshalling.objects.AnotherMessage>\n" +
                "  <another_name>test content</another_name>\n" +
                "</org.kie.server.api.marshalling.objects.AnotherMessage>";

        Set<Class<?>> extraClasses = new HashSet<Class<?>>();
        extraClasses.add(Message.class);
        Marshaller marshaller = MarshallerFactory.getMarshaller(extraClasses, MarshallingFormat.XSTREAM, this.getClass().getClassLoader());

        AnotherMessage testMessage = new AnotherMessage("test content");

        String testMessageString = marshaller.marshall(testMessage);
        assertNotNull(testMessageString);

        assertEquals(expectedXml, testMessageString);
    }

    @Test
    public void testPMMLRequest() {
        PMMLRequestData request = new PMMLRequestData("123", "Test Model");
        request.addRequestParam("param1", 10.0);
        request.addRequestParam("param2", "test string");
        Set<Class<?>> extraClasses = new HashSet<Class<?>>();
        extraClasses.add(PMMLRequestData.class);
        extraClasses.add(ParameterInfo.class);
        Marshaller marshaller = MarshallerFactory.getMarshaller(extraClasses, MarshallingFormat.XSTREAM, getClass().getClassLoader());
        String marshalled = marshaller.marshall(request);
        logger.debug(marshalled);

        PMMLRequestData rqst = marshaller.unmarshall(marshalled, PMMLRequestData.class);
        logger.info(rqst.toString());
    }

    @Test
    public void testUnmarshallListContainersCommandWithNoFilter() {
        String commandString = "<list-containers/>";
        Marshaller marshaller = MarshallerFactory.getMarshaller(MarshallingFormat.XSTREAM, getClass().getClassLoader());
        ListContainersCommand command = marshaller.unmarshall(commandString, ListContainersCommand.class);
        // the default ACCEPT_ALL filter should be set
        Assertions.assertThat(command.getKieContainerResourceFilter()).isEqualTo(KieContainerResourceFilter.ACCEPT_ALL);
    }

    @Test
    public void testMarshallDateObject() {
        String expectedString = "<date-object>\n" +
                "  <localDate>2017-01-01</localDate>\n" +
                "  <localDateTime>2017-01-01T10:10:10</localDateTime>\n" +
                "  <localTime>10:10:10</localTime>\n" +
                "  <offsetDateTime>2017-01-01T10:10:10+01:00</offsetDateTime>\n" +
                "</date-object>";

        Set<Class<?>> extraClasses = new HashSet<Class<?>>();
        extraClasses.add(DateObject.class);
        Marshaller marshaller = MarshallerFactory.getMarshaller(extraClasses, MarshallingFormat.XSTREAM, getClass().getClassLoader());

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
        String expectedString = "<date-object>\n" +
                "  <localDate>2017-01-01</localDate>\n" +
                "  <localDateTime>2017-01-01T10:10:10</localDateTime>\n" +
                "  <localTime>10:10:10</localTime>\n" +
                "  <offsetDateTime>2017-01-01T10:10:10+01:00</offsetDateTime>\n" +
                "</date-object>";

        Set<Class<?>> extraClasses = new HashSet<Class<?>>();
        extraClasses.add(DateObject.class);
        Marshaller marshaller = MarshallerFactory.getMarshaller(extraClasses, MarshallingFormat.XSTREAM, getClass().getClassLoader());

        DateObject dateObject = marshaller.unmarshall(expectedString, DateObject.class);
        assertNotNull(dateObject);

        assertEquals(LocalDate.of(2017, 1, 1), dateObject.getLocalDate());
        assertEquals(LocalDateTime.of(2017, 1, 1, 10, 10, 10), dateObject.getLocalDateTime());
        assertEquals(LocalTime.of(10, 10, 10), dateObject.getLocalTime());
        assertEquals(OffsetDateTime.of(LocalDateTime.of(2017, 1, 1, 10, 10, 10), ZoneOffset.ofHours(1)), dateObject.getOffsetDateTime());
    }

    @Test
    public void testUnmarshallDateObjectWithExtraField() {
        String expectedString = "<date-object>\n" +
                                "  <localDate>2017-01-01</localDate>\n" +
                                "  <localDateTime>2017-01-01T10:10:10</localDateTime>\n" +
                                "  <localTime>10:10:10</localTime>\n" +
                                "  <offsetDateTime>2017-01-01T10:10:10+01:00</offsetDateTime>\n" +
                                "  <unknownField>jojojojo</unknownField>\n" +
                                "</date-object>";
        Set<Class<?>> extraClasses = new HashSet<>();
        extraClasses.add(DateObject.class);
        Marshaller marshaller = MarshallerFactory.getMarshaller(extraClasses, MarshallingFormat.XSTREAM, getClass()
                .getClassLoader());
        
        assertThrows(UnknownFieldException.class, () -> marshaller.unmarshall(expectedString, DateObject.class));
        DateObject dateObject = marshaller.unmarshall(expectedString, DateObject.class, Collections.singletonMap(
                XStreamMarshaller.XSTREAM_IGNORE_UNKNOWN, Boolean.TRUE));
        assertNotNull(dateObject);
        assertEquals(LocalDate.of(2017, 1, 1), dateObject.getLocalDate());
        assertEquals(LocalDateTime.of(2017, 1, 1, 10, 10, 10), dateObject.getLocalDateTime());
        assertEquals(LocalTime.of(10, 10, 10), dateObject.getLocalTime());
        assertEquals(OffsetDateTime.of(LocalDateTime.of(2017, 1, 1, 10, 10, 10), ZoneOffset.ofHours(1)), dateObject
                .getOffsetDateTime());
        assertThrows(UnknownFieldException.class, () -> marshaller.unmarshall(expectedString, DateObject.class));
    }
}
