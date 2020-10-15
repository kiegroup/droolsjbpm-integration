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
package org.kie.server.services.jbpm.kafka;

import java.io.IOException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.TimeZone;

import org.junit.Test;

import static org.junit.Assert.assertEquals;


public class CloudEventTest {
    
    @Test
    public void testStringCloudEventDeserialization() throws IOException, ParseException {
        String cloudEventText = "{\"id\":\"javi\",\"type\":\"one\",\"source\":\"pepe\",\"data\":\"javierito\",\"specversion\":\"1.0\",\"time\":\"2020-03-21T17:43:34.000GMT\"}";
        CloudEvent<String> event = CloudEvent.read(cloudEventText.getBytes(), String.class);
        assertEquals("javi",event.getId());
        assertEquals("one",event.getType());
        assertEquals("pepe",event.getSource());
        assertEquals("1.0",event.getSpecVersion());
        assertEquals("javierito",event.getData());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(event.getTime());
        calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
        assertEquals(17,calendar.get(Calendar.HOUR_OF_DAY));
        assertEquals(2020,calendar.get(Calendar.YEAR));
    }
    
    
    private static class Person {
        private String name;
        
        public String getName() {
            return name;
        }
    }
    
    @Test
    public void testPersonCloudEventDeserialization() throws IOException, ParseException {
        String cloudEventText = "{\"id\":\"javi\",\"type\":\"one\",\"source\":\"pepe\",\"data\":{\"name\":\"javierito\"},\"specversion\":\"1.0\",\"time\":\"2020-03-21T17:43:34.000GMT\"}";
        CloudEvent<Person> event = CloudEvent.read(cloudEventText.getBytes(), Person.class);
        assertEquals("javi",event.getId());
        assertEquals("one",event.getType());
        assertEquals("pepe",event.getSource());
        assertEquals("1.0",event.getSpecVersion());
        assertEquals("javierito",event.getData().getName());
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(event.getTime());
        calendar.setTimeZone(TimeZone.getTimeZone("GMT"));
        assertEquals(17,calendar.get(Calendar.HOUR_OF_DAY));
        assertEquals(2020,calendar.get(Calendar.YEAR));
    }

}
