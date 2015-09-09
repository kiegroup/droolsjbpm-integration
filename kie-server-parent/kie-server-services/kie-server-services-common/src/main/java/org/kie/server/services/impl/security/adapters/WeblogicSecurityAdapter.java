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

package org.kie.server.services.impl.security.adapters;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import javax.security.auth.Subject;

import org.kie.server.api.security.SecurityAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WeblogicSecurityAdapter implements SecurityAdapter {

    private static final Logger logger = LoggerFactory.getLogger(WeblogicSecurityAdapter.class);
    private Class webLogicSecurity;

    private boolean active;

    public WeblogicSecurityAdapter() {
        try {
            this.webLogicSecurity = Class.forName("weblogic.security.Security");
            active = true;
        } catch ( Exception e ) {
            active = false;
            logger.debug( "Unable to find weblogic.security.Security, disabling weblogic adapter" );
        }
    }


    @Override
    public String getUser(Object ... params) {
        if (active) {

            try {
                Subject wlsSubject = getSubject(params);

                if ( wlsSubject != null ) {
                    for ( java.security.Principal p : wlsSubject.getPrincipals() ) {
                        if ( p.getClass().getName().indexOf( "WLSUser" ) != -1 ) {
                            return p.getName();
                        }
                    }
                }
            } catch (Exception e) {
                logger.error( "Unable to get user from subject due to {}", e.getMessage(), e );
            }
        }
        return null;
    }

    @Override
    public List<String> getRoles(Object ... params) {
        List<String> proles = new ArrayList<String>();

        if (active) {

            try {
                Subject wlsSubject = getSubject(params);

                if ( wlsSubject != null ) {
                    for ( java.security.Principal p : wlsSubject.getPrincipals() ) {
                        if ( p.getClass().getName().indexOf( "WLSGroup" ) != -1 ) {
                            proles.add(p.getName());
                        }
                    }
                }
            } catch ( Exception e ) {
                logger.error( "Unable to get groups from subject due to {}", e.getMessage(), e );
            }
        }
        return proles;
    }

    protected Object invoke(Object o, String method) {
        try {
            Method m = o.getClass().getDeclaredMethod(method, new Class[0]);
            return m.invoke(o, new Object[0]);
        } catch (Exception e) {
            return null;
        }
    }

    protected Subject getSubject(Object[] params) throws Exception{

        if (params != null && params.length > 0) {
            for (Object p : params) {
                if (p instanceof Subject) {
                    return (Subject) p;
                }
            }
        }

        Method method = webLogicSecurity.getMethod("getCurrentSubject", new Class[]{});
        Subject wlsSubject = (Subject) method.invoke(null, new Object[]{});

        return wlsSubject;
    }
}
