/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.kie.server.router.utils;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

public final class MediaTypeUtil {

    private MediaTypeUtil() {}

    public static Map<String, String> extractParameterFromMediaTypeString(String input) {
        int idx = input.indexOf(';');
        if (idx < 0) {
            return Collections.emptyMap();
        }
        input = input.substring(idx + 1);
        return Arrays.stream(input.split(","))
                                           .map(s -> s.split("="))
                                           .filter(a -> a.length == 2)
                                           .collect(Collectors.toMap(
                                                                     a -> a[0].trim(),
                                                                     a -> a[1].trim()));
    }

}
