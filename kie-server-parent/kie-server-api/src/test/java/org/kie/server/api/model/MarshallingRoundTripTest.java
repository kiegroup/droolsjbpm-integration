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

package org.kie.server.api.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.assertj.core.api.Assertions;
import org.drools.core.command.runtime.BatchExecutionCommandImpl;
import org.drools.core.command.runtime.rule.ClearAgendaCommand;
import org.drools.core.command.runtime.rule.FireAllRulesCommand;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.kie.api.command.BatchExecutionCommand;
import org.kie.api.command.ExecutableCommand;
import org.kie.server.api.commands.CommandScript;
import org.kie.server.api.commands.DisposeContainerCommand;
import org.kie.server.api.commands.ListContainersCommand;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.marshalling.MarshallingFormat;

/**
 * Roundtrip tests which make sure that the input object is the same as the object created by marshalling + unmarshalling.
 */
@RunWith(Parameterized.class)
public class MarshallingRoundTripTest {

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<Object[]> contextPath() {
        Object[][] data = new Object[][]{
                {createKieContainerResourceFilter()},
                {createReleaseIdFilter()},
                {createBatchExecutionCommand()},
                {createKieContainerResourceFilter()},
                {createListContainersCommand()},
                {createCommandScript()}
        };
        return Arrays.asList(data);
    }

    private static KieContainerStatusFilter createKieContainerStatusFilter() {
        return new KieContainerStatusFilter();
    }

    private static ReleaseIdFilter createReleaseIdFilter() {
        return new ReleaseIdFilter("group", "artifact", "version");
    }

    private static BatchExecutionCommand createBatchExecutionCommand() {
        return new BatchExecutionCommandImpl(
                             Arrays.asList(new ClearAgendaCommand(), new FireAllRulesCommand()), 
                             "Batch Commands");
    }

    private static KieContainerResourceFilter createKieContainerResourceFilter() {
        return new KieContainerResourceFilter(createReleaseIdFilter(), createKieContainerStatusFilter());
    }

    private static ListContainersCommand createListContainersCommand() {
        return new ListContainersCommand(createKieContainerResourceFilter());
    }

    private static DisposeContainerCommand createDisposeContainerCommand() {
        return new DisposeContainerCommand("some-container-id");
    }
    
    private static CommandScript createCommandScript() {
        List<KieServerCommand> commands = new ArrayList<KieServerCommand>();
        commands.add(createListContainersCommand());
        commands.add(createDisposeContainerCommand());
        return new CommandScript(commands);
    }

    @Parameterized.Parameter(0)
    public Object testObject;

    @Test
    public void testJaxb() {
        Marshaller marshaller = MarshallerFactory.getMarshaller(MarshallingFormat.JAXB, getClass().getClassLoader());
        verifyMarshallingRoundTrip(marshaller, testObject);
    }

    @Test
    public void testXStream() {
        Marshaller marshaller = MarshallerFactory.getMarshaller(MarshallingFormat.XSTREAM, getClass().getClassLoader());
        verifyMarshallingRoundTrip(marshaller, testObject);
    }

    @Test
    public void testJSON() {
        Marshaller marshaller = MarshallerFactory.getMarshaller(MarshallingFormat.JSON, getClass().getClassLoader());
        verifyMarshallingRoundTrip(marshaller, testObject);
    }

    private void verifyMarshallingRoundTrip(Marshaller marshaller, Object inputObject) {
        String rawContent = marshaller.marshall(inputObject);
        Object testObjectAfterMarshallingTurnAround = marshaller.unmarshall(rawContent, inputObject.getClass());
        //HACK: Implementations of ExecutableCommand were not overriding equals() & hashCode, only toString.
        if (inputObject instanceof ExecutableCommand<?>) {
            Assertions.assertThat(inputObject.toString()).isEqualTo(testObjectAfterMarshallingTurnAround.toString());
        } else {
        Assertions.assertThat(inputObject).isEqualTo(testObjectAfterMarshallingTurnAround);
        }
        Assert.assertNotSame(inputObject, testObjectAfterMarshallingTurnAround);
    }

    @Test
    public void testMapWithDateJSON() {
        Marshaller marshaller = MarshallerFactory.getMarshaller(MarshallingFormat.JSON, getClass().getClassLoader());

        Map<String, Object> map = new HashMap<>();
        map.put("date", new Date());

        verifyMarshallingRoundTrip(marshaller, map);
    }
}
