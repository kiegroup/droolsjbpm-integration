/*
 * Copyright 2019 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.integrationtests.router.rest;

import java.security.Principal;
import java.util.Collections;
import java.util.Set;

import io.undertow.security.idm.Account;
import io.undertow.security.idm.Credential;
import io.undertow.security.idm.PasswordCredential;
import org.kie.server.router.identity.IdentityService;


public class MockIdentityService implements IdentityService {

    @Override
    public String id() {
        return "mock";
    }

    @Override
    public Account verify(Account account) {
        return null;
    }

    @Override
    public Account verify(final String id, Credential credential) {
        String password ="";
        if (credential instanceof PasswordCredential) {
            password = new String(((PasswordCredential)credential).getPassword());
        }
        
        if (!"mockUser".equals(id) || !"mockPassword".equals(password)) {
            return null;
        }
        return new Account() {


           private static final long serialVersionUID = 1L;


            @Override
            public Principal getPrincipal() {
                return new Principal() {

                    @Override
                    public String getName() {
                        return id;
                    }

                };
            }

            @Override
            public Set<String> getRoles() {
                return Collections.emptySet();
            }

        };
    }

    @Override
    public Account verify(Credential credential) {
        return null;
    }

}
