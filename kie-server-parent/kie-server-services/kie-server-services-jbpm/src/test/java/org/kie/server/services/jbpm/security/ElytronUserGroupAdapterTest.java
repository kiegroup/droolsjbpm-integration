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

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.powermock.reflect.Whitebox;
import org.wildfly.security.auth.server.RealmUnavailableException;
import org.wildfly.security.authz.AuthorizationFailureException;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.lenient;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ElytronUserGroupAdapterTest {

    private static final String USER_ID = "user";
    private static final String WRONG_USER_ID = "anotherUser";

    private static final String ROLE_1 = "role1";
    private static final String ROLE_2 = "role2";
    private static final String ROLE_3 = "role3";


    @Mock
    private ElytronUserGroupAdapter adapter;


    @Test
    public void testNoSecurityContext() {
        setAuthorizationFailureExceptionClass();
        when(adapter.isActive()).thenCallRealMethod();
        when(adapter.getGroupsForUser(Mockito.anyString())).thenCallRealMethod();
        when(adapter.getUserName()).thenReturn(null);
        List<String> roles = adapter.getGroupsForUser(USER_ID);

        Assertions.assertThat(roles)
                .isNotNull()
                .isEmpty();
    }

    @Test
    public void testSecurityContextNoIdentity() {
        setAuthorizationFailureExceptionClass();
        when(adapter.isActive()).thenCallRealMethod();
        when(adapter.getGroupsForUser(Mockito.anyString())).thenCallRealMethod();
        when(adapter.getUserName()).thenReturn(USER_ID);

        List<String> roles = adapter.getGroupsForUser(USER_ID);

        Assertions.assertThat(roles)
                .isNotNull()
                .isEmpty();
    }

    @Test
    public void testSecurityForWrongUser() throws RealmUnavailableException {
        setAuthorizationFailureExceptionClass();
        when(adapter.isActive()).thenCallRealMethod();
        when(adapter.getGroupsForUser(Mockito.anyString())).thenCallRealMethod();
        when(adapter.getUserName()).thenReturn(USER_ID);
        when(adapter.runAsPrincipalExists(WRONG_USER_ID)).thenReturn(true);
        when(adapter.toRunAsPrincipalRoles(WRONG_USER_ID, true)).thenReturn(Arrays.asList(ROLE_1, ROLE_2));

        List<String> roles = adapter.getGroupsForUser(WRONG_USER_ID);

        Assertions.assertThat(roles)
                .isNotNull()
                .hasSize(2)
                .contains(ROLE_1, ROLE_2);
    }

    @Test
    public void testSecurityForLoggedUser() {
        setAuthorizationFailureExceptionClass();
        when(adapter.isActive()).thenCallRealMethod();
        when(adapter.getGroupsForUser(Mockito.anyString())).thenCallRealMethod();
        when(adapter.getUserName()).thenReturn(USER_ID);
        when(adapter.toPrincipalRoles(Mockito.anyString())).thenReturn(Arrays.asList(ROLE_1, ROLE_2, ROLE_3));

        List<String> roles = adapter.getGroupsForUser(USER_ID);

        Assertions.assertThat(roles)
                .isNotNull()
                .hasSize(3)
                .contains(ROLE_1, ROLE_2, ROLE_3);
    }

    @Test
    public void testSecurityOnRealmUnavailable() throws RealmUnavailableException {
        setAuthorizationFailureExceptionClass();
        when(adapter.isActive()).thenCallRealMethod();
        when(adapter.getGroupsForUser(Mockito.anyString())).thenCallRealMethod();
        when(adapter.getUserName()).thenReturn(WRONG_USER_ID);
        when(adapter.runAsPrincipalExists(USER_ID)).thenThrow(new RealmUnavailableException());
        lenient().when(adapter.toPrincipalRoles(Mockito.anyString())).thenReturn(Arrays.asList(ROLE_1, ROLE_2, ROLE_3));
        lenient().when(adapter.toRunAsPrincipalRoles(Mockito.anyString(), Mockito.eq(false))).thenReturn(Arrays.asList(ROLE_1, ROLE_2, ROLE_3));

        List<String> roles = adapter.getGroupsForUser(USER_ID);

        Assertions.assertThat(roles)
                .isNotNull()
                .hasSize(0);
    }

    @Test
    public void testSecurityOnAuthorizationFailure() throws AuthorizationFailureException, RealmUnavailableException {
        setAuthorizationFailureExceptionClass();
        when(adapter.isActive()).thenCallRealMethod();
        when(adapter.getGroupsForUser(Mockito.anyString())).thenCallRealMethod();
        when(adapter.getUserName()).thenReturn(WRONG_USER_ID);
        when(adapter.runAsPrincipalExists(USER_ID)).thenThrow(AuthorizationFailureException.class);
        when(adapter.toRunAsPrincipalRoles(Mockito.anyString(), Mockito.eq(false))).thenReturn(Arrays.asList(ROLE_1, ROLE_2, ROLE_3));

        List<String> roles = adapter.getGroupsForUser(USER_ID);

        Assertions.assertThat(roles)
                .isNotNull()
                .hasSize(3)
                .contains(ROLE_1, ROLE_2, ROLE_3);
    }

    private void setAuthorizationFailureExceptionClass() {
        Whitebox.setInternalState(adapter, Class.class, (Object) AuthorizationFailureException.class);
    }
}

