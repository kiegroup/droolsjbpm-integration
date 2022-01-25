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

package org.kie.server.api.marshalling.json;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

import org.drools.core.command.runtime.BatchExecutionCommandImpl;
import org.drools.core.command.runtime.rule.InsertObjectCommand;
import org.junit.Test;
import org.kie.api.command.Command;
import org.kie.server.api.KieServerConstants;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.marshalling.MarshallingException;
import org.kie.server.api.marshalling.MarshallingFormat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;

public class JSONUnmarshallingTest {

    @Test
    public void testNonExistingClassDefault() {
        try {
            // default to IGNORE
            Object obj = unmarshalNonExistingClass();
            assertEquals(LinkedHashMap.class, obj.getClass());
        } finally {
            System.clearProperty(KieServerConstants.JSON_CUSTOM_OBJECT_DESERIALIZER_CNFE_BEHAVIOR);
        }
    }

    @Test
    public void testNonExistingClassWarn() {
        try {
            System.setProperty(KieServerConstants.JSON_CUSTOM_OBJECT_DESERIALIZER_CNFE_BEHAVIOR, "WARN");
            Object obj = unmarshalNonExistingClass();
            assertEquals(LinkedHashMap.class, obj.getClass());
        } finally {
            System.clearProperty(KieServerConstants.JSON_CUSTOM_OBJECT_DESERIALIZER_CNFE_BEHAVIOR);
        }
    }

    @Test
    public void testNonExistingClassException() {
        try {
            System.setProperty(KieServerConstants.JSON_CUSTOM_OBJECT_DESERIALIZER_CNFE_BEHAVIOR, "EXCEPTION");
            assertThrows(MarshallingException.class, () -> unmarshalNonExistingClass());
        } finally {
            System.clearProperty(KieServerConstants.JSON_CUSTOM_OBJECT_DESERIALIZER_CNFE_BEHAVIOR);
        }
    }

    @Test
    public void testWrongCNFEBehaviorValue() {
        try {
            System.setProperty(KieServerConstants.JSON_CUSTOM_OBJECT_DESERIALIZER_CNFE_BEHAVIOR, "XXX");
            MarshallingException exception = assertThrows(MarshallingException.class, () -> unmarshalNonExistingClass());
            assertThat(exception.getMessage()).contains(Arrays.asList("IGNORE", "WARN", "EXCEPTION"));
        } finally {
            System.clearProperty(KieServerConstants.JSON_CUSTOM_OBJECT_DESERIALIZER_CNFE_BEHAVIOR);
        }
    }

    private Object unmarshalNonExistingClass() {
        Set<Class<?>> extraClasses = new HashSet<Class<?>>();
        Marshaller marshaller = MarshallerFactory.getMarshaller(extraClasses, MarshallingFormat.JSON, this.getClass().getClassLoader());
        String json = "{\"com.nonexisting.Person\":{\"name\":\"taro\",\"age\":\"20\"}}";
        return marshaller.unmarshall(json, Object.class);
    }

    @Test
    public void testNonExistingClassDefaultInDroolsCommand() {
        try {
            // default to IGNORE
            Object obj = unmarshalNonExistingClassInDroolsCommand();
            assertEquals(BatchExecutionCommandImpl.class, obj.getClass());
            BatchExecutionCommandImpl batch = (BatchExecutionCommandImpl) obj;
            Command<?> command = batch.getCommands().get(0);
            assertEquals(InsertObjectCommand.class, command.getClass());
            assertEquals(LinkedHashMap.class, ((InsertObjectCommand) command).getObject().getClass());
        } finally {
            System.clearProperty(KieServerConstants.JSON_CUSTOM_OBJECT_DESERIALIZER_CNFE_BEHAVIOR);
        }
    }

    @Test
    public void testNonExistingClassWarnInDroolsCommand() {
        try {
            System.setProperty(KieServerConstants.JSON_CUSTOM_OBJECT_DESERIALIZER_CNFE_BEHAVIOR, "WARN");
            Object obj = unmarshalNonExistingClassInDroolsCommand();
            assertEquals(BatchExecutionCommandImpl.class, obj.getClass());
            BatchExecutionCommandImpl batch = (BatchExecutionCommandImpl) obj;
            Command<?> command = batch.getCommands().get(0);
            assertEquals(InsertObjectCommand.class, command.getClass());
            assertEquals(LinkedHashMap.class, ((InsertObjectCommand) command).getObject().getClass());
        } finally {
            System.clearProperty(KieServerConstants.JSON_CUSTOM_OBJECT_DESERIALIZER_CNFE_BEHAVIOR);
        }
    }

    @Test
    public void testNonExistingClassExceptionInDroolsCommand() {
        try {
            System.setProperty(KieServerConstants.JSON_CUSTOM_OBJECT_DESERIALIZER_CNFE_BEHAVIOR, "EXCEPTION");
            assertThrows(MarshallingException.class, () -> unmarshalNonExistingClassInDroolsCommand());
        } finally {
            System.clearProperty(KieServerConstants.JSON_CUSTOM_OBJECT_DESERIALIZER_CNFE_BEHAVIOR);
        }
    }

    private Object unmarshalNonExistingClassInDroolsCommand() {
        Set<Class<?>> extraClasses = new HashSet<Class<?>>();
        Marshaller marshaller = MarshallerFactory.getMarshaller(extraClasses, MarshallingFormat.JSON, this.getClass().getClassLoader());
        String json = "{\n" +
                      "  \"lookup\" : \"StatelessKieSession\",\n" +
                      "    \"commands\" : [ {\n" +
                      "      \"insert\" : {\n" +
                      "        \"object\" : {\"com.nonexisting.Person\":{\"name\":\"taro\",\"age\":\"20\"}},\n" +
                      "        \"out-identifier\" : \"person\",\n" +
                      "        \"return-object\" : \"true\",\n" +
                      "        \"entry-point\" : \"DEFAULT\"\n" +
                      "       }\n" +
                      "    }, {\n" +
                      "        \"fire-all-rules\" : { }\n" +
                      "   } ]\n" +
                      "}";
        return marshaller.unmarshall(json, BatchExecutionCommandImpl.class);
    }
}
