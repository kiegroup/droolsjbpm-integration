/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.processmigration.jpa.converter;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Test;

public class CryptoConverterTest {

    private static final String VALID_PWD = "gUkXp2s5v8x/A?D(";
    @After
    public void unsetProperty() {
        System.clearProperty(CryptoConverter.PASSWORD_PROPERTY);
    }

    @Test(expected = RuntimeException.class)
    public void testEncryptPropertyNotSet() {
        new CryptoConverter();
    }

    @Test(expected = RuntimeException.class)
    public void testEncryptPropertyWrongLength() {
        System.setProperty(CryptoConverter.PASSWORD_PROPERTY, "testPwd123");
        new CryptoConverter();
    }

    @Test
    public void testEncrypt() {
        System.setProperty(CryptoConverter.PASSWORD_PROPERTY, VALID_PWD);
        String expected = "Hello world";
        CryptoConverter converter = new CryptoConverter();
        byte[] encrypted = converter.convertToDatabaseColumn(expected);
        assertEquals(expected, converter.convertToEntityAttribute(encrypted));
    }
}
