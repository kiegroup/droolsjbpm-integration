/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.common.security;

import org.junit.Assert;
import org.junit.Test;

import static org.kie.server.common.security.KieVaultReader.isVaultFormat;

public class KieVaultReaderTest {

    private final String VAULT_CORRECT_KEY = "VAULT::kievb::org.kie.server.controller.pwd::1";
    private final String VAULT_INCORRECT_KEY = "VAULT_KEY";

    @Test
    public void isVaultFormatTest() {
        boolean vaultFormat = isVaultFormat(null);
        Assert.assertFalse(vaultFormat);

        vaultFormat = isVaultFormat(VAULT_INCORRECT_KEY);
        Assert.assertFalse(vaultFormat);

        vaultFormat = isVaultFormat(VAULT_CORRECT_KEY);
        Assert.assertTrue(vaultFormat);
    }

    @Test
    public void haveEAPVaultTest() throws Exception {
        final boolean haveEAPVault = KieVaultReader.haveEAPVault();

        Assert.assertFalse(haveEAPVault);
    }

    @Test
    public void decryptValueTest() throws Exception {
        final String value = KieVaultReader.decryptValue(VAULT_CORRECT_KEY);

        Assert.assertNull(value);
    }
}