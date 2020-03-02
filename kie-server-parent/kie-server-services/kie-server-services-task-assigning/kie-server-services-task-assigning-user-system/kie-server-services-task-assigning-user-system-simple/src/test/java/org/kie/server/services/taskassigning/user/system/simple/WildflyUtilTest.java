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
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;
import org.kie.server.services.taskassigning.user.system.api.Group;
import org.kie.server.services.taskassigning.user.system.api.User;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class WildflyUtilTest {

    static final String USERS_FILE = "/org/kie/server/service/taskassigning/user/system/simple/roles.properties";

    static final String USER1 = "user1";
    static final String USER2 = "user2";
    static final String USER3 = "user3";
    static final String USER4 = "user4";
    static final String USER5 = "user5";

    static final String GROUP1 = "group1";
    static final String GROUP2 = "group2";
    static final String GROUP3 = "group3";

    @Test
    public void buildInfoFromURI() throws Exception {
        URL url = WildflyUtilTest.class.getResource(USERS_FILE);
        assertUserGroupInfo(WildflyUtil.buildInfo(url.toURI()));
    }

    @Test
    public void buildFromInputStream() throws Exception {
        URL url = WildflyUtilTest.class.getResource(USERS_FILE);
        try (InputStream in = Files.newInputStream(Paths.get(url.getPath()))) {
            assertUserGroupInfo(WildflyUtil.buildInfo(in));
        }
    }

    private void assertUserGroupInfo(WildflyUtil.UserGroupInfo info) {
        assertUser(info, USER1, GROUP1);
        assertUser(info, USER2, GROUP1, GROUP2);
        assertUser(info, USER3, GROUP3, GROUP1);
        assertUser(info, USER4, GROUP1, GROUP2);
        assertUser(info, USER5);
        assertGroups(info, GROUP1, GROUP2, GROUP3);
    }

    private void assertUser(WildflyUtil.UserGroupInfo info, String userId, String... groupIds) {
        User user = info.getUsers().stream()
                .filter(u -> userId.equals(u.getId()))
                .findFirst().orElse(null);

        assertNotNull("User: " + userId + " was not found", user);
        assertEquals("User:" + userId + " don't have the expected groups count", groupIds.length, user.getGroups().size());
        for (String groupId : groupIds) {
            Group group = user.getGroups().stream()
                    .filter(g -> groupId.equals(g.getId()))
                    .findFirst().orElse(null);
            assertNotNull("Group: " + groupId + " was not found for user: " + userId, group);
        }
    }

    private void assertGroups(WildflyUtil.UserGroupInfo info, String... groupIds) {
        for (String groupId : groupIds) {
            Group group = info.getGroups().stream()
                    .filter(g -> groupId.equals(g.getId()))
                    .findFirst().orElse(null);
            assertNotNull("Group: " + groupId + " was not found", group);
        }
    }
}
