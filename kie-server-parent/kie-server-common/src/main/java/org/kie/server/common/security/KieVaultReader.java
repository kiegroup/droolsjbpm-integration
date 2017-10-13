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

import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KieVaultReader {

    private static final Logger logger = LoggerFactory.getLogger(KieVaultReader.class);

    private static final Pattern VAULT_PATTERN = Pattern.compile("VAULT::.*::.*::.*");

    private KieVaultReader() {
    }

    public static boolean isVaultFormat(String str) {
        return str != null && VAULT_PATTERN.matcher(str).matches();
    }

    public static boolean haveEAPVault() {
        try {
            Class.forName("org.jboss.security.vault.SecurityVaultFactory");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public static String decryptValue(String encryptedValue) throws SecurityException {
        String value = null;

        if (!isVaultFormat(encryptedValue)) {
            throw new SecurityException("Password is not in vault format.");
        }

        String[] tokens = tokens(encryptedValue);
        String vaultBlock = tokens[1];
        String attributeName = tokens[2];
        byte[] sharedKey = null;
        if (tokens.length > 3) {
            sharedKey = tokens[3].getBytes(StandardCharsets.UTF_8);
        }
        try {
            Class secVaultFactory = Class.forName("org.jboss.security.vault.SecurityVaultFactory");
            Method svfGet = secVaultFactory.getDeclaredMethod("get", null);
            final Object secVault = svfGet.invoke(secVaultFactory, null);

            Method svExists = secVault.getClass().getDeclaredMethod("exists", String.class, String.class);
            final boolean exists = (boolean) svExists.invoke(secVault, vaultBlock, attributeName);

            if (exists) {
                Method svRetrieve = secVault.getClass().getDeclaredMethod("retrieve", String.class, String.class, byte[].class);
                final char[] pass = (char[]) svRetrieve.invoke(secVault, vaultBlock, attributeName, sharedKey);
                value = String.valueOf(pass);
            } else {
                throw new SecurityException(
                        String.format("Attribute %s does not exist in vaultblock %s",
                                      attributeName, vaultBlock));
            }
        } catch (ClassNotFoundException e) {
            logger.warn("Unable to locate vault files. Missing class " + e.getMessage());
        } catch (Exception ex) {
            logger.warn("Error while reading vault", ex);
        }
        return value;
    }

    private static String[] tokens(String vaultString) {
        StringTokenizer tokenizer = new StringTokenizer(vaultString, "::");
        int length = tokenizer.countTokens();
        String[] tokens = new String[length];
        int index = 0;
        while (tokenizer.hasMoreTokens()) {
            tokens[index++] = tokenizer.nextToken();
        }
        return tokens;
    }
}
