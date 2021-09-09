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
import org.kie.server.api.marshalling.objects.Pojo1Upper;
import org.kie.server.api.marshalling.objects.Pojo2Upper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;

public class DroolsCommandMarshallingTestWithUpperCase {

    private static final Logger logger = LoggerFactory.getLogger(DroolsCommandMarshallingTestWithUpperCase.class);

    private static BatchExecutionCommand createTestCommand() {

        KieCommands commandsFactory = KieServices.Factory.get().getCommands();
        List<Command<?>> commands = new ArrayList<Command<?>>();
        Pojo2Upper pojo2 = new Pojo2Upper(true);
        Pojo1Upper pojo1 = new Pojo1Upper("SSNJim", pojo2);

        commands.add(commandsFactory.newInsert(pojo1, "fact-pojo1"));
        commands.add(commandsFactory.newFireAllRules("fire-result"));
        BatchExecutionCommand batchExecution = commandsFactory.newBatchExecution(commands);
        return batchExecution;
    }

    private Set<Class<?>> getCustomClasses() {
        HashSet<Class<?>> classes = new HashSet<Class<?>>();
        classes.add(Pojo1Upper.class);
        classes.add(Pojo2Upper.class);
        return classes;
    }

    @Test
    public void testJaxb() {
        Marshaller marshaller = MarshallerFactory.getMarshaller(getCustomClasses(), MarshallingFormat.JAXB, getClass().getClassLoader());
        verifyMarshallingRoundTrip(marshaller, createTestCommand());
    }

    @Test
    public void testXStream() {
        Marshaller marshaller = MarshallerFactory.getMarshaller(getCustomClasses(), MarshallingFormat.XSTREAM, getClass().getClassLoader());
        verifyMarshallingRoundTrip(marshaller, createTestCommand());
    }

    @Test
    public void testJSON() {
        Marshaller marshaller = MarshallerFactory.getMarshaller(getCustomClasses(), MarshallingFormat.JSON, getClass().getClassLoader());
        verifyMarshallingRoundTrip(marshaller, createTestCommand());
    }

    private void verifyMarshallingRoundTrip(Marshaller marshaller, BatchExecutionCommand inputCommand) {
        String rawContent = marshaller.marshall(inputCommand);
        logger.debug(rawContent);

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
