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

import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.kie.processmigration.model.exceptions.CredentialsException;

@Converter
public class CryptoConverter implements AttributeConverter<String, byte[]> {

    public static final String PASSWORD_PROPERTY = "credentials.password";
    
    private static final int AES_BLOCKSIZE = 128;
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/CBC/PKCS5PADDING";
    private static final SecureRandom RANDOM = new SecureRandom();

    private SecretKeySpec secretKey;

    private Cipher encryptCipher;
    private Cipher decryptCipher;

    public CryptoConverter() {
        init();
    }

    private void init() {
        try {
            String password = System.getProperty(PASSWORD_PROPERTY);
            if (password == null) {
                throw new CredentialsException("A system property " + PASSWORD_PROPERTY + " containing a key of length " + AES_BLOCKSIZE + " bytes must be provided");
            }
            if (password.getBytes().length != AES_BLOCKSIZE / 8) {
                throw new CredentialsException("Key length must be " + AES_BLOCKSIZE + " bytes. Received: " + password.getBytes().length);
            }
            secretKey = new SecretKeySpec(password.getBytes(), ALGORITHM);
            encryptCipher = Cipher.getInstance(TRANSFORMATION);
            decryptCipher = Cipher.getInstance(TRANSFORMATION);
        } catch (Exception e) {
            throw new RuntimeException("Unable to initialize ciphers", e);
        }
    }

    @Override
    public byte[] convertToDatabaseColumn(String attribute) {
        if (attribute == null) {
            return null;
        }
        try {
            byte[] iv = getNextIv();
            encryptCipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));
            return encryptCipher.doFinal(concatenateArrays(iv, attribute.getBytes()));
        } catch (Exception e) {
            throw new RuntimeException("Unable to encrypt credentials", e);
        }
    }

    @Override
    public String convertToEntityAttribute(byte[] dbData) {
        if (dbData == null) {
            return null;
        }
        try {
            byte[] iv = Arrays.copyOf(dbData, AES_BLOCKSIZE / 8);
            decryptCipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
            byte[] data = Arrays.copyOfRange(dbData, AES_BLOCKSIZE / 8, dbData.length);
            return new String(decryptCipher.doFinal(data));
        } catch (Exception e) {
            throw new RuntimeException("Unable to decrypt credentials", e);
        }
    }

    private byte[] getNextIv() {
        byte[] iv = new byte[AES_BLOCKSIZE / 8];
        RANDOM.nextBytes(iv);
        return iv;
    }

    private byte[] concatenateArrays(byte[] a, byte[] b) {
        byte[] rv = new byte[a.length + b.length];
        System.arraycopy(a, 0, rv, 0, a.length);
        System.arraycopy(b, 0, rv, a.length, b.length);
        return rv;
    }

}
