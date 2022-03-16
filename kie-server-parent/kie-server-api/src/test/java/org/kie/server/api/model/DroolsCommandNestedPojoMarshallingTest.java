/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.api.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.assertj.core.api.Assertions;
import org.drools.core.command.runtime.BatchExecutionCommandImpl;
import org.drools.core.command.runtime.rule.InsertObjectCommand;
import org.junit.Test;
import org.kie.api.KieServices;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.command.Command;
import org.kie.api.command.KieCommands;
import org.kie.server.api.marshalling.BaseMarshallerBuilder;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallerBuilder;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.marshalling.objects.Pojo1;
import org.kie.server.api.marshalling.objects.Pojo2;
import org.kie.server.api.marshalling.objects.Pojo3;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;

public class DroolsCommandNestedPojoMarshallingTest {

    private static final Logger logger = LoggerFactory.getLogger(DroolsCommandNestedPojoMarshallingTest.class);

    // NOTE: These expected payloads are actual results as of current version. They could be changed with a good reason.

    private static final String jaxbExpectedPayload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                                                      "<batch-execution>\n" +
                                                      "    <insert out-identifier=\"fact-pojo1\" return-object=\"true\" entry-point=\"DEFAULT\" disconnected=\"false\">\n" +
                                                      "        <object xsi:type=\"pojo1\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                                                      "            <desc>C</desc>\n" +
                                                      "            <pojo2>\n" +
                                                      "                <desc2>B</desc2>\n" +
                                                      "                <pojo3>\n" +
                                                      "                    <desc3>A</desc3>\n" +
                                                      "                </pojo3>\n" +
                                                      "                <primitiveBoolean>true</primitiveBoolean>\n" +
                                                      "            </pojo2>\n" +
                                                      "        </object>\n" +
                                                      "    </insert>\n" +
                                                      "    <fire-all-rules max=\"-1\" out-identifier=\"fire-result\"/>\n" +
                                                      "</batch-execution>\n";

    private static final String xstreamExpectedPayload = "<batch-execution>\n" +
                                                         "  <insert out-identifier=\"fact-pojo1\" return-object=\"true\" entry-point=\"DEFAULT\">\n" +
                                                         "    <org.kie.server.api.marshalling.objects.Pojo1>\n" +
                                                         "      <desc>C</desc>\n" +
                                                         "      <pojo2>\n" +
                                                         "        <desc2>B</desc2>\n" +
                                                         "        <primitiveBoolean>true</primitiveBoolean>\n" +
                                                         "        <pojo3>\n" +
                                                         "          <desc3>A</desc3>\n" +
                                                         "        </pojo3>\n" +
                                                         "      </pojo2>\n" +
                                                         "    </org.kie.server.api.marshalling.objects.Pojo1>\n" +
                                                         "  </insert>\n" +
                                                         "  <fire-all-rules out-identifier=\"fire-result\"/>\n" +
                                                         "</batch-execution>";

    private static final String jsonExpectedPayload = "{\n" +
                                                      "  \"lookup\" : null,\n" +
                                                      "  \"commands\" : [ {\n" +
                                                      "    \"insert\" : {\n" +
                                                      "      \"object\" : {\"org.kie.server.api.marshalling.objects.Pojo1\":{\n" +
                                                      "  \"desc\" : \"C\",\n" +
                                                      "  \"pojo2\" : {\n" +
                                                      "    \"desc2\" : \"B\",\n" +
                                                      "    \"primitiveBoolean\" : true,\n" +
                                                      "    \"pojo3\" : {\n" +
                                                      "      \"desc3\" : \"A\"\n" +
                                                      "    }\n" +
                                                      "  }\n" +
                                                      "}},\n" +
                                                      "      \"out-identifier\" : \"fact-pojo1\",\n" +
                                                      "      \"return-object\" : true,\n" +
                                                      "      \"entry-point\" : \"DEFAULT\",\n" +
                                                      "      \"disconnected\" : false\n" +
                                                      "    }\n" +
                                                      "  }, {\n" +
                                                      "    \"fire-all-rules\" : {\n" +
                                                      "      \"max\" : -1,\n" +
                                                      "      \"agendaFilter\" : null,\n" +
                                                      "      \"out-identifier\" : \"fire-result\"\n" +
                                                      "    }\n" +
                                                      "  } ]\n" +
                                                      "}";

