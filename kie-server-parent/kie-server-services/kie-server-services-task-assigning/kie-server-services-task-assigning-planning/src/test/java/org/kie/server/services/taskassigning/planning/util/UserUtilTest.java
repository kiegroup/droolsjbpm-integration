/*
 * Copyright 2020 Red Hat, Inc. and/or its affiliates.
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

package org.kie.server.services.taskassigning.planning.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Test;
import org.kie.server.services.taskassigning.core.model.User;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.kie.server.services.taskassigning.core.model.DefaultLabels.AFFINITIES;
import static org.kie.server.services.taskassigning.core.model.DefaultLabels.SKILLS;
import static org.kie.server.services.taskassigning.planning.TestUtil.assertContains;
import static org.kie.server.services.taskassigning.planning.TestUtil.mockExternalGroup;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class UserUtilTest {

    private static final String SKILLS_ATTRIBUTE = "skills";
    private static final String AFFINITIES_ATTRIBUTE = "affinities";
    private static final String ANOTHER__ATTRIBUTE = "ANOTHER__ATTRIBUTE";
    private static final String USER_ID_1 = "USER_ID_1";
    private static final String USER_ID_2 = "USER_ID_2";
    private static final String USER_ID_3 = "USER_ID_3";
    private static final String USER_ID_4 = "USER_ID_4";
    private static final String GROUP1_ID = "GROUP1_ID";
    private static final String GROUP2_ID = "GROUP2_ID";
    private static final String GROUP3_ID = "GROUP3_ID";
    private static final String SKILL1 = "skill1";
    private static final String SKILL2 = "skill2";
    private static final String SKILL3 = "skill3";
    private static final String AFFINITY1 = "affinity1";
    private static final String AFFINITY2 = "affinity2";

    @Test
    public void fromExternalUser() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put(SKILLS_ATTRIBUTE, String.format("%s,  %s,%s", SKILL2, SKILL1, SKILL3));
        attributes.put(AFFINITIES_ATTRIBUTE, String.format("  %s,  %s", AFFINITY2, AFFINITY1));
        attributes.put(ANOTHER__ATTRIBUTE, "someValue");

        org.kie.server.services.taskassigning.user.system.api.User externalUser = mockUser(USER_ID_1, attributes);

        org.kie.server.services.taskassigning.user.system.api.Group externalGroup1 = mockExternalGroup(GROUP1_ID);
        org.kie.server.services.taskassigning.user.system.api.Group externalGroup2 = mockExternalGroup(GROUP2_ID);
        org.kie.server.services.taskassigning.user.system.api.Group externalGroup3 = mockExternalGroup(GROUP3_ID);
        Set<org.kie.server.services.taskassigning.user.system.api.Group> externalGroups = new HashSet<>();
        externalGroups.add(externalGroup1);
        externalGroups.add(externalGroup2);
        externalGroups.add(externalGroup3);
        when(externalUser.getGroups()).thenReturn(externalGroups);

        User user = UserUtil.fromExternalUser(externalUser);
        assertEquals(USER_ID_1.hashCode(), user.getId(), 0);
        assertEquals(USER_ID_1, user.getEntityId());
        assertTrue(user.isEnabled());
        assertEquals(user.getGroups().size(), externalGroups.size());
        assertContains(GROUP1_ID, user.getGroups());
        assertContains(GROUP2_ID, user.getGroups());
        assertContains(GROUP3_ID, user.getGroups());
        assertEquals(new HashSet<>(Arrays.asList(SKILL3, SKILL2, SKILL1)), user.getLabelValues(SKILLS.name()));
        assertEquals(new HashSet<>(Arrays.asList(AFFINITY1, AFFINITY2)), user.getLabelValues(AFFINITIES.name()));
        assertEquals(attributes, user.getAttributes());
    }

    @Test
    public void isUser() {
        assertTrue(UserUtil.isUser("User"));
        assertFalse(UserUtil.isUser("NotAUser"));
    }

    @Test
    public void filterDuplicates() {
        Collection<org.kie.server.services.taskassigning.user.system.api.User> externalUsers = Arrays.asList(mockUser(USER_ID_1),
                                                                                                             mockUser(USER_ID_2),
                                                                                                             null,
                                                                                                             mockUser(USER_ID_1),
                                                                                                             mockUser(USER_ID_4),
                                                                                                             null,
                                                                                                             mockUser(USER_ID_3),
                                                                                                             mockUser(USER_ID_3));
        List<org.kie.server.services.taskassigning.user.system.api.User> result = UserUtil.filterDuplicates(externalUsers).collect(Collectors.toList());
        assertEquals(4, result.size());
        assertEquals(USER_ID_1, result.get(0).getId());
        assertEquals(USER_ID_2, result.get(1).getId());
        assertEquals(USER_ID_4, result.get(2).getId());
        assertEquals(USER_ID_3, result.get(3).getId());
    }

    @Test
    public void filterDuplicatesWhenNull() {
        assertTrue(UserUtil.filterDuplicates(null).collect(Collectors.toList()).isEmpty());
    }

    private static org.kie.server.services.taskassigning.user.system.api.User mockUser(String userId) {
        return mockUser(userId, null);
    }

    private static org.kie.server.services.taskassigning.user.system.api.User mockUser(String userId, Map<String, Object> attributes) {
        org.kie.server.services.taskassigning.user.system.api.User externalUser =
                mock(org.kie.server.services.taskassigning.user.system.api.User.class);
        when(externalUser.getId()).thenReturn(userId);
        when(externalUser.getAttributes()).thenReturn(attributes);
        return externalUser;
    }
}
