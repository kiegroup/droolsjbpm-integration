/*
 * Copyright 2021 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.server.services.jbpm.security;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.wildfly.security.auth.server.SecurityDomain;
import org.wildfly.security.auth.server.SecurityIdentity;
import org.wildfly.security.authz.Roles;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@PowerMockIgnore("javax.security.auth.*")
@RunWith(PowerMockRunner.class)
@PrepareForTest({SecurityDomain.class, SecurityIdentity.class})
public class ElytronUserGroupAdapterTest {

    private static final String USER_ID = "user";
    private static final String WRONG_USER_ID = "anotherUser";

    private static final String ROLE_1 = "role1";
    private static final String ROLE_2 = "role2";
    private static final String ROLE_3 = "role3";

    private SecurityIdentity identity;

    @Mock
    private Principal principal;

    @Mock
    private Roles roles;

    @Mock
    private SecurityDomain securityDomain;

    private ElytronUserGroupAdapter adapter;
    
    @Before
    public void init() {
        mockStatic(SecurityDomain.class);
        when(SecurityDomain.getCurrent()).thenReturn(securityDomain);

        when(principal.getName()).thenReturn(USER_ID);
        identity = mock(SecurityIdentity.class);
        when(identity.getPrincipal()).thenReturn(principal);
        when(identity.getRoles()).thenReturn(roles);
        when(roles.spliterator()).thenReturn(Arrays.asList(ROLE_1, ROLE_2, ROLE_3).spliterator());

        when(securityDomain.getCurrentSecurityIdentity()).thenReturn(identity);

        adapter = new ElytronUserGroupAdapter();
    }

    @Test
    public void testNoSecurityContext() {
        when(SecurityDomain.getCurrent()).thenReturn(null);

        List<String> roles = adapter.getGroupsForUser(USER_ID);

        Assertions.assertThat(roles)
                .isNotNull()
                .isEmpty();
    }

    @Test
    public void testSecurityContextNoIdentity() {
        when(securityDomain.getCurrentSecurityIdentity()).thenReturn(null);

        List<String> roles = adapter.getGroupsForUser(USER_ID);

        Assertions.assertThat(roles)
                .isNotNull()
                .isEmpty();
    }

    @Test
    public void testSecurityForWrongUser() {
        List<String> roles = adapter.getGroupsForUser(WRONG_USER_ID);

        Assertions.assertThat(roles)
                .isNotNull()
                .isEmpty();
    }

    @Test
    public void testSecurityForLoggedUser() {
        List<String> roles = adapter.getGroupsForUser(USER_ID);

        Assertions.assertThat(roles)
                .isNotNull()
                .hasSize(3)
                .contains(ROLE_1, ROLE_2, ROLE_3);
    }
}
