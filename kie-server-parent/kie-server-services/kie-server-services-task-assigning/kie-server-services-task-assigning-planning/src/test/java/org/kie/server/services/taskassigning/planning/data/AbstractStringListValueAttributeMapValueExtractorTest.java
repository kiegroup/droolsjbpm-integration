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

package org.kie.server.services.taskassigning.planning.data;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.kie.server.api.model.taskassigning.TaskData;
import org.kie.server.services.taskassigning.user.system.api.User;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class AbstractStringListValueAttributeMapValueExtractorTest<M extends Map<String, ?>, T, E extends AbstractAttributeMapValueLabelValueExtractor<M, T>>
        extends AbstractAttributeMapValueLabelValueExtractorTest<M, T, E> {

    protected static TaskData mockTaskData(String attributeName, String value) {
        Map<String, Object> inputData = new HashMap<>();
        inputData.put(attributeName, value);
        return TaskData.builder()
                .inputData(inputData)
                .build();
    }

    protected static User mockUser(String attributeName, String value) {
        User user = mock(User.class);
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(attributeName, value);
        when(user.getAttributes()).thenReturn(attributes);
        return user;
    }

    protected static Set<Object> mockSet(Object... values) {
        return new HashSet<>(Arrays.asList(values));
    }
}
