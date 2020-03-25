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

package org.kie.server.services.taskassigning.user.system.simple;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.Test;
import org.kie.server.services.taskassigning.user.system.api.Group;
import org.kie.server.services.taskassigning.user.system.api.User;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.kie.server.services.taskassigning.user.system.simple.SimpleUserSystemServiceHelper.AFFINITIES_ATTRIBUTE_NAME;
import static org.kie.server.services.taskassigning.user.system.simple.SimpleUserSystemServiceHelper.SKILLS_ATTRIBUTE_NAME;

public class SimpleUserSystemServiceHelperTest {

    static final String RESOURCES = "src/test/resources";
    static final String USERS_FILE = "/org/kie/server/service/taskassigning/user/system/simple/roles.properties";
    static final String SKILLS_FILE = "/org/kie/server/service/taskassigning/user/system/simple/skills.properties";
    static final String AFFINITIES_FILE = "/org/kie/server/service/taskassigning/user/system/simple/affinities.properties";

    static final String USER1 = "user1";
    static final String USER2 = "user2";
    static final String USER3 = "user3";
    static final String USER4 = "user4";
    static final String USER5 = "user5";

    static final String GROUP1 = "group1";
    static final String GROUP2 = "group2";
    static final String GROUP3 = "group3";

    static final String SKILL1 = "skill1";
    static final String SKILL2 = "skill2";
    static final String SKILL3 = "skill3";

    static final String AFFINITY1 = "affinity1";
    static final String AFFINITY2 = "affinity2";
    static final String AFFINITY3 = "affinity3";

    @Test
    public void buildInfoFromPath() throws Exception {
        assertUserGroupInfo(SimpleUserSystemServiceHelper.buildInfo(getTestFile(RESOURCES, USERS_FILE).toAbsolutePath(),
                                                                    getTestFile(RESOURCES, SKILLS_FILE).toAbsolutePath(),
                                                                    getTestFile(RESOURCES, AFFINITIES_FILE).toAbsolutePath()));
    }

    @Test
    public void buildFromInputStream() throws Exception {
        InputStream inUsers = Files.newInputStream(Paths.get(SimpleUserSystemServiceHelperTest.class.getResource(USERS_FILE).getPath()));
        InputStream inSkills = Files.newInputStream(Paths.get(SimpleUserSystemServiceHelperTest.class.getResource(SKILLS_FILE).getPath()));
        InputStream inAffinities = Files.newInputStream(Paths.get(SimpleUserSystemServiceHelperTest.class.getResource(AFFINITIES_FILE).getPath()));

        try {
            assertUserGroupInfo(SimpleUserSystemServiceHelper.buildInfo(inUsers, inSkills, inAffinities));
        } finally {
            inUsers.close();
            inSkills.close();
            inAffinities.close();
        }
    }

    static Path getTestFile(String base, String resource) throws Exception {
        String baseDir = System.getProperty("project.base.dir");
        if (baseDir != null) {
            return Paths.get(baseDir, base + resource);
        } else {
            return Paths.get(SimpleUserSystemServiceHelperTest.class.getResource(resource).toURI());
        }
    }

    private void assertUserGroupInfo(SimpleUserSystemServiceHelper.UserGroupInfo info) {
        assertUser(info, USER1, Collections.singletonList(GROUP1), Arrays.asList(SKILL1, SKILL2), null);
        assertUser(info, USER2, Arrays.asList(GROUP1, GROUP2), Arrays.asList(SKILL2, SKILL3), Collections.singletonList(AFFINITY1));
        assertUser(info, USER3, Arrays.asList(GROUP3, GROUP1), null, null);
        assertUser(info, USER4, Arrays.asList(GROUP1, GROUP2), null, Arrays.asList(AFFINITY2, AFFINITY1));
        assertUser(info, USER5, Collections.emptyList(), Collections.singletonList(SKILL3), Arrays.asList(AFFINITY3, AFFINITY2, AFFINITY1));
        assertGroups(info, GROUP1, GROUP2, GROUP3);
    }

    private void assertUser(SimpleUserSystemServiceHelper.UserGroupInfo info, String userId, List<String> groupIds, List<String> skills, List<String> affinities) {
        User user = info.getUsers().stream()
                .filter(u -> userId.equals(u.getId()))
                .findFirst().orElse(null);

        assertNotNull("User: " + userId + " was not found", user);
        assertTrue("User: " + userId + " is expected to be active", user.isActive());
        assertEquals("User:" + userId + " don't have the expected groups count", groupIds.size(), user.getGroups().size());
        for (String groupId : groupIds) {
            Group group = user.getGroups().stream()
                    .filter(g -> groupId.equals(g.getId()))
                    .findFirst().orElse(null);
            assertNotNull("Group: " + groupId + " was not found for user: " + userId, group);
        }
        assertStringListAttribute(user, SKILLS_ATTRIBUTE_NAME, skills);
        assertStringListAttribute(user, AFFINITIES_ATTRIBUTE_NAME, affinities);
    }

    private void assertStringListAttribute(User user, String attribute, List<String> expectedValues) {
        String value = (String) user.getAttributes().get(attribute);
        if (expectedValues == null) {
            assertNull(value);
        } else {
            assertNotNull(value);
            List<String> valuesList = Stream.of(value.split(",")).collect(Collectors.toList());
            assertEquals(expectedValues.size(), valuesList.size());
            expectedValues.forEach(expectedValue -> assertTrue(valuesList.contains(expectedValue)));
        }
    }

    private void assertGroups(SimpleUserSystemServiceHelper.UserGroupInfo info, String... groupIds) {
        for (String groupId : groupIds) {
            Group group = info.getGroups().stream()
                    .filter(g -> groupId.equals(g.getId()))
                    .findFirst().orElse(null);
            assertNotNull("Group: " + groupId + " was not found", group);
        }
    }
}
