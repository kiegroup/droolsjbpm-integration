/*
 * Copyright 2016 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.server.integrationtests.shared;

import java.util.Collection;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.kie.server.api.model.ServiceResponse;

public class KieServerAssert {
   public static void assertSuccess(ServiceResponse<?> response) {
        ServiceResponse.ResponseType type = response.getType();
        assertEquals("Expected SUCCESS, but got " + type + "! Response: " + response, ServiceResponse.ResponseType.SUCCESS,
                type);
    }

    public static void assertFailure(ServiceResponse<?> response) {
        ServiceResponse.ResponseType type = response.getType();
        assertEquals("Expected FAILURE, but got " + type + "! Response: " + response, ServiceResponse.ResponseType.FAILURE,
                type);
    }

    public static void assertResultContainsString(String result, String expectedString) {
        assertTrue("Expecting string '" + expectedString + "' in result, but got: " + result, result.contains(expectedString));
    }

    public static void assertResultContainsStringRegex(String result, String regex) {
        assertTrue("Regex '" + regex + "' does not matches result string '" + result + "'!",
                Pattern.compile(regex, Pattern.DOTALL).matcher(result).matches());
    }

    public static void assertResultNotContainingStringRegex(String result, String regex) {
        assertFalse("Regex '" + regex + "' matches result string '" + result + "'!",
                Pattern.compile(regex, Pattern.DOTALL).matcher(result).matches());
    }

    public static void assertNullOrEmpty(String errorMessage, Collection<?> result ) {
        if (result != null) {
            assertTrue(errorMessage, result.size() == 0);
        }
    }

    public static void assertNullOrEmpty(String errorMessage, Object[] result ) {
        if (result != null) {
            assertTrue(errorMessage, result.length == 0);
        }
    }

    public static void assertNullOrEmpty(String result ) {
        if (result != null) {
            assertTrue("String is not empty.", result.trim().isEmpty());
        }
    }

}
