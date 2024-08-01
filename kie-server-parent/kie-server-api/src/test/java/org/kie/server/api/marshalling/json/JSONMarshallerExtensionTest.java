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

package org.kie.server.api.marshalling.json;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.assertj.core.api.Assertions;
import org.drools.core.command.runtime.BatchExecutionCommandImpl;
import org.drools.core.command.runtime.rule.InsertObjectCommand;
import org.drools.core.util.IoUtils;
import org.junit.Test;
import org.kie.api.pmml.PMMLRequestData;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.marshalling.objects.CustomPerson;
import org.kie.server.api.marshalling.objects.FreeFormItemType;
import org.kie.server.api.marshalling.objects.ItemsType;
import org.kie.server.api.marshalling.objects.StandardItemType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

public class JSONMarshallerExtensionTest {

    private static final Logger logger = LoggerFactory.getLogger(JSONMarshallerExtensionTest.class);

    private static final DateFormat FORMATTER = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    @Test
    public void testCustomExtensionMarshaller() {
        Set<Class<?>> extraClasses = new HashSet<Class<?>>();
        Marshaller marshaller = MarshallerFactory.getMarshaller(extraClasses, MarshallingFormat.JSON, this.getClass().getClassLoader());
        Calendar calendar = GregorianCalendar.getInstance();

        String marshall = marshaller.marshall(calendar);
        assertEquals(marshall, "\"" + FORMATTER.format(calendar.getTime()) + "\"");

        GregorianCalendar unmarshall = marshaller.unmarshall(marshall, GregorianCalendar.class);
        assertEquals(unmarshall, calendar);

        PMMLRequestData request = new PMMLRequestData("123", "SimpleScorecard");
        request.addRequestParam("param1", 10.0);
        request.addRequestParam("param2", 15.0);
        String mshl = marshaller.marshall(request);
        PMMLRequestData rd = marshaller.unmarshall(mshl, PMMLRequestData.class);
        assertEquals(rd, request);
        logger.info(rd.toString());
    }

    @Test
    public void testObjectInsideCommand() {
        Set<Class<?>> extraClasses = new HashSet<Class<?>>();
        extraClasses.add(CustomPerson.class);
        Marshaller marshaller = MarshallerFactory.getMarshaller(extraClasses, MarshallingFormat.JSON, this.getClass().getClassLoader());

        CustomPerson john = new CustomPerson("John", 20);
        InsertObjectCommand command = new InsertObjectCommand(john);
        String marshall = marshaller.marshall(command);

        // verify if it's processed by JSONMarshallerExtensionCustomPerson serializer
        Assertions.assertThat(marshall).contains("John is CustomPerson");

        InsertObjectCommand unmarshall = marshaller.unmarshall(marshall, InsertObjectCommand.class);
        CustomPerson result = (CustomPerson) unmarshall.getObject();

        // verify if it's processed by JSONMarshallerExtensionCustomPerson deserializer
        assertEquals(50, result.getAge());

    }

    @Test
    public void testPolymorphicInsideCommand() throws IOException {
        Set<Class<?>> extraClasses = new HashSet<Class<?>>();
        extraClasses.add(FreeFormItemType.class);
        extraClasses.add(StandardItemType.class);
        extraClasses.add(ItemsType.class);
        Marshaller marshaller = MarshallerFactory.getMarshaller(extraClasses, MarshallingFormat.JSON, this.getClass().getClassLoader());

        byte[] content = new byte[0];
        String marshall = null;
        URL uri = this.getClass().getClassLoader().getResource("poly_payload.json");
        try (InputStream is = uri.openStream()) {
            content = IoUtils.readBytesFromInputStream(is);
            BatchExecutionCommandImpl command = marshaller.unmarshall(content, BatchExecutionCommandImpl.class);
            marshall = marshaller.marshall(command);
        }

        BatchExecutionCommandImpl input = marshaller.unmarshall(content, BatchExecutionCommandImpl.class);
        BatchExecutionCommandImpl output = marshaller.unmarshall(marshall, BatchExecutionCommandImpl.class);
        
        ObjectMapper mapper = new ObjectMapper().setSerializationInclusion(Include.NON_NULL);
        ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();

        String inputStr = writer.writeValueAsString(input);
        String outputStr = writer.writeValueAsString(output);
        assertThat(inputStr).isEqualTo(outputStr);
    }

    @Test
    public void testPolymorphicInsideCommandHandle() throws IOException {
        Set<Class<?>> extraClasses = new HashSet<Class<?>>();
        extraClasses.add(FreeFormItemType.class);
        extraClasses.add(StandardItemType.class);
        extraClasses.add(ItemsType.class);
        Marshaller marshaller = MarshallerFactory.getMarshaller(extraClasses, MarshallingFormat.JSON, this.getClass().getClassLoader());
        ((JSONMarshaller) marshaller).setXmlAnyElementsNames(true);
        
        byte[] content = new byte[0];
        String marshall = null;
        URL uri = this.getClass().getClassLoader().getResource("poly_payload.json");
        try (InputStream is = uri.openStream()) {
            content = IoUtils.readBytesFromInputStream(is);
            BatchExecutionCommandImpl command = marshaller.unmarshall(content, BatchExecutionCommandImpl.class);
            marshall = marshaller.marshall(command);
        }

        BatchExecutionCommandImpl input = marshaller.unmarshall(content, BatchExecutionCommandImpl.class);
        BatchExecutionCommandImpl output = marshaller.unmarshall(marshall, BatchExecutionCommandImpl.class);
        
        ObjectMapper mapper = new ObjectMapper().setSerializationInclusion(Include.NON_NULL);
        ObjectWriter writer = mapper.writerWithDefaultPrettyPrinter();

        String inputStr = writer.writeValueAsString(input);
        String outputStr = writer.writeValueAsString(output);
        assertThat(inputStr).isEqualTo(outputStr);
    }
}
