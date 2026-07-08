/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kie.integration.tomcat;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;
import javax.security.jacc.PolicyContextHandler;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequestEvent;
import jakarta.servlet.ServletRequestListener;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Request;
import org.apache.catalina.connector.Response;
import org.apache.catalina.realm.GenericPrincipal;
import org.apache.catalina.users.AbstractRole;
import org.apache.catalina.users.AbstractUser;
import org.apache.catalina.valves.ValveBase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Custom Tomcat valve that allows JACC access to principal to simplify integration with UberFire authentication mechanism.
 */
public class JACCValve extends ValveBase {

    private static final Logger logger = LoggerFactory.getLogger(JACCValve.class);

	private static ThreadLocal<Request> currentRequest = new ThreadLocal<Request>();
	
	public JACCValve() {
		try {
			PolicyContext.registerHandler("javax.security.auth.Subject.container", new PolicyContextHandler() {
				
				public boolean supports(String key) throws PolicyContextException {
					if ("javax.security.auth.Subject.container".equals(key)) {
						return true;
					}
					
					return false;
				}
				
				public String[] getKeys() throws PolicyContextException {
					return new String[]{"javax.security.auth.Subject.container"};
				}
				
				public Object getContext(String key, Object data)
						throws PolicyContextException {

                    Request req = currentRequest.get();
                    if (req == null || req.getPrincipal() == null) {
                        return null;
                    }

                    Set<Principal> principals = new HashSet<Principal>();
                    principals.add(req.getPrincipal());
                    principals.add(getGroupPrincipal(req.getPrincipal()));
                    if (req.getPrincipal() instanceof GenericPrincipal genericPrincipal) {
                        String basicAuthHeader = req.getHeader("Authorization");
                        if (basicAuthHeader != null && basicAuthHeader.startsWith("Basic ")) {
                            principals.add(new BasicAuthorizationPrincipal(basicAuthHeader));
                        }
                        // Original method based on GenericPrincipal.getPassword()
                        // which has been removed in Tomcat 10 due to security concerns
                        /*String name = genericPrincipal.getName();
                        String password = genericPrincipal.getPassword();
                        String basicAuthHeader = "Basic " + Base64.getEncoder().encodeToString((name + ":" + password).getBytes(StandardCharsets.UTF_8));
                        principals.add(new BasicAuthorizationPrincipal(basicAuthHeader));*/
                    }

			        final Subject s = new Subject(false, principals , Collections.EMPTY_SET, Collections.EMPTY_SET);
					return s;
				}
			}, false);
			
		
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void invoke(Request request, Response response) throws IOException,
			ServletException {
	    currentRequest.set(request);
        wrapListeners(request);
	    try {
	        getNext().invoke(request, response);
	    } finally {
	        currentRequest.set(null);
	    }
		
	}


    protected Principal getGroupPrincipal(Principal principal) {
            SimpleRolePrincipal group = new SimpleRolePrincipal(new String[0]) {
            
            private List<Principal> members = new ArrayList<Principal>();
            public String getName() {
                return "Roles";
            }
            
            public boolean removeMember(Principal user) {
                return members.remove(user);
            }
            
            public Enumeration<? extends Principal> members() {
                
                return Collections.enumeration(members);
            }
            
            public boolean isMember(Principal member) {
                return members.contains(member);
            }
            
            public boolean addMember(Principal user) {
                
                return members.add(user);
            }
        };
        if (principal instanceof AbstractUser) {
            Iterator<?> it = ((AbstractUser) principal).getRoles();

            while (it.hasNext()) {
                AbstractRole user = ((AbstractRole) it.next());
                group.addMember(user);
                
            }
        } else if (principal instanceof GenericPrincipal) {
            String[] roles = ((GenericPrincipal) principal).getRoles();
            for (final String role : roles) {
                group.addMember(new Principal() {
                    
                    public String getName() {
                        return role;
                    }
                });
            }
        }
        
        return group;
    }

    protected void wrapListeners(Request request) {
        Context context = request.getContext();

        Object[] listeners = context.getApplicationEventListeners();
        for (int i = 0; i < listeners.length; i++) {
            if (listeners[i] instanceof ServletRequestListener && !(listeners[i] instanceof WrappedServletRequestListener)) {
                listeners[i] = new WrappedServletRequestListener((ServletRequestListener)listeners[i]);
            }
        }
    }

    /**
     * Wrapper for ServletRequestListener to overcome bug in WELD on tomcat that causes
     * NPE when request is destroyed, until it gets fixed let's catch the exception as
     * it does not cause any harm as request was already completed and once cleaned.
     */
    private static class WrappedServletRequestListener implements ServletRequestListener {
        private ServletRequestListener delegate;

        WrappedServletRequestListener(ServletRequestListener delegate) {
            this.delegate = delegate;
        }
        @Override
        public void requestDestroyed(ServletRequestEvent servletRequestEvent) {
            try {
                delegate.requestDestroyed(servletRequestEvent);
            } catch (Exception e) {
                logger.debug("Exception at request destroy {}", e.getMessage(), e);
            }
        }

        @Override
        public void requestInitialized(ServletRequestEvent servletRequestEvent) {

            try {
                delegate.requestInitialized(servletRequestEvent);
            } catch (Exception e) {
                logger.debug("Exception at request initialization {}", e.getMessage(), e);
            }
        }
    }
}
