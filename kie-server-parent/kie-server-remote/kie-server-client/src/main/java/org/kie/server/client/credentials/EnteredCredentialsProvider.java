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

import org.kie.server.common.rest.Base64Util;
import org.kie.server.client.CredentialsProvider;

import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;

/**
 * Default implementation of <code>CredentialsProvider</code> that is expected to get
 * user name and password when instantiating instance and then return
 * Basic type of authorization header.
 */
public class EnteredCredentialsProvider implements CredentialsProvider {

    private String username;
    private String password;

    public EnteredCredentialsProvider(String username, String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    public String getHeaderName() {
        return AUTHORIZATION;
    }

    @Override
    public String getAuthorization() {
        return BASIC_AUTH_PREFIX + Base64Util.encode(username + ':' + password);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
