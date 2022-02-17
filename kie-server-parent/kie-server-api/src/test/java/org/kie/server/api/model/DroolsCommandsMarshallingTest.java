/*
 * Copyright 2022 Red Hat, Inc. and/or its affiliates.
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
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

import org.drools.core.base.RuleNameEndsWithAgendaFilter;
import org.drools.core.command.runtime.BatchExecutionCommandImpl;
import org.drools.core.command.runtime.rule.FireAllRulesCommand;
import org.drools.core.command.runtime.rule.FireUntilHaltCommand;
import org.drools.core.command.runtime.rule.GetObjectCommand;
import org.drools.core.command.runtime.rule.InsertObjectCommand;
import org.drools.core.command.runtime.rule.ModifyCommand;
import org.drools.core.command.runtime.rule.UpdateCommand;
import org.drools.core.common.DefaultFactHandle;
import org.drools.core.common.DisconnectedFactHandle;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.kie.api.KieServices;
import org.kie.api.command.Command;
import org.kie.api.command.Setter;
import org.kie.api.runtime.rule.FactHandle;
import org.kie.internal.command.CommandFactory;
import org.kie.server.api.marshalling.BaseMarshallerBuilder;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallerBuilder;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.marshalling.objects.SimplePojo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(Parameterized.class)
public class DroolsCommandsMarshallingTest {

    @Parameterized.Parameters(name = "{index}: {0}")
    public static Collection<Object[]> data() {
        return new ArrayList<>(Arrays.asList(new Object[][]{
                                                            {MarshallingFormat.JAXB},
                                                            {MarshallingFormat.JSON},
                                                            {MarshallingFormat.XSTREAM}
        }));
    }

    @Parameterized.Parameter
    public MarshallingFormat marshallingFormat;

    private MarshallerBuilder marshallerBuilder = new BaseMarshallerBuilder(); // don't use MarshallerFactory to avoid CustomXstreamMarshallerBuilder

    private static Set<Class<?>> extraClasses = new HashSet<>();

    @BeforeClass
    public static void setUp() {
        extraClasses.add(SimplePojo.class);
    }

    // HELPER METHODS -------------------------------------------------------------------------------------------------------------

    private void verifyDisconnectedFactHandle(DisconnectedFactHandle orig, DisconnectedFactHandle copy) {
        assertNotNull("copy disconnected fact handle is null", copy);
        assertEquals("id", orig.getId(), copy.getId());
        assertEquals("identity hash code", orig.getIdentityHashCode(), copy.getIdentityHashCode());
        assertEquals("object hash code", orig.getObjectHashCode(), copy.getObjectHashCode());
        assertEquals("recency", orig.getRecency(), copy.getRecency());
        assertEquals("entry point id", orig.getEntryPointId(), copy.getEntryPointId());
        assertEquals("trait type", orig.getTraitType(), copy.getTraitType());
    }

    private <T> T roundTrip(T obj) {
        Class<T> type = (Class<T>) obj.getClass();
        Marshaller marshaller = marshallerBuilder.build(extraClasses, marshallingFormat, getClass().getClassLoader());
        String strOut = marshaller.marshall(obj);
        return marshaller.unmarshall(strOut, type);
    }

    // TESTS ----------------------------------------------------------------------------------------------------------------------

    private static final Random random = new Random();

    private static String randomString() {
        return UUID.randomUUID().toString();
    }

    @Test
    public void testFireAllRulesCommandAgendaFilter() {
        RuleNameEndsWithAgendaFilter filter = new RuleNameEndsWithAgendaFilter("suffix", true);
        FireAllRulesCommand command = new FireAllRulesCommand(randomString(), random.nextInt(1000), filter);
        FireAllRulesCommand result = roundTrip(command);

        assertEquals(command.getMax(), result.getMax());
        assertEquals(RuleNameEndsWithAgendaFilter.class, result.getAgendaFilter().getClass());
        RuleNameEndsWithAgendaFilter filterResult = (RuleNameEndsWithAgendaFilter) result.getAgendaFilter();
        assertEquals(filter.getSuffix(), filterResult.getSuffix());
        assertEquals(filter.isAccept(), filterResult.isAccept());
    }

    @Test
    public void testFireUntilHaltCommandAgendaFilter() {
        RuleNameEndsWithAgendaFilter filter = new RuleNameEndsWithAgendaFilter("suffix", true);
        FireUntilHaltCommand command = new FireUntilHaltCommand(filter);
        FireUntilHaltCommand result = roundTrip(command);

        assertEquals(RuleNameEndsWithAgendaFilter.class, result.getAgendaFilter().getClass());
        RuleNameEndsWithAgendaFilter filterResult = (RuleNameEndsWithAgendaFilter) result.getAgendaFilter();
        assertEquals(filter.getSuffix(), filterResult.getSuffix());
        assertEquals(filter.isAccept(), filterResult.isAccept());
    }

    @Test
    public void testInsertObjectCommand() {
        InsertObjectCommand command = (InsertObjectCommand) CommandFactory.newInsert("String value");
        InsertObjectCommand result = roundTrip(command);

        assertEquals("String value", result.getObject().toString());
    }

    @Test
    public void testModifyCommand() {
        FactHandle factHandle = DefaultFactHandle.createFromExternalFormat("0:234:345:456:567:789");
        List<Setter> setters = Arrays.asList(CommandFactory.newSetter("age", "30"), CommandFactory.newSetter("salary", "5000"));
        ModifyCommand command = (ModifyCommand) CommandFactory.newModify(factHandle, setters);
        ModifyCommand result = roundTrip(command);

        assertEquals(2, result.getSetters().size());
        assertEquals("0:234:345:456:567:789:NON_TRAIT:null", result.getFactHandle().toExternalForm());
    }

    @Test
    public void testUpdateCommand() {
        SimplePojo pojo = new SimplePojo("ID12345", 100, "string-data");
        DisconnectedFactHandle discFactHandle = new DisconnectedFactHandle(2, 3, 4, 5l, "entry-point-id", pojo, true);
        SimplePojo updatedPojo = new SimplePojo("ID12345", 200, "updated-string-data");
        String[] modifiedProperties = new String[]{"intData", "stringData"};
        UpdateCommand command = new UpdateCommand(discFactHandle, updatedPojo, modifiedProperties);
        UpdateCommand result = roundTrip(command);

        verifyDisconnectedFactHandle(discFactHandle, result.getHandle());
        assertEquals(command.getEntryPoint(), result.getEntryPoint());
        assertEquals(command.getObject(), result.getObject());
        assertEquals(Arrays.asList(command.getModifiedProperties()), Arrays.asList(result.getModifiedProperties()));
    }

    @Test
    public void testUpdateCommandInsideBatch() {
        SimplePojo pojo = new SimplePojo("ID12345", 100, "string-data");
        DisconnectedFactHandle discFactHandle = new DisconnectedFactHandle(2, 3, 4, 5l, "entry-point-id", pojo, true);
        SimplePojo updatedPojo = new SimplePojo("ID12345", 200, "updated-string-data");
        String[] modifiedProperties = new String[]{"intData", "stringData"};
        UpdateCommand command = new UpdateCommand(discFactHandle, updatedPojo, modifiedProperties);

        List<Command<?>> commands = new ArrayList<Command<?>>();
        BatchExecutionCommandImpl batch = (BatchExecutionCommandImpl) KieServices.Factory.get().getCommands().newBatchExecution(commands, "myKieSession");
        commands.add(command);

        BatchExecutionCommandImpl batchResult = roundTrip(batch);
        UpdateCommand result = (UpdateCommand) batchResult.getCommands().get(0);

        verifyDisconnectedFactHandle(discFactHandle, result.getHandle());
        assertEquals(command.getEntryPoint(), result.getEntryPoint());
        assertEquals(command.getObject(), result.getObject());
        assertEquals(Arrays.asList(command.getModifiedProperties()), Arrays.asList(result.getModifiedProperties()));
    }

    @Test
    public void testGetObjectCommand() {
        DisconnectedFactHandle discFactHandle = new DisconnectedFactHandle(2, 3, 4, 5l, "entry-point-id", "str-obj", true);
        GetObjectCommand command = new GetObjectCommand(discFactHandle, "out-id");
        GetObjectCommand result = roundTrip(command);

        verifyDisconnectedFactHandle(discFactHandle, result.getDisconnectedFactHandle());
        assertEquals(command.getOutIdentifier(), result.getOutIdentifier());
    }
}
