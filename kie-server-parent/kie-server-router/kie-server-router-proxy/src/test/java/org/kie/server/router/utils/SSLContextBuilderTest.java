/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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

import java.io.IOException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.junit.Assert.*;

public class SSLContextBuilderTest {

    private static final String KEYSTORE_PASSWORD = "jboss@01";
    private static final String KEYSTORE_ALIAS_ONE = "jboss";
    private static final String KEYSTORE_ALIAS_TWO = "jason";

    private static final String KEYSTORE_PATH = SSLContextBuilder.class.getClassLoader().getResource("keystores/router.keystore").getFile();
    private static final String TRUSTSTORE_PATH = SSLContextBuilder.class.getClassLoader().getResource("keystores/router.truststore").getFile();

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Test
    public void testFirstAlias() {
        try {
            SSLContextBuilder.builder()
                    .setKeyStorePath(KEYSTORE_PATH)
                    .setKeyStorePassword(KEYSTORE_PASSWORD)
                    .setKeyAlias(KEYSTORE_ALIAS_ONE)
                    .build();
        } catch (RuntimeException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testSecondAlias() {
        try {
            SSLContextBuilder.builder()
                    .setKeyStorePath(KEYSTORE_PATH)
                    .setKeyStorePassword(KEYSTORE_PASSWORD)
                    .setKeyAlias(KEYSTORE_ALIAS_TWO)
                    .build();
        } catch (RuntimeException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testNonExistingAlias() {
        try {
            SSLContextBuilder.builder()
                    .setKeyStorePath(KEYSTORE_PATH)
                    .setKeyStorePassword(KEYSTORE_PASSWORD)
                    .setKeyAlias("bla")
                    .build();
            // should fail
            fail("Exception not thrown");
        } catch (RuntimeException e) {
            assertTrue(e.getCause() instanceof IllegalArgumentException);
        }
    }

    @Test
    public void testIncorrectPassword() {
        try {
            SSLContextBuilder.builder()
                    .setKeyStorePath(KEYSTORE_PATH)
                    .setKeyStorePassword("bla")
                    .setKeyAlias(KEYSTORE_ALIAS_ONE)
                    .build();
            // should fail
            fail("Exception not thrown");
        } catch (RuntimeException e) {
            assertTrue(e.getCause() instanceof IOException);
        }
    }

    @Test
    public void testBuildTruststore() {
        try {
            SSLContextBuilder.builder()
                    .setKeyStorePath(TRUSTSTORE_PATH)
                    .setKeyStorePassword("mykeystorepass")
                    .buildTrustore();
        } catch (RuntimeException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void testBuildTruststoreWithIncorrectPassword() {
        try {
            SSLContextBuilder.builder()
                    .setKeyStorePath(TRUSTSTORE_PATH)
                    .setKeyStorePassword("space_invaders")
                    .buildTrustore();
            // should fail
            fail("Exception not thrown");
        } catch (RuntimeException e) {
            assertTrue(e.getCause() instanceof IOException);
        }
    }
}
