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

package org.kie.remote.client.ws;

import java.lang.reflect.Method;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a thread-local {@link Authenticator} instance that allows users to make multiple (authenticated) calls to
 * web services without running into race conditions involving different threads with different authentication information
 * (user/password).
 */
public class KieRemoteWsAuthenticator extends Authenticator {

    protected static final Logger logger = LoggerFactory.getLogger(KieRemoteWsAuthenticator.class);

    private static ThreadLocal<PasswordAuthentication> threadLocalPasswordAuthentication = new ThreadLocal<PasswordAuthentication>();
    private static KieRemoteWsAuthenticator _instance = new KieRemoteWsAuthenticator();


    public static KieRemoteWsAuthenticator getInstance() {
        return _instance;
    }

    private KieRemoteWsAuthenticator() {
        Authenticator.setDefault(this);
    }

    public void setUserAndPassword( String userName, String password ) {
        disableHttpUrlConnectionAuthCache();

        Authenticator.setDefault(_instance);
        PasswordAuthentication pwdAuth = threadLocalPasswordAuthentication.get();
        if( pwdAuth != null ) {
            logger.debug("Replacing password authentication for user '{}' with new authentication for user '{}'",
                    pwdAuth.getUserName(), userName);
        }
        pwdAuth = new PasswordAuthentication(userName, password.toCharArray());
        threadLocalPasswordAuthentication.set(pwdAuth);
    }

    public void clearUserAndPassword() {
        threadLocalPasswordAuthentication.set(null);
    }

    @Override
    protected PasswordAuthentication getPasswordAuthentication() {
        return threadLocalPasswordAuthentication.get();
    }

    private static AtomicBoolean authenticatorSet = new AtomicBoolean(false);

    public static void disableHttpUrlConnectionAuthCache() {
        if( ! authenticatorSet.get() ) {
            if( authenticatorSet.compareAndSet(false, true) ) {
                insertFakeHashMapInAuthCache();
            }
        }
    }

    @SuppressWarnings("serial")
    private static void insertFakeHashMapInAuthCache() {
        try {

            Class<?> authCacheValueClass = Class.forName("sun.net.www.protocol.http.AuthCacheValue");
            Class<?> authCacheClass = Class.forName("sun.net.www.protocol.http.AuthCache");
            Class<?> authCacheImplClass = Class.forName("sun.net.www.protocol.http.AuthCacheImpl");

            Object cache = authCacheImplClass.newInstance();
            Method setMap = cache.getClass().getMethod("setMap", new Class[] { HashMap.class });
            setMap.invoke(cache, new Object[] { new HashMap() {

                @Override
                public Object put( Object key, Object value ) {
                    // don't store anything
                    return null;
                }

            } });

            Method m = authCacheValueClass.getMethod("setAuthCache", new Class[] { authCacheClass });
            m.invoke(null, new Object[] { cache });

        } catch( Exception e ) {
            logger.debug("Unable to disable AuthCache caching of HTTP authentication info", e);
            // ignore as it might not exists as this is sun specific api/impl
        }
    }

}
