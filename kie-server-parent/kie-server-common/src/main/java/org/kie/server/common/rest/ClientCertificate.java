/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.common.rest;

public class ClientCertificate {

    private String certName;

    private String certPassword;

    private String keystore;

    private String keystorePassword;

    private String truststore;

    private String truststorePassword;

    public String getCertName() {
        return certName;
    }

    public ClientCertificate setCertName(String certName) {
        this.certName = certName;
        return this;
    }

    public String getCertPassword() {
        return certPassword;
    }

    public ClientCertificate setCertPassword(String certPassword) {
        this.certPassword = certPassword;
        return this;
    }

    public String getKeystore() {
        return keystore;
    }

    public ClientCertificate setKeystore(String keystore) {
        this.keystore = keystore;
        return this;
    }

    public String getKeystorePassword() {
        return keystorePassword;
    }

    public ClientCertificate setKeystorePassword(String keystorePassword) {
        this.keystorePassword = keystorePassword;
        return this;
    }

    public String getTruststore() {
        return truststore;
    }

    public ClientCertificate setTruststore(String truststore) {
        this.truststore = truststore;
        return this;
    }

    public String getTruststorePassword() {
        return truststorePassword;
    }

    public ClientCertificate setTruststorePassword(String truststorePassword) {
        this.truststorePassword = truststorePassword;
        return this;
    }
}
