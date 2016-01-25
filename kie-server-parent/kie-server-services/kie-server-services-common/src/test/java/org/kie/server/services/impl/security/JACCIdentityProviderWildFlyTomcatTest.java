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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.security.Principal;
import java.security.acl.Group;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.security.auth.Subject;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;
import javax.security.jacc.PolicyContextHandler;

import org.junit.Before;
import org.junit.Test;
import org.kie.internal.identity.IdentityProvider;

public class JACCIdentityProviderWildFlyTomcatTest {

    private static final String PRINCIPAL_NAME="yoda";
    private static final String GROUP_ONE_NAME="groupOne";
    private static final String GROUP_TWO_NAME="groupTwo";

    private PolicyContextHandler handler;

    @Before
    public void setUp() throws Exception{
        // WildFly has java.security.acl.Group named Roles that contains
        // the role names from the application domain to which the user has been assigned.
        // Tomcat uses JACCValve with same Principal structure as WildFly.
        final GroupImpl groupRoles = new GroupImpl("Roles");
        final GroupImpl groupOne = new GroupImpl(GROUP_ONE_NAME);
        final GroupImpl groupTwo = new GroupImpl(GROUP_TWO_NAME);
        final UserImpl user = new UserImpl(PRINCIPAL_NAME);

        groupOne.addMember(user);
        groupTwo.addMember(user);
        groupRoles.addMember(groupOne);
        groupRoles.addMember(groupTwo);

        handler = new PolicyContextHandler() {

            @Override
            public boolean supports(String key) throws PolicyContextException {
                if ("javax.security.auth.Subject.container".equals(key)) {
                    return true;
                }
                return false;
            }

            @Override
            public String[] getKeys() throws PolicyContextException {
                return new String[]{"javax.security.auth.Subject.container"};
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
        PolicyContext.registerHandler("javax.security.auth.Subject.container", handler, true);
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
    }

    private class GroupImpl implements Group {

        private String name;
        private Collection<Principal> members;

        public GroupImpl(String name) {
            this.name = name;
            members = new ArrayList<Principal>();
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public boolean addMember(Principal user) {
            if(members.contains(user)) {
                return false;
            }
            members.add(user);
            return true;
        }

        @Override
        public boolean removeMember(Principal user) {
            if(members.contains(user)) {
                members.remove(user);
                return true;
            }
            return false;
        }

        @Override
        public boolean isMember(Principal member) {
            return members.contains(member);
        }

        @Override
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
