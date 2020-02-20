/*
 * Copyright 2018 Red Hat, Inc. and/or its affiliates.
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
package org.kie.server.router.utils;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.Enumeration;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509KeyManager;

import org.jboss.logging.Logger;

public class SSLContextBuilder {

    private static final Logger logger = Logger.getLogger(SSLContextBuilder.class);

    private static final String KEYSTORE_TYPE = "JKS";

    private String keystorePath;
    private String keystorePassword;
    private String keyAlias;

    public static SSLContextBuilder builder() {
        return new SSLContextBuilder();
    }

    public SSLContextBuilder setKeyStorePath(String keystorePath) {
        this.keystorePath = keystorePath;
        return this;
    }

    public SSLContextBuilder setKeyStorePassword(String keystorePassword) {
        this.keystorePassword = keystorePassword;
        return this;
    }

    public SSLContextBuilder setKeyAlias(String keystoreKeyAlias) {
        this.keyAlias = keystoreKeyAlias;
        return this;
    }

    public SSLContext buildTrustore() {
        try (FileInputStream fis = new FileInputStream(keystorePath)) {
            KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
            char[] trustStorePassword = keystorePassword.toCharArray();
            trustStore.load(fis, trustStorePassword);
            SSLContext context = SSLContext.getInstance("TLSv1.2");
            TrustManagerFactory factory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            factory.init(trustStore);
            TrustManager[] managers = factory.getTrustManagers();
            context.init(null, managers, null);
            SSLContext.setDefault(context);
            return SSLContext.getDefault();
        } catch (final Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public SSLContext build() {
        logger.info("KeyStore path: " + keystorePath);
        try (FileInputStream fis = new FileInputStream(keystorePath)) {
            KeyStore keyStore = KeyStore.getInstance(KEYSTORE_TYPE);
            // We use the same password for the store and the key.
            char[] password = keystorePassword.toCharArray();
            keyStore.load(fis,
                          password);

            //Check whether the keystore contains a key with the provided alias. If not, return a list of aliases in the current keystore.
            if (!keyStore.containsAlias(keyAlias)) {
                Enumeration<String> aliases = keyStore.aliases();
                StringBuilder messageBuilder = new StringBuilder("KeyStore does not contain configured alias '").append(keyAlias).append("'.");
                messageBuilder.append("Valid aliases are ");
                messageBuilder.append("{");
                while (aliases.hasMoreElements()) {
                    String nextAlias = aliases.nextElement();
                    messageBuilder.append(nextAlias);
                    if (aliases.hasMoreElements()) {
                        messageBuilder.append(", ");
                    }
                }
                messageBuilder.append("}.");
                throw new IllegalArgumentException(messageBuilder.toString());
            }

            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore,
                                   password);

            // Wrapping keyManagers in AliasedKeyManager so we can select the certificate based on the passed key alias.
            // The AliasedX509ExtendedKeyManager is copied from Apache Camel codebase (CXF actually contains a copy of the exact same
            // class).
            KeyManager[] keyManagers = keyManagerFactory.getKeyManagers();
            KeyManager[] aliasedKeyManagers = new KeyManager[keyManagers.length];
            int arrayIndex = 0;
            for (KeyManager nextKeyManager : keyManagers) {
                AliasedX509ExtendedKeyManager aliasedKeyMananger;
                try {
                    // Don't really know why the constructor throws an exception, but every copy of this code (e.g. CXF, Camel, etc.) seems
                    // to do this.
                    aliasedKeyMananger = new AliasedX509ExtendedKeyManager(keyAlias,
                                                                           (X509KeyManager) nextKeyManager);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                aliasedKeyManagers[arrayIndex] = aliasedKeyMananger;
                arrayIndex++;
            }

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(aliasedKeyManagers,
                            null,
                            null);

            return sslContext;
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
