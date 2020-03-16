/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.services.taskassigning.planning.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertyUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(PropertyUtil.class);

    public interface PropertyParser<T, E extends Exception> {

        T parse(String value) throws E;
    }

    private PropertyUtil() {
    }

    public static <T, E extends Exception> T readSystemProperty(String propertyName,
                                                                T defaultValue,
                                                                PropertyParser<T, E> parser) {
        String strValue = null;
        try {
            strValue = System.getProperty(propertyName);
            if (strValue == null) {
                LOGGER.debug("Property: {}  was not configured. Default value will be used instead: {}", propertyName,
                             defaultValue);
                return defaultValue;
            }
            return parser.parse(strValue);
        } catch (Exception e) {
            LOGGER.error("An error was produced while parsing " + propertyName + " value from string: " + strValue +
                                 ", default value: " + defaultValue + " will be used instead.", e);
            return defaultValue;
        }
    }
}
