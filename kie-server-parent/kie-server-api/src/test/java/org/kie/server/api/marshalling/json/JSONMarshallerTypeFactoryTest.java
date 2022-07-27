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

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.databind.util.LookupCache;
import org.drools.core.command.runtime.rule.InsertObjectCommand;
import org.junit.Test;
import org.kie.server.api.marshalling.Marshaller;
import org.kie.server.api.marshalling.MarshallerFactory;
import org.kie.server.api.marshalling.MarshallingFormat;
import org.kie.server.api.marshalling.objects.CustomPerson;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

public class JSONMarshallerTypeFactoryTest {

    @Test
    public void testTypeFactoryCacheSize() {
        Set<Class<?>> extraClasses = new HashSet<>();
        extraClasses.add(CustomPerson.class);
        Marshaller marshaller1 = MarshallerFactory.getMarshaller(extraClasses, MarshallingFormat.JSON, this.getClass().getClassLoader());
        CustomPerson john = new CustomPerson("John", 20);
        InsertObjectCommand command1 = new InsertObjectCommand(john);
        marshaller1.marshall(command1);

        Marshaller marshaller2 = MarshallerFactory.getMarshaller(extraClasses, MarshallingFormat.JSON, this.getClass().getClassLoader());
        CustomPerson paul = new CustomPerson("Paul", 17);
        InsertObjectCommand command2 = new InsertObjectCommand(paul);
        marshaller2.marshall(command2);

        TypeFactory typeFactory1 = ((JSONMarshaller) marshaller1).getTypeFactory();
        TypeFactory typeFactory2 = ((JSONMarshaller) marshaller2).getTypeFactory();

        try {
            Field field = TypeFactory.class.getDeclaredField("_typeCache");
            field.setAccessible(true);
            LookupCache<Object, JavaType> _typeCache1 = (LookupCache<Object, JavaType>) field.get(typeFactory1);
            LookupCache<Object, JavaType> _typeCache2 = (LookupCache<Object, JavaType>) field.get(typeFactory2);

            assertThat(_typeCache1.size()).isPositive();
            assertThat(_typeCache2.size()).isPositive();

            marshaller1.dispose();

            assertThat(_typeCache1.size()).isZero();
            assertThat(_typeCache2.size()).isPositive(); // marshaller2 is not affected

        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            fail("failed with reflection", e);
        }
    }
}
