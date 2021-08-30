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
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.marshalling.objects.PojoA;
import org.kie.server.api.marshalling.objects.PojoB;
import org.kie.server.api.marshalling.objects.PojoC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;

public class DroolsCommandWithListMarshallingTest {

    private static final Logger logger = LoggerFactory.getLogger(DroolsCommandWithListMarshallingTest.class);

    // NOTE: These expected payloads are actual results as of current version. They could be changed with a good reason.

    private static final String jaxbExpectedPayload = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
                                                      "<batch-execution>\n" +
                                                      "    <insert out-identifier=\"fact-pojoA\" return-object=\"true\" entry-point=\"DEFAULT\" disconnected=\"false\">\n" +
                                                      "        <object xsi:type=\"pojoA\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n" +
                                                      "            <name>A</name>\n" +
                                                      "            <pojoBList>\n" +
                                                      "                <name>B1</name>\n" +
                                                      "                <pojoCList>\n" +
                                                      "                    <name>C1</name>\n" +
                                                      "                </pojoCList>\n" +
                                                      "                <pojoCList>\n" +
                                                      "                    <name>C2</name>\n" +
                                                      "                </pojoCList>\n" +
                                                      "            </pojoBList>\n" +
                                                      "            <pojoBList>\n" +
                                                      "                <name>B2</name>\n" +
                                                      "                <pojoCList>\n" +
                                                      "                    <name>C3</name>\n" +
                                                      "                </pojoCList>\n" +
                                                      "            </pojoBList>\n" +
                                                      "            <stringList>Hello</stringList>\n" +
                                                      "            <stringList>Bye</stringList>\n" +
                                                      "        </object>\n" +
                                                      "    </insert>\n" +
                                                      "    <fire-all-rules max=\"-1\" out-identifier=\"fire-result\"/>\n" +
                                                      "</batch-execution>";

    private static final String xstreamExpectedPayload = "<org.drools.core.command.runtime.BatchExecutionCommandImpl>\n" +
                                                         "  <commands>\n" +
                                                         "    <org.drools.core.command.runtime.rule.InsertObjectCommand>\n" +
                                                         "      <object class=\"org.kie.server.api.marshalling.objects.PojoA\">\n" +
                                                         "        <name>A</name>\n" +
                                                         "        <pojoBList>\n" +
                                                         "          <org.kie.server.api.marshalling.objects.PojoB>\n" +
                                                         "            <name>B1</name>\n" +
                                                         "            <pojoCList>\n" +
                                                         "              <org.kie.server.api.marshalling.objects.PojoC>\n" +
                                                         "                <name>C1</name>\n" +
                                                         "              </org.kie.server.api.marshalling.objects.PojoC>\n" +
                                                         "              <org.kie.server.api.marshalling.objects.PojoC>\n" +
                                                         "                <name>C2</name>\n" +
                                                         "              </org.kie.server.api.marshalling.objects.PojoC>\n" +
                                                         "            </pojoCList>\n" +
                                                         "          </org.kie.server.api.marshalling.objects.PojoB>\n" +
                                                         "          <org.kie.server.api.marshalling.objects.PojoB>\n" +
                                                         "            <name>B2</name>\n" +
                                                         "            <pojoCList>\n" +
                                                         "              <org.kie.server.api.marshalling.objects.PojoC>\n" +
                                                         "                <name>C3</name>\n" +
                                                         "              </org.kie.server.api.marshalling.objects.PojoC>\n" +
                                                         "            </pojoCList>\n" +
                                                         "          </org.kie.server.api.marshalling.objects.PojoB>\n" +
                                                         "        </pojoBList>\n" +
                                                         "        <stringList>\n" +
                                                         "          <string>Hello</string>\n" +
                                                         "          <string>Bye</string>\n" +
                                                         "        </stringList>\n" +
                                                         "      </object>\n" +
                                                         "      <outIdentifier>fact-pojoA</outIdentifier>\n" +
                                                         "      <returnObject>true</returnObject>\n" +
                                                         "      <entryPoint>DEFAULT</entryPoint>\n" +
                                                         "      <disconnected>false</disconnected>\n" +
                                                         "    </org.drools.core.command.runtime.rule.InsertObjectCommand>\n" +
                                                         "    <org.drools.core.command.runtime.rule.FireAllRulesCommand>\n" +
                                                         "      <max>-1</max>\n" +
                                                         "      <outIdentifier>fire-result</outIdentifier>\n" +
                                                         "    </org.drools.core.command.runtime.rule.FireAllRulesCommand>\n" +
                                                         "  </commands>\n" +
                                                         "</org.drools.core.command.runtime.BatchExecutionCommandImpl>";

