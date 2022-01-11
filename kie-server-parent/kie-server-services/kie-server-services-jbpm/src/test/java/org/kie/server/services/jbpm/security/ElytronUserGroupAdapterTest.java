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

import java.util.Arrays;
import java.util.List;

import org.assertj.core.api.Assertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import org.wildfly.security.auth.server.RealmUnavailableException;

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
        when(adapter.getGroupsForUser(Mockito.anyObject())).thenCallRealMethod();
        when(adapter.getUserName()).thenReturn(null);
        List<String> roles = adapter.getGroupsForUser(USER_ID);

        Assertions.assertThat(roles)
                .isNotNull()
                .isEmpty();
    }

    @Test
    public void testSecurityContextNoIdentity() {
        when(adapter.getGroupsForUser(Mockito.anyObject())).thenCallRealMethod();
        when(adapter.getUserName()).thenReturn(USER_ID);

        List<String> roles = adapter.getGroupsForUser(USER_ID);

        Assertions.assertThat(roles)
                .isNotNull()
                .isEmpty();
    }

    @Test
    public void testSecurityForWrongUser() throws RealmUnavailableException {
        when(adapter.getGroupsForUser(Mockito.anyObject())).thenCallRealMethod();
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
        when(adapter.getGroupsForUser(Mockito.anyObject())).thenCallRealMethod();
        when(adapter.getUserName()).thenReturn(USER_ID);
        when(adapter.toPrincipalRoles(Mockito.anyObject())).thenReturn(Arrays.asList(ROLE_1, ROLE_2, ROLE_3));

        List<String> roles = adapter.getGroupsForUser(USER_ID);

        Assertions.assertThat(roles)
                .isNotNull()
                .hasSize(3)
                .contains(ROLE_1, ROLE_2, ROLE_3);
    }
}
