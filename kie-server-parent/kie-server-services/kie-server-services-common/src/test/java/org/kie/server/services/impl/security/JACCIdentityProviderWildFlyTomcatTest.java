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

package org.kie.server.services.impl.security;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.security.auth.Subject;
import jakarta.security.jacc.PolicyContext;
import jakarta.security.jacc.PolicyContextException;
import jakarta.security.jacc.PolicyContextHandler;

import org.junit.Before;
import org.junit.Test;
import org.kie.internal.identity.IdentityProvider;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.kie.server.services.impl.security.JACCIdentityProvider.SECURITY_AUTH_SUBJECT_CONTAINER;

public class JACCIdentityProviderWildFlyTomcatTest {

    private static final String PRINCIPAL_NAME="yoda";
    private static final String GROUP_ONE_NAME="groupOne";
    private static final String GROUP_TWO_NAME="groupTwo";
    private static final String CONTEXT_USER_NAME="contextUser";

    private PolicyContextHandler handler;

    @Before
    public void setUp() throws Exception{
        // WildFly has java.security.acl.Group named Roles that contains
        // the role names from the application domain to which the user has been assigned.
        // Tomcat uses JACCValve with same Principal structure as WildFly.
        final Group groupRoles = new Group("Roles");
        final Group groupOne = new Group(GROUP_ONE_NAME);
        final Group groupTwo = new Group(GROUP_TWO_NAME);
        final UserImpl user = new UserImpl(PRINCIPAL_NAME);

        groupOne.addMember(user);
        groupTwo.addMember(user);
        groupRoles.addMember(groupOne);
        groupRoles.addMember(groupTwo);

        handler = new PolicyContextHandler() {

            @Override
            public boolean supports(String key) throws PolicyContextException {
                return SECURITY_AUTH_SUBJECT_CONTAINER.equals(key);
            }

            @Override
            public String[] getKeys() throws PolicyContextException {
                return new String[]{SECURITY_AUTH_SUBJECT_CONTAINER};
            }

            @Override
            public Object getContext(String key, Object data) throws PolicyContextException {
                Set<Principal> principals = new LinkedHashSet<Principal>();
                principals.add(groupRoles);
                principals.add(user);

                final Subject s = new Subject(true, principals , Collections.EMPTY_SET, Collections.EMPTY_SET);
                return s;
            }
        };
        PolicyContext.registerHandler(SECURITY_AUTH_SUBJECT_CONTAINER, handler, true);
    }

    @Test
    public void testGetName() throws Exception {
        IdentityProvider jaccIdentityProvider = new JACCIdentityProvider();

        assertEquals(PRINCIPAL_NAME, jaccIdentityProvider.getName());
    }

    @Test
    public void testGetRoles() throws Exception {
        IdentityProvider jaccIdentityProvider = new JACCIdentityProvider();

        assertEquals(2, jaccIdentityProvider.getRoles().size());
        assertTrue(jaccIdentityProvider.getRoles().contains(GROUP_ONE_NAME));
        assertTrue(jaccIdentityProvider.getRoles().contains(GROUP_TWO_NAME));
        assertTrue(jaccIdentityProvider.hasRole(GROUP_ONE_NAME));
        assertTrue(jaccIdentityProvider.hasRole(GROUP_TWO_NAME));
        assertFalse(jaccIdentityProvider.hasRole("non_existing_role"));
        assertFalse(jaccIdentityProvider.hasRole(null));
    }
    
    @Test
    public void testContextUsers() {
        IdentityProvider jaccIdentityProvider = new JACCIdentityProvider();
        
        jaccIdentityProvider.setContextIdentity(CONTEXT_USER_NAME);
        assertEquals(CONTEXT_USER_NAME, jaccIdentityProvider.getName());
        assertEquals(0, jaccIdentityProvider.getRoles().size());
        
        jaccIdentityProvider.removeContextIdentity();
        assertEquals(PRINCIPAL_NAME, jaccIdentityProvider.getName());
        assertEquals(2, jaccIdentityProvider.getRoles().size());
    }

    @Test
    public void testContextUsersMultiThreaded() throws Exception {
        IdentityProvider jaccIdentityProvider = new JACCIdentityProvider();
        
        jaccIdentityProvider.setContextIdentity(CONTEXT_USER_NAME);
        assertEquals(CONTEXT_USER_NAME, jaccIdentityProvider.getName());
        assertEquals(0, jaccIdentityProvider.getRoles().size());
        
        Thread t = new Thread(new Runnable() {

            @Override
            public void run() {
                assertEquals(PRINCIPAL_NAME, jaccIdentityProvider.getName());
            }
        });        
        t.start();
        t.join();
        
        jaccIdentityProvider.removeContextIdentity();
        assertEquals(PRINCIPAL_NAME, jaccIdentityProvider.getName());
        assertEquals(2, jaccIdentityProvider.getRoles().size());
    }

    private class Group implements Principal {

        private String name;
        private Collection<Principal> members;

        public Group(String name) {
            this.name = name;
            members = new ArrayList<Principal>();
        }

        @Override
        public String getName() {
            return name;
        }

        public boolean addMember(Principal user) {
            if(members.contains(user)) {
                return false;
            }
            members.add(user);
            return true;
        }

        public boolean removeMember(Principal user) {
            if(members.contains(user)) {
                members.remove(user);
                return true;
            }
            return false;
        }

        public boolean isMember(Principal member) {
            return members.contains(member);
        }

        public Enumeration<? extends Principal> members() {
            return Collections.enumeration(members);
        }
    }

    private class UserImpl implements Principal {

        private String name;

        public UserImpl(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
    }
}
