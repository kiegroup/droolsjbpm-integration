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

import org.kie.server.client.CredentialsProvider;

import static javax.ws.rs.core.HttpHeaders.*;

/**
 * Token based implementation of <code>CredentialsProvider</code> that is expected to get
 * valid token when instantiating instance and then return
 * Bearer type of authorization header.
 */
public class EnteredTokenCredentialsProvider implements CredentialsProvider {

    private String token;

    public EnteredTokenCredentialsProvider(String token) {
        this.token = token;
    }

    @Override
    public String getHeaderName() {
        return AUTHORIZATION;
    }

    @Override
    public String getAuthorization() {
        return TOKEN_AUTH_PREFIX + token;
    }
}
