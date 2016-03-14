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

package org.kie.security.jaas;

import java.io.IOException;
import java.security.Principal;
import java.util.Base64;
import java.util.Map;
import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;


public class KieLoginModule implements LoginModule {

    private CallbackHandler handler;
    private Subject subject;

    protected boolean committed = false;
    protected Principal principal = null;

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        this.handler = callbackHandler;
        this.subject = subject;
    }

    @Override
    public boolean login() throws LoginException {
        Callback[] callbacks = new Callback[2];
        callbacks[0] = new NameCallback("login");
        callbacks[1] = new PasswordCallback("password", true);

        try {
            handler.handle(callbacks);
            String name = ((NameCallback) callbacks[0]).getName();
            String password = String.valueOf(((PasswordCallback) callbacks[1]).getPassword());

            if (name != null && password != null) {
                String basicAuthHeader = "Basic " + Base64.getEncoder().encodeToString((name + ":" + password).getBytes("UTF-8"));
                principal = new BasicAuthorizationPrincipal(basicAuthHeader);
                return true;
            }


            throw new LoginException("Unable to proceed as there is no username or password given");

        } catch (IOException e) {
            throw new LoginException(e.getMessage());
        } catch (UnsupportedCallbackException e) {
            throw new LoginException(e.getMessage());
        }
    }

    @Override
    public boolean commit() throws LoginException {
        // If authentication was not successful, just return false
        if (principal == null) {
            return (false);
        }

        // Add our Principal to the Subject if needed
        if (!subject.getPrincipals().contains(principal)) {
            subject.getPrincipals().add(principal);
        }

        committed = true;
        return true;
    }

    @Override
    public boolean abort() throws LoginException {
        if(this.principal == null) {
            return false;
        } else {
            if(this.committed) {
                this.logout();
            } else {
                this.committed = false;
                this.principal = null;
            }

            return true;
        }
    }

    @Override
    public boolean logout() throws LoginException {
        this.subject.getPrincipals().remove(this.principal);
        this.committed = false;
        this.principal = null;
        return true;
    }
}