    private static final String jsonExpectedPayload = "{\n" +
                                                      "  \"lookup\" : null,\n" +
                                                      "  \"commands\" : [ {\n" +
                                                      "    \"insert\" : {\n" +
                                                      "      \"object\" : {\"org.kie.server.api.marshalling.objects.PojoA\":{\n" +
                                                      "  \"name\" : \"A\",\n" +
                                                      "  \"pojoBList\" : [ {\n" +
                                                      "    \"name\" : \"B1\",\n" +
                                                      "    \"pojoCList\" : [ {\n" +
                                                      "      \"name\" : \"C1\"\n" +
                                                      "    }, {\n" +
                                                      "      \"name\" : \"C2\"\n" +
                                                      "    } ]\n" +
                                                      "  }, {\n" +
                                                      "    \"name\" : \"B2\",\n" +
                                                      "    \"pojoCList\" : [ {\n" +
                                                      "      \"name\" : \"C3\"\n" +
                                                      "    } ]\n" +
                                                      "  } ],\n" +
                                                      "  \"stringList\" : [ \"Hello\", \"Bye\" ]\n" +
                                                      "}},\n" +
                                                      "      \"out-identifier\" : \"fact-pojoA\",\n" +
                                                      "      \"return-object\" : true,\n" +
                                                      "      \"entry-point\" : \"DEFAULT\",\n" +
                                                      "      \"disconnected\" : false\n" +
                                                      "    }\n" +
                                                      "  }, {\n" +
                                                      "    \"fire-all-rules\" : {\n" +
                                                      "      \"max\" : -1,\n" +
                                                      "      \"out-identifier\" : \"fire-result\"\n" +
                                                      "    }\n" +
                                                      "  } ]\n" +
                                                      "}";

    private static BatchExecutionCommand createTestCommand() {
        KieCommands commandsFactory = KieServices.Factory.get().getCommands();
        List<Command<?>> commands = new ArrayList<Command<?>>();
        commands.add(commandsFactory.newInsert(createTestObject(), "fact-pojoA"));
        commands.add(commandsFactory.newFireAllRules("fire-result"));
        BatchExecutionCommand batchExecution = commandsFactory.newBatchExecution(commands);
        return batchExecution;
    }

    private static PojoA createTestObject() {
        PojoA pojoA = new PojoA("A");
        PojoB pojoB1 = new PojoB("B1");
        PojoB pojoB2 = new PojoB("B2");
        PojoC pojoC1 = new PojoC("C1");
        PojoC pojoC2 = new PojoC("C2");
        PojoC pojoC3 = new PojoC("C3");

        List<PojoC> pojoCList1 = new ArrayList<PojoC>();
        pojoCList1.add(pojoC1);
        pojoCList1.add(pojoC2);
        pojoB1.setPojoCList(pojoCList1);

        List<PojoC> pojoCList2 = new ArrayList<PojoC>();
        pojoCList2.add(pojoC3);
        pojoB2.setPojoCList(pojoCList2);

        List<PojoB> pojoBList = new ArrayList<PojoB>();
        pojoBList.add(pojoB1);
        pojoBList.add(pojoB2);
        pojoA.setPojoBList(pojoBList);

        List<String> stringList = new ArrayList<String>();
        stringList.add("Hello");
        stringList.add("Bye");
        pojoA.setStringList(stringList);

        return pojoA;
    }

    private Set<Class<?>> getCustomClasses() {
        HashSet<Class<?>> classes = new HashSet<Class<?>>();
        classes.add(PojoA.class);
        classes.add(PojoB.class);
        classes.add(PojoC.class);
        return classes;
    }

    @Test
    public void testJaxb() {
        Marshaller marshaller = MarshallerFactory.getMarshaller(getCustomClasses(), MarshallingFormat.JAXB, getClass().getClassLoader());
        verifyMarshallingRoundTrip(marshaller, createTestCommand(), jaxbExpectedPayload);
    }

    @Test
    public void testXStream() {
        Marshaller marshaller = MarshallerFactory.getMarshaller(getCustomClasses(), MarshallingFormat.XSTREAM, getClass().getClassLoader());
        verifyMarshallingRoundTrip(marshaller, createTestCommand(), xstreamExpectedPayload);
    }

    @Test
    public void testJSON() {
        Marshaller marshaller = MarshallerFactory.getMarshaller(getCustomClasses(), MarshallingFormat.JSON, getClass().getClassLoader());
        verifyMarshallingRoundTrip(marshaller, createTestCommand(), jsonExpectedPayload);
    }

    private void verifyMarshallingRoundTrip(Marshaller marshaller, BatchExecutionCommand inputCommand, String expectedPayload) {
        String rawContent = marshaller.marshall(inputCommand);
        logger.info(rawContent);
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
