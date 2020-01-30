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

package org.kie.server.api.model.taskassigning.util;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;
import org.kie.api.task.model.Status;

import static org.junit.Assert.assertEquals;
import static org.kie.api.task.model.Status.Completed;
import static org.kie.api.task.model.Status.Ready;

public class StatusConverterTest {

    @Test
    public void convertFromString() {
        for (Status status : Status.values()) {
            assertEquals(status, StatusConverter.convertFromString(status.name()));
        }
    }

    @Test
    public void convertToString() {
        for (Status status : Status.values()) {
            assertEquals(status.name(), StatusConverter.convertToString(status));
        }
    }

    @Test
    public void convertToStringListEllipsis() {
        List<String> result = Stream.of(Status.values()).map(Status::name).collect(Collectors.toList());
        assertEquals(result, StatusConverter.convertToStringList(Status.values()));
    }

    @Test
    public void convertToStringList() {
        List<String> result = Stream.of(Status.values()).map(Status::name).collect(Collectors.toList());
        assertEquals(result, StatusConverter.convertToStringList(Arrays.asList(Status.values())));
    }
}
