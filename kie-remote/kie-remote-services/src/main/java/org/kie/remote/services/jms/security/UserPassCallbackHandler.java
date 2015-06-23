/*
 * Copyright 2015 JBoss Inc
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

package org.kie.remote.services.jms.security;

import java.io.IOException;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.sasl.RealmCallback;

import org.kie.remote.services.jms.RequestMessageBean;

/**
 * This is the basic, default implementation of a User/Pass callback handler
 * for a JAAS login. 
 * </p>
 * This class is used by the JMS {@link RequestMessageBean} in order to retrieve
 * the subject for the user/pass info stored in a message (the Bean itself runs as
 * an anonymous user).
 */
public class UserPassCallbackHandler implements CallbackHandler {

    private final String[] credentials;

    public UserPassCallbackHandler(String[] credentials) {
        this.credentials = credentials;
    }

    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        for (Callback current : callbacks) {
            if (current instanceof NameCallback) {
                NameCallback ncb = (NameCallback) current;
                ncb.setName(credentials[0]);
            } else if (current instanceof PasswordCallback) {
                PasswordCallback pcb = (PasswordCallback) current;
                pcb.setPassword(credentials[1].toCharArray());
            } else if (current instanceof RealmCallback) {
                RealmCallback realmCallback = (RealmCallback) current;
                realmCallback.setText(realmCallback.getDefaultText());
            } else {
                throw new UnsupportedCallbackException(current);
            }
        }

    }

}
