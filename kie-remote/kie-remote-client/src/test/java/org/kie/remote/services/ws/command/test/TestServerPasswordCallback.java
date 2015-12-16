/*
 * Copyright 2015 Red Hat, Inc. and/or its affiliates.
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

package org.kie.remote.services.ws.command.test;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;

import org.apache.ws.security.WSPasswordCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestServerPasswordCallback implements CallbackHandler {

    private final static Logger logger = LoggerFactory.getLogger(TestServerPasswordCallback.class);
    
    private Map<String, String> userPasswordMap = new ConcurrentHashMap<String, String>();
    
    public TestServerPasswordCallback() { 
        // Default constructor
        userPasswordMap.put("mary", "mary123@");
    }
    
    public void addPassword( String user, String pwd ) { 
        userPasswordMap.put( user, pwd );
    }

    public void handle( Callback[] callbacks ) throws IOException, UnsupportedCallbackException {
        WSPasswordCallback pc = (WSPasswordCallback) callbacks[0];

        String user = pc.getIdentifier();
        System.out.println("Password callback: " + user );
        String pass = null;
        try { 
            pass = getPassword(user);
        } catch (Exception e ) { 
            logger.error("Unable to retrieve password for user {}", user, e ); 
        }
        if( pass != null ) { 
            pc.setPassword(pass);
        }
    }

   
    public String getPassword( String user ) {
        if( user == null ) {
            return null;
        } 
        return userPasswordMap.get(user);
    } 
}
