package org.kie.remote.client.ws;

import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a thread-local {@link Authenticator} instance that allows users to make multiple (authenticated) calls to 
 * web services without running into race conditions involving different threads with different authentication information (user/password). 
 */
public class KieRemoteWsAuthenticator extends Authenticator {

    protected static final Logger logger = LoggerFactory.getLogger(KieRemoteWsAuthenticator.class);
  
    // Is a static ThreadLocal GC safe?
    private static ThreadLocal<PasswordAuthentication> threadLocalPasswordAuthentication = new ThreadLocal<PasswordAuthentication>();
    private static AtomicBoolean authenticatorSet = new AtomicBoolean(false);
   
    public void setUserAndPassword(String userName, String password) { 
       if( ! authenticatorSet.get() ) { 
           // here: 2+ threads, Authenticator not yet set
           synchronized(authenticatorSet) { 
               // only 1 thread because of sync block
               if( authenticatorSet.compareAndSet(false, true) ) { 
                   Authenticator.setDefault(this);
               }
           }
       }
       PasswordAuthentication pwdAuth = threadLocalPasswordAuthentication.get();
       if( pwdAuth != null ) { 
           logger.debug("Replacing password authentication for user '{}' with new authentication for user '{}'", pwdAuth.getUserName(), userName);
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
    
}
