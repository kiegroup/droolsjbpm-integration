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

import java.security.Principal;
import java.util.Set;
import javax.security.auth.Subject;
import javax.security.jacc.PolicyContext;

import org.kie.server.client.CredentialsProvider;

import static javax.ws.rs.core.HttpHeaders.AUTHORIZATION;

public class SubjectCredentialsProvider implements CredentialsProvider {

    @Override
    public String getHeaderName() {
        return AUTHORIZATION;
    }

    @Override
    public String getAuthorization() {

        Subject subject = getSubjectFromContainer();
        if (subject != null && subject.getPrincipals() != null) {

            Set<Principal> principals = subject.getPrincipals();
            for (Principal principal : principals) {

                if (match(principal)) {
                    return principal.getName();
                }
            }
        }

        return null;
    }

    // TODO make sure this can be taken out for all supported containers
    protected Subject getSubjectFromContainer() {
        try {
            return (Subject) PolicyContext.getContext("javax.security.auth.Subject.container");
        } catch (Exception e) {
            return null;
        }
    }

    protected boolean match(Principal principal) {
        if (principal.getClass().getName().endsWith("BasicAuthorizationPrincipal")) {
            return true;
        }

        return false;
    }
}
