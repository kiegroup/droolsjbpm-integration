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

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;
import org.kie.server.api.marshalling.objects.AnotherMessage;
import org.kie.server.api.marshalling.objects.Message;

import static org.junit.Assert.*;

public class XstreamMarshallerTest {

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
}
