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
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.kie.api.task.model.Status;

public class StatusConverter {

    private StatusConverter() {
    }

    public static Status convertFromString(String value) {
        return Status.valueOf(value);
    }

    public static String convertToString(Status value) {
        return value.name();
    }

    public static List<String> convertToStringList(Status... status) {
        return convertToStringList(Arrays.stream(status));
    }

    public static List<String> convertToStringList(List<Status> statusList) {
        return convertToStringList(Optional.ofNullable(statusList).orElse(Collections.emptyList()).stream());
    }

    private static List<String> convertToStringList(Stream<Status> stream) {
        return stream.map(StatusConverter::convertToString).collect(Collectors.toList());
    }
}
