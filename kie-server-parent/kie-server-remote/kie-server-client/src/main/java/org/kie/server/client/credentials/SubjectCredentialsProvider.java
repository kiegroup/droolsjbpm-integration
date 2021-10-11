/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.client.credentials;

import java.io.UnsupportedEncodingException;
import java.util.Base64;

import javax.security.jacc.PolicyContextException;

import org.kie.server.client.CredentialsProvider;
import org.wildfly.security.auth.server.IdentityCredentials;
import org.wildfly.security.auth.server.SecurityDomain;
import org.wildfly.security.auth.server.SecurityIdentity;
import org.wildfly.security.credential.PasswordCredential;
import org.wildfly.security.password.interfaces.ClearPassword;

import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;

public class SubjectCredentialsProvider implements CredentialsProvider {

    @Override
    public String getHeaderName() {
        return AUTHORIZATION;
    }

    @Override
    public String getAuthorization() {

        try {
            SecurityIdentity securityIdentity = null;
            ClearPassword password = null;

            SecurityDomain securityDomain = SecurityDomain.getCurrent();
            if (securityDomain != null) {
                securityIdentity = securityDomain.getCurrentSecurityIdentity();
                IdentityCredentials credentials = securityIdentity.getPrivateCredentials();
                if (credentials != null) {
                    ClearPassword clearPassword = getClearPassword(credentials);
                    if (clearPassword != null) {
                        String basicAuthHeader = "Basic " + Base64.getEncoder().encodeToString((securityIdentity.getPrincipal().getName() + ":" + String.valueOf(clearPassword.getPassword())).getBytes("UTF-8"));
                        return basicAuthHeader;
                    }
                }
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    private ClearPassword getClearPassword(final IdentityCredentials credentials) {
        if (credentials.contains(PasswordCredential.class)) {
            return credentials.getCredential(PasswordCredential.class).getPassword(ClearPassword.class);
        }
        return null;
    }
}
