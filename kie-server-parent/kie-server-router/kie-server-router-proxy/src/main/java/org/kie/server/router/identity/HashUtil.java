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

package org.kie.server.router.identity;


import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class HashUtil {

    private static final String HASH_ALGORITHM = "SHA-512";

    private static final byte COLON = ':';

    private HashUtil() {}

    public static byte[] toBytes(String user, String password) {
        return (user + COLON + password).getBytes(StandardCharsets.UTF_8);
    }

    public static String hash(String user, String password) {
        return hash(user, password, HASH_ALGORITHM);
    }

    public static String hash(String user, String password, String algorithm) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance(algorithm);
            byte[] bytes = md.digest(toBytes(user, password));
            return Base64.getEncoder().encodeToString(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
