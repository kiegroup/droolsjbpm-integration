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

package org.kie.server.services.impl.security.adapters;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import javax.naming.InitialContext;

import org.kie.server.api.security.SecurityAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSphereSecurityAdapter implements SecurityAdapter {

    private static final Logger logger = LoggerFactory.getLogger(WebSphereSecurityAdapter.class);
    private Object registry;
    private Class subject;

    private boolean active;

    public WebSphereSecurityAdapter() {
        try {
            this.registry = InitialContext.doLookup("UserRegistry");
            this.subject = Class.forName("com.ibm.websphere.security.auth.WSSubject");
            active = true;
        } catch ( Exception e ) {
            active = false;
            logger.debug("Unable to look up UserRegistry in JNDI under key 'UserRegistry', disabling websphere adapter");
        }
    }


    @Override
    public String getUser(Object ... params) {
        if (active) {
            try {
                Method method = subject.getMethod("getCallerPrincipal", new Class[]{});
                String principal = (String) method.invoke(null, new Object[]{});

                return principal;
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

            if ( registry == null ) {
                return proles;
            }

            try {
                Method method = registry.getClass().getMethod( "getGroupsForUser", new Class[]{ String.class } );
                List rolesIn = (List) method.invoke( registry, new Object[]{ getUser() } );
                if ( rolesIn != null ) {
                    for ( Object o : rolesIn ) {
                        proles.add(o.toString());
                    }
                }
            } catch ( Exception e ) {
                logger.error( "Unable to get groups from registry due to {}", e.getMessage(), e );
            }

        }
        return proles;
    }
}