    private MarshallerBuilder marshallerBuilder = new BaseMarshallerBuilder(); // don't use MarshallerFactory to avoid CustomXstreamMarshallerBuilder

    private static BatchExecutionCommand createTestCommand() {

        KieCommands commandsFactory = KieServices.Factory.get().getCommands();
        List<Command<?>> commands = new ArrayList<Command<?>>();
        Pojo3 pojo3 = new Pojo3("A");
        Pojo2 pojo2 = new Pojo2("B", true, pojo3);
        Pojo1 pojo1 = new Pojo1("C", pojo2);
        commands.add(commandsFactory.newInsert(pojo1, "fact-pojo1"));
        commands.add(commandsFactory.newFireAllRules("fire-result"));
        BatchExecutionCommand batchExecution = commandsFactory.newBatchExecution(commands);
        return batchExecution;
    }

    private Set<Class<?>> getCustomClasses() {
        HashSet<Class<?>> classes = new HashSet<Class<?>>();
        classes.add(Pojo1.class);
        classes.add(Pojo2.class);
        classes.add(Pojo3.class);
        return classes;
    }

    @Test
    public void testJaxb() {
        Marshaller marshaller = marshallerBuilder.build(getCustomClasses(), MarshallingFormat.JAXB, getClass().getClassLoader());
        verifyMarshallingRoundTrip(marshaller, createTestCommand(), jaxbExpectedPayload);
    }

    @Test
    public void testXStream() {
        Marshaller marshaller = marshallerBuilder.build(getCustomClasses(), MarshallingFormat.XSTREAM, getClass().getClassLoader());
        verifyMarshallingRoundTrip(marshaller, createTestCommand(), xstreamExpectedPayload);
    }

    @Test
    public void testJSON() {
        Marshaller marshaller = marshallerBuilder.build(getCustomClasses(), MarshallingFormat.JSON, getClass().getClassLoader());
        verifyMarshallingRoundTrip(marshaller, createTestCommand(), jsonExpectedPayload);
    }

    private void verifyMarshallingRoundTrip(Marshaller marshaller, BatchExecutionCommand inputCommand, String expectedPayload) {
        String rawContent = marshaller.marshall(inputCommand);
        logger.debug(rawContent);
        Assertions.assertThat(rawContent).isEqualToIgnoringWhitespace(expectedPayload);

        BatchExecutionCommandImpl inputBatch = (BatchExecutionCommandImpl) inputCommand;
        BatchExecutionCommandImpl resultBatch = (BatchExecutionCommandImpl) marshaller.unmarshall(rawContent, inputCommand.getClass());
        assertEquals(inputBatch.getLookup(), resultBatch.getLookup());
        assertEquals(inputBatch.getDistance(), resultBatch.getDistance());

        InsertObjectCommand inputInsert = (InsertObjectCommand) inputBatch.getCommands().get(0);
        InsertObjectCommand resultInsert = (InsertObjectCommand) resultBatch.getCommands().get(0);
        assertEquals(inputInsert.getEntryPoint(), resultInsert.getEntryPoint());
        assertEquals(inputInsert.getObject(), resultInsert.getObject());
        assertEquals(inputInsert.getOutIdentifier(), resultInsert.getOutIdentifier());
        assertEquals(inputInsert.isDisconnected(), resultInsert.isDisconnected());
        assertEquals(inputInsert.isReturnObject(), resultInsert.isReturnObject());
    }
}
