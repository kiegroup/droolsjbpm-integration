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
import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.kie.server.api.security.SecurityAdapter;
import org.kie.server.services.impl.security.web.CaptureHttpRequestFilter;

public class TomcatSecurityAdapter implements SecurityAdapter {

    private Class<?> abstractUserClass = null;
    private Class<?> genericPrincipalClass = null;

    private boolean active;

    public TomcatSecurityAdapter() {
        try {
            abstractUserClass = Class.forName("org.apache.catalina.users.AbstractUser");
            genericPrincipalClass = Class.forName("org.apache.catalina.realm.GenericPrincipal");

            active = true;
        } catch (Exception e) {
            active = false;
        }
    }


    @Override
    public String getUser() {
        if (active) {

            HttpServletRequest request = CaptureHttpRequestFilter.getRequest();
            if (request != null && request.getUserPrincipal() != null) {
                return request.getUserPrincipal().getName();
            }
        }
        return null;
    }

    @Override
    public List<String> getRoles() {
        List<String> proles = new ArrayList<String>();

        if (active) {

            HttpServletRequest request = CaptureHttpRequestFilter.getRequest();
            if (request != null && request.getUserPrincipal() != null) {

                Principal principal = request.getUserPrincipal();
                if (abstractUserClass.isAssignableFrom(principal.getClass())) {
                    Iterator<?> it = (Iterator<?>) invoke(principal, "getRoles");

                    while (it.hasNext()) {
                        Principal user = ((Principal) it.next());
                        proles.add(user.getName());

                    }
                } else if (genericPrincipalClass.isAssignableFrom(principal.getClass())) {
                    String[] roles = (String[]) invoke(principal, "getRoles");
                    proles.addAll(Arrays.asList(roles));
                }
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
}
